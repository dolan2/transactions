package org.finance.transactions.adapter.in.rest.mapper;

import org.finance.transactions.adapter.in.rest.dto.ImportStatusResponse;
import org.finance.transactions.domain.model.ImportJob;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ImportJobMapper {

    @Mapping(target = "importId", source = "id")
    @Mapping(target = "status", expression = "java(job.getStatus().name())")
    @Mapping(target = "errors", expression = "java(formatErrors(job))")
    ImportStatusResponse toResponse(ImportJob job);

    default List<String> formatErrors(ImportJob job) {
        return job.getErrors().stream()
                .map(e -> "Row " + e.rowNumber() + ": " + e.reason())
                .toList();
    }
}
