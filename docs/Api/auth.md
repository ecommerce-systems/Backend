# 인증 API 명세

## 1. 개요
회원가입, 로그인, 로그아웃, 토큰 갱신 등 인증 서비스를 제공함.
본 문서는 인증 API의 V1과 V2 버전을 모두 기술하며, 각 버전의 차이점은 아래와 같음.

-   **V1**: H2 데이터베이스를 사용하여 Refresh Token을 관리.
-   **V2**: In-Memory 저장소인 Redis를 사용하여 Refresh Token을 관리.

## 2. 기본 URL
-   **V1**: `/api/v1/auth`
-   **V2**: `/api/v2/auth`

## 3. 엔드포인트

### 3.1. 일반 사용자 회원가입
-   **URL**: `/signup`
-   **HTTP Method**: `POST`
-   **설명**: 새로운 일반 사용자 계정을 등록함.
-   **요청 본문 (`SignUpRequest`)**:
    ```json
    {
      "username": "user123",
      "password": "password",
      "name": "홍길동",
      "phone": "010-1234-5678",
      "address": "서울시 강남구"
    }
    ```
-   **성공 응답**: `200 OK`

### 3.2. 관리자 회원가입
-   **URL**: `/signup-admin`
-   **HTTP Method**: `POST`
-   **설명**: `ADMIN` 권한을 가진 새로운 관리자 계정을 등록함.
-   **요청 본문 (`SignUpRequest`)**: (일반 회원가입과 동일)
-   **성공 응답**: `200 OK`

### 3.3. 로그인
-   **URL**: `/login`
-   **HTTP Method**: `POST`
-   **설명**: 사용자 인증을 수행하고 `accessToken`과 `refreshToken`을 반환. `refreshToken`은 HTTP-only 속성의 쿠키로 설정됨.
-   **요청 본문 (`LoginRequest`)**:
    ```json
    {
      "username": "user123",
      "password": "password"
    }
    ```
-   **성공 응답**: `200 OK`
    ```json
    {
      "accessToken": "jwt_access_token",
      "refreshToken": "jwt_refresh_token"
    }
    ```

### 3.4. 로그아웃
-   **URL**: `/logout`
-   **HTTP Method**: `POST`
-   **설명**: 사용자를 로그아웃 처리하고 서버에 저장된 `refreshToken`을 무효화함. (인증 필요)
-   **헤더**: `Authorization: Bearer <accessToken>`
-   **성공 응답**: `200 OK`

### 3.5. 토큰 갱신
-   **URL**: `/refresh`
-   **HTTP Method**: `POST`
-   **설명**: 쿠키에 저장된 `refreshToken`을 사용하여 새로운 `accessToken`을 발급받음.
-   **쿠키**: `refreshToken`
-   **성공 응답**: `200 OK`
    ```json
    {
      "accessToken": "new_jwt_access_token",
      "refreshToken": "new_jwt_refresh_token"
    }
    ```

### 3.6. 비밀번호 변경
-   **URL**: `/password`
-   **HTTP Method**: `POST`
-   **설명**: 현재 로그인한 사용자의 비밀번호를 변경함. (인증 필요)
-   **헤더**: `Authorization: Bearer <accessToken>`
-   **요청 본문 (`PasswordChangeRequest`)**:
    ```json
    {
      "oldPassword": "현재_비밀번호",
      "newPassword": "새_비밀번호"
    }
    ```
-   **성공 응답**: `200 OK`

### 3.7. 회원 탈퇴
-   **URL**: `/me`
-   **HTTP Method**: `DELETE`
-   **설명**: 현재 로그인한 사용자의 계정을 삭제함. (인증 필요)
-   **헤더**: `Authorization: Bearer <accessToken>`
-   **성공 응답**: `200 OK`
