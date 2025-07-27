package com.example.notification.repository;

import com.example.notification.entity.Notification;
import com.example.notification.enums.NotificationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByUserIdAndStatus(String userId, NotificationStatus status);

    @Query("SELECT n FROM Notification n WHERE n.status = :status AND n.scheduledAt <= :now ORDER BY n.priority, n.createdAt")
    List<Notification> findPendingNotificationsByPriority(NotificationStatus status, LocalDateTime now);

    List<Notification> findByStatusAndRetryCountLessThan(NotificationStatus status, int maxRetries);

    List<Notification> findScheduledNotificationsPastDue(NotificationStatus status, LocalDateTime dateTime);

    List<Notification> findByUserIdOrderByCreatedAtDesc(String userId);

    long countByStatus(NotificationStatus status);

}
