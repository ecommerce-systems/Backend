# 사용자 API

## 개요
사용자 프로필 정보를 관리합니다.

## 기본 URL
- `/api/v1/users`

## 엔드포인트

### 1. 내 정보 조회
- **URL**: `/me`
- **HTTP 메서드**: `GET`
- **설명**: 현재 로그인한 사용자의 프로필 정보를 조회합니다. (인증 필요)
- **헤더**:
  - `Authorization`: `Bearer <accessToken>`
- **응답**: `200 OK`
  ```json
  {
    "username": "user123",
    "name": "홍길동",
    "phone": "010-1234-5678",
    "address": "서울시 강남구..."
  }
  ```

### 2. 내 정보 수정
- **URL**: `/me`
- **HTTP 메서드**: `PUT`
- **설명**: 사용자의 연락처(전화번호, 주소) 정보를 수정합니다. (인증 필요)
- **헤더**:
  - `Authorization`: `Bearer <accessToken>`
- **요청 본문 (UserUpdateRequest)**:
  ```json
  {
    "phone": "010-9876-5432",
    "address": "경기도 판교..."
  }
  ```
- **응답**: `200 OK`