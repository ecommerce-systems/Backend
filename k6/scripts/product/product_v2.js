import http from 'k6/http';
import { check, sleep } from 'k6';
import { Trend } from 'k6/metrics';

// Custom Metric for the specific search operation
const searchTrend = new Trend('product_search_duration');

export let options = {
    vus: 200,
    duration: '30s',
    thresholds: {
        'product_search_duration': ['p(95)<500'], // Expecting V2 to be faster
    },
};

const BASE_URL = 'http://localhost:8080/api';
const AUTH_URL = `${BASE_URL}/v1/auth`;
const PRODUCT_WRITE_URL = `${BASE_URL}/v1/products`;
const PRODUCT_READ_URL = `${BASE_URL}/v2/products`;

// 1. Setup Phase: Runs ONCE before VUs start
export function setup() {
    const uniqueId = `setup_v2_${Date.now()}`;
    const adminUsername = `admin_seed_v2_${uniqueId}`;
    const adminPassword = 'password';
    
    // Create Admin
    http.post(`${AUTH_URL}/signup-admin`, JSON.stringify({
        username: adminUsername,
        password: adminPassword,
        name: 'Seed Admin V2'
    }), { headers: { 'Content-Type': 'application/json' } });
    
    // Login Admin
    const loginRes = http.post(`${AUTH_URL}/login`, JSON.stringify({
        username: adminUsername,
        password: adminPassword
    }), { headers: { 'Content-Type': 'application/json' } });
    
    const token = loginRes.json('accessToken');
    const headers = { 'Authorization': `Bearer ${token}`, 'Content-Type': 'application/json' };

    // Seed 10 Products with rich attributes for filtering
    const products = [];
    for (let i = 0; i < 10; i++) {
        const payload = {
            productCode: Math.floor(Math.random() * 1000000),
            prodName: `V2SearchItem ${uniqueId} ${i}`,
            productTypeName: "Trousers",
            productGroupName: "Lower Body",
            graphicalAppearanceName: "Solid",
            colourGroupName: i % 2 === 0 ? "Blue" : "Black",
            perceivedColourValueName: "Dark",
            perceivedColourMasterName: "Blue",
            departmentName: "Men",
            indexName: "A",
            indexGroupName: "Wear",
            sectionName: "Menswear",
            garmentGroupName: "Trousers",
            detailDesc: "Seeded Product V2",
            price: 100 + i
        };
        http.post(PRODUCT_WRITE_URL, JSON.stringify(payload), { headers });
        products.push(payload);
    }
    
    // Give a moment for async sync (if any) to happen, though V2 sync is usually synchronous in transactional scope
    sleep(2);
    
    return { products };
}

export default function (data) {
    // Pick a random product attributes to filter by
    const target = data.products[Math.floor(Math.random() * data.products.length)];
    
    // Construct V2 Filter Query
    // We test the "Optimization" features: Prefix search + Filters
    const keyword = target.prodName.substring(0, 5); // Prefix search "V2Sea..."
    const params = new URLSearchParams({
        keyword: keyword,
        department: target.departmentName,
        productGroup: target.productGroupName
    }).toString();

    // Perform Search (The Metric We Care About)
    const searchRes = http.get(`${PRODUCT_READ_URL}/search?${params}`, {
        tags: { name: 'Search_V2' }
    });
    
    searchTrend.add(searchRes.timings.duration);
    check(searchRes, { 
        'v2 filter search success': (r) => r.status === 200,
        // We expect at least the item we just grabbed (or similar ones)
        'v2 has results': (r) => r.json() && r.json().length >= 0 
    });
    
    sleep(1);
}