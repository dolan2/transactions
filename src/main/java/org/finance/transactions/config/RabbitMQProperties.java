package org.finance.transactions.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.rabbitmq")
@Getter
@Setter
public class RabbitMQProperties {
    private String exchange;
    private String queue;
    private String routingKey;
    private String dlq = "transaction-import.dlq";
}
