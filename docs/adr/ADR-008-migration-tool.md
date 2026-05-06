# ADR-008: Database Migration Tool

**Status:** Accepted  
**Date:** 2026-05-06

---

## Context

MongoDB indexes must exist before the application serves traffic. Relying on Spring Data MongoDB's `@Indexed`/`@CompoundIndex` with `spring.data.mongodb.auto-index-creation=true` is convenient in development but has two production problems:

1. Index creation on large collections is slow and blocks the collection until the index is ready (pre-4.4 behaviour; 4.4+ creates in background by default)
2. `auto-index-creation=true` re-runs every startup, adding latency

A migration tool solves both: migrations run once, their completion is recorded, and subsequent startups skip already-applied changes.

---

## Decision

Implement a lightweight **custom `DatabaseMigrationRunner`** using `MongoTemplate.indexOps()` that:

1. Tracks applied migrations in a `schema_migrations` collection (document: `{_id: migrationId, appliedAt: ISODate}`)
2. Checks existence before running each migration
3. Runs at application startup via `ApplicationRunner`

`spring.data.mongodb.auto-index-creation` is set to `false` so the migration runner is the single source of truth for indexes.

**Preferred tool was Mongock**, but as of the current project baseline (Spring Boot 4.0 / Spring Data MongoDB 5.x), Mongock 5.x has not published a compatible driver artifact (`mongodb-springdata-v5-driver`). When Mongock 6.x with Spring Boot 4.x support is released, migrating will be straightforward — the migration classes follow the same `@ChangeUnit` pattern Mongock uses.

---

## Consequences

**Benefits:**
- Zero additional library dependencies; uses `MongoTemplate` already in the classpath
- Fully Spring Boot 4.0 compatible
- Migrations are idempotent: `ensureIndex` is a no-op when the index already exists
- Easy to extend: add a `runIfNotApplied("V3__...", ...)` call for new migrations

**Drawbacks:**
- No rollback support (Mongock and Liquibase provide rollback changelogs)
- No checksumming of applied migrations — a modified migration will not re-run
- Manual tracking compared to a mature tool's lifecycle management

**Migration to Mongock (future):**
When Mongock 6.x supports Spring Boot 4.x:
```groovy
implementation 'io.mongock:mongock-springboot:6.x.x'
implementation 'io.mongock:mongodb-springdata-v5-driver:6.x.x'
```
Replace `DatabaseMigrationRunner` with `@ChangeUnit` classes and remove the `schema_migrations` logic.
