import http from 'k6/http';
import { check, sleep, group } from 'k6';
import { Trend } from 'k6/metrics';

const missTrend = new Trend('copurchase_miss_duration');
const hitTrend = new Trend('copurchase_hit_duration');

export let options = {
    vus: 5,
    duration: '30s',
};

const BASE_URL = 'http://localhost:8080/api/v1';

function signup(username, password, name, isAdmin = false) {
    const endpoint = isAdmin ? '/auth/signup-admin' : '/auth/signup';
    const signUpRequest = { username, password, name };
    return http.post(`${BASE_URL}${endpoint}`, JSON.stringify(signUpRequest), {
        headers: { 'Content-Type': 'application/json' },
    });
}

function login(username, password) {
    const credentials = { username, password };
    return http.post(`${BASE_URL}/auth/login`, JSON.stringify(credentials), {
        headers: { 'Content-Type': 'application/json' },
    });
}

function createProduct(authHeaders, productName, price) {
    const productPayload = {
        productCode: Math.floor(Math.random() * 100000),
        prodName: productName,
        detailDesc: "Created by k6 for co-purchase test",
        price: price
    };
    return http.post(`${BASE_URL}/products`, JSON.stringify(productPayload), { headers: authHeaders });
}

export default function () {
    const uniqueId = `${__VU}_${Date.now()}`;

    const adminUsername = `admin_cp_${uniqueId}`;
    const password = 'password';
    
    signup(adminUsername, password, `Admin ${uniqueId}`, true);
    const adminLoginRes = login(adminUsername, password);

    let product1Id, product2Id;
    
    if (adminLoginRes.status === 200) {
        const adminToken = adminLoginRes.json('accessToken');
        const adminHeaders = { 'Authorization': `Bearer ${adminToken}`, 'Content-Type': 'application/json' };

        const p1 = createProduct(adminHeaders, `P1_${uniqueId}`, 10);
        const p2 = createProduct(adminHeaders, `P2_${uniqueId}`, 20);
        
        if (p1.status === 201) product1Id = p1.json('productId');
        if (p2.status === 201) product2Id = p2.json('productId');
    }

    if (!product1Id || !product2Id) return;

    sleep(1);

    const userUsername = `user_cp_${uniqueId}`;
    signup(userUsername, password, `User ${uniqueId}`, false);
    const userLoginRes = login(userUsername, password);

    if (userLoginRes.status === 200) {
        const userToken = userLoginRes.json('accessToken');
        const userHeaders = { 'Authorization': `Bearer ${userToken}`, 'Content-Type': 'application/json' };

        const orderPayload = { items: [{ productId: product1Id, quantity: 1 }, { productId: product2Id, quantity: 1 }] };
        const orderRes = http.post(`${BASE_URL}/orders`, JSON.stringify(orderPayload), { headers: userHeaders });
        check(orderRes, { 'order created': (r) => r.status === 200 });

        sleep(1); 

        group('1st Request (Cache Miss)', () => {
            const start = new Date();
            const res = http.get(`${BASE_URL}/co-purchase/${product1Id}`, { headers: userHeaders });
            const end = new Date();
            
            check(res, { 'miss request ok': (r) => r.status === 200 });
            missTrend.add(end - start);
        });

        sleep(0.5);

        group('2nd Request (Cache Hit)', () => {
            const start = new Date();
            const res = http.get(`${BASE_URL}/co-purchase/${product1Id}`, { headers: userHeaders });
            const end = new Date();
            
            check(res, { 'hit request ok': (r) => r.status === 200 });
            hitTrend.add(end - start);
        });
    }
}
