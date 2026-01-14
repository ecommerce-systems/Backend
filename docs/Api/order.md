# 주문 API

## 개요
주문 생성 및 주문 내역 조회를 관리합니다.

## 기본 URL
- `/api/v1/orders`

## 엔드포인트

### 1. 주문 생성
- **URL**: `/`
- **HTTP 메서드**: `POST`
- **설명**: 현재 로그인한 사용자의 주문을 생성합니다. (인증 필요)
- **헤더**:
  - `Authorization`: `Bearer <accessToken>`
- **요청 본문 (OrderRequest)**:
  ```json
  {
    "items": [
      {
        "productId": 1,
        "quantity": 2
      },
      {
        "productId": 5,
        "quantity": 1
      }
    ]
  }
  ```
- **응답**: `200 OK`
  ```json
  {
    "orderId": 101,
    "orderDate": "2023-10-27T10:00:00",
    "status": "PENDING",
    "orderDetails": [
      {
        "productId": 1,
        "productName": "상품 A",
        "quantity": 2,
        "price": 20000.00
      },
       {
        "productId": 5,
        "productName": "상품 B",
        "quantity": 1,
        "price": 5000.00
      }
    ]
  }
  ```

### 2. 내 주문 내역 조회
- **URL**: `/`
- **HTTP 메서드**: `GET`
- **설명**: 현재 로그인한 사용자의 모든 주문 내역을 조회합니다. (인증 필요)
- **헤더**:
  - `Authorization`: `Bearer <accessToken>`
- **응답**: `200 OK`
  ```json
  [
    {
      "orderId": 101,
      "orderDate": "2023-10-27T10:00:00",
      "status": "PENDING",
      "orderDetails": [...]
    }
  ]
  ```

### 3. 주문 상세 조회
- **URL**: `/{orderId}`
- **HTTP 메서드**: `GET`
- **설명**: 특정 주문의 상세 정보를 조회합니다. 본인의 주문만 조회 가능합니다. (인증 필요)
- **헤더**:
  - `Authorization`: `Bearer <accessToken>`
- **경로 변수**:
  - `orderId`: 주문 ID.
- **응답**: `200 OK` (OrderResponse 객체)