package org.finance.transactions.application.dto;

public record ParsedCsvRow(
        int rowNumber,
        String iban,
        String date,
        String amount,
        String currency,
        String category,
        String description
) {}
