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

export default function () {
    // Admin user scenario
    const adminUsername = `adminuser${__VU}_${Date.now()}`;
    const adminPassword = 'password';
    const adminName = `Admin User ${__VU}`;

    const adminSignupRes = signup(adminUsername, adminPassword, adminName, true);
    check(adminSignupRes, { 'admin signup successful': (r) => r.status === 200 || r.status === 201 });
    // Removed sleep(1)

    const adminLoginRes = login(adminUsername, adminPassword);
    check(adminLoginRes, { 'admin login successful': (r) => r.status === 200 });

            if (adminLoginRes.status === 200) {
                const adminAccessToken = adminLoginRes.json('accessToken');
                const authHeaders = { 'Authorization': `Bearer ${adminAccessToken}`, 'Content-Type': 'application/json' };
                console.log(`Admin Access Token: ${adminAccessToken}`);
                console.log(`Auth Headers for Product Create/Update: ${JSON.stringify(authHeaders)}`);
    
                const productPayload = {            productCode: 12345,
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
        const createRes = http.post(`${BASE_URL}/products`, JSON.stringify(productPayload), { headers: authHeaders });
        check(createRes, { 'admin can create product': (r) => r.status === 201 });
        
        if (createRes.status === 201) {
            console.log("Create Product Response:", JSON.stringify(createRes.json(), null, 2));
            const productId = createRes.json().productId;

            const getRes = http.get(`${BASE_URL}/products/${productId}`, { headers: authHeaders });
            check(getRes, { 'admin can read product': (r) => r.status === 200 });

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
            
            const updateRes = http.put(`${BASE_URL}/products/${productId}`, JSON.stringify(productUpdatePayload), { headers: authHeaders });
            if (updateRes.status === 200) {
                console.log("Update Product Response:", JSON.stringify(updateRes.json(), null, 2));
            } else {
                console.log(`Update Product Failed: Status ${updateRes.status}, Body: ${updateRes.body}`);
            }
            check(updateRes, { 'admin can update product': (r) => r.status === 200 });

            // const deleteRes = http.del(`${BASE_URL}/products/${productId}`, null, { headers: authHeaders });
            // check(deleteRes, { 'admin can delete product': (r) => r.status === 204 });
        }
    }
    sleep(1);


    const userUsername = `testuser${__VU}_${Date.now()}`;
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


        const productPayload = { prodName: "K6 Test Product", detailDesc: "Created by k6" };
        const createAttemptRes = http.post(`${BASE_URL}/products`, JSON.stringify(productPayload), { headers: userAuthHeaders });
        check(createAttemptRes, { 'user cannot create product': (r) => r.status === 403 });



        const getAllRes = http.get(`${BASE_URL}/products`, { headers: userAuthHeaders });
        check(getAllRes, { 'user can read products': (r) => r.status === 200 });

        const searchRes = http.get(`${BASE_URL}/products/search?keyword=Test`, { headers: userAuthHeaders });
        check(searchRes, {
            'user can search for products': (r) => r.status === 200,
            'search results are an array': (r) => r.json() && Array.isArray(r.json()),
        });
    }
    sleep(1);
}