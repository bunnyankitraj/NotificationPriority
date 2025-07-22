package com.example.notification.service;

import com.example.notification.entity.Notification;
import com.example.notification.enums.NotificationPriority;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PriorityService {

    @Autowired
    private UserService userService;

    public NotificationPriority calculatePriority(Notification notification) {
        NotificationPriority basePriority = notification.getPriority();

        // VIP users get priority boost
        if (userService.isVIPUser(notification.getUserId())) {
            return boostPriority(basePriority);
        }

        return basePriority;
    }

    private NotificationPriority boostPriority(NotificationPriority priority) {
        switch (priority) {
            case LOW: return NotificationPriority.MEDIUM;
            case MEDIUM: return NotificationPriority.HIGH;
            case HIGH: return NotificationPriority.CRITICAL;
            case CRITICAL: return NotificationPriority.CRITICAL;
            default: return priority;
        }
    }

    public String getRoutingKey(NotificationPriority priority) {
        return "notification." + priority.name().toLowerCase();
    }

    public String getQueueName(NotificationPriority priority) {
        return "notification." + priority.name().toLowerCase();
    }
}
