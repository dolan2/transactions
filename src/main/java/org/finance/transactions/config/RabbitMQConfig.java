package org.finance.transactions.config;

import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class RabbitMQConfig {

    private final RabbitMQProperties props;

    @Bean
    DirectExchange transactionsExchange() {
        return new DirectExchange(props.getExchange(), true, false);
    }

    @Bean
    Queue importQueue() {
        return QueueBuilder.durable(props.getQueue())
                .withArgument("x-dead-letter-exchange", "")
                .withArgument("x-dead-letter-routing-key", props.getDlq())
                .build();
    }

    @Bean
    Queue deadLetterQueue() {
        return QueueBuilder.durable(props.getDlq()).build();
    }

    @Bean
    Binding importBinding(Queue importQueue, DirectExchange transactionsExchange) {
        return BindingBuilder.bind(importQueue).to(transactionsExchange).with(props.getRoutingKey());
    }

    @Bean
    Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, Jackson2JsonMessageConverter converter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(converter);
        return template;
    }
}
