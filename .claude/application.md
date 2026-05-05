# Application Layer

## Responsibilities

- Orchestrate use cases
- No business logic
- No framework logic

---

## Use Case Rules

Each use case:
- Single responsibility
- Input DTO
- Output DTO

---

## Example

GOOD:
class ImportTransactionsUseCase {
void execute(ImportCommand cmd)
}

BAD:
class TransactionService {
void doEverything()
}