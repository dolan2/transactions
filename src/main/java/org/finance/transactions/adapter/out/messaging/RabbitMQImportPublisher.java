package org.finance.transactions.adapter.out.messaging;

import lombok.RequiredArgsConstructor;
import org.finance.transactions.application.dto.ImportMessage;
import org.finance.transactions.config.RabbitMQProperties;
import org.finance.transactions.domain.port.out.ImportPublisher;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RabbitMQImportPublisher implements ImportPublisher {

    private final RabbitTemplate rabbitTemplate;
    private final RabbitMQProperties props;

    @Override
    public void publish(String importId, String fileLocation) {
        rabbitTemplate.convertAndSend(props.getExchange(), props.getRoutingKey(),
                new ImportMessage(importId, fileLocation));
    }
}
