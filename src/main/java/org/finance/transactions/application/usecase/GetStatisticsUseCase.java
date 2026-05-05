package org.finance.transactions.application.usecase;

import org.finance.transactions.application.dto.StatisticsQuery;
import org.finance.transactions.application.dto.StatisticsResult;

import java.util.List;

public interface GetStatisticsUseCase {
    List<StatisticsResult> getStatistics(StatisticsQuery query);
}
