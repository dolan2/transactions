package org.finance.transactions.adapter.in.rest;

import org.finance.transactions.TestcontainersConfiguration;
import org.finance.transactions.adapter.out.persistence.document.TransactionDocument;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
@TestPropertySource(properties = {
        "app.security.api-key=test-api-key",
        "app.security.enabled=true"
})
class StatisticsControllerIT {

    private static final String API_KEY = "test-api-key";

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private MongoTemplate mongoTemplate;

    private MockMvc mockMvc;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();

        mongoTemplate.dropCollection("transactions");
        insertTransaction("1", "DE89370400440532013000", new BigDecimal("100.00"), "EUR",
                LocalDate.of(2024, 3, 15), "groceries");
        insertTransaction("2", "DE89370400440532013000", new BigDecimal("50.00"), "EUR",
                LocalDate.of(2024, 3, 20), "groceries");
        insertTransaction("3", "GB29NWBK60161331926819", new BigDecimal("200.00"), "GBP",
                LocalDate.of(2024, 3, 10), "transport");
        insertTransaction("4", "DE89370400440532013000", new BigDecimal("75.00"), "EUR",
                LocalDate.of(2024, 4, 5), "entertainment");
    }

    @Test
    void statisticsRequiresApiKey() throws Exception {
        mockMvc.perform(get("/transactions/v1/statistics"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void statisticsReturnsPaginatedResults() throws Exception {
        mockMvc.perform(get("/transactions/v1/statistics")
                        .header("X-API-Key", API_KEY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.number").value(0))
                .andExpect(jsonPath("$.size").value(20))
                .andExpect(jsonPath("$.totalElements").isNumber())
                .andExpect(jsonPath("$.totalPages").isNumber());
    }

    @Test
    void filterByCategoryReturnsMatchingEntries() throws Exception {
        mockMvc.perform(get("/transactions/v1/statistics")
                        .param("category", "groceries")
                        .header("X-API-Key", API_KEY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].groupKey").value("groceries"))
                .andExpect(jsonPath("$.content[0].transactionCount").value(2));
    }

    @Test
    void filterByMonthReturnsOnlyMatchingTransactions() throws Exception {
        mockMvc.perform(get("/transactions/v1/statistics")
                        .param("month", "2024-03")
                        .header("X-API-Key", API_KEY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(2));
    }

    @Test
    void paginationPageSizeIsRespected() throws Exception {
        mockMvc.perform(get("/transactions/v1/statistics")
                        .param("page", "0")
                        .param("size", "1")
                        .header("X-API-Key", API_KEY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.size").value(1));
    }

    private void insertTransaction(String id, String iban, BigDecimal amount, String currency,
                                   LocalDate date, String category) {
        mongoTemplate.save(new TransactionDocument(id, iban, amount, currency, date, category, ""), "transactions");
    }
}
