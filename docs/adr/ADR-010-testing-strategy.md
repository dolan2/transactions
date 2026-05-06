# ADR-010: Testing Strategy

**Status:** Accepted  
**Date:** 2026-05-06

---

## Context

A production-grade system requires a test suite that gives confidence in correctness at multiple levels — domain rules, database interactions, message flows, and API contracts — without being so slow or fragile that developers stop running it.

The challenge is that the system touches three infrastructure components (MongoDB, RabbitMQ, filesystem). Tests that mock these give false confidence; tests that require real Docker containers take time to start.

---

## Decision

Three-tier testing pyramid:

### Tier 1: Unit Tests (domain layer)
- Test all domain objects: `ImportJob`, `Transaction`, `IBAN`, `Money`, `Category`
- Pure JUnit 5, no Spring context, no mocking frameworks
- Verify invariants, state transitions, and validation rules
- Run in milliseconds; should be the most numerous tests

### Tier 2: Integration Tests (adapter layer)
- Test MongoDB adapters with a real MongoDB instance via **Testcontainers**
- `TestcontainersConfiguration` provides `@ServiceConnection` beans (auto-configured connection)
- Verify that domain objects survive a round-trip through `MongoImportJobAdapter`
- Verify `MongoStatisticsAdapter` aggregation pipeline against realistic data

### Tier 3: API Tests (end-to-end, in-process)
- `@SpringBootTest(webEnvironment = RANDOM_PORT)` with full Spring context
- `MockMvc` for synchronous endpoint verification
- Testcontainers provides real MongoDB and RabbitMQ
- Tests cover: file upload → immediate 202, status polling, statistics query with filters and pagination
- Security tests: verify 401 on missing API key, 200 with correct key

**Test profiles:**
- Tests use `@TestPropertySource` to set `app.security.api-key=test-api-key`

---

## Consequences

**Benefits:**
- Domain tests catch regressions instantly without container startup
- Integration tests use real MongoDB, catching index issues and aggregation bugs that mocks would miss (the team was previously burned by mocked repository tests passing while a MongoDB aggregation pipeline syntax error caused production failures)
- `@ServiceConnection` eliminates hardcoded container host/port configuration
- API tests verify the full stack including security filters, exception handlers, and serialisation

**Drawbacks:**
- Testcontainers requires Docker on the developer machine and CI runner
- First test run per session pulls Docker images (mitigated by pinning specific image versions)
- `@SpringBootTest` tests are slow compared to unit tests; they should be run in CI, not on every file save

**Alternatives considered:**
- *Mocking repositories in service tests* — rejected because mocks cannot catch MongoDB aggregation bugs or index constraint violations
- *Embedded MongoDB (Flapdoodle)* — rejected because it does not support all MongoDB 5+ features (notably some aggregation operators and transactions) and lags behind the real MongoDB version
