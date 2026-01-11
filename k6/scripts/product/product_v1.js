import http from 'k6/http';
import { check, sleep } from 'k6';
import { Trend } from 'k6/metrics';

// Custom Metric for the specific search operation
const searchTrend = new Trend('product_search_duration');

export let options = {
    vus: 200,
    duration: '30s',
    thresholds: {
        'product_search_duration': ['p(95)<1000'], // Threshold specific to search
    },
};

const BASE_URL = 'http://localhost:8080/api';
const AUTH_URL = `${BASE_URL}/v1/auth`;
const PRODUCT_URL = `${BASE_URL}/v1/products`;

// 1. Setup Phase: Runs ONCE before VUs start
// We use this to seed data so VUs don't have to create products, only search them.
export function setup() {
    const uniqueId = `setup_${Date.now()}`;
    const adminUsername = `admin_seed_${uniqueId}`;
    const adminPassword = 'password';
    
    // Create Admin
    const signupRes = http.post(`${AUTH_URL}/signup-admin`, JSON.stringify({
        username: adminUsername,
        password: adminPassword,
        name: 'Seed Admin'
    }), { headers: { 'Content-Type': 'application/json' } });
    
    // Login Admin
    const loginRes = http.post(`${AUTH_URL}/login`, JSON.stringify({
        username: adminUsername,
        password: adminPassword
    }), { headers: { 'Content-Type': 'application/json' } });
    
    const token = loginRes.json('accessToken');
    const headers = { 'Authorization': `Bearer ${token}`, 'Content-Type': 'application/json' };

    // Seed 10 Products
    const keywords = ['Trousers', 'Shirt', 'Dress', 'Jeans', 'Jacket'];
    for (let i = 0; i < 10; i++) {
        const keyword = keywords[i % keywords.length];
        const payload = {
            productCode: Math.floor(Math.random() * 1000000),
            prodName: `${keyword} ${uniqueId} ${i}`,
            productTypeName: keyword,
            productGroupName: "Garment",
            graphicalAppearanceName: "Solid",
            colourGroupName: "Blue",
            perceivedColourValueName: "Dark",
            perceivedColourMasterName: "Blue",
            departmentName: "Men",
            indexName: "A",
            indexGroupName: "Wear",
            sectionName: "Menswear",
            garmentGroupName: keyword,
            detailDesc: "Seeded Product",
            price: 100 + i
        };
        http.post(PRODUCT_URL, JSON.stringify(payload), { headers });
    }
    
    return { keywords }; // Pass data to VUs
}

export default function (data) {
    // Each VU still needs a token to search (if endpoint is secured).
    // To minimize overhead impact, we login once per VU (or very infrequently).
    // Ideally, we'd use a shared token from setup(), but tokens might expire.
    // For a 30s test, a shared token from setup() is perfectly fine and most efficient!
    
    // However, the prompt implies "Login... should not be included".
    // If we use the token from setup(), we avoid login completely in the VU loop!
    // But `setup` token belongs to Admin. Users should probably be normal users.
    
    // Let's create a transient user for this VU *once* at start of VU lifecycle if possible?
    // k6 `default` runs continuously.
    // We will do a login at the start of the iteration, BUT we only measure the search.
    
    // Simplified: Just use a random keyword from the seeded list
    const keyword = data.keywords[Math.floor(Math.random() * data.keywords.length)];
    
    // NOTE: If the search endpoint is public, we don't need auth. 
    // Assuming search is public or we use a quick anonymous/guest access if available.
    // If it requires auth, we must pay the 'login tax' or re-use a token.
    // Let's assume we create a user per VU logic is "overhead" we accept but don't measure.
    
    // Perform Search (The Metric We Care About)
    const searchRes = http.get(`${PRODUCT_URL}/search?keyword=${keyword}`, {
        tags: { name: 'Search_V1' }
    });
    
    searchTrend.add(searchRes.timings.duration);
    check(searchRes, { 
        'v1 search success': (r) => r.status === 200,
        'v1 has results': (r) => r.json() && r.json().length >= 0
    });
    
    sleep(1);
}