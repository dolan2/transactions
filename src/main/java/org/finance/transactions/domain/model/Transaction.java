package org.finance.transactions.domain.model;

import lombok.Getter;
import org.finance.transactions.domain.valueobject.Category;
import org.finance.transactions.domain.valueobject.IBAN;
import org.finance.transactions.domain.valueobject.Money;
import org.finance.transactions.domain.valueobject.TransactionDate;

@Getter
public class Transaction {

    private final String id;
    private final IBAN iban;
    private final Money amount;
    private final TransactionDate date;
    private final Category category;
    private final String description;

    public Transaction(String id, IBAN iban, Money amount, TransactionDate date, Category category, String description) {
        this.id = id;
        this.iban = iban;
        this.amount = amount;
        this.date = date;
        this.category = category;
        this.description = description;
    }

    public boolean isDebit() { return amount.isNegative(); }

    public boolean isCredit() { return !amount.isNegative(); }
}
