import http from 'k6/http';
import { check, sleep, group } from 'k6';
import { Trend } from 'k6/metrics';

const v1MissTrend = new Trend('copurchase_v1_miss_duration');
const v1HitTrend = new Trend('copurchase_v1_hit_duration');
const v2MissTrend = new Trend('copurchase_v2_miss_duration');
const v2HitTrend = new Trend('copurchase_v2_hit_duration');

export let options = {
    scenarios: {
        v1_copurchase: {
            executor: 'constant-vus',
            exec: 'testCoPurchaseV1',
            vus: 5,
            duration: '45s',
        },
        v2_copurchase: {
            executor: 'constant-vus',
            exec: 'testCoPurchaseV2',
            vus: 5,
            duration: '45s',
            startTime: '47s',
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

// Main flow for a single VU
function setupAndExecute() {
    const uniqueId = `${__VU}_${Date.now()}`;
    const adminUsername = `admin_cp_${uniqueId}`;
    const userUsername = `user_cp_${uniqueId}`;
    const password = 'password';

    // 1. Setup: Create admin, login, create products
    let adminToken, product1Id, product2Id;

    const adminSignupRes = http.post(`${BASE_URL}/v1/auth/signup-admin`, JSON.stringify({ username: adminUsername, password: password, name: "Admin"}), { headers: { 'Content-Type': 'application/json' }});
    if (adminSignupRes.status !== 200) return null;
    
    const adminLoginRes = http.post(`${BASE_URL}/v1/auth/login`, JSON.stringify({ username: adminUsername, password: password }), { headers: { 'Content-Type': 'application/json' }});
    if (adminLoginRes.status !== 200) return null;

    adminToken = adminLoginRes.json('accessToken');
    const adminHeaders = { 'Authorization': `Bearer ${adminToken}`, 'Content-Type': 'application/json' };
    
    const p1Res = createProduct(adminHeaders, `P1_${uniqueId}`);
    const p2Res = createProduct(adminHeaders, `P2_${uniqueId}`);

    if (p1Res.status === 201) product1Id = p1Res.json('productId');
    if (p2Res.status === 201) product2Id = p2Res.json('productId');

    if (!product1Id || !product2Id) return null;

    // 2. Setup: Create user, login, create order
    let userToken;
    const userSignupRes = http.post(`${BASE_URL}/v1/auth/signup`, JSON.stringify({ username: userUsername, password: password, name: "User"}), { headers: { 'Content-Type': 'application/json' }});
    if (userSignupRes.status !== 200) return null;

    const userLoginRes = http.post(`${BASE_URL}/v1/auth/login`, JSON.stringify({ username: userUsername, password: password }), { headers: { 'Content-Type': 'application/json' }});
    if (userLoginRes.status === 200) {
        userToken = userLoginRes.json('accessToken');
        const userHeaders = { 'Authorization': `Bearer ${userToken}`, 'Content-Type': 'application/json' };

        const orderPayload = { items: [{ productId: product1Id, quantity: 1 }, { productId: product2Id, quantity: 1 }] };
        http.post(`${BASE_URL}/v1/orders`, JSON.stringify(orderPayload), { headers: userHeaders });
    }

    if (!userToken) return null;

    sleep(1); 
    
    return { userToken, product1Id };
}

// V1 Test Execution
export function testCoPurchaseV1() {
    const setupData = setupAndExecute();
    if (!setupData) return;

    const { userToken, product1Id } = setupData;
    const userHeaders = { 'Authorization': `Bearer ${userToken}` };

    // V1 - Cache Miss
    group('V1 - Cache Miss', () => {
        const startTime = new Date();
        const res = http.get(`${BASE_URL}/v1/co-purchase/${product1Id}`, { headers: userHeaders });
        if (res.status === 200 && res.json().length > 0) {
            const recommendedIds = res.json().map(p => p.productId);
            for (const id of recommendedIds) {
                http.get(`${BASE_URL}/v1/products/${id}`, { headers: userHeaders });
            }
        }
        const endTime = new Date();
        v1MissTrend.add(endTime - startTime);
    });

    sleep(0.5);

    // V1 - Cache Hit
    group('V1 - Cache Hit', () => {
        const startTime = new Date();
        const res = http.get(`${BASE_URL}/v1/co-purchase/${product1Id}`, { headers: userHeaders });
        if (res.status === 200 && res.json().length > 0) {
            const recommendedIds = res.json().map(p => p.productId);
            for (const id of recommendedIds) {
                http.get(`${BASE_URL}/v1/products/${id}`, { headers: userHeaders });
            }
        }
        const endTime = new Date();
        v1HitTrend.add(endTime - startTime);
    });
}

// V2 Test Execution
export function testCoPurchaseV2() {
    const setupData = setupAndExecute();
    if (!setupData) return;

    const { userToken, product1Id } = setupData;
    const userHeaders = { 'Authorization': `Bearer ${userToken}` };

    // V2 - Cache Miss
    group('V2 - Cache Miss', () => {
        const startTime = new Date();
        http.get(`${BASE_URL}/v2/co-purchase/${product1Id}`, { headers: userHeaders });
        const endTime = new Date();
        v2MissTrend.add(endTime - startTime);
    });

    sleep(0.5);

    // V2 - Cache Hit
    group('V2 - Cache Hit', () => {
        const startTime = new Date();
        http.get(`${BASE_URL}/v2/co-purchase/${product1Id}`, { headers: userHeaders });
        const endTime = new Date();
        v2HitTrend.add(endTime - startTime);
    });
}
