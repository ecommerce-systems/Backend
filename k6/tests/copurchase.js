import http from 'k6/http';
import { check, sleep, group } from 'k6';
import { Trend } from 'k6/metrics';

const v1Trend = new Trend('copurchase_v1_duration');
const v2Trend = new Trend('copurchase_v2_duration');

export let options = {
    scenarios: {
        v1_copurchase: {
            executor: 'constant-vus',
            exec: 'testCoPurchaseV1',
            vus: 5,
            duration: '30s',
        },
        v2_copurchase: {
            executor: 'constant-vus',
            exec: 'testCoPurchaseV2',
            vus: 5,
            duration: '30s',
            startTime: '32s',
        },
    },
};

const BASE_URL = 'http://localhost:8080/api';

// Helper to create a product
function createProduct(authHeaders, productName) {
    const productPayload = {
        productCode: Math.floor(Math.random() * 100000),
        prodName: productName,
        detailDesc: "k6 test product",
        price: 10
    };
    return http.post(`${BASE_URL}/v1/products`, JSON.stringify(productPayload), { headers: authHeaders });
}

// Main logic
function runCoPurchaseFlow(version) {
    const uniqueId = `${__VU}_${Date.now()}`;
    const adminUsername = `admin_cp_${uniqueId}`;
    const userUsername = `user_cp_${uniqueId}`;
    const password = 'password';

    // 1. Setup: Create admin, login, create products
    let adminToken, product1Id, product2Id;

    const adminSignupRes = http.post(`${BASE_URL}/v1/auth/signup-admin`, JSON.stringify({ username: adminUsername, password: password, name: "Admin"}), { headers: { 'Content-Type': 'application/json' }});
    check(adminSignupRes, { 'admin signup ok': (r) => r.status === 200 });
    
    const adminLoginRes = http.post(`${BASE_URL}/v1/auth/login`, JSON.stringify({ username: adminUsername, password: password }), { headers: { 'Content-Type': 'application/json' }});
    if (adminLoginRes.status === 200) {
        adminToken = adminLoginRes.json('accessToken');
        const adminHeaders = { 'Authorization': `Bearer ${adminToken}`, 'Content-Type': 'application/json' };
        
        const p1Res = createProduct(adminHeaders, `P1_${uniqueId}`);
        const p2Res = createProduct(adminHeaders, `P2_${uniqueId}`);

        if (p1Res.status === 201) product1Id = p1Res.json('productId');
        if (p2Res.status === 201) product2Id = p2Res.json('productId');
    }

    if (!product1Id || !product2Id) {
        return; // Can't proceed if products weren't created
    }

    // 2. Setup: Create user, login, create order
    let userToken;
    const userSignupRes = http.post(`${BASE_URL}/v1/auth/signup`, JSON.stringify({ username: userUsername, password: password, name: "User"}), { headers: { 'Content-Type': 'application/json' }});
    check(userSignupRes, { 'user signup ok': (r) => r.status === 200 });

    const userLoginRes = http.post(`${BASE_URL}/v1/auth/login`, JSON.stringify({ username: userUsername, password: password }), { headers: { 'Content-Type': 'application/json' }});
    if (userLoginRes.status === 200) {
        userToken = userLoginRes.json('accessToken');
        const userHeaders = { 'Authorization': `Bearer ${userToken}`, 'Content-Type': 'application/json' };

        const orderPayload = { items: [{ productId: product1Id, quantity: 1 }, { productId: product2Id, quantity: 1 }] };
        http.post(`${BASE_URL}/v1/orders`, JSON.stringify(orderPayload), { headers: userHeaders });
    }

    if (!userToken) {
        return; // Can't proceed without user login
    }
    
    sleep(1); // Give time for order to process

    // 3. Execute and Measure
    const userHeaders = { 'Authorization': `Bearer ${userToken}` };
    const totalStartTime = new Date();

    if (version === 'v1') {
        const res = http.get(`${BASE_URL}/v1/co-purchase/${product1Id}`, { headers: userHeaders });
        check(res, { 'v1 get recommendation IDs ok': (r) => r.status === 200 });

        if (res.status === 200 && res.json().length > 0) {
            const recommendedIds = res.json().map(p => p.productId);
            for (const id of recommendedIds) {
                http.get(`${BASE_URL}/v1/products/${id}`, { headers: userHeaders });
            }
        }
        const totalEndTime = new Date();
        v1Trend.add(totalEndTime - totalStartTime);

    } else { // version === 'v2'
        const res = http.get(`${BASE_URL}/v2/co-purchase/${product1Id}`, { headers: userHeaders });
        check(res, { 'v2 get recommendations ok': (r) => r.status === 200 });
        const totalEndTime = new Date();
        v2Trend.add(totalEndTime - totalStartTime);
    }
}

export function testCoPurchaseV1() {
    runCoPurchaseFlow('v1');
}

export function testCoPurchaseV2() {
    runCoPurchaseFlow('v2');
}