package com.uuriturg.alert.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Value("${messaging.exchange.listings}")
    private String listingExchangeName;

    @Value("${messaging.queues.alert-listing}")
    private String alertListingQueueName;

    @Value("${messaging.routing-keys.listing-new}")
    private String listingNewRoutingKey;

    @Bean
    TopicExchange listingExchange() {
        return new TopicExchange(listingExchangeName);
    }

    @Bean
    Queue alertListingQueue() {
        return new Queue(alertListingQueueName, true);
    }

    @Bean
    Binding alertListingBinding(Queue alertListingQueue, TopicExchange listingExchange) {
        return BindingBuilder
                .bind(alertListingQueue)
                .to(listingExchange)
                .with(listingNewRoutingKey);
    }

    @Bean
    MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    AmqpTemplate amqpTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        return template;
    }
}
