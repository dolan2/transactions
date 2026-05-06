package org.finance.transactions.domain.valueobject;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class CategoryTest {

    @Test
    void valueIsNormalizedToLowercase() {
        var category = new Category("GROCERIES");

        assertThat(category.value()).isEqualTo("groceries");
    }

    @Test
    void valueIsTrimmed() {
        var category = new Category("  food  ");

        assertThat(category.value()).isEqualTo("food");
    }

    @Test
    void nullValueThrowsIllegalArgument() {
        assertThatThrownBy(() -> new Category(null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void blankValueThrowsIllegalArgument() {
        assertThatThrownBy(() -> new Category("   "))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void equalityIsCaseInsensitive() {
        var a = new Category("Groceries");
        var b = new Category("GROCERIES");

        assertThat(a).isEqualTo(b);
        assertThat(a.hashCode()).isEqualTo(b.hashCode());
    }

    @Test
    void toStringReturnsNormalizedValue() {
        var category = new Category("Entertainment");

        assertThat(category.toString()).isEqualTo("entertainment");
    }
}
