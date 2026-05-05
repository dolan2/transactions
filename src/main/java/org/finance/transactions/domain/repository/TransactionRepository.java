package org.finance.transactions.domain.repository;

import org.finance.transactions.domain.model.Transaction;
import org.finance.transactions.domain.valueobject.Category;
import org.finance.transactions.domain.valueobject.IBAN;

import java.time.YearMonth;
import java.util.List;

public interface TransactionRepository {
    Transaction save(Transaction transaction);
    List<Transaction> saveAll(List<Transaction> transactions);
    List<Transaction> findByIban(IBAN iban);
    List<Transaction> findByCategory(Category category);
    List<Transaction> findByMonth(YearMonth month);
}
