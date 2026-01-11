import http from 'k6/http';
import { check, sleep } from 'k6';

export let options = {
    vus: 200,
    duration: '30s',
};

const VERSION = __ENV.VERSION || 'v1';
const BASE_URL = 'http://localhost:8080/api';

const AUTH_URL = `${BASE_URL}/v1/auth`;
const PRODUCT_WRITE_URL = `${BASE_URL}/v1/products`;
const PRODUCT_READ_URL = `${BASE_URL}/${VERSION}/products`;

function signup(username, password, name, isAdmin = false) {
    const endpoint = isAdmin ? '/signup-admin' : '/signup';
    const signUpRequest = {
        username: username,
        password: password,
        name: name,
    };
    const signupRes = http.post(`${AUTH_URL}${endpoint}`, JSON.stringify(signUpRequest), {
        headers: { 'Content-Type': 'application/json' },
    });
    return signupRes;
}

function login(username, password) {
    const credentials = { username: username, password: password };
    const loginRes = http.post(`${AUTH_URL}/login`, JSON.stringify(credentials), {
        headers: { 'Content-Type': 'application/json' },
    });
    return loginRes;
}

export default function () {
    const uniqueId = `${__VU}_${Date.now()}`;

    const adminUsername = `admin_${uniqueId}_${VERSION}`;
    const adminPassword = 'password';
    const adminName = `Admin ${uniqueId}`;

    const adminSignupRes = signup(adminUsername, adminPassword, adminName, true);
    check(adminSignupRes, { 'admin signup successful': (r) => r.status === 200 || r.status === 201 });

    const adminLoginRes = login(adminUsername, adminPassword);
    check(adminLoginRes, { 'admin login successful': (r) => r.status === 200 });

    if (adminLoginRes.status === 200) {
        const adminAccessToken = adminLoginRes.json('accessToken');
        const authHeaders = { 'Authorization': `Bearer ${adminAccessToken}`, 'Content-Type': 'application/json' };

        const productPayload = {
            productCode: Math.floor(Math.random() * 100000),
            prodName: `K6 Test Product ${uniqueId}`,
            productTypeName: "Trousers",
            productGroupName: "Garment Lower body",
            graphicalAppearanceName: "Solid",
            colourGroupName: "Dark Blue",
            perceivedColourValueName: "Dark",
            perceivedColourMasterName: "Blue",
            departmentName: "Men",
            indexName: "A",
            indexGroupName: "Ladieswear",
            sectionName: "Menswear",
            garmentGroupName: "Trousers",
            detailDesc: "Created by k6 performance test",
            price: 100.0
        };

        const createRes = http.post(`${PRODUCT_WRITE_URL}`, JSON.stringify(productPayload), { headers: authHeaders });
        check(createRes, { 'admin can create product': (r) => r.status === 201 });

        if (createRes.status !== 201) {
            console.error(`Product creation failed: Status ${createRes.status}, Body: ${createRes.body}`);
        }

        if (createRes.status === 201) {
            const productId = createRes.json().productId;

            const getRes = http.get(`${PRODUCT_READ_URL}/${productId}`, { headers: authHeaders });
            check(getRes, {
                [`admin can read product (${VERSION})`]: (r) => r.status === 200,
                'has product name': (r) => r.json('prodName') !== undefined
            });

            if (getRes.status !== 200) {
                console.error(`Product read failed: Status ${getRes.status}, Body: ${getRes.body}`);
            }

            const productUpdatePayload = {
                ...productPayload,
                prodName: `K6 Updated Product ${uniqueId}`,
                price: 150.0
            };

            const updateRes = http.put(`${PRODUCT_WRITE_URL}/${productId}`, JSON.stringify(productUpdatePayload), { headers: authHeaders });
            check(updateRes, { 'admin can update product': (r) => r.status === 200 });

            const deleteRes = http.del(`${PRODUCT_WRITE_URL}/${productId}`, null, { headers: authHeaders });
            check(deleteRes, { 'admin can delete product': (r) => r.status === 204 });
        }
    }
    sleep(1);

    const userUsername = `user_${uniqueId}_${VERSION}`;
    const userPassword = 'password';
    const userName = `Test User ${uniqueId}`;

    const userSignupRes = signup(userUsername, userPassword, userName, false);
    check(userSignupRes, { 'user signup successful': (r) => r.status === 200 || r.status === 201 });

    const userLoginRes = login(userUsername, userPassword);
    check(userLoginRes, { 'user login successful': (r) => r.status === 200 });

    if (userLoginRes.status === 200) {
        const userAccessToken = userLoginRes.json('accessToken');
        const userAuthHeaders = { 'Authorization': `Bearer ${userAccessToken}`, 'Content-Type': 'application/json' };

        const searchRes = http.get(`${PRODUCT_READ_URL}/search?keyword=Trousers`, { headers: userAuthHeaders });
        check(searchRes, {
            [`user can search for products (${VERSION})`]: (r) => r.status === 200,
            'search results are an array': (r) => r.json() && Array.isArray(r.json()),
        });

        if (VERSION === 'v2') {
            const productGroup = encodeURIComponent('Garment Lower body');
            const params = `keyword=Trousers&department=Men&productGroup=${productGroup}`;
            const filterSearchRes = http.get(`${PRODUCT_READ_URL}/search?${params}`, { headers: userAuthHeaders });

            const isStatus200 = check(filterSearchRes, {
                'user can search with filters (v2)': (r) => r.status === 200,
            });

            if (!isStatus200) {
                console.error(`Filtered search failed. Status: ${filterSearchRes.status}, Body: ${filterSearchRes.body}`);
            } else {
                check(filterSearchRes, {
                    'filtered results are an array': (r) => {
                        try {
                            return Array.isArray(r.json());
                        } catch (e) {
                            console.error(`JSON Parse Error: ${e}, Body: ${r.body}`);
                            return false;
                        }
                    },
                });
            }
        } else {
            const getAllRes = http.get(`${PRODUCT_READ_URL}`, { headers: userAuthHeaders });
            check(getAllRes, { 'user can read all products (v1)': (r) => r.status === 200 });
        }
    }
    sleep(1);
}