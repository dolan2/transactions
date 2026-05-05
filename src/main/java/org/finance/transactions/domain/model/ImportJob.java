package org.finance.transactions.domain.model;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class ImportJob {

    private final String id;
    private final String fileName;
    private ImportJobStatus status;
    private int totalRows;
    private int processedRows;
    private int errorRows;
    private final LocalDateTime createdAt;

    public ImportJob(String id, String fileName) {
        this.id = id;
        this.fileName = fileName;
        this.status = ImportJobStatus.PENDING;
        this.createdAt = LocalDateTime.now();
    }

    public ImportJob(String id, String fileName, ImportJobStatus status,
                     int totalRows, int processedRows, int errorRows, LocalDateTime createdAt) {
        this.id = id;
        this.fileName = fileName;
        this.status = status;
        this.totalRows = totalRows;
        this.processedRows = processedRows;
        this.errorRows = errorRows;
        this.createdAt = createdAt;
    }

    public void startProcessing(int totalRows) {
        this.status = ImportJobStatus.PROCESSING;
        this.totalRows = totalRows;
    }

    public void recordProcessed() { this.processedRows++; }

    public void recordError() { this.errorRows++; }

    public void complete() { this.status = ImportJobStatus.COMPLETED; }

    public void fail() { this.status = ImportJobStatus.FAILED; }

    public boolean isFinished() {
        return status == ImportJobStatus.COMPLETED || status == ImportJobStatus.FAILED;
    }
}
