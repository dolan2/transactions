package org.finance.transactions.application.dto;

import java.math.BigDecimal;

public record StatisticsResult(String groupKey, BigDecimal totalAmount, long transactionCount, String currency) {}
