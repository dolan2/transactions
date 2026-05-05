package org.finance.transactions.domain.valueobject;

import java.time.LocalDate;
import java.time.YearMonth;

public record TransactionDate(LocalDate value) {

    public TransactionDate {
        if (value == null) throw new IllegalArgumentException("Transaction date must not be null");
    }

    public static TransactionDate of(String isoDate) {
        return new TransactionDate(LocalDate.parse(isoDate));
    }

    public YearMonth getYearMonth() {
        return YearMonth.from(value);
    }
}
