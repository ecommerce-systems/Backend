import http from 'k6/http';
import { check, sleep, group } from 'k6';
import { Trend } from 'k6/metrics';

// Custom Metrics for high-level user journey steps
const stepSignup = new Trend('journey_signup_duration');
const stepLogin = new Trend('journey_login_duration');
const stepSearch = new Trend('journey_search_duration');
const stepViewProduct = new Trend('journey_view_product_duration');
const stepUserInfo = new Trend('journey_user_info_duration');

export let options = {
    vus: 50, // Lower VUs for a complex scenario
    duration: '1m',
    thresholds: {
        'http_req_duration': ['p(95)<2000'], // Global threshold
        'journey_search_duration': ['p(95)<1000'],
    },
};

const BASE_URL = 'http://localhost:8080/api';

export default function () {
    const uniqueId = `${__VU}_${Date.now()}`;
    const username = `journey_user_${uniqueId}`;
    const password = 'password123';
    
    // 1. Signup
    group('User Signup', function () {
        const payload = JSON.stringify({
            username: username,
            password: password,
            name: 'Journey User'
        });
        const res = http.post(`${BASE_URL}/v2/auth/signup`, payload, {
            headers: { 'Content-Type': 'application/json' },
            tags: { name: 'Signup' }
        });
        stepSignup.add(res.timings.duration);
        check(res, { 'signup status is 200': (r) => r.status === 200 });
    });
    
    sleep(1);

    // 2. Login
    let token;
    group('User Login', function () {
        const payload = JSON.stringify({ username, password });
        const res = http.post(`${BASE_URL}/v2/auth/login`, payload, {
            headers: { 'Content-Type': 'application/json' },
            tags: { name: 'Login' }
        });
        stepLogin.add(res.timings.duration);
        check(res, { 'login status is 200': (r) => r.status === 200 });
        if (res.status === 200) {
            token = res.json('accessToken');
        }
    });

    if (!token) return;
    const authHeaders = { 'Authorization': `Bearer ${token}`, 'Content-Type': 'application/json' };
    sleep(1);

    // 3. Get User Info
    group('View User Info', function () {
        const res = http.get(`${BASE_URL}/v1/users/me`, {
            headers: authHeaders,
            tags: { name: 'GetUserInfo' }
        });
        stepUserInfo.add(res.timings.duration);
        check(res, { 'user info status is 200': (r) => r.status === 200 });
    });
    sleep(1);

    // 4. Search Products (V2)
    let productId;
    group('Search Products', function () {
        const res = http.get(`${BASE_URL}/v2/products/search?keyword=Trousers`, {
            headers: authHeaders,
            tags: { name: 'Search' }
        });
        stepSearch.add(res.timings.duration);
        check(res, { 
            'search status is 200': (r) => r.status === 200 
        });
        
        // Try to pick a product to view
        // Note: Response might be just strings (names) depending on V2 implementation
        // If V2 search returns Strings, we might need a separate call to get details if we don't have ID.
        // Let's assume for this scenario we just search.
    });
    sleep(1);

    // 5. View Product Detail (V1 - assuming we had an ID, but for now we'll just hit a known endpoint if possible or skip)
    // Since we don't have a guaranteed ID from the search strings, we will skip detailed view in this generic script
    // or simulate it by guessing an ID if the system has data.
}
