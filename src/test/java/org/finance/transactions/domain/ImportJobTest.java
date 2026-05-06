package org.finance.transactions.domain;

import org.finance.transactions.domain.model.ImportError;
import org.finance.transactions.domain.model.ImportJob;
import org.finance.transactions.domain.model.ImportJobStatus;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class ImportJobTest {

    private static final String ID = "job-1";
    private static final String FILE_NAME = "transactions.csv";
    private static final String FILE_LOCATION = "/uploads/uuid-transactions.csv";

    @Test
    void newJobStartsInPendingStatus() {
        var job = new ImportJob(ID, FILE_NAME, FILE_LOCATION);

        assertThat(job.getStatus()).isEqualTo(ImportJobStatus.PENDING);
        assertThat(job.getProcessedRows()).isZero();
        assertThat(job.getErrorRows()).isZero();
        assertThat(job.getTotalRows()).isZero();
        assertThat(job.getErrors()).isEmpty();
        assertThat(job.getCreatedAt()).isNotNull();
        assertThat(job.getFileLocation()).isEqualTo(FILE_LOCATION);
    }

    @Test
    void startProcessingTransitionsFromPendingToProcessing() {
        var job = new ImportJob(ID, FILE_NAME, FILE_LOCATION);

        job.startProcessing();

        assertThat(job.getStatus()).isEqualTo(ImportJobStatus.PROCESSING);
    }

    @Test
    void startProcessingThrowsIfNotPending() {
        var job = new ImportJob(ID, FILE_NAME, FILE_LOCATION);
        job.startProcessing();

        assertThatThrownBy(job::startProcessing)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("PROCESSING");
    }

    @Test
    void completeWithNoErrorsTransitionsToCompleted() {
        var job = new ImportJob(ID, FILE_NAME, FILE_LOCATION);
        job.startProcessing();
        job.recordProcessedBatch(100);
        job.complete();

        assertThat(job.getStatus()).isEqualTo(ImportJobStatus.COMPLETED);
        assertThat(job.getTotalRows()).isEqualTo(100);
        assertThat(job.getProcessedRows()).isEqualTo(100);
    }

    @Test
    void completeWithErrorsTransitionsToCompletedWithErrors() {
        var job = new ImportJob(ID, FILE_NAME, FILE_LOCATION);
        job.startProcessing();
        job.recordProcessedBatch(95);
        job.addError(new ImportError(3, "Invalid IBAN"));
        job.addError(new ImportError(7, "Missing currency"));
        job.complete();

        assertThat(job.getStatus()).isEqualTo(ImportJobStatus.COMPLETED_WITH_ERRORS);
        assertThat(job.getTotalRows()).isEqualTo(97);
        assertThat(job.getErrorRows()).isEqualTo(2);
        assertThat(job.getErrors()).hasSize(2);
    }

    @Test
    void failTransitionsToFailed() {
        var job = new ImportJob(ID, FILE_NAME, FILE_LOCATION);
        job.startProcessing();
        job.fail();

        assertThat(job.getStatus()).isEqualTo(ImportJobStatus.FAILED);
        assertThat(job.isFinished()).isTrue();
    }

    @Test
    void retryResetFailedJobToPending() {
        var job = new ImportJob(ID, FILE_NAME, FILE_LOCATION);
        job.startProcessing();
        job.recordProcessedBatch(50);
        job.addError(new ImportError(1, "bad row"));
        job.fail();

        job.retry();

        assertThat(job.getStatus()).isEqualTo(ImportJobStatus.PENDING);
        assertThat(job.getProcessedRows()).isZero();
        assertThat(job.getErrorRows()).isZero();
        assertThat(job.getTotalRows()).isZero();
        assertThat(job.getErrors()).isEmpty();
    }

    @Test
    void retryThrowsIfJobIsNotFailed() {
        var job = new ImportJob(ID, FILE_NAME, FILE_LOCATION);

        assertThatThrownBy(job::retry)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("FAILED");
    }

    @Test
    void retryThrowsIfJobIsCompleted() {
        var job = new ImportJob(ID, FILE_NAME, FILE_LOCATION);
        job.startProcessing();
        job.complete();

        assertThatThrownBy(job::retry)
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void addErrorStoresUpToMaxErrors() {
        var job = new ImportJob(ID, FILE_NAME, FILE_LOCATION);
        job.startProcessing();

        for (int i = 0; i < 105; i++) {
            job.addError(new ImportError(i, "error " + i));
        }

        assertThat(job.getErrorRows()).isEqualTo(105);
        assertThat(job.getErrors()).hasSize(100);
    }

    @Test
    void isRetryableOnlyWhenFailed() {
        var job = new ImportJob(ID, FILE_NAME, FILE_LOCATION);
        assertThat(job.isRetryable()).isFalse();

        job.startProcessing();
        assertThat(job.isRetryable()).isFalse();

        job.fail();
        assertThat(job.isRetryable()).isTrue();
    }

    @Test
    void recordProcessedBatchAccumulatesCount() {
        var job = new ImportJob(ID, FILE_NAME, FILE_LOCATION);
        job.startProcessing();

        job.recordProcessedBatch(500);
        job.recordProcessedBatch(500);
        job.recordProcessedBatch(300);

        assertThat(job.getProcessedRows()).isEqualTo(1300);
    }
}
