# ADR-007: CSV Processing — Streaming and Batch Inserts

**Status:** Accepted  
**Date:** 2026-05-06

---

## Context

The system must handle CSV files with up to 10,000 transaction rows. A naïve implementation that reads all rows into a `List<Transaction>` before saving would consume ~10–50 MB of heap per file and cause GC pressure under concurrent imports. It would also delay feedback on errors until the entire file is read.

---

## Decision

**Streaming parse:** Apache Commons CSV 1.11.x reads rows lazily via `CSVParser` (which is `Iterable<CSVRecord>`). The parser wraps an `InputStream` from the file storage layer. At no point is the entire file held in memory.

**Batch inserts:** Validated rows are accumulated in an `ArrayList<Transaction>` of configurable size (default: 500, max recommended: 1000). When the batch is full, `transactionRepository.saveAll(batch)` is called, which maps to a single MongoDB bulk write. The batch is cleared and reused. The final partial batch is flushed after the loop.

**Per-row validation with error tracking:** Each row is validated inside `CsvImportParser.toRow()`. Rows that throw `IllegalArgumentException` are logged and counted as errors in `ImportJob.addError()`. Up to 100 error details are stored; beyond that, only the error count increments. Processing continues so valid rows are not discarded.

**Configuration:** Batch size is externalised to `app.import.batch-size` (default 500), allowing tuning without recompilation.

---

## Consequences

**Benefits:**
- Memory footprint per import is bounded by batch size, not file size
- Partial progress is committed — if the process crashes mid-file, processed rows are not lost (though the job will be marked FAILED and operators must decide whether to retry)
- Error tracking provides actionable feedback without stopping the import
- Configurable batch size allows performance tuning per environment

**Drawbacks:**
- Partial commitment means a FAILED import may leave some transactions in the database. A retry re-processes the full file, potentially creating duplicates for already-processed rows. Mitigation: transaction-level deduplication (see ADR-006).
- Streaming means the error count for the entire file is not known upfront; `totalRows` is only set on `complete()`, not during processing.

**Alternatives considered:**
- *Spring Batch* — provides checkpointing, skip policies, and chunk-oriented processing out of the box. Rejected because it introduces significant configuration overhead (Job, Step, ItemReader, ItemWriter) for a single-file use case. Spring Batch would be the right choice if the system needed to handle multiple concurrent large imports with restart-from-checkpoint semantics.
- *Reactive streams (Flux)* — elegant backpressure, but MongoDB's reactive driver and RabbitMQ reactive integration add complexity without a clear throughput benefit at 10k rows.
