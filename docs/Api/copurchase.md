# 함께 구매한 상품(연관 상품) API

## 개요
구매 이력을 기반으로 한 연관 상품 추천 서비스를 제공합니다.

## 기본 URL
- `/api/v1/co-purchase`

## 엔드포인트

### 1. 추천 상품 목록 조회
- **URL**: `/{productId}`
- **HTTP 메서드**: `GET`
- **설명**: 특정 상품과 함께 자주 구매된 추천 상품 목록을 조회합니다. (인증 필요)
- **헤더**:
  - `Authorization`: `Bearer <accessToken>`
- **경로 변수**:
  - `productId`: 기준 상품의 ID.
- **응답**: `200 OK`
  ```json
  [
    {
      "productId": 2,
      "prodName": "추천 상품 A",
      "price": 50000.00
    }
  ]
  ```