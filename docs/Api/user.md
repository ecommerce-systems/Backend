# 사용자 API 명세

## 1. 개요
사용자 프로필 정보 조회 및 관리를 위한 API.

## 2. 기본 URL
-   `/api/v1/users`

## 3. 엔드포인트

### 3.1. 내 정보 조회
-   **URL**: `/me`
-   **HTTP Method**: `GET`
-   **설명**: 현재 로그인한 사용자의 프로필 정보를 조회함. (인증 필요)
-   **헤더**: `Authorization: Bearer <accessToken>`
-   **성공 응답**: `200 OK` (`UserResponse`)
    ```json
    {
      "username": "user123",
      "name": "홍길동",
      "phone": "010-1234-5678",
      "address": "서울시 강남구"
    }
    ```

### 3.2. 내 정보 수정
-   **URL**: `/me`
-   **HTTP Method**: `PUT`
-   **설명**: 현재 로그인한 사용자의 연락처(전화번호, 주소) 정보를 수정함. (인증 필요)
-   **헤더**: `Authorization: Bearer <accessToken>`
-   **요청 본문 (`UserUpdateRequest`)**:
    ```json
    {
      "phone": "010-9876-5432",
      "address": "경기도 판교"
    }
    ```
-   **성공 응답**: `200 OK`
