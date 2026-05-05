# Persistence Layer (MongoDB)

## Rules

- Use repository interfaces in domain
- Implement in adapter layer

---

## Mongo Guidelines

- Use indexes:
    - IBAN
    - date
    - category

- Use batch inserts

---

## Anti-Patterns

❌ Mongo queries in domain  
❌ Business logic in repository  