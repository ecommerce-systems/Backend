import http from 'k6/http';
import { check, sleep } from 'k6';
import { Trend } from 'k6/metrics';

// Custom Metrics to isolate specific operations
const signupTrend = new Trend('auth_signup_duration');
const loginTrend = new Trend('auth_login_duration');
const refreshTrend = new Trend('auth_refresh_duration');
const logoutTrend = new Trend('auth_logout_duration');

export let options = {
    vus: 200,
    duration: '10s',
    thresholds: {
        // Define thresholds on the custom metrics, not just global http_req_duration
        'auth_login_duration': ['p(95)<500'], 
    },
};

const BASE_URL = 'http://localhost:8080/api/v1/auth';

export default function () {
    const username = `auth_v1_${__VU}_${Date.now()}_${Math.random()}`;
    const password = 'password123';
    const name = `Test User V1 ${__VU}`;

    // 1. Signup
    const signUpPayload = JSON.stringify({ username, password, name });
    const signupRes = http.post(`${BASE_URL}/signup`, signUpPayload, {
        headers: { 'Content-Type': 'application/json' },
        tags: { name: 'Signup' } // Tag for filtering
    });
    signupTrend.add(signupRes.timings.duration); // Record specific duration
    check(signupRes, { 'signup successful': (r) => r.status === 200 || r.status === 201 });
    sleep(1);

    // 2. Login
    const loginPayload = JSON.stringify({ username, password });
    const loginRes = http.post(`${BASE_URL}/login`, loginPayload, {
        headers: { 'Content-Type': 'application/json' },
        tags: { name: 'Login' }
    });
    loginTrend.add(loginRes.timings.duration);
    check(loginRes, { 'login successful': (r) => r.status === 200 });

    if (loginRes.status !== 200) return;
    const accessToken = loginRes.json('accessToken');
    sleep(1);

    // 3. Refresh Token (Assuming cookie handling is automatic or we simulating it)
    // k6 handles cookies automatically in the jar.
    const refreshRes = http.post(`${BASE_URL}/refresh`, null, {
        tags: { name: 'RefreshToken' }
    });
    refreshTrend.add(refreshRes.timings.duration);
    check(refreshRes, { 'refresh successful': (r) => r.status === 200 });
    sleep(1);

    // 4. Logout
    const logoutRes = http.post(`${BASE_URL}/logout`, null, {
        headers: { 'Authorization': `Bearer ${accessToken}` },
        tags: { name: 'Logout' }
    });
    logoutTrend.add(logoutRes.timings.duration);
    check(logoutRes, { 'logout successful': (r) => r.status === 200 });
}