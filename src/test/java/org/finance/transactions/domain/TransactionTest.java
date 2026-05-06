package org.finance.transactions.domain;

import org.finance.transactions.domain.model.Transaction;
import org.finance.transactions.domain.valueobject.Category;
import org.finance.transactions.domain.valueobject.IBAN;
import org.finance.transactions.domain.valueobject.Money;
import org.finance.transactions.domain.valueobject.TransactionDate;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.*;

class TransactionTest {

    private static final IBAN SAMPLE_IBAN = new IBAN("DE89370400440532013000");
    private static final TransactionDate SAMPLE_DATE = TransactionDate.of("2024-03-15");
    private static final Category SAMPLE_CATEGORY = new Category("groceries");

    @Test
    void creditTransactionHasPositiveAmount() {
        var tx = transaction(Money.of("150.00", "EUR"));

        assertThat(tx.isCredit()).isTrue();
        assertThat(tx.isDebit()).isFalse();
    }

    @Test
    void debitTransactionHasNegativeAmount() {
        var tx = transaction(Money.of("-75.50", "EUR"));

        assertThat(tx.isDebit()).isTrue();
        assertThat(tx.isCredit()).isFalse();
    }

    @Test
    void zeroAmountIsCredit() {
        var tx = transaction(Money.of("0.00", "EUR"));

        assertThat(tx.isCredit()).isTrue();
        assertThat(tx.isDebit()).isFalse();
    }

    @Test
    void transactionExposesAllFields() {
        var money = Money.of("200.00", "USD");
        var tx = new Transaction("id-1", SAMPLE_IBAN, money, SAMPLE_DATE, SAMPLE_CATEGORY, "Supermarket");

        assertThat(tx.getId()).isEqualTo("id-1");
        assertThat(tx.getIban()).isEqualTo(SAMPLE_IBAN);
        assertThat(tx.getAmount()).isEqualTo(money);
        assertThat(tx.getDate()).isEqualTo(SAMPLE_DATE);
        assertThat(tx.getCategory()).isEqualTo(SAMPLE_CATEGORY);
        assertThat(tx.getDescription()).isEqualTo("Supermarket");
    }

    private Transaction transaction(Money money) {
        return new Transaction("id", SAMPLE_IBAN, money, SAMPLE_DATE, SAMPLE_CATEGORY, "desc");
    }
}
