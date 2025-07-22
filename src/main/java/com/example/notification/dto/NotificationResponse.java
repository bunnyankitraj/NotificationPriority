package com.example.notification.dto;

import com.example.notification.enums.NotificationChannel;
import com.example.notification.enums.NotificationPriority;
import com.example.notification.enums.NotificationStatus;
import lombok.Data;

@Data
public class NotificationResponse {
    private Long id;
    private String userId;
    private String title;
    private String message;
    private NotificationPriority priority;
    private NotificationChannel channel;
    private NotificationStatus status;
    private String createdAt;
    private String sentAt;
}
