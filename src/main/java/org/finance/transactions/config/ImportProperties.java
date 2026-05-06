package org.finance.transactions.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.import")
@Getter
@Setter
public class ImportProperties {
    private String uploadDir = "./uploads";
    private int batchSize = 500;
}
