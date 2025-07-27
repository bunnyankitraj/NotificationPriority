package com.example.notification.service;

import com.example.notification.config.RabbitMQConfig;
import com.example.notification.entity.Notification;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class QueueService {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private PriorityService priorityService;

    public void sendToQueue(Notification notification) {
        String routingKey = priorityService.getRoutingKey(notification.getPriority());

        rabbitTemplate.convertAndSend(
                RabbitMQConfig.NOTIFICATION_EXCHANGE,
                routingKey,
                notification,
                message -> {
                    // Set message priority - higher number = higher priority
                    int messagePriority = 10 - notification.getPriority().getLevel(); // Invert so CRITICAL=9, LOW=6
                    message.getMessageProperties().setPriority(messagePriority);

                    // Add timestamp for tracking
                    message.getMessageProperties().setTimestamp(new java.util.Date());

                    // Add custom headers
                    message.getMessageProperties().setHeader("priority", notification.getPriority().name());
                    message.getMessageProperties().setHeader("userId", notification.getUserId());
                    message.getMessageProperties().setHeader("channel", notification.getChannel().name());

                    return message;
                }
        );

        System.out.println("📤 Queued " + notification.getPriority() +
                " notification " + notification.getId() +
                " to " + routingKey + " queue");
    }

    // Bulk send method for high-throughput scenarios
    public void sendBulkToQueue(List<Notification> notifications) {
        notifications.forEach(this::sendToQueue);
    }
}
