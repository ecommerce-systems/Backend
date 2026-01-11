# K6 Performance Tests

This directory contains K6 performance test scripts organized by domain and scenario.

## Directory Structure

*   **`scripts/auth/`**: Authentication specific benchmarks.
    *   `auth_v1.js`: Tests the V1 (Database) authentication flow.
    *   `auth_v2.js`: Tests the V2 (Redis) authentication flow.
*   **`scripts/product/`**: Product domain benchmarks.
    *   `product_v1.js`: Tests V1 Product Search (Legacy, JOIN-based).
    *   `product_v2.js`: Tests V2 Product Search (Optimized, No-JOIN).
*   **`scripts/scenarios/`**: Integrated user journey scenarios.
    *   `integrated_scenario.js`: A full "Signup -> Login -> User Info -> Search" flow.

## Running Tests

Prerequisite: Ensure the backend server is running on `localhost:8080`.

### Run Comparison Tests

**Authentication:**
```bash
k6 run scripts/auth/auth_v1.js
k6 run scripts/auth/auth_v2.js
```

**Product Search:**
```bash
k6 run scripts/product/product_v1.js
k6 run scripts/product/product_v2.js
```

### Run Integrated Scenario

```bash
k6 run scripts/scenarios/integrated_scenario.js
```

## Metrics

Scripts are configured with **Custom Metrics** to isolate the performance of the specific function under test, excluding setup overhead (like creating admin users).

*   Look for `product_search_duration` in the output for search performance.
*   Look for `auth_login_duration` etc. in the output for auth performance.
