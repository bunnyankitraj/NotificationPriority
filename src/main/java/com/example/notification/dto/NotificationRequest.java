package com.example.notification.dto;

import com.example.notification.enums.NotificationChannel;
import com.example.notification.enums.NotificationPriority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

@Data
public class NotificationRequest {
    @NotBlank
    private String userId;

    @NotBlank
    private String title;

    @NotBlank
    private String message;

    @NotNull
    private NotificationPriority priority;

    @NotNull
    private NotificationChannel channel;

    private Map<String, String> metadata;

    // NEW: Optional scheduled time - if null, send immediately
    private LocalDateTime scheduledAt;

    // Constructors
    public NotificationRequest() {}

    public NotificationRequest(String userId, String title, String message,
                               NotificationPriority priority, NotificationChannel channel) {
        this.userId = userId;
        this.title = title;
        this.message = message;
        this.priority = priority;
        this.channel = channel;
    }

    public NotificationRequest(String userId, String title, String message,
                               NotificationPriority priority, NotificationChannel channel,
                               LocalDateTime scheduledAt) {
        this(userId, title, message, priority, channel);
        this.scheduledAt = scheduledAt;
    }

    // Helper method
    public boolean isScheduled() {
        return scheduledAt != null && scheduledAt.isAfter(LocalDateTime.now());
    }
}
