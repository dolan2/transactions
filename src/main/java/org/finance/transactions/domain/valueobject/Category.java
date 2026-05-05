package org.finance.transactions.domain.valueobject;

import org.jspecify.annotations.NonNull;

import java.util.Objects;

public record Category(String value) {

    public Category(String value) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException("Category must not be blank");
        this.value = value.trim().toLowerCase();
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Category(String value1))) return false;
        return Objects.equals(value, value1);
    }

    @Override
    public @NonNull String toString() {
        return value;
    }
}
