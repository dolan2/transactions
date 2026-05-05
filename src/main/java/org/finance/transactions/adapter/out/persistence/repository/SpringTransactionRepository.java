package org.finance.transactions.adapter.out.persistence.repository;

import org.finance.transactions.adapter.out.persistence.document.TransactionDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.LocalDate;
import java.util.List;

public interface SpringTransactionRepository extends MongoRepository<TransactionDocument, String> {
    List<TransactionDocument> findByIban(String iban);
    List<TransactionDocument> findByCategory(String category);
    List<TransactionDocument> findByDateBetween(LocalDate from, LocalDate to);
}
