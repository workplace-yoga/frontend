# AdaptiveTrust Backend Architecture & Technology Inventory

## 1. Technology Stack
- **Language**: Python 3.10+
- **Framework**: FastAPI (Asynchronous ASGI REST API)
- **ASGI Server**: Uvicorn / Gunicorn
- **Database**: PostgreSQL (with asyncpg / psycopg2)
- **ORM**: SQLAlchemy (Declarative Base & Sessionmaker)
- **Data Validation**: Pydantic v1/v2 Schemas
- **Security & Tokens**: python-jose (JWT), passlib (Bcrypt / Argon2)
- **Deployment Platform**: Render Cloud (`https://adaptivetrust-backend.onrender.com/api/v1`)

## 2. Architectural Design
- **Pattern**: Layered Clean Architecture (Monolithic Gateway)
  - `main.py`: Gateway router, middleware, lifecycle handlers, exception handlers.
  - `trust_engine.py`: Zero Trust scoring algorithms, telemetry evaluation, geofence anomaly math.
  - `models.py`: Database schema definitions (Users, Companies, AuditLogs, TelemetryHistory).
  - `schemas.py`: Pydantic request/response transfer objects.
  - `database.py`: Engine connection pooling and session lifecycle dependency injection.
  - `worker.py`: Asynchronous background task scheduler for telemetry processing.

## 3. Authentication & Authorization Structure
- **Authentication Mechanism**: JWT (JSON Web Tokens) with HS256 / RS256 signature verification.
- **MFA / OTP Flow**: 6-digit numeric OTP sent via SMTP with a 5-minute validity window.
- **Role-Based Access Control (RBAC)**:
  - `SUPER_ADMIN`: Cross-tenant platform governance and company lifecycle control.
  - `ADMIN`: Tenant-scoped employee management, directory view, manual trust score overrides.
  - `EMPLOYEE`: Self-service telemetry submission, trust profile audit, personal log retrieval.

## 4. Complete Discovered Endpoint Inventory

| Endpoint | Method | Auth Required | Expected Roles | Controller / Route File | Description |
|---|---|---|---|---|---|
| `/auth/login` | POST | None | Public | `main.py` | Authenticates email & password; initiates OTP dispatch |
| `/auth/register/admin` | POST | None | Public | `main.py` | Provisions new corporate tenant workspace & admin account |
| `/auth/register/employee` | POST | None | Public | `main.py` | Self-service employee signup using 8-char company code |
| `/auth/verify-login-otp` | POST | None | Public | `main.py` | Validates 6-digit OTP code and returns Bearer JWT |
| `/auth/verify-email` | POST | None | Public | `main.py` | Account activation OTP validation fallback |
| `/auth/resend-code` | POST | None | Public | `main.py` | Dispatches fresh verification code to target email |
| `/admin/dashboard` | GET | Bearer JWT | ADMIN, SUPER_ADMIN | `main.py` | Aggregates tenant user counts, active devices, risk alerts |
| `/admin/employees/search` | GET | Bearer JWT | ADMIN, SUPER_ADMIN | `main.py` | Filters and sorts tenant employee directory records |
| `/admin/override/boost` | POST | Bearer JWT | ADMIN, SUPER_ADMIN | `main.py` | Manually resets user trust score to 100 (Clean State) |
| `/admin/override/mfa` | POST | Bearer JWT | ADMIN, SUPER_ADMIN | `main.py` | Forces immediate biometric / OTP MFA challenge |
| `/admin/override/lock` | POST | Bearer JWT | ADMIN, SUPER_ADMIN | `main.py` | Suspends target user session and revokes access tokens |
| `/sync/stream` | GET | Bearer JWT | ADMIN, SUPER_ADMIN | `main.py` | Server-Sent Events (SSE) real-time telemetry stream |
| `/employee/dashboard` | GET | Bearer JWT | EMPLOYEE, ADMIN | `main.py` | Returns caller's dynamic trust score, gauge status |
| `/employee/history` | GET | Bearer JWT | EMPLOYEE, ADMIN | `main.py` | Retrieves historical trust audit timeline records |
| `/employee/history/{logId}` | GET | Bearer JWT | EMPLOYEE, ADMIN | `main.py` | Detailed telemetry event rationale and delta cause |
| `/telemetry/submit` | POST | Bearer JWT | EMPLOYEE, ADMIN | `main.py` | Ingests GPS coordinates, device status & activity type |
| `/super-admin/summary` | GET | Bearer JWT | SUPER_ADMIN | `main.py` | Platform-wide macro metrics (companies, global posture) |
| `/super-admin/companies` | GET | Bearer JWT | SUPER_ADMIN | `main.py` | Lists all corporate workspaces and invite codes |
| `/super-admin/users` | GET | Bearer JWT | SUPER_ADMIN | `main.py` | Global user directory across all tenants |
| `/super-admin/company/{id}/toggle` | POST | Bearer JWT | SUPER_ADMIN | `main.py` | Activates or suspends corporate tenant workspace |
| `/health` | GET | None | Public | `main.py` | Gateway health check and readiness probe |
