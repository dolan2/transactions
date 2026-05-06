package org.finance.transactions.application.service;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.finance.transactions.application.dto.ParsedCsvRow;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public final class CsvImportParser {

    private static final CSVFormat FORMAT = CSVFormat.Builder.create(CSVFormat.DEFAULT)
            .setHeader()
            .setSkipHeaderRecord(true)
            .setIgnoreHeaderCase(true)
            .setIgnoreEmptyLines(true)
            .setTrim(true)
            .setIgnoreSurroundingSpaces(true)
            .build();

    private CsvImportParser() {}

    public static CSVParser open(InputStream input) throws IOException {
        return FORMAT.parse(new InputStreamReader(input, StandardCharsets.UTF_8));
    }

    public static ParsedCsvRow toRow(CSVRecord record, int rowNumber) {
        return new ParsedCsvRow(
                rowNumber,
                require(record, "iban"),
                require(record, "date"),
                require(record, "amount"),
                require(record, "currency"),
                require(record, "category"),
                optional(record, "description")
        );
    }

    private static String require(CSVRecord record, String column) {
        String value = record.get(column);
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Missing required column: " + column);
        }
        return value;
    }

    private static String optional(CSVRecord record, String column) {
        try {
            String value = record.get(column);
            return value != null ? value : "";
        } catch (IllegalArgumentException e) {
            return "";
        }
    }
}
