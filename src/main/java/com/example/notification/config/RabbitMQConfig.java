package com.example.notification.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String NOTIFICATION_EXCHANGE = "notification.exchange";
    public static final String CRITICAL_QUEUE = "notification.critical";
    public static final String HIGH_QUEUE = "notification.high";
    public static final String MEDIUM_QUEUE = "notification.medium";
    public static final String LOW_QUEUE = "notification.low";

    @Bean
    public TopicExchange notificationExchange() {
        return new TopicExchange(NOTIFICATION_EXCHANGE);
    }

    @Bean
    public Queue criticalQueue() {
        return QueueBuilder.durable(CRITICAL_QUEUE)
                .withArgument("x-max-priority", 10)
                .build();
    }

    @Bean
    public Queue highQueue() {
        return QueueBuilder.durable(HIGH_QUEUE)
                .withArgument("x-max-priority", 8)
                .build();
    }

    @Bean
    public Queue mediumQueue() {
        return QueueBuilder.durable(MEDIUM_QUEUE)
                .withArgument("x-max-priority", 5)
                .build();
    }

    @Bean
    public Queue lowQueue() {
        return QueueBuilder.durable(LOW_QUEUE)
                .withArgument("x-max-priority", 2)
                .build();
    }

    @Bean
    public Binding criticalBinding() {
        return BindingBuilder.bind(criticalQueue()).to(notificationExchange()).with("notification.critical");
    }

    @Bean
    public Binding highBinding() {
        return BindingBuilder.bind(highQueue()).to(notificationExchange()).with("notification.high");
    }

    @Bean
    public Binding mediumBinding() {
        return BindingBuilder.bind(mediumQueue()).to(notificationExchange()).with("notification.medium");
    }

    @Bean
    public Binding lowBinding() {
        return BindingBuilder.bind(lowQueue()).to(notificationExchange()).with("notification.low");
    }

    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter());
        return template;
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(messageConverter());
        factory.setConcurrentConsumers(3);
        factory.setMaxConcurrentConsumers(10);
        return factory;
    }
}
