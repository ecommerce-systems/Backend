# 🚀 High-Performance E-Commerce Backend (v2)

대규모 트래픽을 고려한 **성능 최적화(Performance Optimization)** 및 **아키텍처 고도화** 프로젝트입니다.  
기존 RDB 중심의 모놀리식 구조(V1)가 가진 성능 한계를 **Redis 캐싱, 역정규화, 인덱싱 최적화**를 통해 극복하고,  
**k6 부하 테스트**를 통해 정량적인 성능 향상을 입증했습니다.

---

## 📈 핵심 성과 (Key Performance Metrics)

| 도메인 | 최적화 기법 | Before (V1) | After (V2) | 성능 향상 | 보고서 |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **인증 (Auth)** | Redis Session | 29.55ms (Err 0.6%) | **9.07ms** | **3.2배** 🚀 | [Link](docs/Performance/auth/refresh.md) |
| **검색 (Search)** | 역정규화 (No-Join) | 101.05ms | **12.64ms** | **8배** 🚀 | [Link](docs/Performance/product-search/search-optimization.md) |
| **추천 (Recommend)** | 연산 결과 캐싱 | 289.34ms | **13.36ms** | **21배** 🔥 | [Link](docs/Performance/copurchase/caching.md) |
| **인프라 (Infra)** | Scale-Out (LB) | 489ms | **406ms** | **17%** ⚡ | [Link](docs/Performance/infra/load.md) |

> *테스트 환경: k6 Load Test (VUs: 100~200), Docker Swarm, H2/Redis*

---

## 🛠 기술적 도전과 해결 (Technical Challenges)

### 1. 검색 속도가 느려지는 문제 (The N+1 Join Problem)
- **문제**: 상품 목록 조회 시 `ProductType`, `Department` 등 10개 이상의 테이블을 조인(Join)해야 하여, 데이터가 늘어날수록 조회 성능이 급격히 저하됨.
- **해결**: **"읽기(Read) 전용 역정규화 테이블(`ProductSearch`)"**을 도입.
    - 모든 카테고리 명칭을 단일 테이블에 `String`으로 저장하여 **Zero-Join** 달성.
    - 검색 필드에 복합 인덱스(Composite Index)를 적용하여 **랜덤 키워드 검색 시 8배 성능 향상**.

### 2. 고부하 시 로그인 풀림 현상 (Race Condition)
- **문제**: 트래픽 폭주 시 DB의 Insert 커밋이 지연되는 동안, 클라이언트가 Refresh Token을 요청하면 **"Token Not Found" (401)** 에러 발생.
- **해결**: **Redis(In-Memory)**로 세션 저장소 이관.
    - 메모리의 빠른 쓰기 속도로 Race Condition 원천 차단 (에러율 0.6% → **0.0%**).
    - **RTR(Refresh Token Rotation)** 전략을 적용하여 보안성 강화.

### 3. 실시간 추천 연산의 부하 (CPU Bound)
- **문제**: 공동 구매 추천(`CoPurchase`) 기능이 매 요청마다 주문 테이블 전체를 집계(`GROUP BY`)하여 DB CPU를 과도하게 점유.
- **해결**: **Look-Aside Caching** 전략 적용.
    - 최초 1회만 연산하고 결과를 Redis에 1시간 동안 캐싱.
    - Cold Data(289ms) 대비 Hot Data(13ms) 호출 시 **21배 빠른 응답** 확보.

---

## 🏗 아키텍처 (Architecture)

### V2 Architecture (Current)
```
[Client] -> [Nginx (Load Balancer)] -> [Spring Boot API (V2)]
                                            |
                                    +-------+-------+
                                    |               |
                                [MySQL(RDB)]     [Redis]
                                (Write/Main)     (Cache/Session)
```

---

## 🧪 테스트 및 검증 방법
모든 성능 수치는 **k6**를 사용하여 공정한 환경에서 측정되었습니다.
- **Random Keyword**: 캐시 편향을 막기 위해 랜덤한 검색어로 테스트 수행.
- **Cold/Hot Separation**: 최초 조회와 캐시 적중 시의 성능을 분리하여 측정.
- **Scenario**: 단순 API 호출이 아닌 회원가입부터 주문까지의 **User Journey** 시나리오 기반 테스트.

---

## 📂 프로젝트 구조
- `src/`: Spring Boot 소스 코드 (Domain Driven Design)
- `k6/`: 부하 테스트 스크립트 (`search.js`, `refresh.js` 등)
- `docs/Performance/`: 상세 성능 분석 보고서
- `docker-compose.yml`: 로컬 개발 및 테스트 환경 구성
