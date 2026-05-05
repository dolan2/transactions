package org.finance.transactions.adapter.in.rest.dto;

public record ImportStatusResponse(
        String importId,
        String fileName,
        String status,
        int totalRows,
        int processedRows,
        int errorRows
) {}
