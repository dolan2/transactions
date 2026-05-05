package org.finance.transactions.application.usecase;

import lombok.RequiredArgsConstructor;
import org.finance.transactions.domain.model.ImportJob;
import org.finance.transactions.domain.port.out.ImportPublisher;
import org.finance.transactions.domain.repository.ImportJobRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ImportTransactionsService implements ImportTransactionsUseCase {

    private final ImportJobRepository importJobRepository;
    private final ImportPublisher importPublisher;

    @Override
    public ImportJob initiateImport(String fileName, String fileLocation) {
        ImportJob job = new ImportJob(UUID.randomUUID().toString(), fileName);
        ImportJob saved = importJobRepository.save(job);
        importPublisher.publish(saved.getId(), fileLocation);
        return saved;
    }
}
