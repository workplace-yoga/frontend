# AdaptiveTrust Backend Security Audit & Vulnerability Assessment

**Assessment Standard**: OWASP Top 10 API Security (2023) & CWE Classification  
**Target Environment**: `https://adaptivetrust-backend.onrender.com/api/v1`  
**Review Type**: Non-Destructive DAST + Full Source SAST Code Review

---

## High & Critical Vulnerability Findings

### Finding ID: SEC-001
- **Severity**: High
- **Type**: Broken Authentication & Credential Stuffing
- **CWE**: CWE-307 (Improper Restriction of Authentication Attempts)
- **OWASP**: API2:2023 - Broken Authentication
- **Endpoint**: `POST /auth/login`
- **Description**: The authentication endpoint lacks rate limiting or account lockout controls. An attacker can execute automated credential stuffing or dictionary attacks against corporate accounts.
- **Impact**: Account takeover of employee and corporate administrator accounts.
- **Remediation**: Implement a rate limiting middleware (e.g., `slowapi` or Redis-backed token bucket) capping login attempts to 5 per minute per IP.

### Finding ID: SEC-002
- **Severity**: High
- **Type**: Missing Endpoint / Feature Inconsistency
- **CWE**: CWE-440 (Expected Behavior Violation)
- **OWASP**: API10:2023 - Unsafe Consumption of APIs
- **Endpoint**: `POST /auth/register/employee`
- **Description**: The frontend web and mobile applications call `POST /auth/register/employee` with an 8-character invite code, but backend deployments without the recent schema update return 404/405 errors.
- **Impact**: Legitimate employees cannot self-enroll or register their mobile devices.
- **Remediation**: Expose the dedicated public endpoint for employee self-registration with transactional validation of `company_code`.

### Finding ID: SEC-003
- **Severity**: Medium
- **Type**: Insufficient Token Invalidation on Logout
- **CWE**: CWE-613 (Insufficient Session Expiration)
- **OWASP**: API2:2023 - Broken Authentication
- **Endpoint**: `POST /auth/logout`
- **Description**: JWTs are verified statelessly. Client logout only purges `localStorage`, leaving the JWT valid on the network until its natural expiration time.
- **Impact**: Intercepted JWTs can be replayed until expiration.
- **Remediation**: Maintain a short TTL (15 min) for access tokens and implement a Redis revocation blacklist for revoked `jti` identifiers.

### Finding ID: SEC-004
- **Severity**: Medium
- **Type**: Overly Permissive CORS Policy
- **CWE**: CWE-942 (Permissive Cross-Domain Policy with Untrusted Domains)
- **OWASP**: API7:2023 - Security Misconfiguration
- **Endpoint**: Global Middleware
- **Description**: `allow_origins=["*"]` allows any web domain to send cross-origin requests to the API.
- **Impact**: Unauthorized web origins can interact with the API if user credentials or tokens are present in browsers.
- **Remediation**: Restrict `allow_origins` strictly to `https://workplace-yoga.github.io` and designated enterprise subdomains.

### Finding ID: SEC-005
- **Severity**: Medium
- **Type**: Information Exposure in Exception Traces
- **CWE**: CWE-209 (Generation of Error Message Containing Sensitive Information)
- **OWASP**: API7:2023 - Security Misconfiguration
- **Endpoint**: `POST /telemetry/submit`, `POST /admin/override/*`
- **Description**: Unhandled runtime exceptions return FastAPI default traceback JSON details.
- **Impact**: Discloses internal database table names and stack hierarchy.
- **Remediation**: Implement global exception handlers returning sanitized RFC 7807 problem details.
