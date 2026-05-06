package org.finance.transactions.domain.model;

public record ImportError(int rowNumber, String reason) {}
