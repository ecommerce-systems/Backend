import http from 'k6/http';
import { check, sleep, group } from 'k6';
import { Trend } from 'k6/metrics';

const refreshTrendV1 = new Trend('refresh_duration_v1');
const refreshTrendV2 = new Trend('refresh_duration_v2');

export let options = {
    scenarios: {
        v1_refresh: {
            executor: 'constant-vus',
            exec: 'testV1',
            vus: 100,
            duration: '10s',
        },
        v2_refresh: {
            executor: 'constant-vus',
            exec: 'testV2',
            vus: 100,
            duration: '10s',
            startTime: '12s',
        },
    },
};

const BASE_URL_V1 = 'http://localhost:8080/api/v1/auth';
const BASE_URL_V2 = 'http://localhost:8080/api/v2/auth';

function runRefreshFlow(baseUrl, version) {
    const username = `refresh_user_${__VU}_${Date.now()}_${Math.random()}`;
    const password = 'password123';
    
    http.post(`${baseUrl}/signup`, JSON.stringify({ username, password, name: "Test User" }), {
        headers: { 'Content-Type': 'application/json' },
    });

    const loginRes = http.post(`${baseUrl}/login`, JSON.stringify({ username, password }), {
        headers: { 'Content-Type': 'application/json' },
    });

    if (loginRes.status !== 200) return;
    
    const refreshToken = loginRes.json('refreshToken');
    
    sleep(1);

    group('refresh stage', () => {
        const params = {
            headers: { 
                'Content-Type': 'application/json',
                'Cookie': `refreshToken=${refreshToken}` 
            },
        };

        const startTime = new Date();
        const refreshRes = http.post(`${baseUrl}/refresh`, null, params);
        const endTime = new Date();

        const checkRes = check(refreshRes, { 'refresh ok': (r) => r.status === 200 });
        
        if (!checkRes) {
            console.error(`Refresh Failed (${version}): Status ${refreshRes.status}, Body: ${refreshRes.body}`);
        }
        
        if (version === 'v1') {
            refreshTrendV1.add(endTime - startTime);
        } else {
            refreshTrendV2.add(endTime - startTime);
        }
    });
    sleep(1);
}

export function testV1() {
    runRefreshFlow(BASE_URL_V1, 'v1');
}

export function testV2() {
    runRefreshFlow(BASE_URL_V2, 'v2');
}
