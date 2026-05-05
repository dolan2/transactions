# REST API Guidelines

## Rules

- Controllers are thin
- No business logic
- Validate only request format

---

## Endpoints

POST /imports
GET /imports/{id}
GET /statistics

---

## DTO Rules

- Never expose domain objects
- Use explicit request/response models

---

## Error Handling

- Use global exception handler
- Return meaningful HTTP codes