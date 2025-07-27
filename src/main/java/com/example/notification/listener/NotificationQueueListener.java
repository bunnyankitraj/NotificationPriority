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

    // CRITICAL: Most consumers, highest priority
    @RabbitListener(
            queues = RabbitMQConfig.CRITICAL_QUEUE,
            containerFactory = "criticalListenerFactory"
    )
    public void handleCriticalNotification(Notification notification) {
        System.out.println("⚡ Processing CRITICAL notification: " + notification.getId() + " - IMMEDIATE");
        processNotification(notification);
    }

    // HIGH: Good number of consumers
    @RabbitListener(
            queues = RabbitMQConfig.HIGH_QUEUE,
            containerFactory = "highListenerFactory"
    )
    public void handleHighNotification(Notification notification) {
        System.out.println("🔥 Processing HIGH priority notification: " + notification.getId());
        processNotification(notification);
    }

    // MEDIUM: Moderate consumers
    @RabbitListener(
            queues = RabbitMQConfig.MEDIUM_QUEUE,
            containerFactory = "mediumListenerFactory"
    )
    public void handleMediumNotification(Notification notification) {
        System.out.println("📊 Processing MEDIUM priority notification: " + notification.getId());
        processNotification(notification);
    }

    // LOW: Fewer consumers - will naturally be slower under load
    @RabbitListener(
            queues = RabbitMQConfig.LOW_QUEUE,
            containerFactory = "lowListenerFactory"
    )
    public void handleLowNotification(Notification notification) {
        System.out.println("📝 Processing LOW priority notification: " + notification.getId());
        processNotification(notification);
    }

    private void processNotification(Notification notification) {
        NotificationProcessor processor = processors.get(notification.getChannel());
        if (processor != null) {
            long startTime = System.currentTimeMillis();
            processor.processNotification(notification);
            long processingTime = System.currentTimeMillis() - startTime;

            System.out.println("✅ Completed " + notification.getPriority() +
                    " notification " + notification.getId() +
                    " in " + processingTime + "ms");
        } else {
            System.err.println("❌ No processor found for channel: " + notification.getChannel());
        }
    }
}
