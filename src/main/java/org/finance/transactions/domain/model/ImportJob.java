package org.finance.transactions.domain.model;

import lombok.Getter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
public class ImportJob {

    private static final int MAX_STORED_ERRORS = 100;

    private final String id;
    private final String fileName;
    private final String fileLocation;
    private ImportJobStatus status;
    private int processedRows;
    private int errorRows;
    private int totalRows;
    private final LocalDateTime createdAt;
    private final List<ImportError> errors;

    public ImportJob(String id, String fileName, String fileLocation) {
        this.id = id;
        this.fileName = fileName;
        this.fileLocation = fileLocation;
        this.status = ImportJobStatus.PENDING;
        this.createdAt = LocalDateTime.now();
        this.errors = new ArrayList<>();
    }

    public ImportJob(String id, String fileName, String fileLocation, ImportJobStatus status,
                     int totalRows, int processedRows, int errorRows,
                     LocalDateTime createdAt, List<ImportError> errors) {
        this.id = id;
        this.fileName = fileName;
        this.fileLocation = fileLocation;
        this.status = status;
        this.totalRows = totalRows;
        this.processedRows = processedRows;
        this.errorRows = errorRows;
        this.createdAt = createdAt;
        this.errors = new ArrayList<>(errors);
    }

    public void startProcessing() {
        if (status != ImportJobStatus.PENDING) {
            throw new IllegalStateException("Cannot start processing from status: " + status);
        }
        this.status = ImportJobStatus.PROCESSING;
    }

    public void recordProcessedBatch(int count) {
        this.processedRows += count;
    }

    public void addError(ImportError error) {
        this.errorRows++;
        if (errors.size() < MAX_STORED_ERRORS) {
            errors.add(error);
        }
    }

    public void complete() {
        this.totalRows = this.processedRows + this.errorRows;
        this.status = this.errorRows > 0 ? ImportJobStatus.COMPLETED_WITH_ERRORS : ImportJobStatus.COMPLETED;
    }

    public void fail() {
        this.status = ImportJobStatus.FAILED;
    }

    public void retry() {
        if (status != ImportJobStatus.FAILED) {
            throw new IllegalStateException("Only FAILED jobs can be retried, current status: " + status);
        }
        this.status = ImportJobStatus.PENDING;
        this.processedRows = 0;
        this.errorRows = 0;
        this.totalRows = 0;
        this.errors.clear();
    }

    public boolean isFinished() {
        return status == ImportJobStatus.COMPLETED
                || status == ImportJobStatus.COMPLETED_WITH_ERRORS
                || status == ImportJobStatus.FAILED;
    }

    public boolean isRetryable() {
        return status == ImportJobStatus.FAILED;
    }
}
