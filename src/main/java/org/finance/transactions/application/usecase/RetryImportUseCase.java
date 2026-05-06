package org.finance.transactions.application.usecase;

import org.finance.transactions.domain.model.ImportJob;

public interface RetryImportUseCase {

    /**
     * Resets a FAILED import job to PENDING and re-queues it for processing.
     * Throws {@link IllegalStateException} if the job is not in FAILED status.
     * Throws {@link java.util.NoSuchElementException} if no job exists with the given id.
     */
    ImportJob retry(String importId);
}
