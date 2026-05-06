package org.finance.transactions.adapter.in.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.finance.transactions.adapter.in.rest.dto.StatisticsResponse;
import org.finance.transactions.adapter.in.rest.mapper.StatisticsMapper;
import org.finance.transactions.application.dto.StatisticsQuery;
import org.finance.transactions.application.usecase.GetStatisticsUseCase;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Statistics", description = "Transaction aggregation and reporting")
@RestController
@RequestMapping("/transactions/v1/statistics")
@RequiredArgsConstructor
public class StatisticsController {

    private final GetStatisticsUseCase getStatisticsUseCase;
    private final StatisticsMapper statisticsMapper;

    @Operation(summary = "Get transaction statistics",
               description = "Aggregates transactions by category. Supports filtering by IBAN, category, and month (yyyy-MM).")
    @ApiResponse(responseCode = "200", description = "Statistics returned")
    @ApiResponse(responseCode = "400", description = "Invalid filter parameters")
    @GetMapping
    public Page<StatisticsResponse.Entry> getStatistics(
            @Parameter(description = "Filter by category (case-insensitive)")
            @RequestParam(required = false) String category,

            @Parameter(description = "Filter by IBAN")
            @RequestParam(required = false) String iban,

            @Parameter(description = "Filter by month in yyyy-MM format, e.g. 2024-03")
            @RequestParam(required = false) String month,

            @PageableDefault(size = 20) Pageable pageable
    ) {
        return getStatisticsUseCase
                .getStatistics(new StatisticsQuery(category, iban, month), pageable)
                .map(statisticsMapper::toEntry);
    }
}
