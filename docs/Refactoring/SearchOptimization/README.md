# 검색 최적화 및 리팩토링 보고서

## 1. 개요 (Overview)
본 문서는 상품 검색 기능의 성능 병목 현상을 해결하기 위해 적용된 최적화 전략을 기술합니다. 기존 로직(V1)을 유지하면서 고성능 검색 엔진(V2)을 도입하는 **이중 아키텍처(Dual-Architecture)** 방식을 채택했습니다.

## 2. 주요 문제점 (V1 Legacy)
*   **과도한 JOIN 연산**: `Product` 엔티티는 11개 이상의 테이블(`Category`, `Department`, `ProductGroup` 등)과 관계를 맺고 있습니다. 단순 검색 시에도 복잡한 JOIN이 발생하여 성능 저하의 주원인이 되었습니다.
*   **풀 테이블 스캔 (Full Table Scan)**: `LIKE %keyword%` 방식의 검색은 데이터베이스 인덱스를 효율적으로 타지 못해, 데이터가 많아질수록 전체 테이블을 스캔해야 하는 비효율이 발생했습니다.
*   **느린 필터링**: 여러 카테고리로 필터링할 때마다 JOIN된 테이블들을 모두 확인해야 하므로 응답 속도가 현저히 느려졌습니다.

## 3. 해결 솔루션 (V2 Optimized)

### 3.1. 역정규화 (Denormalization & CQRS)
읽기 작업(검색)에 특화된 `ProductSearch`라는 **단일 테이블(Flat Table)**을 새로 생성했습니다.
*   **JOIN 제거**: 모든 카테고리 이름을 `ProductSearch` 테이블의 컬럼으로 포함시켜, 검색 시 JOIN 연산을 완전히 제거했습니다.
*   **읽기 최적화**: 단 한 번의 SELECT 쿼리로 데이터를 조회하므로 오버헤드가 없습니다.

### 3.2. 인덱싱 전략 (Indexing)
*   **접두사 검색 인덱스**: `prod_name` 컬럼에 B-Tree 인덱스를 적용했습니다.
*   **카테고리 인덱스**: 주요 필터링 컬럼(`product_type_name`, `department_name` 등)에 개별 인덱스를 적용하여 필터링 속도를 극대화했습니다.

### 3.3. 하이브리드 검색 알고리즘 (Hybrid Search)
속도와 정확도의 균형을 맞추기 위해 하이브리드 방식을 적용했습니다.
1.  **1순위 (접두사 검색)**: `LIKE 'keyword%'`를 사용합니다. 인덱스를 사용하여 O(log N) 속도로 즉시 결과를 반환합니다. (매우 빠름)
2.  **2순위 (보완 검색)**: 1순위 결과가 5건 미만일 경우, `LIKE '%keyword%'`를 수행하여 포함된 단어도 찾아서 결과를 보완합니다.

### 3.4. 데이터 동기화 (Dual Write)
V1(원본 데이터)과 V2(검색용 데이터) 간의 정합성을 보장하기 위한 메커니즘입니다.
*   **실시간 동기화**: `ProductService`에서 생성(`save`), 수정(`update`), 삭제(`delete`) 발생 시 자동으로 `ProductSearch` 테이블에도 반영됩니다.
*   **초기 마이그레이션**: 서버 시작 시 `ProductSearch` 테이블이 비어 있다면, 기존 `Product` 데이터를 자동으로 복사해옵니다.

## 4. 성능 비교 계획

| 기능 | V1 (Legacy) | V2 (Optimized) | 예상 성능 향상 |
| :--- | :--- | :--- | :--- |
| **검색 쿼리** | `LIKE %...%` (Full Scan) | `LIKE ...%` (Index Scan) | **10배 ~ 100배 이상** |
| **쿼리 복잡도** | 11+ JOINs | 0 JOINs | **CPU/IO 사용량 대폭 감소** |
| **필터링** | JOIN 후 필터링 | 복합 인덱스 활용 | **즉각적인 필터링 결과** |

## 5. 산출물
*   **아키텍처 비교도**: `docs/Refactoring/SearchOptimization/architecture_comparison.puml`
*   **동기화 로직 시퀀스**: `docs/Refactoring/SearchOptimization/data_synchronization.puml`
