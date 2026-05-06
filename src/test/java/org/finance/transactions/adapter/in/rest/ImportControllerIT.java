package org.finance.transactions.adapter.in.rest;

import org.finance.transactions.TestcontainersConfiguration;
import org.finance.transactions.domain.model.ImportJobStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
@TestPropertySource(properties = {
        "app.security.api-key=test-api-key",
        "app.security.enabled=true",
        "app.import.upload-dir=./test-uploads"
})
class ImportControllerIT {

    private static final String BASE = "/transactions/v1/imports";
    private static final String API_KEY = "test-api-key";
    private static final String VALID_CSV =
            "iban,date,amount,currency,category,description\n" +
            "DE89370400440532013000,2024-03-15,100.50,EUR,groceries,Supermarket\n" +
            "DE89370400440532013000,2024-03-16,-45.00,EUR,transport,Bus ticket\n";

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();
    }

    @Test
    void uploadRequiresApiKey() throws Exception {
        mockMvc.perform(multipart(BASE).file(csvFile("test.csv", VALID_CSV)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void uploadReturns202WithImportId() throws Exception {
        mockMvc.perform(multipart(BASE).file(csvFile("transactions.csv", VALID_CSV))
                        .header("X-API-Key", API_KEY))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.importId").isNotEmpty());
    }

    @Test
    void getStatusReturns404ForUnknownId() throws Exception {
        mockMvc.perform(get(BASE + "/non-existent-id")
                        .header("X-API-Key", API_KEY))
                .andExpect(status().isNotFound());
    }

    @Test
    void getStatusReturnsImportJobAfterUpload() throws Exception {
        MvcResult uploadResult = mockMvc.perform(multipart(BASE).file(csvFile("status-test.csv", VALID_CSV))
                        .header("X-API-Key", API_KEY))
                .andExpect(status().isAccepted())
                .andReturn();

        String importId = extractImportId(uploadResult.getResponse().getContentAsString());

        mockMvc.perform(get(BASE + "/{id}", importId)
                        .header("X-API-Key", API_KEY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.importId").value(importId))
                .andExpect(jsonPath("$.fileName").value("status-test.csv"))
                .andExpect(jsonPath("$.status").value(anyOf(
                        equalTo(ImportJobStatus.PENDING.name()),
                        equalTo(ImportJobStatus.PROCESSING.name()),
                        equalTo(ImportJobStatus.COMPLETED.name())
                )));
    }

    @Test
    void retryNonExistentJobReturns404() throws Exception {
        mockMvc.perform(post(BASE + "/does-not-exist/retry")
                        .header("X-API-Key", API_KEY))
                .andExpect(status().isNotFound());
    }

    @Test
    void retryPendingJobReturnsConflict() throws Exception {
        MvcResult uploadResult = mockMvc.perform(multipart(BASE).file(csvFile("retry-test.csv", VALID_CSV))
                        .header("X-API-Key", API_KEY))
                .andExpect(status().isAccepted())
                .andReturn();

        String importId = extractImportId(uploadResult.getResponse().getContentAsString());

        mockMvc.perform(post(BASE + "/{id}/retry", importId)
                        .header("X-API-Key", API_KEY))
                .andExpect(status().isConflict());
    }

    private MockMultipartFile csvFile(String fileName, String content) {
        return new MockMultipartFile("file", fileName, MediaType.TEXT_PLAIN_VALUE, content.getBytes());
    }

    private String extractImportId(String json) {
        int start = json.indexOf("\"importId\":\"") + 12;
        int end = json.indexOf("\"", start);
        return json.substring(start, end);
    }
}
