# ADR-009: MapStruct for DTO Mapping

**Status:** Accepted  
**Date:** 2026-05-06

---

## Context

The system has three distinct object models that must not bleed into each other:

1. **Domain objects** (`ImportJob`, `Transaction`, value objects) — no Spring or serialisation annotations
2. **Persistence documents** (`ImportJobDocument`, `TransactionDocument`) — MongoDB `@Document` classes
3. **REST DTOs** (`ImportStatusResponse`, `PagedStatisticsResponse`) — JSON response records

Mapping between these layers is repetitive and error-prone to write manually. Each new field requires updating the mapper in multiple places. Manual mapping in controllers also mixes orchestration logic with transformation logic.

---

## Decision

Use **MapStruct 1.6.x** for all mapping between domain objects and REST response DTOs.

MapStruct generates compile-time Java source code from annotated interfaces, with zero runtime reflection. Mappers are Spring beans (`componentModel = "spring"`) injected via constructor injection.

Key mapper decisions:
- `ImportJobMapper.toResponse(ImportJob)` — maps `status` enum to `String` via `expression = "java(job.getStatus().name())"`, and `errors` via a `default` helper method
- `StatisticsMapper.toEntries(List<StatisticsResult>)` — direct field mapping (identical names and types)

The persistence layer (`MongoImportJobAdapter`, `MongoTransactionAdapter`) retains manual mapping because the mapping logic involves domain reconstitution constructors with specific ordering — MapStruct's record support requires exact parameter matching and the reconstitution constructors have too many fields with non-obvious ordering to be safe to generate automatically.

---

## Consequences

**Benefits:**
- Mapping bugs are caught at compile time, not runtime
- No reflection overhead — generated code is plain Java
- IDE can show generated implementations; debugging is straightforward
- Adding a field to a mapped type produces a compile warning if the mapper is not updated

**Drawbacks:**
- `lombok-mapstruct-binding:0.2.0` must be on the annotation processor path to ensure Lombok runs before MapStruct
- Annotation processor ordering in Gradle requires explicit configuration
- `expression = "java(...)"` strings are not type-checked by the Java compiler; a typo produces a compile error in the generated source, which can be confusing

**Alternatives considered:**
- *ModelMapper* — runtime reflection, slower, harder to debug
- *Manual mapping* — explicit and debuggable but verbose and error-prone; already caused bugs when `ImportController.toResponse()` was an inline private method that new contributors missed when adding fields
- *Lombok `@Builder` + direct code* — still requires updating multiple call sites
