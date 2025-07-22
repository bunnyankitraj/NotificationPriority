package com.example.notification.processor;

import com.example.notification.entity.Notification;
import com.example.notification.enums.NotificationChannel;
import com.example.notification.websocket.NotificationWebSocketHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class InAppNotificationProcessor extends NotificationProcessor {

    @Autowired
    private NotificationWebSocketHandler webSocketHandler;

    @Override
    public NotificationChannel getChannel() {
        return NotificationChannel.IN_APP;
    }

    @Override
    public boolean sendNotification(Notification notification) {
        try {
            // Store in-app notification in cache/database for user to see when they log in
            storeInAppNotification(notification);

            // Also try to send via WebSocket if user is online
            webSocketHandler.sendNotificationToUser(notification.getUserId(), notification);

            System.out.println("Sending IN-APP notification: " + notification.getTitle() + " to " + notification.getUserId());
            return true;
        } catch (Exception e) {
            System.err.println("Error sending in-app notification: " + e.getMessage());
            return false;
        }
    }

    private void storeInAppNotification(Notification notification) {
        // Store in Redis or database for later retrieval
        // Implementation would depend on your caching strategy
        System.out.println("Stored in-app notification for user: " + notification.getUserId());
    }
}
