package com.example.notification.processor;

import com.example.notification.entity.Notification;
import com.example.notification.enums.NotificationChannel;
import com.example.notification.websocket.NotificationWebSocketHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class WebSocketNotificationProcessor extends NotificationProcessor {

    @Autowired
    private NotificationWebSocketHandler webSocketHandler;

    @Override
    public NotificationChannel getChannel() {
        return NotificationChannel.WEBSOCKET;
    }

    @Override
    public boolean sendNotification(Notification notification) {
        try {
            return webSocketHandler.sendNotificationToUser(notification.getUserId(), notification);
        } catch (Exception e) {
            System.err.println("Error sending WebSocket notification: " + e.getMessage());
            return false;
        }
    }
}
