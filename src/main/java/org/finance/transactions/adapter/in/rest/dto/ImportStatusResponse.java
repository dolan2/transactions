package org.finance.transactions.adapter.in.rest.dto;

import java.util.List;

public record ImportStatusResponse(
        String importId,
        String fileName,
        String status,
        int totalRows,
        int processedRows,
        int errorRows,
        List<String> errors
) {}
