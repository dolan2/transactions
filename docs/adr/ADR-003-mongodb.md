# ADR-003: MongoDB as Primary Database

**Status:** Accepted  
**Date:** 2026-05-06

---

## Context

The system stores bank transactions imported from CSV files and aggregates them by category, IBAN, and month. Key characteristics:

- Write pattern: high-volume batch inserts (up to 10,000 rows per file)
- Read pattern: aggregations (sum by group, filter by IBAN/category/month)
- Schema: transaction fields are relatively fixed but could evolve (new CSV columns)
- No cross-entity transactional requirements

---

## Decision

Use **MongoDB** as the primary database with Spring Data MongoDB.

Data is modelled in two collections:
- `transactions` — one document per transaction, indexed on `iban`, `date`, `category` with compound indexes for efficient range + category queries
- `import_jobs` — one document per import, tracks status and validation errors

Aggregation queries (statistics by category/IBAN/month) are handled via MongoDB's native aggregation pipeline (`$match`, `$group`, `$project`), which runs server-side and avoids fetching all documents.

Indexes are managed by a versioned `DatabaseMigrationRunner` that runs at startup and records applied migrations in a `schema_migrations` collection.

---

## Consequences

**Benefits:**
- Flexible schema supports evolving transaction fields without migrations
- `saveAll()` maps efficiently to bulk write operations, which are critical for 10k-row files
- Aggregation pipeline is expressive and runs close to the data
- Spring Data MongoDB provides both the repository abstraction and `MongoTemplate` for custom queries

**Drawbacks:**
- No ACID transactions across multiple documents (not needed here, but would be a blocker for ledger-style use cases)
- Aggregation pipeline syntax is verbose in Java; complex grouping logic lives in adapter code rather than a query DSL
- Less familiar to SQL-oriented teams; queries are harder to introspect than SQL

**Alternatives considered:**
- *PostgreSQL* — strongest option if cross-document transactions or complex relational queries were needed. Rejected because the workload is write-heavy bulk inserts + aggregations with a flexible schema, where MongoDB shines.
- *Elasticsearch* — excellent aggregations and full-text search, but overkill for this use case and adds operational complexity.
- *Cassandra* — fits write-heavy workloads, but aggregations require client-side computation, which eliminates the aggregation pipeline advantage.
