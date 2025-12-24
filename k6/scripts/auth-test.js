import http from 'k6/http';
import { check, sleep } from 'k6';

export let options = {
    vus: 10,
    duration: '10s',
};

const BASE_URL = 'http://localhost:8080/api/v1/auth';

export default function () {
    const username = `auth_flow_user_${__VU}_${Date.now()}`;
    const initialPassword = 'password123';
    const newPassword = 'newPassword456';
    const name = `Test User ${__VU}`;


    const signUpRequest = { username, password: initialPassword, name };
    const signupRes = http.post(`${BASE_URL}/signup`, JSON.stringify(signUpRequest), {
        headers: { 'Content-Type': 'application/json' },
    });
    check(signupRes, { 'signup successful': (r) => r.status === 200 || r.status === 201 });
    sleep(1);


    let credentials = { username, password: initialPassword };
    const loginRes1 = http.post(`${BASE_URL}/login`, JSON.stringify(credentials), {
        headers: { 'Content-Type': 'application/json' },
    });
    check(loginRes1, { 'initial login successful': (r) => r.status === 200 });

    if (loginRes1.status !== 200) {
        console.error(`Initial login failed for ${username}: ${loginRes1.body}`);
        return;
    }
    const accessToken1 = loginRes1.json('accessToken');
    sleep(1);


    const passwordChangeRequest = { oldPassword: initialPassword, newPassword: newPassword };
    const changePwRes = http.post(`${BASE_URL}/password`, JSON.stringify(passwordChangeRequest), {
        headers: {
            'Authorization': `Bearer ${accessToken1}`,
            'Content-Type': 'application/json',
        },
    });
    check(changePwRes, { 'password change successful': (r) => r.status === 200 });
    sleep(1);


    credentials = { username, password: newPassword };
    const loginRes2 = http.post(`${BASE_URL}/login`, JSON.stringify(credentials), {
        headers: { 'Content-Type': 'application/json' },
    });
    check(loginRes2, { 'login with new password successful': (r) => r.status === 200 });

    if (loginRes2.status !== 200) {
        console.error(`Login with new password failed for ${username}: ${loginRes2.body}`);
        return;
    }
    const accessToken2 = loginRes2.json('accessToken');
    sleep(1);


    const deleteRes = http.del(`${BASE_URL}/me`, null, {
        headers: { 'Authorization': `Bearer ${accessToken2}` },
    });
    check(deleteRes, { 'delete account successful': (r) => r.status === 200 });
    sleep(1);


    const loginRes3 = http.post(`${BASE_URL}/login`, JSON.stringify(credentials), {
        headers: { 'Content-Type': 'application/json' },
    });
    check(loginRes3, { 'login after deletion fails': (r) => r.status !== 200 });
}