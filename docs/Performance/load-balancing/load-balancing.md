# 성능 향상 분석 보고서

## 개요
본 문서는 k6 부하 테스트를 통해 **단일 인스턴스 환경(1개)**과  
**로드밸런싱 환경(인스턴스 3개)**을 비교하여 성능 향상 효과를 분석한 결과를 담고 있다.

## 테스트 환경
- 단일 인스턴스 (1개)
- 로드밸런싱 인스턴스 (3개)
- 테스트 스크립트: 회원가입 → 로그인 → 토큰 갱신 → 로그아웃 시나리오
- VUs: 200, Duration: 10초


## 테스트 환경
- **단일 인스턴스 (1개)**
- **로드밸런싱 인스턴스 (3개)**

## 결과 요약

| 항목 | 단일 인스턴스 (1개) | 로드밸런싱 인스턴스 (3개) |
|------|--------------------|--------------------------|
| 총 요청 수 (http_reqs) | 1600 (125.76/s) | 1600 (132.93/s) |
| 성공률 (checks_succeeded) | 100% (1600/1600) | 100% (1600/1600) |
| 평균 응답 시간 (http_req_duration) | 489.67ms | 406.03ms |
| 최대 응답 시간 (max) | 2.24s | 2.40s |
| 90% 응답 시간 (p90) | 1.22s | 1.21s |
| Iteration 평균 시간 | 5.96s | 5.62s |
| 데이터 수신량 | 892 kB (70 kB/s) | 892 kB (74 kB/s) |
| 데이터 송신량 | 690 kB (54 kB/s) | 690 kB (57 kB/s) |

## 성능 향상 포인트
- **응답 속도 개선**: 로드밸런싱 환경이 평균 응답 시간에서 약 17% 더 빠름 (489.67ms → 406.03ms)
- **처리율 증가**: 초당 요청 처리량이 더 높음 (125.76/s → 132.93/s)
- **Iteration 효율성**: 평균 Iteration 시간이 더 짧아 사용자 경험 개선 (5.96s → 5.62s)
- **안정성 유지**: 두 환경 모두 실패율 0%로 안정적

## 결론
로드밸런싱(인스턴스 3개) 환경은 **응답 속도, 처리율, Iteration 효율성**에서 단일 인스턴스보다 우수한 성능을 보여준다.  
특히 평균 응답 시간과 초당 요청 처리량에서 차이가 뚜렷하며, 안정성은 동일하게 유지된다.  
따라서 서비스 확장성과 성능 최적화를 위해 로드밸런싱 환경이 더 적합하다고 결론지을 수 있다.

## 트레이드 오프
- 인스턴스 수가 늘어나면서 고정 자원 점유율이 누적됨.
- 요청이 여러 인스턴스로 분산되므로 개별 인스턴스의 부담은 줄어들고 성능은 높힘.

---

![성능 비교 그래프](graph.png)  

# 단일 인스턴스 결과
![단일 인스턴스 결과](single-instance.png)

```bash 
checks_total.......: 1600    125.764707/s
checks_succeeded...: 100.00% 1600 out of 1600
checks_failed......: 0.00%   0 out of 1600

✓ signup successful
✓ login successful
✓ token refresh successful
✓ logout successful

HTTP
http_req_duration..............: avg=489.67ms min=3.48ms med=382.1ms max=2.24s p(90)=1.22s p(95)=1.6s 
  { expected_response:true }...: avg=489.67ms min=3.48ms med=382.1ms max=2.24s p(90)=1.22s p(95)=1.6s 
http_req_failed................: 0.00%  0 out of 1600
http_reqs......................: 1600   125.764707/s

EXECUTION
iteration_duration.............: avg=5.96s    min=4.67s  med=5.87s   max=7.57s p(90)=7.12s p(95)=7.29s
iterations.....................: 400    31.441177/s
vus............................: 80     min=80        max=200
vus_max........................: 200    min=200       max=200

NETWORK
data_received..................: 892 kB 70 kB/s
data_sent......................: 690 kB 54 kB/s
```

# 로드밸런싱 결과
![로드밸런싱 결과](load-balancing.png)
```bash
checks_total.......: 1600    132.927204/s
checks_succeeded...: 100.00% 1600 out of 1600
checks_failed......: 0.00%   0 out of 1600

✓ signup successful
✓ login successful
✓ token refresh successful
✓ logout successful

HTTP
http_req_duration..............: avg=406.03ms min=4.66ms med=160.03ms max=2.4s  p(90)=1.21s p(95)=1.55s
  { expected_response:true }...: avg=406.03ms min=4.66ms med=160.03ms max=2.4s  p(90)=1.21s p(95)=1.55s
http_req_failed................: 0.00%  0 out of 1600
http_reqs......................: 1600   132.927204/s

EXECUTION
iteration_duration.............: avg=5.62s    min=4.41s  med=5.54s    max=7.26s p(90)=6.92s p(95)=7.04s
iterations.....................: 400    33.231801/s
vus............................: 8      min=8         max=200
vus_max........................: 200    min=200       max=200

NETWORK
data_received..................: 892 kB 74 kB/s
data_sent......................: 690 kB 57 kB/s
```