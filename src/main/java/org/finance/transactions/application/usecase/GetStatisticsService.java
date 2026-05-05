package org.finance.transactions.application.usecase;

import org.finance.transactions.application.dto.StatisticsQuery;
import org.finance.transactions.application.dto.StatisticsResult;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GetStatisticsService implements GetStatisticsUseCase {

    @Override
    public List<StatisticsResult> getStatistics(StatisticsQuery query) {
        // TODO: implement aggregation against TransactionRepository
        return List.of();
    }
}
