package org.finance.transactions.domain.valueobject;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.*;

class IBANTest {

    @Test
    void validGermanIBANIsAccepted() {
        assertThatCode(() -> new IBAN("DE89370400440532013000")).doesNotThrowAnyException();
    }

    @Test
    void validPolishIBANIsAccepted() {
        assertThatCode(() -> new IBAN("PL61109010140000071219812874")).doesNotThrowAnyException();
    }

    @Test
    void nullValueThrowsIllegalArgument() {
        assertThatThrownBy(() -> new IBAN(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("null");
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " ", "de89", "12345", "DE", "D3891234", "xx1234"})
    void invalidFormatsAreRejected(String invalid) {
        assertThatThrownBy(() -> new IBAN(invalid))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void leadingAndTrailingSpacesAreTrimmed() {
        var iban = new IBAN("  DE89370400440532013000  ");

        assertThat(iban.value()).isEqualTo("DE89370400440532013000");
    }

    @Test
    void equalityIsValueBased() {
        var iban1 = new IBAN("DE89370400440532013000");
        var iban2 = new IBAN("DE89370400440532013000");

        assertThat(iban1).isEqualTo(iban2);
        assertThat(iban1.hashCode()).isEqualTo(iban2.hashCode());
    }

    @Test
    void differentValuesAreNotEqual() {
        var iban1 = new IBAN("DE89370400440532013000");
        var iban2 = new IBAN("GB29NWBK60161331926819");

        assertThat(iban1).isNotEqualTo(iban2);
    }
}
