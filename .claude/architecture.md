# Architecture Rules

## Hexagonal Architecture

Layers:
- Domain (pure Java, no frameworks)
- Application (use cases)
- Adapters (REST, DB, messaging)

## Rules

- Domain MUST NOT depend on Spring
- Adapters MUST NOT contain business logic
- Controllers MUST call only use cases
- Use Ports for all external dependencies

## Dependency Direction

adapter → application → domain

NEVER reverse this.

---

## Anti-Patterns (STRICTLY FORBIDDEN)

❌ Business logic in controllers  
❌ Direct DB access from application  
❌ Using entities as DTOs  
❌ God services (>300 lines)  
❌ Anemic domain (no behavior)

---

## Preferred Patterns

✅ Rich domain model  
✅ Small focused services  
✅ Explicit use cases  
✅ Value objects instead of primitives  