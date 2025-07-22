package com.example.notification.processor;

import com.example.notification.entity.Notification;
import com.example.notification.enums.NotificationChannel;
import org.springframework.stereotype.Component;

@Component
public class SMSNotificationProcessor extends NotificationProcessor {

    @Override
    public NotificationChannel getChannel() {
        return NotificationChannel.SMS;
    }

    @Override
    public boolean sendNotification(Notification notification) {
        try {
            // SMS sending logic here
            System.out.println("Sending SMS notification: " + notification.getMessage() + " to " + notification.getUserId());

            // Simulate SMS sending delay
            Thread.sleep(50);

            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
