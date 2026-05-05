package org.finance.transactions.adapter.out.persistence.repository;

import org.finance.transactions.adapter.out.persistence.document.ImportJobDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface SpringImportJobRepository extends MongoRepository<ImportJobDocument, String> {}
