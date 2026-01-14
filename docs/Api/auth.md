# 인증 API

## 개요
회원가입, 로그인, 로그아웃, 토큰 갱신 등 인증 서비스를 제공합니다.
두 가지 버전을 지원합니다:
- **V1**: DB를 사용한 리프레시 토큰 관리.
- **V2**: Redis를 사용한 리프레시 토큰 관리.

## 기본 URL
- V1: `/api/v1/auth`
- V2: `/api/v2/auth`

## 엔드포인트

### 1. 일반 사용자 회원가입
- **URL**: `/signup`
- **HTTP 메서드**: `POST`
- **설명**: 새로운 일반 사용자를 등록합니다.
- **요청 본문 (SignUpRequest)**:
  ```json
  {
    "username": "user123",
    "password": "password",
    "name": "홍길동",
    "phone": "010-1234-5678",
    "address": "서울시 강남구..."
  }
  ```
- **응답**: `200 OK`

### 2. 관리자 회원가입
- **URL**: `/signup-admin`
- **HTTP 메서드**: `POST`
- **설명**: 새로운 관리자 계정을 등록합니다.
- **요청 본문 (SignUpRequest)**: (일반 회원가입과 동일)
- **응답**: `200 OK`

### 3. 로그인
- **URL**: `/login`
- **HTTP 메서드**: `POST`
- **설명**: 사용자 인증을 수행하고 토큰을 반환합니다. `refreshToken`은 HTTP-only 쿠키로 설정됩니다.
- **요청 본문 (LoginRequest)**:
  ```json
  {
    "username": "user123",
    "password": "password"
  }
  ```
- **응답**: `200 OK`
  ```json
  {
    "accessToken": "jwt_access_token",
    "refreshToken": "jwt_refresh_token"
  }
  ```

### 4. 로그아웃
- **URL**: `/logout`
- **HTTP 메서드**: `POST`
- **설명**: 사용자를 로그아웃 처리하고 리프레시 토큰을 무효화합니다.
- **헤더**:
  - `Authorization`: `Bearer <accessToken>`
- **응답**: `200 OK`

### 5. 토큰 갱신
- **URL**: `/refresh`
- **HTTP 메서드**: `POST`
- **설명**: 쿠키에 저장된 리프레시 토큰을 사용하여 새로운 액세스 토큰을 발급합니다.
- **쿠키**: `refreshToken`
- **응답**: `200 OK`
  ```json
  {
    "accessToken": "new_jwt_access_token",
    "refreshToken": "new_jwt_refresh_token"
  }
  ```

### 6. 비밀번호 변경
- **URL**: `/password`
- **HTTP 메서드**: `POST`
- **설명**: 현재 로그인한 사용자의 비밀번호를 변경합니다. (인증 필요)
- **헤더**:
  - `Authorization`: `Bearer <accessToken>`
- **요청 본문 (PasswordChangeRequest)**:
  ```json
  {
    "oldPassword": "현재_비밀번호",
    "newPassword": "새_비밀번호"
  }
  ```
- **응답**: `200 OK`

### 7. 회원 탈퇴
- **URL**: `/me`
- **HTTP 메서드**: `DELETE`
- **설명**: 현재 로그인한 사용자의 계정을 삭제합니다. (인증 필요)
- **헤더**:
  - `Authorization`: `Bearer <accessToken>`
- **응답**: `200 OK`