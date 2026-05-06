package org.finance.transactions.adapter.out.filesystem;

import org.finance.transactions.config.ImportProperties;
import org.finance.transactions.domain.port.out.FileStorage;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

@Component
public class LocalFileStorage implements FileStorage {

    private final Path uploadDir;

    public LocalFileStorage(ImportProperties props) throws IOException {
        this.uploadDir = Path.of(props.getUploadDir());
        Files.createDirectories(uploadDir);
    }

    @Override
    public String store(String filename, InputStream content) throws IOException {
        String uniqueName = UUID.randomUUID() + "_" + filename;
        Path target = uploadDir.resolve(uniqueName);
        Files.copy(content, target);
        return target.toAbsolutePath().toString();
    }

    @Override
    public InputStream retrieve(String location) throws IOException {
        return Files.newInputStream(Path.of(location));
    }
}
