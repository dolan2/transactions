package org.finance.transactions.domain.repository;

import org.finance.transactions.domain.model.StatisticsFilter;
import org.finance.transactions.domain.model.TransactionSummary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface StatisticsRepository {

    Page<TransactionSummary> aggregate(StatisticsFilter filter, Pageable pageable);
}
