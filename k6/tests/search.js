import http from 'k6/http';
import { check, sleep } from 'k6';
import { Trend } from 'k6/metrics';

const searchTrendV1 = new Trend('search_duration_v1');
const searchTrendV2 = new Trend('search_duration_v2');

export let options = {
    scenarios: {
        v1_product: {
            executor: 'constant-vus',
            exec: 'testProductV1',
            vus: 50,
            duration: '20s',
        },
        v2_product: {
            executor: 'constant-vus',
            exec: 'testProductV2',
            vus: 50,
            duration: '20s',
            startTime: '22s',
        },
    },
};

const BASE_URL = 'http://localhost:8080/api';

function signup(baseUrl, username, password, name, isAdmin = false) {
    const endpoint = isAdmin ? '/signup-admin' : '/signup';
    const signUpRequest = { username, password, name };
    return http.post(`${baseUrl}/auth${endpoint}`, JSON.stringify(signUpRequest), {
        headers: { 'Content-Type': 'application/json' },
    });
}

function login(baseUrl, username, password) {
    const credentials = { username, password };
    return http.post(`${baseUrl}/auth/login`, JSON.stringify(credentials), {
        headers: { 'Content-Type': 'application/json' },
    });
}

function runProductFlow(version) {
    const uniqueId = `${__VU}_${Date.now()}_${Math.random()}`;
    const authUrl = `${BASE_URL}/${version}`;
    const productWriteUrl = `${BASE_URL}/v1/products`;
    const productReadUrl = `${BASE_URL}/${version}/products`;

    const adminUsername = `admin_${uniqueId}_${version}`;
    const password = 'password';
    
    signup(authUrl, adminUsername, password, `Admin ${uniqueId}`, true);
    const adminLoginRes = login(authUrl, adminUsername, password);
    
    if (adminLoginRes.status !== 200) return;

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

    const createRes = http.post(`${productWriteUrl}`, JSON.stringify(productPayload), { headers: authHeaders });
    check(createRes, { 'admin create product': (r) => r.status === 201 });
    
    let productId;
    if (createRes.status === 201) {
        productId = createRes.json().productId;
    }

    sleep(1);

    const userUsername = `user_${uniqueId}_${version}`;
    signup(authUrl, userUsername, password, `User ${uniqueId}`, false);
    const userLoginRes = login(authUrl, userUsername, password);

    if (userLoginRes.status === 200) {
        const userAccessToken = userLoginRes.json('accessToken');
        const userAuthHeaders = { 'Authorization': `Bearer ${userAccessToken}`, 'Content-Type': 'application/json' };

        const keywords = ['Trousers', 'Shirt', 'Dress', 'Shoes', 'Denim', 'Jacket', 'Blue', 'Black', 'Casual', 'Formal'];
        const randomKeyword = keywords[Math.floor(Math.random() * keywords.length)];

        const searchStartTime = new Date();
        const searchRes = http.get(`${productReadUrl}/search?keyword=${randomKeyword}`, { headers: userAuthHeaders });
        const searchEndTime = new Date();

        check(searchRes, { 
            [`user search (${version}) ok`]: (r) => r.status === 200,
            'search results array': (r) => r.json() && Array.isArray(r.json())
        });

        if (version === 'v1') {
            searchTrendV1.add(searchEndTime - searchStartTime);
        } else {
            searchTrendV2.add(searchEndTime - searchStartTime);
        }

        if (version === 'v2') {
            const productGroup = encodeURIComponent('Garment Lower body');
            const params = `keyword=${randomKeyword}&department=Men&productGroup=${productGroup}`;
            
            const filterStartTime = new Date();
            const filterRes = http.get(`${productReadUrl}/search?${params}`, { headers: userAuthHeaders });
            const filterEndTime = new Date();
            
            check(filterRes, { 'v2 filtered search ok': (r) => r.status === 200 });
            searchTrendV2.add(filterEndTime - filterStartTime); 
        }
    }

    sleep(1);
    
    if (productId) {
        http.del(`${productWriteUrl}/${productId}`, null, { headers: authHeaders });
    }
}

export function testProductV1() {
    runProductFlow('v1');
}

export function testProductV2() {
    runProductFlow('v2');
}
