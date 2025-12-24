import http from 'k6/http';
import { check, sleep } from 'k6';

export let options = {
    vus: 10,
    duration: '10s',
};

const BASE_URL = 'http://localhost:8080/api/v1';

export default function () {
    const username = `user_info_test_${__VU}_${Date.now()}`;
    const password = 'password';
    const name = `Test User ${__VU}`;
    const phone = "111-222-3333";
    const address = "123 K6 Street";


    const signUpRequest = { username, password, name, phone, address };
    const signupRes = http.post(`${BASE_URL}/auth/signup`, JSON.stringify(signUpRequest), {
        headers: { 'Content-Type': 'application/json' },
    });
    check(signupRes, { 'signup successful': (r) => r.status === 200 || r.status === 201 });
    sleep(1);


    const credentials = { username, password };
    const loginRes = http.post(`${BASE_URL}/auth/login`, JSON.stringify(credentials), {
        headers: { 'Content-Type': 'application/json' },
    });
    check(loginRes, { 'login successful': (r) => r.status === 200 });

    if (loginRes.status !== 200) {
        console.error(`Login failed for ${username}`);
        return;
    }

    const accessToken = loginRes.json('accessToken');
    const authHeaders = { 'Authorization': `Bearer ${accessToken}`, 'Content-Type': 'application/json' };


    const getMeRes = http.get(`${BASE_URL}/users/me`, { headers: authHeaders });
    check(getMeRes, {
        'get user info successful': (r) => r.status === 200,
        'get user info response has correct data': (r) => {
            const body = r.json();
            return body.username === username && body.name === name && body.phone === phone;
        }
    });
    sleep(1);


    const updatedPhone = "444-555-6666";
    const updatedAddress = "456 K6 Avenue";
    const updateRequest = { phone: updatedPhone, address: updatedAddress };
    const putMeRes = http.put(`${BASE_URL}/users/me`, JSON.stringify(updateRequest), { headers: authHeaders });
    check(putMeRes, { 'update user info successful': (r) => r.status === 200 });
    sleep(1);


    const getMeAfterUpdateRes = http.get(`${BASE_URL}/users/me`, { headers: authHeaders });
    check(getMeAfterUpdateRes, {
        'get user info after update successful': (r) => r.status === 200,
        'get user info shows updated data': (r) => {
            const body = r.json();
            return body.phone === updatedPhone && body.address === updatedAddress;
        }
    });
    sleep(1);
}
