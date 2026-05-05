package org.finance.transactions.application.usecase;

import lombok.RequiredArgsConstructor;
import org.finance.transactions.domain.model.ImportJob;
import org.finance.transactions.domain.repository.ImportJobRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class GetImportStatusService implements GetImportStatusUseCase {

    private final ImportJobRepository importJobRepository;

    @Override
    public Optional<ImportJob> getStatus(String importId) {
        return importJobRepository.findById(importId);
    }
}
