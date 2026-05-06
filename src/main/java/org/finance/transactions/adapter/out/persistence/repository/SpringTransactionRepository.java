package org.finance.transactions.adapter.out.persistence.repository;

import org.finance.transactions.adapter.out.persistence.document.TransactionDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface SpringTransactionRepository extends MongoRepository<TransactionDocument, String> {
}
