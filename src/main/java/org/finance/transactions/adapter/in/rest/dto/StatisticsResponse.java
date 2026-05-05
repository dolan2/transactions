package org.finance.transactions.adapter.in.rest.dto;

import java.math.BigDecimal;
import java.util.List;

public record StatisticsResponse(List<Entry> entries) {
    public record Entry(String groupKey, BigDecimal totalAmount, long transactionCount, String currency) {}
}
