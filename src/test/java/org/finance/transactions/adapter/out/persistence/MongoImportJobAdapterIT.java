package org.finance.transactions.adapter.out.persistence;

import org.finance.transactions.TestcontainersConfiguration;
import org.finance.transactions.domain.model.ImportError;
import org.finance.transactions.domain.model.ImportJob;
import org.finance.transactions.domain.model.ImportJobStatus;
import org.finance.transactions.domain.repository.ImportJobRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.context.TestPropertySource;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
@TestPropertySource(properties = "app.security.api-key=test-api-key")
class MongoImportJobAdapterIT {

    @Autowired
    private ImportJobRepository importJobRepository;

    @Autowired
    private MongoTemplate mongoTemplate;

    @BeforeEach
    void setUp() {
        mongoTemplate.dropCollection("import_jobs");
    }

    @Test
    void savedJobCanBeFoundById() {
        var job = new ImportJob("job-1", "file.csv", "/uploads/uuid-file.csv");

        importJobRepository.save(job);

        Optional<ImportJob> found = importJobRepository.findById("job-1");
        assertThat(found).isPresent();
        assertThat(found.get().getFileName()).isEqualTo("file.csv");
        assertThat(found.get().getFileLocation()).isEqualTo("/uploads/uuid-file.csv");
        assertThat(found.get().getStatus()).isEqualTo(ImportJobStatus.PENDING);
    }

    @Test
    void statusTransitionsArePersistedCorrectly() {
        var job = new ImportJob("job-2", "file.csv", "/uploads/uuid-file.csv");
        importJobRepository.save(job);

        job.startProcessing();
        importJobRepository.save(job);

        var loaded = importJobRepository.findById("job-2").orElseThrow();
        assertThat(loaded.getStatus()).isEqualTo(ImportJobStatus.PROCESSING);
    }

    @Test
    void errorsArePersistedAndReconstituted() {
        var job = new ImportJob("job-3", "file.csv", "/uploads/uuid-file.csv");
        job.startProcessing();
        job.addError(new ImportError(5, "Invalid IBAN format"));
        job.addError(new ImportError(12, "Missing currency"));
        job.complete();
        importJobRepository.save(job);

        var loaded = importJobRepository.findById("job-3").orElseThrow();
        assertThat(loaded.getStatus()).isEqualTo(ImportJobStatus.COMPLETED_WITH_ERRORS);
        assertThat(loaded.getErrors()).hasSize(2);
        assertThat(loaded.getErrors().get(0).rowNumber()).isEqualTo(5);
        assertThat(loaded.getErrors().get(0).reason()).isEqualTo("Invalid IBAN format");
        assertThat(loaded.getErrorRows()).isEqualTo(2);
    }

    @Test
    void completedJobHasCorrectRowCounts() {
        var job = new ImportJob("job-4", "file.csv", "/uploads/uuid-file.csv");
        job.startProcessing();
        job.recordProcessedBatch(500);
        job.recordProcessedBatch(400);
        job.addError(new ImportError(901, "Bad row"));
        job.complete();
        importJobRepository.save(job);

        var loaded = importJobRepository.findById("job-4").orElseThrow();
        assertThat(loaded.getProcessedRows()).isEqualTo(900);
        assertThat(loaded.getErrorRows()).isEqualTo(1);
        assertThat(loaded.getTotalRows()).isEqualTo(901);
    }

    @Test
    void findByIdReturnsEmptyForUnknownId() {
        assertThat(importJobRepository.findById("does-not-exist")).isEmpty();
    }
}
