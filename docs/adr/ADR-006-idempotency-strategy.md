# ADR-006: Idempotency Strategy

**Status:** Accepted  
**Date:** 2026-05-06

---

## Context

A client that never receives an HTTP 202 response (network timeout, proxy failure) may re-submit the same CSV file. The consumer may also process the same RabbitMQ message twice if the broker re-delivers before receiving an ACK. Both scenarios risk duplicate transactions in the database.

---

## Decision

Idempotency is enforced at two levels:

**Level 1 — ImportJob ID as message key**  
The `ImportJob` is created and persisted *before* the message is published. The message carries the `importId`. If the message is re-delivered, `CsvImportProcessorService` loads the job and calls `job.startProcessing()`, which throws `IllegalStateException` if the job is already `PROCESSING`, `COMPLETED`, or `FAILED`. The exception causes a NACK → DLQ routing, preventing double-processing.

**Level 2 — Transaction ID deduplication**  
Each transaction is assigned a UUID on creation. While this does not deduplicate across file re-uploads (a new upload creates a new job with new UUIDs), it ensures a single import does not insert the same row twice.

**File upload idempotency (client-facing)**  
`POST /imports` always creates a new `ImportJob` and returns a new `importId`. Clients that want upload idempotency should track their own `importId` and call `GET /imports/{id}` to check whether a prior upload completed, avoiding a redundant re-upload.

---

## Consequences

**Benefits:**
- The domain model enforces idempotency without a separate distributed lock
- RabbitMQ re-deliveries on broker restart are safely handled by the state machine guard
- No external idempotency key store (Redis, database table) required

**Drawbacks:**
- True file-level deduplication (same CSV bytes → same result) is not implemented; re-uploading the same file creates duplicate transactions. Addressing this would require content hashing and a unique index on file hash.
- The NACK-on-duplicate behaviour routes to the DLQ, which operators may find noisy. A structured log message distinguishes duplicate from real failures.

**Future improvements:**
- Hash the CSV file on upload and store the hash in `ImportJob`; add a unique index on hash to reject true duplicates at the API level (HTTP 409)
