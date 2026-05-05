package org.finance.transactions.application.usecase;

import org.finance.transactions.domain.model.ImportJob;

public interface ImportTransactionsUseCase {
    ImportJob initiateImport(String fileName, String fileLocation);
}
