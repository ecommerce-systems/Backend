# 상품 API 명세

## 1. 개요
상품 조회, 검색 및 관리 기능을 제공함.
성능 최적화 비교를 위해 두 가지 버전의 API를 지원함.

-   **V1**: 정규화된 RDB 모델을 직접 조회. 다중 `JOIN`으로 인한 성능 저하 가능성이 존재.
-   **V2**: 검색에 최적화된 비정규화 테이블을 조회하여 고속 검색을 지원.

## 2. 기본 URL
-   **V1**: `/api/v1/products`
-   **V2**: `/api/v2/products`
-   **카테고리**: `/api/v1/products/categories`

---

## 3. 상품 엔드포인트 (V1)

### 3.1. 전체 상품 조회
-   **URL**: `/`
-   **HTTP Method**: `GET`
-   **설명**: 등록된 모든 상품 목록을 조회함.
-   **응답**: `200 OK` (`Product` 엔티티 리스트)

### 3.2. 상품 상세 조회
-   **URL**: `/{id}`
-   **HTTP Method**: `GET`
-   **설명**: 상품 ID를 통해 특정 상품의 상세 정보를 조회함.
-   **응답**: `200 OK` 또는 `404 Not Found`

### 3.3. 상품명 검색
-   **URL**: `/search`
-   **HTTP Method**: `GET`
-   **쿼리 파라미터**: `keyword` (검색어)
-   **응답**: `200 OK` (상품명 `String` 리스트)
    ```json
    ["상품 A", "상품 B"]
    ```

### 3.4. 상품 페이징 검색
-   **URL**: `/search/results`
-   **HTTP Method**: `GET`
-   **설명**: 키워드로 상품을 검색하고, 결과를 페이징하여 반환함.
-   **쿼리 파라미터**:
    -   `keyword`: 검색어 (필수)
    -   `page`: 페이지 번호 (0부터 시작, 기본값: 0)
    -   `size`: 페이지당 아이템 수 (기본값: 10)
    -   `sort`: 정렬 기준 필드 (기본값: productId)
-   **응답**: `200 OK` (Spring `Page<Product>` 객체)

### 3.5. 상품 등록 (관리자)
-   **URL**: `/`
-   **HTTP Method**: `POST`
-   **권한**: `ADMIN`
-   **설명**: 새로운 상품을 등록함.
-   **요청 본문 (`ProductCreateRequestDto`)**:
    ```json
    { "prodName": "새 상품", "price": 99000, ... }
    ```
-   **응답**: `201 Created`

### 3.6. 상품 수정 (관리자)
-   **URL**: `/{id}`
-   **HTTP Method**: `PUT`
-   **권한**: `ADMIN`
-   **설명**: 기존 상품 정보를 수정함.
-   **응답**: `200 OK`

### 3.7. 상품 삭제 (관리자)
-   **URL**: `/{id}`
-   **HTTP Method**: `DELETE`
-   **권한**: `ADMIN`
-   **설명**: 상품을 삭제함.
-   **응답**: `204 No Content`

---

## 4. 상품 엔드포인트 (V2)

### 4.1. 상품 상세 조회
-   **URL**: `/{id}`
-   **HTTP Method**: `GET`
-   **설명**: 비정규화된 테이블에서 상품 정보를 빠르게 조회함. (`JOIN` 없음)
-   **응답**: `200 OK` (`ProductSearch` 엔티티)

### 4.2. 필터 기반 상품명 검색
-   **URL**: `/search`
-   **HTTP Method**: `GET`
-   **설명**: 다양한 카테고리를 조합하여 상품명을 검색하는 고급 검색 기능.
-   **쿼리 파라미터**:
    -   `keyword`: 검색어 (필수)
    -   `productType`, `department`, `productGroup`, `section`: 카테고리 필터 (선택)
-   **응답**: `200 OK` (상품명 `String` 리스트)

### 4.3. 상품 페이징 검색
-   **URL**: `/search/results`
-   **HTTP Method**: `GET`
-   **설명**: 키워드로 상품을 검색하고, 비정규화 테이블에서 페이징하여 반환함.
-   **쿼리 파라미터**: (V1과 동일)
-   **응답**: `200 OK` (Spring `Page<ProductSearch>` 객체)

---

## 5. 카테고리 엔드포인트
-   **기본 URL**: `/api/v1/products/categories`
-   **설명**: 모든 카테고리 엔드포인트는 해당 카테고리에 속한 이름 목록(`List<String>`)을 반환함.
-   **엔드포인트 목록**:
    -   `GET /colour-groups`, `GET /departments`, `GET /garment-groups`, 등 11개 카테고리.
