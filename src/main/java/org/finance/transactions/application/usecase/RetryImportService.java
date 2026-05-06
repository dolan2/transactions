package org.finance.transactions.application.usecase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.finance.transactions.domain.model.ImportJob;
import org.finance.transactions.domain.port.out.ImportPublisher;
import org.finance.transactions.domain.repository.ImportJobRepository;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;

@Slf4j
@Service
@RequiredArgsConstructor
public class RetryImportService implements RetryImportUseCase {

    private final ImportJobRepository importJobRepository;
    private final ImportPublisher importPublisher;

    @Override
    public ImportJob retry(String importId) {
        ImportJob job = importJobRepository.findById(importId)
                .orElseThrow(() -> new NoSuchElementException("ImportJob not found: " + importId));

        job.retry();
        ImportJob saved = importJobRepository.save(job);
        importPublisher.publish(saved.getId(), saved.getFileLocation());

        log.info("Retrying import {}, re-published to queue from {}", importId, saved.getFileLocation());
        return saved;
    }
}
