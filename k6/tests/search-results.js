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
    const params = `keyword=${randomKeyword}&page=0&size=10`;

    const searchStartTime = new Date();
    const searchRes = http.get(`${productReadUrl}/search/results?${params}`, { headers: authHeaders });
    const searchEndTime = new Date();

    const searchCheck = {};
    searchCheck[`search results (${version}) status 200`] = (r) => r.status === 200;
    searchCheck[`search results (${version}) has content`] = (r) => r.json('content') !== undefined && Array.isArray(r.json('content'));
    check(searchRes, searchCheck);

    // 4. Add trend data
    if (searchRes.status === 200) {
        if (version === 'v1') {
            searchResultsTrendV1.add(searchEndTime - searchStartTime);
        } else {
            searchResultsTrendV2.add(searchEndTime - searchStartTime);
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