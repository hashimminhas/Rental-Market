package com.uuriturg.notification.config;

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

    @Value("${messaging.exchange.landlord}")
    private String landlordExchangeName;

    @Value("${messaging.queues.notification-listing}")
    private String listingQueueName;

    @Value("${messaging.queues.notification-landlord}")
    private String landlordQueueName;

    @Value("${messaging.routing-keys.listing-claimed}")
    private String listingClaimedKey;

    @Value("${messaging.routing-keys.review-posted}")
    private String reviewPostedKey;

    @Bean
    TopicExchange listingExchange() {
        return new TopicExchange(listingExchangeName);
    }

    @Bean
    TopicExchange landlordExchange() {
        return new TopicExchange(landlordExchangeName);
    }

    @Bean
    Queue notificationListingQueue() {
        return new Queue(listingQueueName, true);
    }

    @Bean
    Queue notificationLandlordQueue() {
        return new Queue(landlordQueueName, true);
    }

    @Bean
    Binding listingBinding(Queue notificationListingQueue, TopicExchange listingExchange) {
        return BindingBuilder.bind(notificationListingQueue).to(listingExchange).with(listingClaimedKey);
    }

    @Bean
    Binding landlordBinding(Queue notificationLandlordQueue, TopicExchange landlordExchange) {
        return BindingBuilder.bind(notificationLandlordQueue).to(landlordExchange).with(reviewPostedKey);
    }

    @Bean
    MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, MessageConverter messageConverter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter);
        return template;
    }
}
