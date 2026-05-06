package org.finance.transactions.adapter.out.persistence;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.finance.transactions.domain.model.StatisticsFilter;
import org.finance.transactions.domain.model.TransactionSummary;
import org.finance.transactions.domain.repository.StatisticsRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.aggregation.GroupOperation;
import org.springframework.data.mongodb.core.aggregation.ProjectionOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class MongoStatisticsAdapter implements StatisticsRepository {

    private final MongoTemplate mongoTemplate;

    @Override
    public Page<TransactionSummary> aggregate(StatisticsFilter filter, Pageable pageable) {
        List<TransactionSummary> data = fetchPage(filter, pageable);
        long total = countGroups(filter);
        return new PageImpl<>(data, pageable, total);
    }

    private List<TransactionSummary> fetchPage(StatisticsFilter filter, Pageable pageable) {
        List<AggregationOperation> ops = buildGroupingOps(filter);
        ops.add(Aggregation.skip(pageable.getOffset()));
        ops.add(Aggregation.limit(pageable.getPageSize()));

        return mongoTemplate
                .aggregate(Aggregation.newAggregation(ops), "transactions", SummaryDoc.class)
                .getMappedResults()
                .stream()
                .map(d -> new TransactionSummary(d.groupKey, d.totalAmount, d.transactionCount, d.currency))
                .toList();
    }

    private long countGroups(StatisticsFilter filter) {
        List<AggregationOperation> ops = buildMatchOps(filter);
        ops.add(Aggregation.group("category"));
        ops.add(Aggregation.count().as("total"));

        CountDoc result = mongoTemplate
                .aggregate(Aggregation.newAggregation(ops), "transactions", CountDoc.class)
                .getUniqueMappedResult();
        return result != null ? result.total : 0L;
    }

    private List<AggregationOperation> buildGroupingOps(StatisticsFilter filter) {
        List<AggregationOperation> ops = buildMatchOps(filter);
        ops.add(groupOp());
        ops.add(projectOp());
        return ops;
    }

    private List<AggregationOperation> buildMatchOps(StatisticsFilter filter) {
        List<AggregationOperation> ops = new ArrayList<>();
        List<Criteria> conditions = buildConditions(filter);
        if (!conditions.isEmpty()) {
            ops.add(Aggregation.match(new Criteria().andOperator(conditions.toArray(Criteria[]::new))));
        }
        return ops;
    }

    private GroupOperation groupOp() {
        return Aggregation.group("category")
                .sum("amount").as("totalAmount")
                .count().as("transactionCount")
                .first("currency").as("currency");
    }

    private ProjectionOperation projectOp() {
        return Aggregation.project("totalAmount", "transactionCount", "currency")
                .and("_id").as("groupKey")
                .andExclude("_id");
    }

    private List<Criteria> buildConditions(StatisticsFilter filter) {
        List<Criteria> conditions = new ArrayList<>();
        if (StringUtils.hasText(filter.iban())) {
            conditions.add(Criteria.where("iban").is(filter.iban()));
        }
        if (StringUtils.hasText(filter.category())) {
            conditions.add(Criteria.where("category").is(filter.category().toLowerCase()));
        }
        if (StringUtils.hasText(filter.month())) {
            YearMonth ym = YearMonth.parse(filter.month());
            conditions.add(Criteria.where("date").gte(ym.atDay(1)).lte(ym.atEndOfMonth()));
        }
        return conditions;
    }

    @Getter
    @NoArgsConstructor
    private static class SummaryDoc {
        private String groupKey;
        private BigDecimal totalAmount;
        private long transactionCount;
        private String currency;
    }

    @Getter
    @NoArgsConstructor
    private static class CountDoc {
        private long total;
    }
}
