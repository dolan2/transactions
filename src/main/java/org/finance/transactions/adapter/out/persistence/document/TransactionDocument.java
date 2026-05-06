package org.finance.transactions.adapter.out.persistence.document;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDate;

@Document(collection = "transactions")
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class TransactionDocument {

    @Id
    private String id;
    private String iban;
    private BigDecimal amount;
    private String currency;
    private LocalDate date;
    private String category;
    private String description;
}
