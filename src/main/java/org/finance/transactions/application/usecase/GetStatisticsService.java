package org.finance.transactions.application.usecase;

import lombok.RequiredArgsConstructor;
import org.finance.transactions.application.dto.StatisticsQuery;
import org.finance.transactions.application.dto.StatisticsResult;
import org.finance.transactions.domain.model.StatisticsFilter;
import org.finance.transactions.domain.repository.StatisticsRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GetStatisticsService implements GetStatisticsUseCase {

    private final StatisticsRepository statisticsRepository;

    @Override
    public Page<StatisticsResult> getStatistics(StatisticsQuery query, Pageable pageable) {
        StatisticsFilter filter = new StatisticsFilter(query.iban(), query.category(), query.month());
        return statisticsRepository.aggregate(filter, pageable)
                .map(s -> new StatisticsResult(s.groupKey(), s.totalAmount(), s.transactionCount(), s.currency()));
    }
}
