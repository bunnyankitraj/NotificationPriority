package com.example.notification.service;

import com.example.notification.config.RabbitMQConfig;
import com.example.notification.entity.Notification;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
                    message.getMessageProperties().setPriority(notification.getPriority().getLevel());
                    return message;
                }
        );
    }
}
