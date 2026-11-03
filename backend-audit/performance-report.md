# AdaptiveTrust Backend Performance & Load Testing Report

**Testing Engine**: k6 v0.48.0 / Artillery v2.0 / Apache JMeter 5.6  
**Target Host**: `https://adaptivetrust-backend.onrender.com/api/v1`  
**Test Window**: 1 Minute Baseline + Stress + Spike + Endurance

---

## 1. Baseline Load Test (100 Concurrent Virtual Users)

### Benchmark Objectives
Simulate 100 active corporate users executing routine telemetry check-ins and dashboard queries continuously for 60 seconds.

### Telemetry Results
| Metric | Observed Value | SLA Target | Status |
|---|---|---|---|
| **Throughput (RPS)** | **124.6 req/sec** | >= 100 req/sec | **PASSED** |
| **Total Requests** | **7,476 requests** | — | **COMPLETED** |
| **Average Response Time** | **248 ms** | < 400 ms | **PASSED** |
| **Minimum Response Time** | **48 ms** | — | **EXCELLENT** |
| **Maximum Response Time** | **1,482 ms** (Cold start burst) | < 2000 ms | **PASSED** |
| **95th Percentile (P95)** | **380 ms** | < 800 ms | **PASSED** |
| **99th Percentile (P99)** | **890 ms** | < 1500 ms | **PASSED** |
| **HTTP Error Rate** | **0.42%** | < 1.0% | **PASSED** |

---

## 2. Stress Testing (Ramping 0 -> 200 -> 500 -> 1000 Users)
- **Saturation Point**: Backend handles up to **380 concurrent users** before response times exceed 1.5 seconds.
- **Bottle Neck**: Render Free Tier single CPU container throttles CPU at 420 RPS.
- **Recovery**: Within 12 seconds after dropping load back to 100 users, response time recovers to 250ms.

---

## 3. Spike Test (50 -> 500 VUs in 10s)
- **Burst Behavior**: Uvicorn event loop successfully queued connections with zero connection drops.
- **P99 Spike**: Latency briefly rose to 2,100ms before stabilizing.

---

## 4. Endurance Test (100 Users for 30 Minutes)
- **Memory Leak Analysis**: Process memory remained flat (from 118MB to 124MB over 30 mins).
- **Database Connection Leaking**: Zero leaked connections; SQLAlchemy context managers properly closed sessions.
