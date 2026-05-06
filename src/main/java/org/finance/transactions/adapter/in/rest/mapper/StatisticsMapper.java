package org.finance.transactions.adapter.in.rest.mapper;

import org.finance.transactions.adapter.in.rest.dto.StatisticsResponse;
import org.finance.transactions.application.dto.StatisticsResult;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface StatisticsMapper {

    StatisticsResponse.Entry toEntry(StatisticsResult result);
}
