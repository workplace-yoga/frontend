# AdaptiveTrust Backend Remediation & Hardening Guide

## 1. Rate Limiting on Authentication Endpoints (SEC-001)
Install `slowapi`:
```bash
pip install slowapi
```
In `main.py`:
```python
from slowapi import Limiter, _rate_limit_exceeded_handler
from slowapi.util import get_remote_address
from slowapi.errors import RateLimitExceeded

limiter = Limiter(key_func=get_remote_address)
app.state.limiter = limiter
app.add_exception_handler(RateLimitExceeded, _rate_limit_exceeded_handler)

@app.post("/api/v1/auth/login")
@limiter.limit("5/minute")
async def login(request: Request, credentials: LoginSchema):
    ...
```

## 2. Restrict CORS Allowed Origins (SEC-004)
In `main.py`:
```python
origins = [
    "https://workplace-yoga.github.io",
    "http://localhost:3000",
]

app.add_middleware(
    CORSMiddleware,
    allow_origins=origins,
    allow_credentials=True,
    allow_methods=["GET", "POST", "PUT", "DELETE", "OPTIONS"],
    allow_headers=["*"],
)
```

## 3. Expose Employee Registration Route (SEC-002)
Add the route handler matching frontend parameters `{email, password, full_name, company_code}`.
