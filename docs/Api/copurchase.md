# 함께 구매한 상품 API 명세

## 1. 개요
특정 상품을 조회하는 고객에게 구매 이력을 기반으로 연관 상품을 추천하는 API.
클라이언트-서버 통신 방식과 백엔드 데이터 조회 전략에 따라 V1과 V2 두 가지 버전으로 나뉨.

---

## 2. V1: ID 목록 우선 조회 방식 (N+1 문제 발생)

클라이언트가 추천 상품 ID 목록을 먼저 받은 후(`1`회 요청), 각 ID에 해당하는 상품의 상세 정보를 다시 개별적으로 요청해야 하는 방식(`N`회 요청).

### 2.1. 추천 상품 ID 목록 조회
-   **URL**: `/api/v1/co-purchase/{productId}`
-   **HTTP Method**: `GET`
-   **설명**: 특정 상품과 함께 자주 구매된 추천 상품들의 ID 목록을 반환함. (인증 필요)
-   **헤더**: `Authorization: Bearer <accessToken>`
-   **성공 응답**: `200 OK` (`CoPurchaseResponseV1` 리스트)
    ```json
    [
      { "productId": 101 },
      { "productId": 102 }
    ]
    ```

### 2.2. 개별 상품 상세 정보 조회
-   **URL**: `/api/v1/products/{id}`
-   **HTTP Method**: `GET`
-   **설명**: `2.1`에서 받은 각 `productId`를 사용하여 상품의 상세 정보를 개별적으로 조회함.
-   **성공 응답**: `200 OK` (`Product` 엔티티)

---

## 3. V2: 전체 데이터 단일 조회 방식 (최적화)

한 번의 요청으로 추천 상품들의 모든 상세 정보를 받는 최적화된 방식.

### 3.1. 추천 상품 상세 목록 조회
-   **URL**: `/api/v2/co-purchase/{productId}`
-   **HTTP Method**: `GET`
-   **설명**: 특정 상품과 함께 자주 구매된 추천 상품 목록을 상세 정보까지 포함하여 한 번에 반환함. 백엔드에서는 비정규화된 `product_searches` 테이블을 조회하여 성능을 확보함. (인증 필요)
-   **헤더**: `Authorization: Bearer <accessToken>`
-   **성공 응답**: `200 OK` (`ProductSearch` 엔티티 리스트)
    ```json
    [
      {
        "productId": 101,
        "prodName": "추천 상품 A",
        "price": 50000.00,
        "imageUrl": "...",
        "productTypeName": "상의"
      },
      {
        "productId": 102,
        "prodName": "추천 상품 B",
        "price": 75000.00,
        "imageUrl": "...",
        "productTypeName": "하의"
      }
    ]
    ```