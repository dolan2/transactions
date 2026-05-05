package org.finance.transactions.domain.repository;

import org.finance.transactions.domain.model.ImportJob;

import java.util.Optional;

public interface ImportJobRepository {
    ImportJob save(ImportJob importJob);
    Optional<ImportJob> findById(String id);
}
