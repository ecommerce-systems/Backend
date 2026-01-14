import http from 'k6/http';
import { check, sleep } from 'k6';

export let options = {
    vus: 5,
    duration: '30s',
};

const BASE_URL = 'http://localhost:8080/api/v1';

function signup(username, password, name, isAdmin = false) {
    const endpoint = isAdmin ? '/auth/signup-admin' : '/auth/signup';
    const signUpRequest = {
        username: username,
        password: password,
        name: name,
    };
    const signupRes = http.post(`${BASE_URL}${endpoint}`, JSON.stringify(signUpRequest), {
        headers: { 'Content-Type': 'application/json' },
    });
    return signupRes;
}

function login(username, password) {
    const credentials = { username: username, password: password };
    const loginRes = http.post(`${BASE_URL}/auth/login`, JSON.stringify(credentials), {
        headers: { 'Content-Type': 'application/json' },
    });
    return loginRes;
}

function createProduct(authHeaders, productName, price) {
    const productPayload = {
        prodName: productName,
        detailDesc: "Created by k6 for co-purchase test",
        price: price
    };
    return http.post(`${BASE_URL}/products`, JSON.stringify(productPayload), { headers: authHeaders });
}

export default function () {
    const adminUsername = `admin_copurchase_${__VU}_${Date.now()}`;
    const adminPassword = 'password';
    const adminName = `Admin Co-Purchase User ${__VU}`;

    const adminSignupRes = signup(adminUsername, adminPassword, adminName, true);
    check(adminSignupRes, { 'admin signup successful': (r) => r.status === 200 || r.status === 201 });
    sleep(1);

    const adminLoginRes = login(adminUsername, adminPassword);
    check(adminLoginRes, { 'admin login successful': (r) => r.status === 200 });

    let product1Id, product2Id, product3Id;
    if (adminLoginRes.status === 200) {
        const adminAccessToken = adminLoginRes.json('accessToken');
        const adminAuthHeaders = { 'Authorization': `Bearer ${adminAccessToken}`, 'Content-Type': 'application/json' };

        const createP1Res = createProduct(adminAuthHeaders, `Product 1 ${__VU}`, 10);
        check(createP1Res, { 'admin created product 1': (r) => r.status === 200 || r.status === 201 });
        if (createP1Res.status === 200 || createP1Res.status === 201) product1Id = createP1Res.json('productId');

        const createP2Res = createProduct(adminAuthHeaders, `Product 2 ${__VU}`, 20);
        check(createP2Res, { 'admin created product 2': (r) => r.status === 200 || r.status === 201 });
        if (createP2Res.status === 200 || createP2Res.status === 201) product2Id = createP2Res.json('productId');

        const createP3Res = createProduct(adminAuthHeaders, `Product 3 ${__VU}`, 30);
        check(createP3Res, { 'admin created product 3': (r) => r.status === 200 || r.status === 201 });
        if (createP3Res.status === 200 || createP3Res.status === 201) product3Id = createP3Res.json('productId');
    }
    sleep(1);

    if (!product1Id || !product2Id || !product3Id) {
        console.log("Failed to create products, skipping co-purchase tests for this VU.");
        return;
    }

    const userUsername = `user_copurchase_${__VU}_${Date.now()}`;
    const userPassword = 'password';
    const userName = `Co-Purchase Test User ${__VU}`;

    const userSignupRes = signup(userUsername, userPassword, userName);
    check(userSignupRes, { 'user signup successful': (r) => r.status === 200 || r.status === 201 });
    sleep(1);

    const userLoginRes = login(userUsername, userPassword);
    check(userLoginRes, { 'user login successful': (r) => r.status === 200 });

    if (userLoginRes.status === 200) {
        const userAccessToken = userLoginRes.json('accessToken');
        const userAuthHeaders = { 'Authorization': `Bearer ${userAccessToken}`, 'Content-Type': 'application/json' };

        const order1Payload = { items: [{ productId: product1Id, quantity: 1 }, { productId: product2Id, quantity: 1 }] };
        const createOrder1Res = http.post(`${BASE_URL}/orders`, JSON.stringify(order1Payload), { headers: userAuthHeaders });
        check(createOrder1Res, { 'user created order with P1 and P2': (r) => r.status === 200 });
        sleep(1);

        const order2Payload = { items: [{ productId: product1Id, quantity: 1 }, { productId: product3Id, quantity: 1 }] };
        const createOrder2Res = http.post(`${BASE_URL}/orders`, JSON.stringify(order2Payload), { headers: userAuthHeaders });
        check(createOrder2Res, { 'user created order with P1 and P3': (r) => r.status === 200 });
        sleep(1);

        const recommendationsRes = http.get(`${BASE_URL}/co-purchase/${product1Id}`, { headers: userAuthHeaders });
        check(recommendationsRes, {
            'get recommendations returns 200': (r) => r.status === 200,
            'get recommendations returns an array': (r) => {
                try {
                    return Array.isArray(r.json());
                } catch (e) {
                    console.error(`Failed to parse recommendations response: ${r.body}`);
                    return false;
                }
            },
        });
        sleep(1);
    }
}