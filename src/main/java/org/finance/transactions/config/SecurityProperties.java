package org.finance.transactions.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.security")
@Getter
@Setter
public class SecurityProperties {

    private String apiKey = "change-me-in-production";
    private boolean enabled = true;
}
