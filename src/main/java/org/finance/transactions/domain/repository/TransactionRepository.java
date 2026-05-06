package org.finance.transactions.domain.repository;

import org.finance.transactions.domain.model.Transaction;

import java.util.List;

public interface TransactionRepository {
    Transaction save(Transaction transaction);
    List<Transaction> saveAll(List<Transaction> transactions);
}
