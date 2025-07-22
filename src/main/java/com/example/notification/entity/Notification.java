package com.example.notification.entity;

import com.example.notification.enums.NotificationChannel;
import com.example.notification.enums.NotificationPriority;
import com.example.notification.enums.NotificationStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Entity
@Table(name = "notifications")
@Data
@AllArgsConstructor
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String userId;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String message;

    @Enumerated(EnumType.STRING)
    private NotificationPriority priority;

    @Enumerated(EnumType.STRING)
    private NotificationChannel channel;

    @Enumerated(EnumType.STRING)
    private NotificationStatus status;

    @ElementCollection
    @CollectionTable(name = "notification_metadata")
    private Map<String, String> metadata;

    private LocalDateTime createdAt;
    private LocalDateTime scheduledAt;
    private LocalDateTime sentAt;
    private int retryCount;
    private String errorMessage;


    public Notification() {
        this.createdAt = LocalDateTime.now();
        this.status = NotificationStatus.PENDING;
        this.retryCount = 0;
    }

    public Notification(String userId, String title, String message,
                        NotificationPriority priority, NotificationChannel channel) {
        this();
        this.userId = userId;
        this.title = title;
        this.message = message;
        this.priority = priority;
        this.channel = channel;
    }

}
