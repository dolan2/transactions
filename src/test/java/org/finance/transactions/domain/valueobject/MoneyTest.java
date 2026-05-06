package org.finance.transactions.domain.valueobject;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.*;

class MoneyTest {

    @Test
    void currencyIsNormalizedToUppercase() {
        var money = new Money(BigDecimal.ONE, "eur");

        assertThat(money.currency()).isEqualTo("EUR");
    }

    @Test
    void positiveAmountIsNotNegative() {
        var money = new Money(new BigDecimal("100.00"), "USD");

        assertThat(money.isNegative()).isFalse();
    }

    @Test
    void negativeAmountIsNegative() {
        var money = new Money(new BigDecimal("-50.00"), "USD");

        assertThat(money.isNegative()).isTrue();
    }

    @Test
    void zeroAmountIsNotNegative() {
        var money = new Money(BigDecimal.ZERO, "EUR");

        assertThat(money.isNegative()).isFalse();
    }

    @Test
    void nullAmountThrowsIllegalArgument() {
        assertThatThrownBy(() -> new Money(null, "EUR"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Amount");
    }

    @Test
    void nullCurrencyThrowsIllegalArgument() {
        assertThatThrownBy(() -> new Money(BigDecimal.ONE, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Currency");
    }

    @Test
    void blankCurrencyThrowsIllegalArgument() {
        assertThatThrownBy(() -> new Money(BigDecimal.ONE, "  "))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void factoryMethodParsesValidDecimalString() {
        var money = Money.of("123.45", "GBP");

        assertThat(money.amount()).isEqualByComparingTo(new BigDecimal("123.45"));
        assertThat(money.currency()).isEqualTo("GBP");
    }

    @Test
    void factoryMethodThrowsOnInvalidAmountString() {
        assertThatThrownBy(() -> Money.of("not-a-number", "EUR"))
                .isInstanceOf(NumberFormatException.class);
    }

    @Test
    void equalityIsValueBased() {
        var a = new Money(new BigDecimal("10.00"), "EUR");
        var b = new Money(new BigDecimal("10.00"), "EUR");

        assertThat(a).isEqualTo(b);
    }

    @Test
    void differentCurrenciesAreNotEqual() {
        var eur = new Money(new BigDecimal("10.00"), "EUR");
        var usd = new Money(new BigDecimal("10.00"), "USD");

        assertThat(eur).isNotEqualTo(usd);
    }
}
