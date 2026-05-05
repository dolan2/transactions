package org.finance.transactions.domain.valueobject;

import java.util.regex.Pattern;

public record IBAN(String value) {

    private static final Pattern PATTERN = Pattern.compile("[A-Z]{2}\\d{2}[A-Z0-9]{1,30}");

    public IBAN {
        if (value == null) throw new IllegalArgumentException("IBAN must not be null");
        value = value.trim();
        if (!PATTERN.matcher(value).matches()) throw new IllegalArgumentException("Invalid IBAN: " + value);
    }
}
