package com.example.notification.processor;

import com.example.notification.entity.Notification;
import com.example.notification.enums.NotificationChannel;
import com.example.notification.enums.NotificationStatus;
import com.example.notification.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public abstract class NotificationProcessor {

    @Autowired
    protected NotificationService notificationService;

    public abstract NotificationChannel getChannel();

    public abstract boolean sendNotification(Notification notification);

    public void processNotification(Notification notification) {
        try {
            notificationService.updateNotificationStatus(
                    notification.getId(),
                    NotificationStatus.PROCESSING,
                    "Processing started"
            );

            boolean success = sendNotification(notification);

            if (success) {
                notificationService.updateNotificationStatus(
                        notification.getId(),
                        NotificationStatus.SENT,
                        "Notification sent successfully"
                );
            } else {
                handleFailure(notification, "Failed to send notification");
            }

        } catch (Exception e) {
            handleFailure(notification, "Exception: " + e.getMessage());
        }
    }

    private void handleFailure(Notification notification, String errorMessage) {
        if (notification.getRetryCount() < 3) {
            notificationService.incrementRetryCount(notification.getId(), errorMessage);
            // Re-queue for retry (implementation depends on retry strategy)
        } else {
            notificationService.updateNotificationStatus(
                    notification.getId(),
                    NotificationStatus.FAILED,
                    "Max retries exceeded: " + errorMessage
            );
        }
    }
}
