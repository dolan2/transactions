package org.finance.transactions.domain.port.out;

public interface ImportPublisher {
    void publish(String importId, String fileLocation);
}
