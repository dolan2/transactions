# Domain Design Guidelines

## Entities

Must:
- Have identity
- Encapsulate behavior

Example:

GOOD:
class Transaction {
private final IBAN iban;

public boolean isDebit() {
return amount.isNegative();
}
}

BAD:
class Transaction {
public String iban;
public BigDecimal amount;
}

---

## Value Objects

- Immutable
- Validated on creation

Examples:
- IBAN
- Money
- Category

---

## Aggregates

- Define consistency boundaries
- Only root is accessed externally

Example:
ImportJob manages:
- status
- processedRecords
- errorCount

---

## Domain Services

Use ONLY when logic doesn't fit entity