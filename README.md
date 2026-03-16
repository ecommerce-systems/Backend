# 🚀 High-Performance E-Commerce Backend (v2)

## 🛠 1. 실행 및 테스트 방법

### **로컬 실행 (Docker Swarm)**

로드 밸런싱과 고가용성 테스트를 위해 Docker Swarm 환경에서 배포합니다.

```powershell
# 1. 이미지 빌드
docker build -t ecommerce-backend:latest .

# 2. 서비스 스택 배포 (Nginx, API 인스턴스 3개, MySQL, Redis 포함)
docker stack deploy -c docker-compose.yml ecommerce
```

### **검증 및 테스트**

```powershell
# 유닛 및 통합 테스트 실행
.\mvnw.cmd test

# k6 부하 테스트 실행 (예: 공동구매 추천 API)
# 스크립트 위치: k6/scripts/
k6 run k6/scripts/copurchase/copurchase_v1.js
```

---
