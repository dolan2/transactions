# Performance Guidelines

## File Processing

- Stream CSV
- Do NOT load entire file

---

## DB

- Batch writes
- Use indexes

---

## Async

- Always process via RabbitMQ

---

## Target

- 10,000 rows per file without memory issues