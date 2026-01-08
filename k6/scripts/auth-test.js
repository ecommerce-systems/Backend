import http from 'k6/http';
import { check, sleep } from 'k6';

export let options = {
    scenarios: {
        v1_db_auth: {
            executor: 'constant-vus',
            exec: 'testV1',
            vus: 200,
            duration: '10s',
        },
        v2_redis_auth: {
            executor: 'constant-vus',
            exec: 'testV2',
            vus: 200,
            duration: '10s',
            startTime: '10s',
        },
    },
};

const BASE_URL_V1 = 'http://localhost:8080/api/v1/auth';
const BASE_URL_V2 = 'http://localhost:8080/api/v2/auth';

function runAuthFlow(baseUrl) {
    const username = `auth_user_${__VU}_${Date.now()}_${Math.random()}`;
    const initialPassword = 'password123';
    const newPassword = 'newPassword456';
    const name = `Test User ${__VU}`;


    const signUpRequest = { username, password: initialPassword, name };
    const signupRes = http.post(`${baseUrl}/signup`, JSON.stringify(signUpRequest), {
        headers: { 'Content-Type': 'application/json' },
    });
    check(signupRes, { 'signup successful': (r) => r.status === 200 || r.status === 201 });
    sleep(1);


    let credentials = { username, password: initialPassword };
    const loginRes1 = http.post(`${baseUrl}/login`, JSON.stringify(credentials), {
        headers: { 'Content-Type': 'application/json' },
    });
    check(loginRes1, { 'initial login successful': (r) => r.status === 200 });

    if (loginRes1.status !== 200) {
        console.error(`Initial login failed for ${username} at ${baseUrl}: ${loginRes1.body}`);
        return;
    }
    const accessToken1 = loginRes1.json('accessToken');
    sleep(1);


    const passwordChangeRequest = { oldPassword: initialPassword, newPassword: newPassword };
    const changePwRes = http.post(`${baseUrl}/password`, JSON.stringify(passwordChangeRequest), {
        headers: {
            'Authorization': `Bearer ${accessToken1}`,
            'Content-Type': 'application/json',
        },
    });
    check(changePwRes, { 'password change successful': (r) => r.status === 200 });
    sleep(1);


    credentials = { username, password: newPassword };
    const loginRes2 = http.post(`${baseUrl}/login`, JSON.stringify(credentials), {
        headers: { 'Content-Type': 'application/json' },
    });
    check(loginRes2, { 'login with new password successful': (r) => r.status === 200 });

    if (loginRes2.status !== 200) {
        console.error(`Login with new password failed for ${username} at ${baseUrl}: ${loginRes2.body}`);
        return;
    }
    const accessToken2 = loginRes2.json('accessToken');
    sleep(1);


    const deleteRes = http.del(`${baseUrl}/me`, null, {
        headers: { 'Authorization': `Bearer ${accessToken2}` },
    });
    check(deleteRes, { 'delete account successful': (r) => r.status === 200 });
    sleep(1);


    const loginRes3 = http.post(`${baseUrl}/login`, JSON.stringify(credentials), {
        headers: { 'Content-Type': 'application/json' },
    });
    check(loginRes3, { 'login after deletion fails': (r) => r.status !== 200 });
}

export function testV1() {
    runAuthFlow(BASE_URL_V1);
}

export function testV2() {
    runAuthFlow(BASE_URL_V2);
}
