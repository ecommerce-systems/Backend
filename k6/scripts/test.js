import http from 'k6/http';
import { check, sleep } from 'k6';

export let options = {
    vus: 300,
    duration: '10s',
};

const BASE_URL = 'http://localhost:8080/api/v1/auth';

export default function () {
    const username = `testuser${__VU}_${Date.now()}`;
    const password = 'password';
    const name = `Test User ${__VU}`;


    const signUpRequest = {
        username: username,
        password: password,
        name: name,
    };

    const signupRes = http.post(`${BASE_URL}/signup`, JSON.stringify(signUpRequest), {
        headers: { 'Content-Type': 'application/json' },
    });

    check(signupRes, { 'signup successful': (r) => r.status === 200 || r.status === 201 });

    if (signupRes.status !== 200 && signupRes.status !== 201) {
        console.error(`❌ Signup failed for ${username}: ${signupRes.status} ${signupRes.body}`);
        return;
    }
    console.log(`✅ Signup successful for ${username}`);
    sleep(1);


    const credentials = { username: username, password: password };
    const loginRes = http.post(`${BASE_URL}/login`, JSON.stringify(credentials), {
        headers: { 'Content-Type': 'application/json' },
    });

    check(loginRes, { 'login successful': (r) => r.status === 200 });

    if (loginRes.status !== 200) {
        console.error(`❌ Login failed for ${username}: ${loginRes.status} ${loginRes.body}`);
        return;
    }


    let accessToken = loginRes.json('accessToken');
    let refreshToken = loginRes.json('refreshToken');
    console.log(`✅ Login successful for ${username}`);
    sleep(1);


    const jar = http.cookieJar();
    jar.set('http://localhost:8080', 'refreshToken', refreshToken);


    const refreshRes = http.post(`${BASE_URL}/refresh`, null, {
        headers: { 'Content-Type': 'application/json' },
        cookies: jar.cookiesForURL('http://localhost:8080'),
    });

    check(refreshRes, { 'token refresh successful': (r) => r.status === 200 });

    if (refreshRes.status === 200) {
        accessToken = refreshRes.json('accessToken');
        console.log(`🔄 Access token refreshed for ${username}`);
    } else {
        console.error(`❌ Failed to refresh token for ${username}: ${refreshRes.status} ${refreshRes.body}`);
    }
    sleep(1);


    const logoutRes = http.post(`${BASE_URL}/logout`, null, {
        headers: {
            'Authorization': `Bearer ${accessToken}`,
            'Content-Type': 'application/json',
        },
        cookies: jar.cookiesForURL('http://localhost:8080'),
    });

    check(logoutRes, { 'logout successful': (r) => r.status === 200 });

    if (logoutRes.status === 200) {
        console.log(`👋 Logout successful for ${username}`);
    } else {
        console.error(`❌ Logout failed for ${username}: ${logoutRes.status} ${logoutRes.body}`);
    }
    sleep(1);
}