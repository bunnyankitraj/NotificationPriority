package com.example.notification.listener;

import com.example.notification.config.RabbitMQConfig;
import com.example.notification.entity.Notification;
import com.example.notification.enums.NotificationChannel;
import com.example.notification.processor.NotificationProcessor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class NotificationQueueListener {

    private final Map<NotificationChannel, NotificationProcessor> processors;

    @Autowired
    public NotificationQueueListener(List<NotificationProcessor> processorList) {
        this.processors = processorList.stream()
                .collect(Collectors.toMap(
                        NotificationProcessor::getChannel,
                        Function.identity()
                ));
    }

    @RabbitListener(queues = RabbitMQConfig.CRITICAL_QUEUE)
    public void handleCriticalNotification(Notification notification) {
        System.out.println("Processing CRITICAL notification: " + notification.getId());
        processNotification(notification);
    }

    @RabbitListener(queues = RabbitMQConfig.HIGH_QUEUE)
    public void handleHighNotification(Notification notification) {
        System.out.println("Processing HIGH priority notification: " + notification.getId());
        processNotification(notification);
    }

    @RabbitListener(queues = RabbitMQConfig.MEDIUM_QUEUE)
    public void handleMediumNotification(Notification notification) {
        System.out.println("Processing MEDIUM priority notification: " + notification.getId());
        processNotification(notification);
    }

    @RabbitListener(queues = RabbitMQConfig.LOW_QUEUE)
    public void handleLowNotification(Notification notification) {
        System.out.println("Processing LOW priority notification: " + notification.getId());
        processNotification(notification);
    }

    private void processNotification(Notification notification) {
        NotificationProcessor processor = processors.get(notification.getChannel());
        if (processor != null) {
            processor.processNotification(notification);
        } else {
            System.err.println("No processor found for channel: " + notification.getChannel());
        }
    }
}
