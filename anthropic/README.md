# Cert Alert

A Spring Boot service that stores TLS certificates, tracks their expiry, and alerts
when any of them gets close to its `notAfter` date. Built to satisfy the technical
test in `test-tehnic.md`.

---

## TL;DR — run it

```bash
mvn spring-boot:run
# open http://localhost:8080
```

Log in with one of the seeded users — password is `password` for all of them:

| username | group    | roles             | can add certificates? |
|----------|----------|-------------------|-----------------------|
| `alice`  | security | VIEWER, MANAGER   | yes                   |
| `bob`    | security | VIEWER            | no (list only)        |
| `carol`  | platform | VIEWER, MANAGER   | yes                   |
| `dave`   | platform | VIEWER            | no (list only)        |

`alice` and `bob` share the **security** group — they can see the same certificates.
`carol` and `dave` are in **platform** and cannot see anything `alice` added, and vice
versa. That is the group-isolation rule from the spec.

Try the UI:

1. Sign in as `alice`.
2. Paste `www.google.com` into "Fetch from URL" and submit.
3. The certificate lands in the table. Adjust the threshold — certificates expiring
   within the threshold turn red.
4. Sign out, sign back in as `carol`, confirm the table is empty.

---

## Stack & key choices

| Area            | Choice                                                                                                | Why                                                                                                                   |
|-----------------|-------------------------------------------------------------------------------------------------------|-----------------------------------------------------------------------------------------------------------------------|
| Runtime         | Spring Boot 3.3, Java 21                                                                              | Current LTS, records + pattern matching keep the code terse without a code-gen dep.                                   |
| Security        | Spring Security OAuth2 Resource Server, JWT signed HS256                                              | Satisfies the OAuth2 requirement without pulling in a whole IdP. The resource server is the "real" production piece.  |
| Token issuance  | Local `/api/auth/token` endpoint against a BCrypt user store                                          | Lets a reviewer run the project with zero external dependencies. Swap it for Keycloak/Auth0 by changing one bean.     |
| Persistence     | Spring Data JPA + H2 in-memory                                                                        | Fast to bootstrap. Schema is PostgreSQL-compatible (`MODE=PostgreSQL`) so moving to Postgres is a config change.      |
| Certificate I/O | JDK `CertificateFactory` and `SSLSocket`                                                              | No BouncyCastle needed — the JDK handles X.509 (DER + PEM) and TLS handshakes natively.                               |
| Frontend        | Static HTML + vanilla JS served from `src/main/resources/static`                                      | The spec says "SPA frontend + REST API". A single-file SPA proves the split without pulling in a Node toolchain.      |
| Build           | Maven                                                                                                | Convention-heavy, boring, ubiquitous.                                                                                |
| Tests           | JUnit 5 + MockMvc + Spring Security Test                                                              | 22 tests covering parser, URL parsing, and the full REST/security stack.                                              |

### Why HS256 (symmetric) and not RS256?

HS256 keeps the service self-contained — a single secret in `application.yml` (or
`CERT_ALERT_JWT_SECRET` env var) is enough. For production where you want to verify
tokens without holding the signing key, swap `SecurityConfig#jwtDecoder` to
`NimbusJwtDecoder.withJwkSetUri(...)` and point it at your IdP. Everything else stays
the same because the app only relies on `sub`, `group`, and `roles` claims.

### Why not Lombok?

It would shave a few hundred lines but introduces an annotation-processor that is
easy for a reviewer to misconfigure. Plain Java records/POJOs compile everywhere.

---

## Domain model

```
User ──┐
       │ username, password (BCrypt), group, roles[]
       │
       │ (implicitly via JWT "group" claim)
       ▼
Certificate
  alias
  subject, issuer, serialNumber
  notBefore, notAfter
  signatureAlgorithm
  fingerprintSha256
  ownerGroup      ← enforced in every query
  uploadedBy, uploadedAt
  source (FILE | URL), sourceRef
  pem             ← preserved for re-download

ThresholdSetting
  thresholdDays   ← single row, runtime-editable
```

Group isolation is not a filter bolted on in the service layer — it is baked into
the repository methods (`findAllByOwnerGroupOrderByNotAfterDesc`,
`findByIdAndOwnerGroup`). You cannot accidentally leak a cert across groups by
forgetting a where-clause, because the wrong-clause query does not exist.

## REST API

All `/api/**` endpoints (except `/api/auth/token`) require a `Bearer` JWT.

| Method | Path                          | Role           | Description                                             |
|--------|-------------------------------|----------------|---------------------------------------------------------|
| POST   | `/api/auth/token`             | public         | Exchange username/password for a JWT.                  |
| GET    | `/api/me`                     | any            | Current user info (username, group, roles).           |
| GET    | `/api/certificates?order=`    | VIEWER+MANAGER | List certs for caller's group. `order=desc` (default) or `asc`. |
| GET    | `/api/certificates/{id}`      | VIEWER+MANAGER | Get a single certificate (group-scoped).             |
| POST   | `/api/certificates/upload`    | MANAGER        | `multipart/form-data`, field `file` = `.cer`/`.pem`. |
| POST   | `/api/certificates/fetch`     | MANAGER        | `{"url": "www.google.com", "alias": "…"}`           |
| DELETE | `/api/certificates/{id}`      | MANAGER        | Delete (group-scoped).                               |
| GET    | `/api/alerts`                 | VIEWER+MANAGER | Certs in caller's group within the current threshold.|
| GET    | `/api/config/threshold`       | VIEWER+MANAGER | Current threshold days.                             |
| PUT    | `/api/config/threshold`       | MANAGER        | `{"thresholdDays": 45}`.                            |

### Example cURL flow

```bash
TOKEN=$(curl -s -X POST localhost:8080/api/auth/token \
  -H 'Content-Type: application/json' \
  -d '{"username":"alice","password":"password"}' | jq -r .accessToken)

curl -s localhost:8080/api/me -H "Authorization: Bearer $TOKEN"

curl -s -X POST localhost:8080/api/certificates/fetch \
  -H "Authorization: Bearer $TOKEN" \
  -H 'Content-Type: application/json' \
  -d '{"url":"www.google.com"}'

curl -s localhost:8080/api/certificates -H "Authorization: Bearer $TOKEN" | jq
curl -s localhost:8080/api/alerts       -H "Authorization: Bearer $TOKEN" | jq
```

---

## Alerting

- A `@Scheduled` job (`cert-alert.alert.scan-cron`, default `0 0 8 * * *`) walks every
  stored certificate across every group and pushes the expiring ones through every
  registered `AlertPublisher` bean.
- `LogAlertPublisher` is the only shipping publisher — it writes a WARN line per cert.
- The `AlertPublisher` SPI is deliberately boring (`publish(list, thresholdDays)`) so
  adding email/Slack/PagerDuty is "write a bean, register it"; no scan-logic changes.
- Users also call `GET /api/alerts` for the interactive view — the UI polls this on
  every refresh and paints the expiring certs red.

### Threshold

The threshold is stored in a single-row `threshold_setting` table, lazily seeded from
`cert-alert.alert.default-threshold-days`. MANAGER-role users can change it at runtime
via `PUT /api/config/threshold`. Viewers can read it (so the UI can display it) but
cannot change it.

---

## Design notes and trade-offs

### "Descending by expiry date"

The spec says *"list certificates, descending by expiry date"*. Taken literally that
means the cert expiring furthest in the future comes first — which is not very useful
in an alerting context, where you want the soonest expiry at the top. I kept **DESC
as the default** because it matches the spec verbatim, but added `?order=asc` and a
UI toggle so you can flip to the operationally useful order in one click. The README
documenting this ambiguity is, itself, a deliverable of the test.

### URL fetching and SSRF

The URL fetcher uses a trust-all `X509TrustManager` intentionally — we want to record
whatever certificate the server actually presents, *especially* expired or
self-signed ones. It would be worse than useless for an alerting service to refuse to
look at the certs most likely to need attention.

The trade-off is that "fetch from URL" can become an SSRF sink if the instance runs
inside a VPC. To defuse that, `UrlCertificateFetcher` resolves the host up-front and
rejects loopback / link-local / site-local / multicast addresses. Disable with
`cert-alert.url-fetch.block-private-networks: false` only in trusted environments.

### Deduplication

Uploading the same certificate twice to the same group returns **409 Conflict** with
the pre-existing id. Detection is by SHA-256 fingerprint of the DER bytes, so
re-uploading after a cosmetic PEM re-encoding also dedupes. Different groups may each
hold their own copy of the same cert — that is intentional, since "visible only to
group members" implies per-group ownership.

### Group isolation

Enforced at three layers:

1. **Repository** — every read method takes `ownerGroup` as a parameter. There is no
   `findAll()` equivalent used by controllers, so a forgotten `where` is impossible.
2. **Service** — `SecurityUtils.currentUser()` reads the group from the JWT, not from
   request params, so a user cannot spoof another group by fiddling with URLs.
3. **Test** — `crossGroupIsolation` in `CertAlertIntegrationTest` proves the rule
   end-to-end.

### Why no refresh tokens, CSRF, CORS, rate limiting?

- Refresh tokens: the spec asks for OAuth2 resource-server behavior, not a full token
  dance. A 60-minute access token is fine for a demo.
- CSRF: disabled because the only client is a stateless JS app sending a Bearer
  header. CSRF protection matters for cookie auth, not bearer auth.
- CORS: the SPA is served from the same origin as the API, so CORS is a no-op.
- Rate limiting: out of scope for a take-home, but I would plug in Bucket4j + Redis
  behind a filter in production.

### What would I add for production?

- Real IdP integration (Keycloak/Auth0) via JWK set URI — already one-line swap.
- Flyway migrations instead of `ddl-auto: update`.
- Metrics — Micrometer + a Prometheus endpoint, counters for cert count per group
  and per-scan expiring counts.
- `AlertPublisher` implementations for email (JavaMail) and Slack (webhook).
- Proper RBAC model — groups and roles in the DB rather than on the user row, so
  multi-group users are possible.
- PKCS#12 / keystore upload in addition to single `.cer`.
- Certificate chain handling — today we keep only the leaf. For chain-aware alerting
  (e.g. alert when the intermediate expires) we would persist the whole chain.

---

## Project layout

```
src/main/java/com/example/certalert/
├── CertAlertApplication.java
├── auth/              — /api/auth/token + /api/me
├── certificate/
│   ├── Certificate.java / CertificateRepository.java
│   ├── CertificateService.java / CertificateController.java
│   ├── dto/            — CertificateDto, UrlFetchRequest
│   └── parser/         — CertificateParser, UrlCertificateFetcher
├── alert/             — AlertService, AlertScheduler, AlertPublisher SPI, ThresholdService, AlertController
├── user/              — User, UserRepository, Role
├── security/          — SecurityUtils (JWT -> CurrentUser)
├── config/            — SecurityConfig, JwtProperties, AlertProperties, UrlFetchProperties, DataSeeder
└── error/             — GlobalExceptionHandler

src/main/resources/
├── application.yml
└── static/            — index.html, app.js, style.css (the SPA)

src/test/
├── java/com/example/certalert/
│   ├── CertAlertIntegrationTest.java
│   └── certificate/parser/
│       ├── CertificateParserTest.java
│       └── UrlCertificateFetcherTest.java
└── resources/test-cert.pem    — self-signed, 10-year test certificate
```

---

## Running the tests

```bash
mvn test
```

Expected: **22 tests, all green** (`CertAlertIntegrationTest` – 11,
`CertificateParserTest` – 3, `UrlCertificateFetcherTest` – 8).

The integration test exercises: bad credentials, unauthenticated access, the `/me`
endpoint, viewer-cannot-upload, manager-uploads-viewer-lists, cross-group isolation,
duplicate uploads, list ordering (both directions), threshold role enforcement, and
the alert filter.

---

## Configuration reference

```yaml
cert-alert:
  jwt:
    secret: <HS256 key, ≥32 bytes>   # env: CERT_ALERT_JWT_SECRET
    ttl-minutes: 60
    issuer: cert-alert
  alert:
    default-threshold-days: 30       # initial value for the threshold row
    scan-cron: "0 0 8 * * *"         # daily at 08:00
  url-fetch:
    block-private-networks: true     # SSRF guard
    connect-timeout-ms: 5000
    read-timeout-ms: 5000
```
