package org.finance.transactions.adapter.in.messaging;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.finance.transactions.application.dto.ImportMessage;
import org.finance.transactions.application.usecase.CsvImportProcessorUseCase;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RabbitMQImportConsumer {

    private final CsvImportProcessorUseCase csvImportProcessorUseCase;

    @RabbitListener(queues = "${app.rabbitmq.queue}")
    public void onMessage(ImportMessage message) {
        log.info("Received import message for job {}", message.importId());
        try {
            csvImportProcessorUseCase.process(message.importId(), message.fileLocation());
        } catch (Exception e) {
            // Swallow to prevent infinite requeue; DLQ handles dead messages.
            log.error("Unrecoverable error for import {}: {}", message.importId(), e.getMessage(), e);
        }
    }
}
