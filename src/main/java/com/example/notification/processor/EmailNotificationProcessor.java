package com.example.notification.processor;

import com.example.notification.entity.Notification;
import com.example.notification.enums.NotificationChannel;
import org.springframework.stereotype.Component;

@Component
public class EmailNotificationProcessor extends NotificationProcessor {

    @Override
    public NotificationChannel getChannel() {
        return NotificationChannel.EMAIL;
    }

    @Override
    public boolean sendNotification(Notification notification) {
        try {
            // Email sending logic here
            System.out.println("Sending EMAIL notification: " + notification.getTitle() + " to " + notification.getUserId());

            // Simulate email sending delay
            Thread.sleep(100);

            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
