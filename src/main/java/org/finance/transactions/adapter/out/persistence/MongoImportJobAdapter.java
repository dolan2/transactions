package org.finance.transactions.adapter.out.persistence;

import lombok.RequiredArgsConstructor;
import org.finance.transactions.adapter.out.persistence.document.ImportJobDocument;
import org.finance.transactions.adapter.out.persistence.repository.SpringImportJobRepository;
import org.finance.transactions.domain.model.ImportJob;
import org.finance.transactions.domain.model.ImportJobStatus;
import org.finance.transactions.domain.repository.ImportJobRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class MongoImportJobAdapter implements ImportJobRepository {

    private final SpringImportJobRepository springRepo;

    @Override
    public ImportJob save(ImportJob job) {
        return toDomain(springRepo.save(toDocument(job)));
    }

    @Override
    public Optional<ImportJob> findById(String id) {
        return springRepo.findById(id).map(this::toDomain);
    }

    private ImportJobDocument toDocument(ImportJob job) {
        return new ImportJobDocument(job.getId(), job.getFileName(), job.getStatus().name(),
                job.getTotalRows(), job.getProcessedRows(), job.getErrorRows(), job.getCreatedAt());
    }

    private ImportJob toDomain(ImportJobDocument doc) {
        return new ImportJob(doc.getId(), doc.getFileName(), ImportJobStatus.valueOf(doc.getStatus()),
                doc.getTotalRows(), doc.getProcessedRows(), doc.getErrorRows(), doc.getCreatedAt());
    }
}
