package com.example.notification.service;

import com.example.notification.dto.NotificationRequest;
import com.example.notification.dto.NotificationResponse;
import com.example.notification.entity.Notification;
import com.example.notification.entity.NotificationAudit;
import com.example.notification.enums.NotificationPriority;
import com.example.notification.enums.NotificationStatus;
import com.example.notification.repository.NotificationAuditRepository;
import com.example.notification.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private NotificationAuditRepository auditRepository;

    @Autowired
    private PriorityService priorityService;

    @Autowired
    private QueueService queueService;

    @Autowired
    private ScheduledNotificationService scheduledNotificationService;

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public NotificationResponse createNotification(NotificationRequest request) {
        Notification notification = new Notification(
                request.getUserId(),
                request.getTitle(),
                request.getMessage(),
                request.getPriority(),
                request.getChannel()
        );

        notification.setMetadata(request.getMetadata());

        // Calculate final priority (including VIP boost)
        NotificationPriority finalPriority = priorityService.calculatePriority(notification);
        notification.setPriority(finalPriority);

        // Handle scheduling
        if (request.getScheduledAt() != null && request.getScheduledAt().isAfter(LocalDateTime.now())) {
            // SCHEDULED NOTIFICATION
            notification.setScheduledAt(request.getScheduledAt());
            notification.setStatus(NotificationStatus.SCHEDULED);

            // Save notification first
            notification = notificationRepository.save(notification);

            // Create audit entry
            createAuditEntry(notification, null, NotificationStatus.SCHEDULED,
                    "Notification scheduled for: " + request.getScheduledAt().format(formatter));

            // Add to scheduler instead of immediate queue
            scheduledNotificationService.scheduleNotification(notification);

        } else {
            // IMMEDIATE NOTIFICATION
            notification.setScheduledAt(LocalDateTime.now());
            notification.setStatus(NotificationStatus.PENDING);

            // Save notification
            notification = notificationRepository.save(notification);

            // Create audit entry
            createAuditEntry(notification, null, NotificationStatus.PENDING,
                    "Notification created - queued for immediate processing");

            // Send to appropriate priority queue immediately
            queueService.sendToQueue(notification);
        }

        return convertToResponse(notification);
    }

    // NEW: Method to process scheduled notifications when their time comes
    public void processScheduledNotification(Long notificationId) {
        Optional<Notification> optionalNotification = notificationRepository.findById(notificationId);
        if (optionalNotification.isPresent()) {
            Notification notification = optionalNotification.get();

            if (notification.getStatus() == NotificationStatus.SCHEDULED) {
                // Update status from SCHEDULED to PENDING
                notification.setStatus(NotificationStatus.PENDING);
                notificationRepository.save(notification);

                // Create audit entry
                createAuditEntry(notification, NotificationStatus.SCHEDULED, NotificationStatus.PENDING,
                        "Scheduled time reached - moving to processing queue");

                // Send to queue for processing
                queueService.sendToQueue(notification);
            }
        }
    }

    // NEW: Get all user notifications including scheduled ones
    public List<NotificationResponse> getAllUserNotifications(String userId) {
        List<Notification> notifications = notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
        return notifications.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    // NEW: Get only scheduled notifications for a user
    public List<NotificationResponse> getScheduledNotifications(String userId) {
        List<Notification> notifications = notificationRepository.findByUserIdAndStatus(userId, NotificationStatus.SCHEDULED);
        return notifications.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    // NEW: Cancel a scheduled notification
    public boolean cancelScheduledNotification(Long notificationId, String userId) {
        Optional<Notification> optionalNotification = notificationRepository.findById(notificationId);
        if (optionalNotification.isPresent()) {
            Notification notification = optionalNotification.get();

            // Verify ownership and status
            if (notification.getUserId().equals(userId) &&
                    notification.getStatus() == NotificationStatus.SCHEDULED) {

                // Update status to FAILED (cancelled)
                notification.setStatus(NotificationStatus.FAILED);
                notification.setErrorMessage("Cancelled by user");
                notificationRepository.save(notification);

                // Create audit entry
                createAuditEntry(notification, NotificationStatus.SCHEDULED, NotificationStatus.FAILED,
                        "Notification cancelled by user");

                // Remove from scheduler
                scheduledNotificationService.cancelScheduledNotification(notificationId);

                return true;
            }
        }
        return false;
    }

    // Existing methods...
    public List<NotificationResponse> getUserNotifications(String userId) {
        List<Notification> notifications = notificationRepository.findByUserIdAndStatus(userId, NotificationStatus.SENT);
        return notifications.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    public Optional<NotificationResponse> getNotification(Long id) {
        return notificationRepository.findById(id).map(this::convertToResponse);
    }

    public void updateNotificationStatus(Long notificationId, NotificationStatus newStatus, String details) {
        Optional<Notification> optionalNotification = notificationRepository.findById(notificationId);
        if (optionalNotification.isPresent()) {
            Notification notification = optionalNotification.get();
            NotificationStatus oldStatus = notification.getStatus();

            notification.setStatus(newStatus);
            if (newStatus == NotificationStatus.SENT) {
                notification.setSentAt(LocalDateTime.now());
            }

            notificationRepository.save(notification);
            createAuditEntry(notification, oldStatus, newStatus, details);
        }
    }

    public void incrementRetryCount(Long notificationId, String errorMessage) {
        Optional<Notification> optionalNotification = notificationRepository.findById(notificationId);
        if (optionalNotification.isPresent()) {
            Notification notification = optionalNotification.get();
            notification.setRetryCount(notification.getRetryCount() + 1);
            notification.setErrorMessage(errorMessage);
            notification.setStatus(NotificationStatus.RETRYING);

            notificationRepository.save(notification);
            createAuditEntry(notification, NotificationStatus.FAILED, NotificationStatus.RETRYING,
                    "Retry attempt #" + notification.getRetryCount() + ": " + errorMessage);
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

    private NotificationResponse convertToResponse(Notification notification) {
        NotificationResponse response = new NotificationResponse();
        response.setId(notification.getId());
        response.setUserId(notification.getUserId());
        response.setTitle(notification.getTitle());
        response.setMessage(notification.getMessage());
        response.setPriority(notification.getPriority());
        response.setChannel(notification.getChannel());
        response.setStatus(notification.getStatus());
        response.setCreatedAt(notification.getCreatedAt().format(formatter));

        // Set scheduled information
        if (notification.getScheduledAt() != null) {
            response.setScheduledAt(notification.getScheduledAt().format(formatter));
            response.setScheduled(notification.getStatus() == NotificationStatus.SCHEDULED);
        }

        if (notification.getSentAt() != null) {
            response.setSentAt(notification.getSentAt().format(formatter));
        }

        return response;
    }
}
