package org.finance.transactions.application.usecase;

import org.finance.transactions.application.dto.StatisticsQuery;
import org.finance.transactions.application.dto.StatisticsResult;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface GetStatisticsUseCase {
    Page<StatisticsResult> getStatistics(StatisticsQuery query, Pageable pageable);
}
