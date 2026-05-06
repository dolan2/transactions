package org.finance.transactions.application.usecase;

import org.finance.transactions.domain.model.ImportJob;

import java.io.IOException;
import java.io.InputStream;

public interface ImportTransactionsUseCase {
    ImportJob initiateImport(String fileName, InputStream content) throws IOException;
}
