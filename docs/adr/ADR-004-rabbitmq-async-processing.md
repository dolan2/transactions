# ADR-004: RabbitMQ for Asynchronous CSV Processing

**Status:** Accepted  
**Date:** 2026-05-06

---

## Context

Uploading a CSV file with 10,000 rows cannot block an HTTP request. Processing time depends on file size, validation, and database write throughput. A synchronous implementation would either time out or force the client to wait an unacceptable amount of time.

The system also needs resilience: if processing fails mid-file, the job should be retryable without re-uploading the file.

---

## Decision

Use **RabbitMQ** to decouple file upload from processing:

1. `POST /imports` stores the file and publishes an `ImportMessage(importId, fileLocation)` to a direct exchange
2. A `@RabbitListener` consumer picks up the message and runs `CsvImportProcessorService`
3. The `ImportJob` aggregate tracks status transitions: `PENDING → PROCESSING → COMPLETED/FAILED`

Message converter is `JacksonJsonMessageConverter` (Jackson 3), which serialises the message to JSON so the payload is human-readable in the RabbitMQ management UI.

A Dead Letter Queue (`transaction-import.dlq`) catches messages that fail after exhausting retries, preventing message loss.

---

## Consequences

**Benefits:**
- Upload response is immediate (HTTP 202 Accepted); clients poll `GET /imports/{id}` for status
- Processing throughput scales independently from API throughput (add more consumer instances)
- DLQ ensures no message is silently dropped; failed jobs can be inspected and retried via `POST /imports/{id}/retry`
- RabbitMQ's management UI provides visibility into queue depth and consumer health

**Drawbacks:**
- Eventual consistency: the client must poll or subscribe for completion
- Requires an additional infrastructure component (RabbitMQ container)
- Message ordering is per-queue but not guaranteed across multiple consumers — acceptable here since each import is independent

**Alternatives considered:**
- *Kafka* — preferred for high-throughput event streaming and long-term log retention. Rejected because the import volume does not justify Kafka's operational overhead; RabbitMQ's simpler routing and DLQ model fits better for task queues.
- *Database polling (outbox pattern)* — no additional broker. Rejected because it adds polling overhead and complicates the database schema.
- *Spring Batch (synchronous)* — processes inline in a request-scoped thread. Rejected because it blocks the HTTP thread and makes horizontal scaling harder.
