package org.finance.transactions.application.usecase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.finance.transactions.application.dto.ParsedCsvRow;
import org.finance.transactions.application.service.CsvImportParser;
import org.finance.transactions.config.ImportProperties;
import org.finance.transactions.domain.model.ImportError;
import org.finance.transactions.domain.model.ImportJob;
import org.finance.transactions.domain.model.Transaction;
import org.finance.transactions.domain.port.out.FileStorage;
import org.finance.transactions.domain.repository.ImportJobRepository;
import org.finance.transactions.domain.repository.TransactionRepository;
import org.finance.transactions.domain.valueobject.Category;
import org.finance.transactions.domain.valueobject.IBAN;
import org.finance.transactions.domain.valueobject.Money;
import org.finance.transactions.domain.valueobject.TransactionDate;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CsvImportProcessorService implements CsvImportProcessorUseCase {

    private final ImportJobRepository importJobRepository;
    private final TransactionRepository transactionRepository;
    private final FileStorage fileStorage;
    private final ImportProperties importProperties;

    @Override
    public void process(String importId, String fileLocation) {
        ImportJob job = importJobRepository.findById(importId)
                .orElseThrow(() -> new IllegalStateException("ImportJob not found: " + importId));

        job.startProcessing();
        importJobRepository.save(job);
        log.info("Started processing import {} from {}", importId, fileLocation);

        try {
            processFile(job, fileLocation);
            log.info("Completed import {}: {} processed, {} errors",
                    importId, job.getProcessedRows(), job.getErrorRows());
        } catch (Exception e) {
            log.error("Error during import {}: {}", importId, e.getMessage(), e);
            job.fail();
            importJobRepository.save(job);
        }
    }

    private void processFile(ImportJob job, String fileLocation) throws IOException {
        List<Transaction> batch = new ArrayList<>(importProperties.getBatchSize());

        try (InputStream stream = fileStorage.retrieve(fileLocation);
             CSVParser parser = CsvImportParser.open(stream)) {

            for (CSVRecord record : parser) {
                int rowNum = (int) record.getRecordNumber();
                processRecord(job, record, rowNum, batch);

                if (batch.size() >= importProperties.getBatchSize()) {
                    flushBatch(job, batch);
                }
            }

            if (!batch.isEmpty()) {
                flushBatch(job, batch);
            }
        }

        job.complete();
        importJobRepository.save(job);
    }

    private void processRecord(ImportJob job, CSVRecord record, int rowNum, List<Transaction> batch) {
        try {
            ParsedCsvRow row = CsvImportParser.toRow(record, rowNum);
            batch.add(toTransaction(row));
        } catch (IllegalArgumentException e) {
            log.debug("Skipping row {}: {}", rowNum, e.getMessage());
            job.addError(new ImportError(rowNum, e.getMessage()));
        }
    }

    private void flushBatch(ImportJob job, List<Transaction> batch) {
        transactionRepository.saveAll(batch);
        job.recordProcessedBatch(batch.size());
        importJobRepository.save(job);
        batch.clear();
    }

    private Transaction toTransaction(ParsedCsvRow row) {
        return new Transaction(
                UUID.randomUUID().toString(),
                new IBAN(row.iban()),
                Money.of(row.amount(), row.currency()),
                TransactionDate.of(row.date()),
                new Category(row.category()),
                row.description()
        );
    }
}
