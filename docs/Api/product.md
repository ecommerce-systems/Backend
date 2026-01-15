# 상품 API

## 개요
상품 조회, 검색 및 관리 기능을 제공합니다.
두 가지 버전을 지원합니다:
- **V1**: 표준 엔드포인트 (RDB 조인 기반 조회).
- **V2**: 최적화된 검색 엔드포인트 (비정규화된 테이블 기반 고속 조회).

## 기본 URL
- V1: `/api/v1/products`
- V2: `/api/v2/products`
- 카테고리: `/api/v1/products/categories`

---

## 상품 엔드포인트 (V1)

### 1. 전체 상품 조회
- **URL**: `/`
- **HTTP 메서드**: `GET`
- **설명**: 등록된 모든 상품 목록을 조회합니다.
- **응답**: `200 OK` (Product 엔티티 리스트)

### 2. 상품 상세 조회
- **URL**: `/{id}`
- **HTTP 메서드**: `GET`
- **설명**: 상품 ID를 통해 특정 상품의 상세 정보를 조회합니다.
- **응답**: `200 OK` 또는 `404 Not Found`

### 3. 상품명 검색 (V1)
- **URL**: `/search`
- **HTTP 메서드**: `GET`
- **쿼리 파라미터**:
  - `keyword`: 검색어.
- **응답**: `200 OK`
  ```json
  ["상품 A", "상품 B"]
  ```

### 4. 상품 결과 페이징 검색 (V1)
- **URL**: `/search/results`
- **HTTP 메서드**: `GET`
- **설명**: 키워드로 상품을 검색하고, 결과를 페이징하여 반환합니다.
- **쿼리 파라미터**:
  - `keyword`: 검색어 (필수).
  - `page`: 페이지 번호 (0부터 시작, 기본값: 0).
  - `size`: 페이지당 아이템 수 (기본값: 10).
  - `sort`: 정렬 기준 필드 (기본값: productId).
- **응답**: `200 OK` (Spring의 `Page<Product>` 객체)

### 5. 상품 등록 (관리자)
- **URL**: `/`
- **HTTP 메서드**: `POST`
- **설명**: 새로운 상품을 등록합니다. `ADMIN` 권한이 필요합니다.
- **헤더**:
  - `Authorization`: `Bearer <accessToken>`
- **요청 본문 (ProductCreateRequestDto)**:
  ```json
  {
    "productCode": 12345,
    "prodName": "새로운 상품",
    "detailDesc": "상품 설명...",
    "price": 99000,
    "productTypeName": "바지",
    "departmentName": "남성",
    "productGroupName": "하의",
    ...
  }
  ```
- **응답**: `201 Created`

### 6. 상품 수정 (관리자)
- **URL**: `/{id}`
- **HTTP 메서드**: `PUT`
- **설명**: 기존 상품 정보를 수정합니다. `ADMIN` 권한이 필요합니다.
- **헤더**:
  - `Authorization`: `Bearer <accessToken>`
- **요청 본문 (ProductUpdateRequestDto)**:
- **응답**: `200 OK`

### 7. 상품 삭제 (관리자)
- **URL**: `/{id}`
- **HTTP 메서드**: `DELETE`
- **설명**: 상품을 삭제합니다. `ADMIN` 권한이 필요합니다.
- **헤더**:
  - `Authorization`: `Bearer <accessToken>`
- **응답**: `204 No Content`

---

## 상품 엔드포인트 (V2)

### 1. 상품 상세 조회 (V2)
- **URL**: `/{id}`
- **HTTP 메서드**: `GET`
- **설명**: 최적화된 검색 인덱스(비정규화 테이블)에서 상품 정보를 조회합니다. (조인 없음)
- **응답**: `200 OK` (ProductSearch 엔티티)

### 2. 필터 기반 상품명 검색
- **URL**: `/search`
- **HTTP 메서드**: `GET`
- **설명**: 다양한 카테고리 필터를 지원하는 고급 검색입니다.
- **쿼리 파라미터**:
  - `keyword`: 검색어 (필수).
  - `productType` (선택)
  - `department` (선택)
  - `productGroup` (선택)
  - `section` (선택)
- **응답**: `200 OK`
  ```json
  ["필터링된 상품 A", "필터링된 상품 B"]
  ```

### 3. 상품 결과 페이징 검색 (V2)
- **URL**: `/search/results`
- **HTTP 메서드**: `GET`
- **설명**: 키워드로 상품을 검색하고, 결과를 페이징하여 반환합니다. (비정규화 테이블 기반)
- **쿼리 파라미터**:
  - `keyword`: 검색어 (필수).
  - `page`: 페이지 번호 (0부터 시작, 기본값: 0).
  - `size`: 페이지당 아이템 수 (기본값: 10).
  - `sort`: 정렬 기준 필드 (기본값: productId).
- **응답**: `200 OK` (Spring의 `Page<ProductSearch>` 객체)


---

## 카테고리 엔드포인트
**기본 URL**: `/api/v1/products/categories`

모든 엔드포인트는 해당 카테고리에 속한 이름 목록(`List<String>`)을 반환합니다.

- `GET /colour-groups`: 색상 그룹 목록 조회
- `GET /departments`: 부서 목록 조회
- `GET /garment-groups`: 의류 그룹 목록 조회
- `GET /graphical-appearances`: 그래픽 모양 목록 조회
- `GET /index-groups`: 인덱스 그룹 목록 조회
- `GET /indices`: 인덱스 목록 조회
- `GET /perceived-colour-masters`: 인지된 기본 색상 목록 조회
- `GET /perceived-colour-values`: 인지된 색상 값 목록 조회
- `GET /product-groups`: 상품 그룹 목록 조회
- `GET /product-types`: 상품 타입 목록 조회
- `GET /sections`: 섹션 목록 조회