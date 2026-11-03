# AdaptiveTrust Backend Security, Audit & Performance Suite

This repository contains the comprehensive security audit, API inventory, functional test suite (400+ cases), SAST/DAST evidence, load/performance testing suite (k6, Artillery, JMeter), and automated DevSecOps CI/CD pipeline for the AdaptiveTrust FastAPI backend.

## Structure
- `backend-inventory.md`: Complete tech stack, architecture, auth & API inventory.
- `security-review.md`: In-depth vulnerability report mapped to OWASP Top 10 and CWE.
- `executive-summary.md`: Academic & enterprise evaluation summary (Security Score: 78/100).
- `dependency-report.md`: Software supply chain, CVE scan and secrets audit.
- `performance-report.md`: Baseline (100 VUs, 120 RPS, 250ms avg), Stress, Spike & Endurance analysis.
- `remediation-guide.md`: Step-by-step code fixes for identified security risks.
- `k6-load-test.js`: Production-ready k6 load simulation script.
- `artillery-load-test.yml`: Artillery cloud/local performance scenario.
- `jmeter-test-plan.jmx`: Apache JMeter test plan for distributed load testing.
- `test-cases/`: 400+ executable automated API tests.
