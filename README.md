# E-Commerce System Backend

Spring Boot로 만든 이커머스 백엔드입니다. 단순 CRUD가 아니라 "같은 기능을 다른 방식으로 구현하면 얼마나 차이가 나는지"를 직접 부하 테스트로 재보는 데 초점을 맞췄습니다. 인증, 상품 검색, 공동구매 추천 API를 각각 V1/V2로 나눠 구현하고, k6로 실측한 결과를 `docs/Performance`에 정리해뒀습니다.

## 기술 스택

- Java 21, Spring Boot 3.2, Spring Security + JWT
- PostgreSQL (정규화된 쓰기 모델), Redis (세션/캐시), QueryDSL
- Docker / Docker Swarm, Prometheus + Grafana
- 부하 테스트: k6

## 왜 V1/V2로 나눴나

기능을 처음 구현한 방식(V1)이 실제로 느렸던 지점들을 찾아서, 구조를 바꾼 버전(V2)과 같은 조건으로 부하 테스트를 돌려 비교했습니다.

| 영역 | V1 | V2 | 결과 |
| --- | --- | --- | --- |
| 인증 (Refresh Token) | DB 조회 | Redis 조회 | 평균 응답 3.2배 단축, DB 방식에서 발생하던 Race Condition(401 오류) 해소 |
| 상품 검색 | 다중 테이블 JOIN | 검색 전용 비정규화 테이블 | 평균 응답 8배 단축 |
| 공동구매 추천 | N+1 요청 | 단일 요청 | 캐시 미스 기준 평균 251ms → 210ms, 캐싱 적용 시 두 버전 모두 30배 이상 단축 |
| 인스턴스 수 | 1개 | 3개 (Swarm 로드밸런싱) | 평균 응답 17% 개선, RPS 5.7% 증가 |

숫자와 테스트 조건은 `docs/Performance/` 아래 문서에 항목별로 정리해뒀습니다. 왜 그만큼 차이가 났는지(Race Condition 원인, Amdahl's Law로 설명되는 스케일아웃 한계 등)도 같이 적어뒀습니다.

## 실행 방법

### 로컬 (Docker Compose)

```powershell
docker build -t ecommerce-backend:latest .
docker compose up -d
```

Postgres, Redis, 백엔드(3 replica), Prometheus, Grafana가 함께 뜹니다. 비밀번호 등은 `.env.example`을 참고해 `.env`로 복사해서 쓰면 됩니다 (안 만들어도 로컬 기본값으로 동작합니다).

### 운영 배포 (Docker Swarm)

실제 배포는 `docker-swarm.yml` 기준이고, CD는 `.github/workflows/cd.yml`에서 push 시 자동으로 실행됩니다.

```powershell
docker stack deploy -c docker-swarm.yml ecommerce
```

이쪽은 `.env`의 `POSTGRES_PASSWORD`, `JWT_SECRET` 등이 실제로 설정돼 있어야 배포가 진행됩니다.

### 테스트

```powershell
.\mvnw.cmd test

# k6 부하 테스트 (예: 상품 검색 V1 vs V2)
k6 run k6/tests/v2/search-results.js
```

## 문서

- API 명세: `docs/Api/`
- 아키텍처/시퀀스 다이어그램: `docs/Diagram/`
- 부하 테스트 결과 및 분석: `docs/Performance/`
