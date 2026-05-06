package org.finance.transactions.domain.port.out;

import java.io.IOException;
import java.io.InputStream;

public interface FileStorage {
    String store(String filename, InputStream content) throws IOException;
    InputStream retrieve(String location) throws IOException;
}
