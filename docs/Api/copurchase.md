# 함께 구매한 상품(연관 상품) API

## 개요
구매 이력을 기반으로 한 연관 상품 추천 서비스를 제공합니다. V1과 V2 두 가지 버전의 API가 존재하며, 이는 클라이언트-서버 통신 방식과 백엔드 데이터 조회 전략의 차이를 보여줍니다.

## V1: ID 목록 우선 조회 방식 (N+1 문제 발생)

클라이언트가 추천 상품 ID 목록을 먼저 받은 후, 각 ID에 해당하는 상품의 상세 정보를 다시 개별적으로 요청해야 하는 방식입니다.

### 1. 추천 상품 ID 목록 조회
- **URL**: `/api/v1/co-purchase/{productId}`
- **HTTP 메서드**: `GET`
- **설명**: 특정 상품과 함께 자주 구매된 추천 상품들의 ID 목록을 반환합니다.
- **헤더**:
  - `Authorization`: `Bearer <accessToken>`
- **응답**: `200 OK` (CoPurchaseResponseV1 리스트)
  ```json
  [
    { "productId": 101 },
    { "productId": 102 }
  ]
  ```

### 2. 개별 상품 상세 정보 조회
- **URL**: `/api/v1/products/{id}`
- **HTTP 메서드**: `GET`
- **설명**: V1 추천 API로 받은 각 `productId`를 사용하여 상품의 상세 정보를 개별적으로 조회합니다.
- **응답**: `200 OK` (Product 엔티티)

---

## V2: 전체 데이터 단일 조회 방식 (최적화)

한 번의 요청으로 추천 상품들의 모든 상세 정보를 받는 최적화된 방식입니다.

### 1. 추천 상품 상세 목록 조회
- **URL**: `/api/v2/co-purchase/{productId}`
- **HTTP 메서드**: `GET`
- **설명**: 특정 상품과 함께 자주 구매된 추천 상품 목록을 상세 정보까지 포함하여 한 번에 반환합니다. 백엔드에서는 비정규화된 `product_searches` 테이블을 조회하여 성능을 높였습니다.
- **헤더**:
  - `Authorization`: `Bearer <accessToken>`
- **응답**: `200 OK` (ProductSearch 엔티티 리스트)
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
