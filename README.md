# User API Tests (Kotlin + REST Assured + JUnit 5)

Black-box API tests for user registration (`POST /user/create` as `multipart/form-data`) and listing users (`GET /user/get`). The repository is test-only (`src/test`): no production application code. Tests use unique usernames/emails so they stay independent on a shared environment.

`src/test/resources/test.properties` is not committed (local overrides). Copy **`src/test/resources/test.properties.example`** to **`test.properties`** before running tests, or set **`BASE_URL`** / **`-DbaseUrl`** (see Configuration below).

## API under test

| Method | Path           | Purpose                         | Body |
|--------|----------------|----------------------------------|------|
| POST   | `/user/create` | Create a user                    | `multipart/form-data`: `username`, `email`, `password` |
| GET    | `/user/get`    | List users (optional `id` query) | —    |

## Tech stack

- Kotlin, JDK 21  
- Gradle (Kotlin DSL), JUnit 5  
- REST Assured 5.x, AssertJ  
- Jackson — maps JSON responses to DTOs where the shape is stable  
- SLF4J + Logback (test logging via `logback-test.xml`)

## API client (`client/UserApiClient`)

`UserApiClient` is a thin REST Assured wrapper around the two endpoints:

- **`createUser`** — sends `multiPart` fields for `username`, `email`, and `password` (each part omitted when the argument is `null`, which supports negative tests).  
- **`getUsers()`** — `GET /user/get` without query parameters.  
- **`getUserById(userId)`** — `GET /user/get?id=...`.

After each HTTP call it logs **status and full body** at **DEBUG** (`logHttpResponse`), so you can see traffic when logging is verbose enough.

Configuration (base URI, timeouts) is applied once from `TestConfig` before the client is used (see `BaseApiTest`).

## DTO models (`dto/`)

DTOs are **test-side** shapes for parsing JSON with Jackson (`ResponseMapper.toDto` / list parsing where applicable):

| Type | Role |
|------|------|
| `CreateUserResponse` | Successful create payload (`success`, `details` with `id`, identity fields, etc.). |
| `CreateUserErrorBody` | Error create payload (`success`, `message` list) for validation/uniqueness assertions. |
| `UserListItemDto` | One row from the users list (`id`, `username`, `email`, …) for structured list checks. |

They are not generated from an OpenAPI spec; they evolve with what the suite actually asserts.

## API error messages (`constants/ApiErrorMessages`)

**`object ApiErrorMessages`** holds strings used around **create-user errors**:

| Constant | Role |
|----------|------|
| `EMAIL_ALREADY_EXISTS`, `USERNAME_ALREADY_TAKEN` | Wire text in the API `message` array — use with **`CustomAssert.assertCreateUserErrorBody`** (case-insensitive `contains`). |
| `MISSING_FIELD_EXPECTED_400` | AssertJ **`withFailMessage`** template for TC-005 when a required multipart field is **omitted** but status ≠ 400 (not returned by the API as-is). |
| `EMPTY_FIELD_EXPECTED_400` | Same for TC-008 when a part is present but **empty string** and status ≠ 400. |

Update wire strings when the environment’s API copy changes.

## Fixtures as preconditions (`fixtures/UserFixtures`)

`BaseApiTest` exposes `userFixtures: UserFixtures`, backed by the same `UserApiClient` instance.

- **`createRegisteredUser()`** — full Arrange for “a user exists”: creates a valid user via `POST /user/create`, asserts success, maps `CreateUserResponse`, and returns `RegisteredUser` (payload + server `id`). Logs **INFO** after a successful registration.  
- **`ensureAtLeastOneUserExists()`** — calls `GET /user/get`; if the parsed list is **empty**, registers one user via `createRegisteredUser()`. Used when a test needs **at least one row** without assuming shared DB state.  
- **`isGetUsersListNonEmpty()`** — delegates to `ensureAtLeastOneUserExists()`; convenience when the scenario only needs a non-empty list.

Use fixtures when the test’s scenario is “given a registered user” or “given a non-empty catalog”, not when the test is purely about the first registration call.

## Logging and log levels

Logging is **centralized** for the whole test JVM:

1. **`src/test/resources/test.properties`** — property **`testLogLevel`** (e.g. `WARN`, `INFO`, `DEBUG`).  
2. **`src/test/resources/logback-test.xml`** — loads `test.properties` and sets the **root** logger level to `${testLogLevel}` (default **`WARN`** if unset).

All SLF4J loggers inherit the root level unless you add explicit `<logger>` rules.

| Level | Typical use |
|-------|-------------|
| **WARN** | Quiet runs; only warnings/errors from libraries and your code. |
| **INFO** | See **`UserFixtures`** registration success (`log.info`). |
| **DEBUG** | See **`UserApiClient`** per-request **status + body** (`log.debug`). Third-party loggers may get louder too. |

There is no REST Assured global wire logging in policy; control noise with **`testLogLevel`**.

## JUnit 5 annotations in this suite

| Annotation | How we use it |
|------------|----------------|
| `@Test` | Marks an executable test method. |
| `@DisplayName("…")` | Human-readable / TC-aligned title in IDE and reports. |
| `@Tag("…")` | Filters and grouping. |

**Tags you will see:**

- **`p0`** — high-priority regression coverage.  
- **`smoke`** — minimal happy-path check (`CreateUserTests`).  
- **`business`**, **`validation`**, **`security`** — suite / area markers on the class or method.  
- **`TC-xxx`** — traceability to a test-case id (e.g. `TC-001`, `TC-004`).

You can filter by tags in the IDE, or add a `includeTags` / `excludeTags` block inside `tasks.test { useJUnitPlatform { … } }` in `build.gradle.kts` when you need CLI/CI splits.

## Project layout

```
src/test/kotlin/
├── client/          UserApiClient
├── config/          TestConfig (URL, timeouts, REST Assured setup)
├── constants/       ApiErrorMessages
├── data/            NewUser, RegisteredUser, TestUserFactory
├── dto/             JSON response DTOs
├── fixtures/        UserFixtures
├── support/         CustomAssert, ResponseMapper, logging helpers
└── tests/
    ├── BaseApiTest.kt
    ├── business/
    ├── validation/
    └── security/
```

CI: `.github/workflows/api-tests.yml`.

## How to run

First time (or after clone), create local config:

```bash
cp src/test/resources/test.properties.example src/test/resources/test.properties
```

Then:

```bash
./gradlew test
```

Windows (PowerShell), override base URL:

```powershell
.\gradlew.bat test "-DbaseUrl=http://18.194.45.232:3333"
```

Configuration resolution: **system properties** → **environment variables** (`BASE_URL`, etc.) → **`test.properties`** (create from **`test.properties.example`** if missing from VCS). If `baseUrl` is missing, startup fails with a clear error.

Single class:

```bash
./gradlew test --tests "tests.business.CreateUserTests"
```

HTML report: `build/reports/tests/test/index.html`.
