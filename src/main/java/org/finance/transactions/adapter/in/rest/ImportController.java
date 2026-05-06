package org.finance.transactions.adapter.in.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.finance.transactions.adapter.in.rest.dto.ImportResponse;
import org.finance.transactions.adapter.in.rest.dto.ImportStatusResponse;
import org.finance.transactions.adapter.in.rest.mapper.ImportJobMapper;
import org.finance.transactions.application.usecase.GetImportStatusUseCase;
import org.finance.transactions.application.usecase.ImportTransactionsUseCase;
import org.finance.transactions.application.usecase.RetryImportUseCase;
import org.finance.transactions.domain.model.ImportJob;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;

@Tag(name = "Imports", description = "CSV file import lifecycle management")
@RestController
@RequestMapping("/transactions/v1/imports")
@RequiredArgsConstructor
public class ImportController {

    private final ImportTransactionsUseCase importTransactionsUseCase;
    private final GetImportStatusUseCase getImportStatusUseCase;
    private final RetryImportUseCase retryImportUseCase;
    private final ImportJobMapper importJobMapper;

    @Operation(summary = "Upload a CSV file for async processing",
               description = "Stores the file and enqueues it for processing. Returns 202 Accepted immediately.")
    @ApiResponse(responseCode = "202", description = "Import accepted, use the importId to poll for status")
    @ApiResponse(responseCode = "400", description = "Invalid or empty file")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ImportResponse> upload(@RequestParam("file") MultipartFile file) {
        try {
            ImportJob job = importTransactionsUseCase.initiateImport(
                    file.getOriginalFilename(), file.getInputStream());
            return ResponseEntity.status(HttpStatus.ACCEPTED).body(new ImportResponse(job.getId()));
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to read uploaded file");
        }
    }

    @Operation(summary = "Get import status", description = "Returns current status and error details for an import job")
    @ApiResponse(responseCode = "200", description = "Import found")
    @ApiResponse(responseCode = "404", description = "Import not found")
    @GetMapping("/{id}")
    public ResponseEntity<ImportStatusResponse> getStatus(@PathVariable String id) {
        return getImportStatusUseCase.getStatus(id)
                .map(importJobMapper::toResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Retry a failed import",
               description = "Resets a FAILED import job to PENDING and re-queues it for processing. " +
                             "The original file is re-read from storage.")
    @ApiResponse(responseCode = "200", description = "Retry accepted")
    @ApiResponse(responseCode = "404", description = "Import not found")
    @ApiResponse(responseCode = "409", description = "Import is not in FAILED status")
    @PostMapping("/{id}/retry")
    public ResponseEntity<ImportStatusResponse> retry(@PathVariable String id) {
        ImportJob job = retryImportUseCase.retry(id);
        return ResponseEntity.ok(importJobMapper.toResponse(job));
    }
}
