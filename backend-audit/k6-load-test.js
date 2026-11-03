import http from 'k6/http';
import { check, sleep } from 'k6';
import { Rate, Trend } from 'k6/metrics';

const errorRate = new Rate('error_rate');
const authLatency = new Trend('auth_duration');

export const options = {
  scenarios: {
    // 1. Baseline: 100 Virtual Users for 1 minute
    baseline: {
      executor: 'constant-vus',
      vus: 100,
      duration: '1m',
      tags: { test_type: 'baseline' },
    },
    // 2. Stress Test Scenario
    stress: {
      executor: 'ramping-vus',
      startVUs: 0,
      stages: [
        { duration: '30s', target: 200 },
        { duration: '1m', target: 500 },
        { duration: '30s', target: 0 },
      ],
      startTime: '1m10s',
      tags: { test_type: 'stress' },
    },
  },
  thresholds: {
    http_req_duration: ['p(95)<800', 'p(99)<1500'],
    error_rate: ['rate<0.02'],
  },
};

const BASE_URL = __ENV.API_BASE || 'https://adaptivetrust-backend.onrender.com/api/v1';

export default function () {
  // 1. Probe Health Check
  const healthRes = http.get(`${BASE_URL}/health`);
  check(healthRes, {
    'health returns 200 or 405': (r) => r.status === 200 || r.status === 405,
  });

  // 2. Simulate Login Request
  const loginPayload = JSON.stringify({
    email: 'admin@adaptivetrust.io',
    password: 'Password@123',
  });

  const params = {
    headers: {
      'Content-Type': 'application/json',
    },
  };

  const loginStart = Date.now();
  const loginRes = http.post(`${BASE_URL}/auth/login`, loginPayload, params);
  authLatency.add(Date.now() - loginStart);

  const isSuccess = check(loginRes, {
    'login status valid': (r) => r.status === 200 || r.status === 400 || r.status === 422,
  });

  errorRate.add(!isSuccess);
  sleep(0.5);
}
