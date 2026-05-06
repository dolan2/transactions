package org.finance.transactions.adapter.out.persistence.document;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Document(collection = "import_jobs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ImportJobDocument {

    @Id
    private String id;
    private String fileName;
    private String fileLocation;
    private String status;
    private int totalRows;
    private int processedRows;
    private int errorRows;
    private LocalDateTime createdAt;
    private List<ErrorDoc> errors = new ArrayList<>();

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ErrorDoc {
        private int rowNumber;
        private String reason;
    }
}
