package org.finance.transactions.adapter.out.persistence;

import lombok.RequiredArgsConstructor;
import org.finance.transactions.adapter.out.persistence.document.ImportJobDocument;
import org.finance.transactions.adapter.out.persistence.repository.SpringImportJobRepository;
import org.finance.transactions.domain.model.ImportError;
import org.finance.transactions.domain.model.ImportJob;
import org.finance.transactions.domain.model.ImportJobStatus;
import org.finance.transactions.domain.repository.ImportJobRepository;
import org.springframework.stereotype.Component;

import java.util.List;
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
        List<ImportJobDocument.ErrorDoc> errors = job.getErrors().stream()
                .map(e -> new ImportJobDocument.ErrorDoc(e.rowNumber(), e.reason()))
                .toList();
        return new ImportJobDocument(
                job.getId(), job.getFileName(), job.getFileLocation(),
                job.getStatus().name(), job.getTotalRows(), job.getProcessedRows(),
                job.getErrorRows(), job.getCreatedAt(), errors);
    }

    private ImportJob toDomain(ImportJobDocument doc) {
        List<ImportError> errors = doc.getErrors().stream()
                .map(e -> new ImportError(e.getRowNumber(), e.getReason()))
                .toList();
        return new ImportJob(
                doc.getId(), doc.getFileName(), doc.getFileLocation(),
                ImportJobStatus.valueOf(doc.getStatus()),
                doc.getTotalRows(), doc.getProcessedRows(), doc.getErrorRows(),
                doc.getCreatedAt(), errors);
    }
}
