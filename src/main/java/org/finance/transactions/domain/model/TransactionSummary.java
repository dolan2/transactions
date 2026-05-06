package org.finance.transactions.domain.model;

import java.math.BigDecimal;

public record TransactionSummary(String groupKey, BigDecimal totalAmount, long transactionCount, String currency) {}
