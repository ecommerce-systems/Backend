import http from 'k6/http';
import { check, sleep } from 'k6';
import { Trend } from 'k6/metrics';

const searchResultsTrendV1 = new Trend('search_results_duration_v1');
const searchResultsTrendV2 = new Trend('search_results_duration_v2');

export let options = {
    scenarios: {
        v1_search_results: {
            executor: 'constant-vus',
            exec: 'testSearchResultsV1',
            vus: 50,
            duration: '20s',
        },
        v2_search_results: {
            executor: 'constant-vus',
            exec: 'testSearchResultsV2',
            vus: 50,
            duration: '20s',
            startTime: '22s',
        },
    },
};

const BASE_URL = 'http://localhost:8080/api';

function runSearchResultsFlow(version) {
    const uniqueId = `${__VU}_${Date.now()}_${Math.random()}`;
    const authUrl = `${BASE_URL}/${version}/auth`;
    const productReadUrl = `${BASE_URL}/${version}/products`;

    const username = `user_${uniqueId}_${version}`;
    const password = 'password';

    // 1. Sign up
    const signupRes = http.post(`${authUrl}/signup`, JSON.stringify({ username, password, name: `User ${uniqueId}` }), {
        headers: { 'Content-Type': 'application/json' },
    });
    check(signupRes, { [`signup (${version}) ok`]: (r) => r.status === 200 });

    // 2. Login
    const loginRes = http.post(`${authUrl}/login`, JSON.stringify({ username, password }), {
        headers: { 'Content-Type': 'application/json' },
    });
    check(loginRes, { [`login (${version}) ok`]: (r) => r.status === 200 });
    
    if (loginRes.status !== 200) {
        return; // Cannot proceed without login
    }
    
    const accessToken = loginRes.json('accessToken');
    const authHeaders = { 'Authorization': `Bearer ${accessToken}` };
    
    // 3. Search
    const keywords = ['Trousers', 'Shirt', 'Dress', 'Shoes', 'Denim', 'Jacket', 'Blue', 'Black', 'Casual', 'Formal'];
    const randomKeyword = keywords[Math.floor(Math.random() * keywords.length)];
    const searchParams = `keyword=${randomKeyword}&page=0&size=10`;

    const totalSearchStartTime = new Date(); // Start time for the entire V1 process

    if (version === 'v1') {
        // V1: Get IDs first, then make separate requests for each ID
        const searchResultsRes = http.get(`${productReadUrl}/search/results?${searchParams}`, { headers: authHeaders });
        check(searchResultsRes, { 
            'v1 search results status 200': (r) => r.status === 200,
            'v1 search results has content': (r) => r.json('content') !== undefined && Array.isArray(r.json('content'))
        });

        if (searchResultsRes.status === 200 && searchResultsRes.json('content')) {
            const productIds = searchResultsRes.json('content').map(p => p.productId);
            
            for (const productId of productIds) {
                const productDetailRes = http.get(`${productReadUrl}/${productId}`, { headers: authHeaders });
                check(productDetailRes, { [`v1 get product detail ${productId} status 200`]: (r) => r.status === 200 });
            }
        }
        const totalSearchEndTime = new Date();
        searchResultsTrendV1.add(totalSearchEndTime - totalSearchStartTime);

    } else { // version === 'v2'
        // V2: Get all data in a single request
        const searchResultsRes = http.get(`${productReadUrl}/search/results?${searchParams}`, { headers: authHeaders });
        const totalSearchEndTime = new Date(); // End time for V2 single request
        
        check(searchResultsRes, { 
            'v2 search results status 200': (r) => r.status === 200,
            'v2 search results has content': (r) => r.json('content') !== undefined && Array.isArray(r.json('content'))
        });

        if (searchResultsRes.status === 200) {
            searchResultsTrendV2.add(totalSearchEndTime - totalSearchStartTime);
        }
    }

    sleep(1);
}

export function testSearchResultsV1() {
    runSearchResultsFlow('v1');
}

export function testSearchResultsV2() {
    runSearchResultsFlow('v2');
}