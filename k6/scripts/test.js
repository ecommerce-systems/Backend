import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
  // 1 virtual user iterating for 30 seconds
  vus: 1,
  duration: '30s',
};

const BASE_URL = 'http://localhost:8080';

export default function () {
  const loginUrl = `${BASE_URL}/api/v1/auth/login`;

  // Dummy credentials - replace with actual test user credentials if needed
  const loginPayload = JSON.stringify({
    username: 'testuser',
    password: 'password123',
  });

  const params = {
    headers: {
      'Content-Type': 'application/json',
    },
  };

  // Send POST request to login
  const loginRes = http.post(loginUrl, loginPayload, params);

  // Check if the login was successful
  check(loginRes, {
    'login returns 200 OK': (r) => r.status === 200,
    'response body contains access token': (r) => r.body.includes('accessToken'),
  });

  if (loginRes.status === 200 && loginRes.body.includes('accessToken')) {
    const { accessToken, refreshToken } = loginRes.json();

    // Test logout
    const logoutUrl = `${BASE_URL}/api/v1/auth/logout`;
    const authHeaders = {
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${accessToken}`,
      },
    };

    const logoutRes = http.post(logoutUrl, null, authHeaders);
    check(logoutRes, {
      'logout returns 200 OK': (r) => r.status === 200,
    });

    // Test refresh
    const refreshUrl = `${BASE_URL}/api/v1/auth/refresh`;
    const refreshPayload = JSON.stringify({
        refreshToken: refreshToken,
    });

    const refreshRes = http.post(refreshUrl, refreshPayload, params);
    check(refreshRes, {
        'refresh returns 200 OK': (r) => r.status === 200,
        'response body contains new access token': (r) => r.body.includes('accessToken'),
    });
  }

  sleep(1); // Wait for 1 second between iterations
}
