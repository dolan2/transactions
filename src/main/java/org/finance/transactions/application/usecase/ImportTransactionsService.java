package org.finance.transactions.application.usecase;

import lombok.RequiredArgsConstructor;
import org.finance.transactions.domain.model.ImportJob;
import org.finance.transactions.domain.port.out.FileStorage;
import org.finance.transactions.domain.port.out.ImportPublisher;
import org.finance.transactions.domain.repository.ImportJobRepository;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ImportTransactionsService implements ImportTransactionsUseCase {

    private final ImportJobRepository importJobRepository;
    private final ImportPublisher importPublisher;
    private final FileStorage fileStorage;

    @Override
    public ImportJob initiateImport(String fileName, InputStream content) throws IOException {
        String location = fileStorage.store(fileName, content);
        ImportJob job = new ImportJob(UUID.randomUUID().toString(), fileName, location);
        ImportJob saved = importJobRepository.save(job);
        importPublisher.publish(saved.getId(), location);
        return saved;
    }
}
