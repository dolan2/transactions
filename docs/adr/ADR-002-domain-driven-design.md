# ADR-002: Domain-Driven Design

**Status:** Accepted  
**Date:** 2026-05-06

---

## Context

Bank transaction processing involves non-trivial business rules: IBAN validation, currency normalization, import lifecycle management, row-level error tracking, and retry semantics. These rules must be correct, explicit, and testable regardless of which framework delivers requests.

An anemic domain model (plain data containers with logic scattered across service classes) makes it hard to reason about invariants and leads to duplicated validation across layers.

---

## Decision

Apply **Domain-Driven Design** with rich domain objects:

- **Value Objects** (`IBAN`, `Money`, `TransactionDate`, `Category`) — immutable, self-validating, equality by value. IBAN validates format on construction; Money enforces currency normalization to uppercase; Category normalizes to lowercase. Invalid values cannot exist.

- **Entities** (`Transaction`, `ImportJob`) — identity-based equality. `Transaction` exposes `isDebit()`/`isCredit()` behavior. `ImportJob` is the **Aggregate Root** for the import lifecycle, enforcing legal state transitions.

- **Domain Services** — CSV parsing is an application-level concern (not domain) because it involves infrastructure formats, not business rules.

- **No Spring annotations in domain classes** — the domain has zero infrastructure dependencies, making it trivially unit-testable.

---

## Consequences

**Benefits:**
- Business invariants are enforced at construction time — invalid states cannot be created
- `ImportJob` state machine prevents illegal transitions (e.g., retrying a PROCESSING job)
- Domain tests are fast, pure JUnit5 with no mocking frameworks needed
- Ubiquitous language makes the code readable to non-engineers familiar with banking concepts

**Drawbacks:**
- Value objects as record constructors require careful MapStruct configuration (expression mappings for enum→String, custom formatters)
- More classes than a flat service-per-entity approach
- Developers need to understand the aggregate pattern to add features correctly

**Alternatives considered:**
- *Transaction Script pattern* — all logic in service methods. Rejected because it centralises nothing and duplicates validation over time.
- *Active Record pattern* — entities know how to persist themselves. Rejected because it couples domain to the ORM, violating the hexagonal architecture dependency rule.
