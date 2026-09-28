# 🚀 High-Performance E-Commerce Backend (v2)

## 🛠 1. 실행 및 테스트 방법

### **로컬 실행 (Docker Compose)**

```powershell
# 1. 이미지 빌드
docker build -t ecommerce-backend:latest .

# 2. 서비스 스택 기동 (API 인스턴스 3개, PostgreSQL, Redis, MongoDB, Prometheus, Grafana 포함)
docker compose up -d
```

### **운영 배포 (Docker Swarm)**

실제 운영 서버는 `docker-swarm.yml`로 배포됩니다 (CD 파이프라인: `.github/workflows/cd.yml`).

```powershell
docker stack deploy -c docker-swarm.yml ecommerce
```

### **검증 및 테스트**

```powershell
# 유닛 및 통합 테스트 실행
.\mvnw.cmd test

# k6 부하 테스트 실행 (예: 상품 검색 V1 vs V2 비교)
# 스크립트 위치: k6/scripts/, k6/tests/v1/, k6/tests/v2/
k6 run k6/tests/v2/search-results.js
```

---
