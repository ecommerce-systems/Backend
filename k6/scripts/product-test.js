import http from 'k6/http';
import { check, sleep } from 'k6';

export let options = {
    vus: 10,
    duration: '10s',
};

const VERSION = __ENV.VERSION || 'v1'; // Default to v1, can be set to 'v2'
const BASE_URL = 'http://localhost:8080/api';

// Auth endpoints (Assuming we want to use the same version for Auth, or fallback to v1 if v2 auth is unstable/different)
const AUTH_URL = `${BASE_URL}/${VERSION}/auth`;

// Product Write Endpoints (Always v1 as v2 is read-only)
const PRODUCT_WRITE_URL = `${BASE_URL}/v1/products`;

// Product Read Endpoints (Follows the requested version)
const PRODUCT_READ_URL = `${BASE_URL}/${VERSION}/products`;

console.log(`Running Product Test with Version: ${VERSION}`);
console.log(`Auth URL: ${AUTH_URL}`);
console.log(`Product Write URL: ${PRODUCT_WRITE_URL}`);
console.log(`Product Read URL: ${PRODUCT_READ_URL}`);

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
    // Admin user scenario
    const adminUsername = `adminuser${__VU}_${Date.now()}_${VERSION}`;
    const adminPassword = 'password';
    const adminName = `Admin User ${__VU}`;

    const adminSignupRes = signup(adminUsername, adminPassword, adminName, true);
    check(adminSignupRes, { 'admin signup successful': (r) => r.status === 200 || r.status === 201 });

    const adminLoginRes = login(adminUsername, adminPassword);
    check(adminLoginRes, { 'admin login successful': (r) => r.status === 200 });

    if (adminLoginRes.status === 200) {
        const adminAccessToken = adminLoginRes.json('accessToken');
        const authHeaders = { 'Authorization': `Bearer ${adminAccessToken}`, 'Content-Type': 'application/json' };
        
        const productPayload = {
            productCode: 12345,
            prodName: "K6 Test Product",
            productTypeName: "Test Type",
            productGroupName: "Test Group",
            graphicalAppearanceName: "Test Appearance",
            colourGroupName: "Test Color",
            perceivedColourValueName: "Test Color Value",
            perceivedColourMasterName: "Test Color Master",
            departmentName: "Test Department",
            indexName: "Test Index",
            indexGroupName: "Test Index Group",
            sectionName: "Test Section",
            garmentGroupName: "Test Garment Group",
            detailDesc: "Created by k6",
            price: 100.0
        };
        
        // Create - Always V1
        const createRes = http.post(`${PRODUCT_WRITE_URL}`, JSON.stringify(productPayload), { headers: authHeaders });
        check(createRes, { 'admin can create product': (r) => r.status === 201 });
        
        if (createRes.status !== 201) {
            console.error(`Product creation failed: Status ${createRes.status}, Body: ${createRes.body}`);
        }

        if (createRes.status === 201) {
            const productId = createRes.json().productId;

            // Read - V1 or V2
            const getRes = http.get(`${PRODUCT_READ_URL}/${productId}`, { headers: authHeaders });
            check(getRes, { [`admin can read product (${VERSION})`]: (r) => r.status === 200 });
            
            if (getRes.status !== 200) {
                console.error(`Product read failed: Status ${getRes.status}, Body: ${getRes.body}`);
            }

            // Update - Always V1
            const productUpdatePayload = {
                productId: productId,
                productCode: 54321,
                prodName: "K6 Updated Product",
                detailDesc: "Updated by k6",
                price: 150.0,
                productTypeName: "Updated Type",
                productGroupName: "Updated Group",
                graphicalAppearanceName: "Updated Appearance",
                colourGroupName: "Updated Color",
                perceivedColourValueName: "Updated Color Value",
                perceivedColourMasterName: "Updated Color Master",
                departmentName: "Updated Department",
                indexName: "Updated Index",
                indexGroupName: "Updated Index Group",
                sectionName: "Updated Section",
                garmentGroupName: "Updated Garment Group"
            };

            const updateRes = http.put(`${PRODUCT_WRITE_URL}/${productId}`, JSON.stringify(productUpdatePayload), { headers: authHeaders });
            check(updateRes, { 'admin can update product': (r) => r.status === 200 });

            // Delete - Always V1
            const deleteRes = http.del(`${PRODUCT_WRITE_URL}/${productId}`, null, { headers: authHeaders });
            check(deleteRes, { 'admin can delete product': (r) => r.status === 204 });
        }
    } else {
        console.error(`Admin login failed: Status ${adminLoginRes.status}, Body: ${adminLoginRes.body}`);
    }
    sleep(1);


    // User Scenario
    const userUsername = `testuser${__VU}_${Date.now()}_${VERSION}`;
    const userPassword = 'password';
    const userName = `Test User ${__VU}`;

    const userSignupRes = signup(userUsername, userPassword, userName, false);
    check(userSignupRes, { 'user signup successful': (r) => r.status === 200 || r.status === 201 });
    sleep(1);

    const userLoginRes = login(userUsername, userPassword);
    check(userLoginRes, { 'user login successful': (r) => r.status === 200 });

    if (userLoginRes.status === 200) {
        const userAccessToken = userLoginRes.json('accessToken');
        const userAuthHeaders = { 'Authorization': `Bearer ${userAccessToken}`, 'Content-Type': 'application/json' };


        // Create Attempt - Always V1 URL to check permissions
        const productPayload = { prodName: "K6 Test Product", detailDesc: "Created by k6" };
        const createAttemptRes = http.post(`${PRODUCT_WRITE_URL}`, JSON.stringify(productPayload), { headers: userAuthHeaders });
        check(createAttemptRes, { 'user cannot create product': (r) => r.status === 403 });

        // Read All - Only V1 supports this
        if (VERSION === 'v1') {
            const getAllRes = http.get(`${PRODUCT_READ_URL}`, { headers: userAuthHeaders });
            check(getAllRes, { 'user can read products': (r) => r.status === 200 });
        }

        // Search - V1 and V2 support this
        const searchRes = http.get(`${PRODUCT_READ_URL}/search?keyword=Test`, { headers: userAuthHeaders });
        check(searchRes, {
            [`user can search for products (${VERSION})`]: (r) => r.status === 200,
            'search results are an array': (r) => r.json() && Array.isArray(r.json()),
        });
    }
    sleep(1);
}
