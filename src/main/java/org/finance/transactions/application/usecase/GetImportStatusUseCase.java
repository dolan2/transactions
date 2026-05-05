package org.finance.transactions.application.usecase;

import org.finance.transactions.domain.model.ImportJob;

import java.util.Optional;

public interface GetImportStatusUseCase {
    Optional<ImportJob> getStatus(String importId);
}
