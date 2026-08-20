# Hei Grades API

REST API for HEI's grade management — courses, exams, grades, and graduation tracking across a 3-year curriculum (EL & TN tracks). 🎓✨
Because grades deserve a little tech magic.

![Java 21](https://img.shields.io/badge/Java-21-%23ED8B00)
![Spring Boot 3.2](https://img.shields.io/badge/Spring%20Boot-3.2.2-%236DB33F)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-%234169E1)
![Flyway](https://img.shields.io/badge/Flyway-9.22.3-red)
![Coverage](https://img.shields.io/badge/coverage-80%25-brightgreen)

---

## Tech Stack

| Technology | Version |
|------------|---------|
| Java | 21 |
| Spring Boot | 3.2.2 |
| PostgreSQL | 16+ |
| Flyway | 9.22.3 |
| Gradle | 8.5 |
| Testcontainers / JUnit 5 / Mockito | 2.0.2 / — / — |
| JaCoCo (80% min) | 0.8.11 |

---

## Getting Started

### Prerequisites

- Java 21 _(your JVM will cry otherwise)_
- PostgreSQL 16+ _(your data will flee otherwise)_

### Environment Variables

| Variable | Required | Description |
|----------|----------|-------------|
| `SPRING_DATASOURCE_URL` | Yes | PostgreSQL JDBC URL |
| `SPRING_DATASOURCE_USERNAME` | Yes | DB username |
| `SPRING_DATASOURCE_PASSWORD` | Yes | DB password |
| `SCHOOL_HEI_API_JWT_SECRET` | Yes | JWT signing secret |
| `SCHOOL_HEI_API_JWT_EXPIRATION_SECONDS` | No | JWT lifetime (default 3600) |
| `AWS_EVENT_BRIDGE_BUS` | No | EventBridge bus (default `default-bus`) |
| `AWS_S3BUCCKET_BUS` | No | S3 bucket for transcripts (default `dummy-bucket`) |

### Commands

```bash
./gradlew bootRun        # Summon the app into existence
./gradlew test           # Make the tests happy (or sad)
./gradlew build          # Build it like LEGO
./format.sh              # Make the code pretty
```

---

## API Documentation

All endpoints are documented and testable via Swagger UI — the interactive playground where you can poke, prod, and pet every endpoint without writing a single `curl` command. Swagger also exposes the raw spec at `/v3/api-docs`, and a hand-written copy lives in [`doc/api.yml`](doc/api.yml).

| Environment | URL |
|-------------|-----|
| **Local development** | <http://localhost:8080/swagger-ui/index.html> |
| **Deployed (preprod)** | <https://iqyhgwsbyy7pnxpx2qgqwnmb7a0caurf.lambda-url.eu-west-3.on.aws/swagger-ui/index.html> |

Covers everything: auth (JWT login), users (auto-generated matricules `STD…`/`TEACH…`/`ADMIN…`), promotions, groups & group flows (JOIN/LEAVE), courses, course assignments, exams, grades (with reclamation history), graduation lists (JSON + XLSX export), PDF transcript generation, `whoami`, and health — with request/response schemas and live "Try it out" buttons.

Not REST? The public graduates page is served by Thymeleaf at `GET /graduates` — no token needed, EL/TN buttons, XLSX download.

---

## Testing

```bash
./gradlew test                    # Tests with coverage (min 80%)
./gradlew jacocoTestReport        # Fancy HTML report → build/reports/jacoco/test/html/
```

We test in **3 layers** — like a cake, but with more assertions:

| Layer | Tool | Vibe |
|-------|------|------|
| **Unit** | JUnit 5 + Mockito | Services doing their thing in isolation |
| **Integration** | Testcontainers + TestRestTemplate | The full symphony with a real Postgres over real HTTP, AWS third parties mocked |
| **Export** | JUnit 5 + POI / PDFBox | XLSX and PDF files behaving on the inside |

321 tests and counting — JaCoCo gates the build at 80% line coverage.

---

## Project Structure

```
src/main/java/school/hei/api/
├── PojaApplication.java          # Where it all begins
├── endpoint/
│   ├── rest/
│   │   ├── controller/           # 12 REST controllers saying hello
│   │   ├── controller/health/    # Are we alive? /ping, /health, /health/email
│   │   ├── mapper/               # 6 Entity ↔ DTO translators
│   │   ├── security/             # Who goes there? (JWT, roles, self matchers)
│   │   └── model/                # Request/response DTOs + error response
│   └── event/                    # Async gossips (SQS/EventBridge)
├── model/                        # 6 JPA entities living their best life
│   ├── dto/                      # 14 request/response DTOs
│   ├── enums/                    # 3 enums — choices matter (Path, FlowType, Role)
│   └── exception/                # Ways things can go wrong
├── repository/                   # 9 JPA repositories, data's best friend
├── service/                      # 12 business + event services
├── export/                       # XLSX graduates + PDF transcript generators
├── concurrency/ & datastructure/ # Helpers for when lists need a pep talk
├── file/hash/                    # File whisperers
├── handler/                      # AWS Lambda handlers
└── mail/                         # Email carrier pigeons (SES)
```

---

## One more thing...

```bash
./gradlew bootRun --args='--server.port=9090'  # When 8080 is just too mainstream
./gradlew test --tests "*GraduateServiceTest"  # Test ONE thing, you glorious rebel
curl -X POST http://localhost:8080/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"email":"admin@hei.school","password":"password123"}'   # Hello, ADMIN
```

---

<div align="center">
  <sub>Built with ☕, ❤️, and questionable commit messages — <code>school.hei.api</code></sub>
</div>