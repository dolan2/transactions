package org.finance.transactions.adapter.in.rest;

import lombok.RequiredArgsConstructor;
import org.finance.transactions.adapter.in.rest.dto.ImportResponse;
import org.finance.transactions.adapter.in.rest.dto.ImportStatusResponse;
import org.finance.transactions.application.usecase.GetImportStatusUseCase;
import org.finance.transactions.application.usecase.ImportTransactionsUseCase;
import org.finance.transactions.domain.model.ImportJob;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/imports")
@RequiredArgsConstructor
public class ImportController {

    private final ImportTransactionsUseCase importTransactionsUseCase;
    private final GetImportStatusUseCase getImportStatusUseCase;

    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<ImportResponse> upload(@RequestParam("file") MultipartFile file) {
        ImportJob job = importTransactionsUseCase.initiateImport(
                file.getOriginalFilename(),
                file.getOriginalFilename()
        );
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(new ImportResponse(job.getId()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ImportStatusResponse> getStatus(@PathVariable String id) {
        return getImportStatusUseCase.getStatus(id)
                .map(this::toResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    private ImportStatusResponse toResponse(ImportJob job) {
        return new ImportStatusResponse(
                job.getId(), job.getFileName(), job.getStatus().name(),
                job.getTotalRows(), job.getProcessedRows(), job.getErrorRows()
        );
    }
}
