import http from 'k6/http';
import { check, sleep } from 'k6';

export let options = {
    vus: 10,
    duration: '10s',
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
        detailDesc: "Created by k6 for order test",
        price: price
    };
    return http.post(`${BASE_URL}/products`, JSON.stringify(productPayload), { headers: authHeaders });
}

export default function () {
    const adminUsername = `admin_order_test_${__VU}_${Date.now()}`;
    const adminPassword = 'password';
    const adminName = `Admin Order User ${__VU}`;

    const adminSignupRes = signup(adminUsername, adminPassword, adminName, true);
    check(adminSignupRes, { 'admin signup successful': (r) => r.status === 200 || r.status === 201 });
    sleep(1);

    const adminLoginRes = login(adminUsername, adminPassword);
    check(adminLoginRes, { 'admin login successful': (r) => r.status === 200 });

    let productId;
    if (adminLoginRes.status === 200) {
        const adminAccessToken = adminLoginRes.json('accessToken');
        const adminAuthHeaders = { 'Authorization': `Bearer ${adminAccessToken}`, 'Content-Type': 'application/json' };

        const productName = `Test Product ${__VU}`;
        const productPrice = 100.0;
        const createProductRes = createProduct(adminAuthHeaders, productName, productPrice);

        check(createProductRes, { 'admin can create product': (r) => r.status === 201 || r.status === 200 });
        if (createProductRes.status === 201 || createProductRes.status === 200) {
            productId = createProductRes.json('productId');
        }
    }
    sleep(1);

    if (!productId) {
        console.log("Failed to create a product, skipping order tests for this VU.");
        return;
    }

    const userUsername = `user_order_test_${__VU}_${Date.now()}`;
    const userPassword = 'password';
    const userName = `Order Test User ${__VU}`;

    const userSignupRes = signup(userUsername, userPassword, userName);
    check(userSignupRes, { 'user signup successful': (r) => r.status === 200 || r.status === 201 });
    sleep(1);

    const userLoginRes = login(userUsername, userPassword);
    check(userLoginRes, { 'user login successful': (r) => r.status === 200 });

    if (userLoginRes.status === 200) {
        const userAccessToken = userLoginRes.json('accessToken');
        const userAuthHeaders = { 'Authorization': `Bearer ${userAccessToken}`, 'Content-Type': 'application/json' };

        const orderPayload = {
            items: [{ productId: productId, quantity: 2 }]
        };
        const createOrderRes = http.post(`${BASE_URL}/orders`, JSON.stringify(orderPayload), { headers: userAuthHeaders });
        check(createOrderRes, { 'user can create order': (r) => r.status === 200 });
        let orderId;
        if (createOrderRes.status === 200) {
            orderId = createOrderRes.json('orderId');
        }
        sleep(1);

        if (orderId) {
            const getMyOrdersRes = http.get(`${BASE_URL}/orders`, { headers: userAuthHeaders });
            check(getMyOrdersRes, { 'user can get their orders': (r) => r.status === 200 });
            sleep(1);

            const getOrderByIdRes = http.get(`${BASE_URL}/orders/${orderId}`, { headers: userAuthHeaders });
            check(getOrderByIdRes, { 'user can get order by ID': (r) => r.status === 200 });
        }
    }
    sleep(1);
}
