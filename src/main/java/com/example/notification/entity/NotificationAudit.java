package com.example.notification.entity;

import com.example.notification.enums.NotificationStatus;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "notification_audit")
public class NotificationAudit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long notificationId;
    private String userId;
    private NotificationStatus previousStatus;
    private NotificationStatus newStatus;
    private LocalDateTime timestamp;
    private String details;


    public NotificationAudit() {
        this.timestamp = LocalDateTime.now();
    }

    public NotificationAudit(Long notificationId, String userId,
                             NotificationStatus previousStatus, NotificationStatus newStatus, String details) {
        this();
        this.notificationId = notificationId;
        this.userId = userId;
        this.previousStatus = previousStatus;
        this.newStatus = newStatus;
        this.details = details;
    }


}
