import http from 'k6/http';
import { check, sleep } from 'k6';

export let options = {
    vus: 200,
    duration: '30s',
};

const VERSION = __ENV.VERSION || 'v1'; // Default to v1, can be set to 'v2'
const BASE_URL = 'http://localhost:8080/api';

// Auth endpoints - Pinned to v1 to isolate Product testing
const AUTH_URL = `${BASE_URL}/v1/auth`;

// Product Write Endpoints (Always v1 as v2 is read-only/read-optimized)
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
    const uniqueId = `${__VU}_${Date.now()}`;

    // Admin user scenario
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
        
        // Create - Always V1
        const createRes = http.post(`${PRODUCT_WRITE_URL}`, JSON.stringify(productPayload), { headers: authHeaders });
        check(createRes, { 'admin can create product': (r) => r.status === 201 });
        
        if (createRes.status !== 201) {
            console.error(`Product creation failed: Status ${createRes.status}, Body: ${createRes.body}`);
        }

        if (createRes.status === 201) {
            const productId = createRes.json().productId;

            // Read - V1 or V2
            // V1 returns Product (nested), V2 returns ProductSearch (flat). 
            // Both should be 200 OK.
            const getRes = http.get(`${PRODUCT_READ_URL}/${productId}`, { headers: authHeaders });
            check(getRes, { 
                [`admin can read product (${VERSION})`]: (r) => r.status === 200,
                'has product name': (r) => r.json('prodName') !== undefined
            });
            
            if (getRes.status !== 200) {
                console.error(`Product read failed: Status ${getRes.status}, Body: ${getRes.body}`);
            }

            // Update - Always V1
            const productUpdatePayload = {
                ...productPayload,
                prodName: `K6 Updated Product ${uniqueId}`,
                price: 150.0
            };

            const updateRes = http.put(`${PRODUCT_WRITE_URL}/${productId}`, JSON.stringify(productUpdatePayload), { headers: authHeaders });
            check(updateRes, { 'admin can update product': (r) => r.status === 200 });

            // Delete - Always V1
            const deleteRes = http.del(`${PRODUCT_WRITE_URL}/${productId}`, null, { headers: authHeaders });
            check(deleteRes, { 'admin can delete product': (r) => r.status === 204 });
        }
    } 
    sleep(1);


    // User Scenario
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

        // Search Keyword - V1 and V2 support this
        // Using "Trousers" or "K6" which we likely inserted (though we deleted the specific one, 
        // in a real perf test multiple VUs run in parallel so data might exist, or seed data exists)
        const searchRes = http.get(`${PRODUCT_READ_URL}/search?keyword=Trousers`, { headers: userAuthHeaders });
        check(searchRes, {
            [`user can search for products (${VERSION})`]: (r) => r.status === 200,
            'search results are an array': (r) => r.json() && Array.isArray(r.json()),
        });

        // Version Specific Tests
        if (VERSION === 'v2') {
            // V2: Test Filtered Search
            // Filter by Department and Product Group
            // Ensure parameters are encoded (e.g. spaces)
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
            // V1: Test Get All Products (Only in V1)
            const getAllRes = http.get(`${PRODUCT_READ_URL}`, { headers: userAuthHeaders });
            check(getAllRes, { 'user can read all products (v1)': (r) => r.status === 200 });
        }
    }
    sleep(1);
}
