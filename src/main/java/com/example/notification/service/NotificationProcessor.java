package com.example.notification.service;

import com.example.notification.entity.Notification;
import com.example.notification.entity.NotificationAudit;
import com.example.notification.enums.NotificationStatus;
import com.example.notification.repository.NotificationAuditRepository;
import com.example.notification.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class NotificationProcessor {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private QueueService queueService;

    @Autowired
    private NotificationAuditRepository auditRepository;

    // Handles processing when scheduled time arrives
    public void processScheduledNotification(Long notificationId) {
        Optional<Notification> optionalNotification = notificationRepository.findById(notificationId);
        if (optionalNotification.isPresent()) {
            Notification notification = optionalNotification.get();

            if (notification.getStatus() == NotificationStatus.SCHEDULED) {
                notification.setStatus(NotificationStatus.PENDING);
                notificationRepository.save(notification);

                createAuditEntry(notification, NotificationStatus.SCHEDULED,
                        NotificationStatus.PENDING, "Scheduled time reached - moving to processing queue");

                queueService.sendToQueue(notification);
            }
        }
    }

    private void createAuditEntry(Notification notification, NotificationStatus previousStatus,
                                  NotificationStatus newStatus, String details) {
        NotificationAudit audit = new NotificationAudit(
                notification.getId(),
                notification.getUserId(),
                previousStatus,
                newStatus,
                details
        );
        auditRepository.save(audit);
    }

}
