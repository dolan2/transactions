package org.finance.transactions.adapter.out.persistence;

import lombok.RequiredArgsConstructor;
import org.finance.transactions.adapter.out.persistence.document.TransactionDocument;
import org.finance.transactions.adapter.out.persistence.repository.SpringTransactionRepository;
import org.finance.transactions.domain.model.Transaction;
import org.finance.transactions.domain.repository.TransactionRepository;
import org.finance.transactions.domain.valueobject.*;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class MongoTransactionAdapter implements TransactionRepository {

    private final SpringTransactionRepository springRepo;

    @Override
    public Transaction save(Transaction transaction) {
        return toDomain(springRepo.save(toDocument(transaction)));
    }

    @Override
    public List<Transaction> saveAll(List<Transaction> transactions) {
        return springRepo.saveAll(transactions.stream().map(this::toDocument).toList())
                .stream().map(this::toDomain).toList();
    }

    private TransactionDocument toDocument(Transaction t) {
        String id = t.getId() != null ? t.getId() : UUID.randomUUID().toString();
        return new TransactionDocument(id, t.getIban().value(), t.getAmount().amount(),
                t.getAmount().currency(), t.getDate().value(), t.getCategory().value(), t.getDescription());
    }

    private Transaction toDomain(TransactionDocument doc) {
        return new Transaction(
                doc.getId(),
                new IBAN(doc.getIban()),
                new Money(doc.getAmount(), doc.getCurrency()),
                new TransactionDate(doc.getDate()),
                new Category(doc.getCategory()),
                doc.getDescription()
        );
    }
}
