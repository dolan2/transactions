package org.finance.transactions.adapter.in.rest;

import lombok.RequiredArgsConstructor;
import org.finance.transactions.adapter.in.rest.dto.StatisticsResponse;
import org.finance.transactions.application.dto.StatisticsQuery;
import org.finance.transactions.application.usecase.GetStatisticsUseCase;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/statistics")
@RequiredArgsConstructor
public class StatisticsController {

    private final GetStatisticsUseCase getStatisticsUseCase;

    @GetMapping
    public StatisticsResponse getStatistics(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String iban,
            @RequestParam(required = false) String month
    ) {
        var results = getStatisticsUseCase.getStatistics(new StatisticsQuery(category, iban, month));
        var entries = results.stream()
                .map(r -> new StatisticsResponse.Entry(r.groupKey(), r.totalAmount(), r.transactionCount(), r.currency()))
                .toList();
        return new StatisticsResponse(entries);
    }
}
