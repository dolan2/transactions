package org.finance.transactions.application.usecase;

public interface CsvImportProcessorUseCase {
    void process(String importId, String fileLocation);
}
