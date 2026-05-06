# ADR-005: Retry and Dead Letter Queue Strategy

**Status:** Accepted  
**Date:** 2026-05-06

---

## Context

CSV processing can fail due to transient infrastructure issues (MongoDB unavailable, disk full) or permanent issues (corrupt file). The system must distinguish between these, avoid processing the same message indefinitely, and give operators a way to investigate and recover without data loss.

---

## Decision

Two-tier failure strategy:

**Tier 1 — Automatic RabbitMQ retry (transient failures)**  
The main queue (`transaction-import`) is configured with a Dead Letter Exchange pointing to the DLQ (`transaction-import.dlq`). RabbitMQ moves a message to the DLQ when:
- The consumer throws an exception and rejects the message (NACK without requeue)
- The message exceeds the retry limit (when configured via `x-message-ttl` and re-enqueue)

**Tier 2 — Explicit operator retry (permanent or investigated failures)**  
`POST /imports/{id}/retry` resets a `FAILED` `ImportJob` to `PENDING` and re-publishes the message. This requires a human decision — it does not happen automatically. The file remains in local storage at its original location, so re-processing reads the same bytes.

**ImportJob state machine guards:**
- `retry()` throws `IllegalStateException` if status is not `FAILED`
- `startProcessing()` throws if status is not `PENDING`

This prevents duplicate processing if two retry messages are accidentally published.

---

## Consequences

**Benefits:**
- Operators can inspect the DLQ in the RabbitMQ management UI before deciding to retry
- No message is lost — every failure is either in the DLQ or tracked in the `ImportJob.status = FAILED`
- The state machine guard in `ImportJob` acts as a domain-level idempotency lock
- `isRetryable()` helper makes the UI able to expose retry only when appropriate

**Drawbacks:**
- File storage must persist the file until the job succeeds (currently local filesystem — `LocalFileStorage`)
- If the file is deleted before retry, the job will fail again with an IO error
- Manual retry is a human-in-the-loop process; for high volumes, an automated DLQ drainer would be needed

**Future improvements:**
- Add `x-delivery-count` header check to limit automatic re-delivery before DLQ routing
- Migrate `LocalFileStorage` to object storage (S3/MinIO) for durable file retention
