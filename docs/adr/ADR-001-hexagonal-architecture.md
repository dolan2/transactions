# ADR-001: Hexagonal Architecture (Ports & Adapters)

**Status:** Accepted  
**Date:** 2026-05-06

---

## Context

The system needs to process bank transaction CSV files, store them in a database, publish messages to a broker, and expose a REST API. The core challenge is keeping business logic testable and independent from infrastructure choices — we may swap MongoDB for PostgreSQL, RabbitMQ for Kafka, or REST for gRPC in the future without rewriting domain logic.

A layered architecture (controller → service → repository) couples the domain tightly to frameworks and makes it difficult to test business rules without spinning up infrastructure.

---

## Decision

Adopt **Hexagonal Architecture (Ports & Adapters)** with the following structure:

```
Domain (core) — pure Java, no Spring, no infrastructure
Application (use cases) — orchestrates domain, defines ports
Adapters/In (REST, RabbitMQ consumer) — translate HTTP/AMQP into use cases
Adapters/Out (MongoDB, RabbitMQ publisher, filesystem) — implement ports
Config — Spring wiring only
```

**Dependency rule:** Domain ← Application ← Adapters. Nothing points inward to the outer layers.

**Ports** are interfaces defined by the application layer expressing what it needs (`ImportJobRepository`, `FileStorage`, `ImportPublisher`). **Adapters** implement those interfaces without the domain knowing their existence.

---

## Consequences

**Benefits:**
- Domain and application logic can be unit-tested without a database or message broker
- Infrastructure can be swapped transparently (e.g., `LocalFileStorage` → `S3FileStorage`)
- Each adapter has a single responsibility, making the codebase easy to navigate
- Testcontainers integration tests verify adapters in isolation

**Drawbacks:**
- More boilerplate: explicit port interfaces, mapping between domain objects and documents, DTOs at every boundary
- Steeper onboarding for developers unfamiliar with the pattern
- Over-engineering risk for simple CRUD services — justified here because the async pipeline and CSV processing make the domain non-trivial

**Alternatives considered:**
- *Layered architecture* — rejected because it tends to leak infrastructure concerns (Spring annotations, JPA entities) into service classes over time
- *CQRS* — considered but deemed premature without explicit read/write scaling requirements; the current aggregation pipeline is sufficient
