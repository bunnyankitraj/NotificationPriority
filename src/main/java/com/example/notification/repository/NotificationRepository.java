package com.example.notification.repository;

import com.example.notification.entity.Notification;
import com.example.notification.enums.NotificationPriority;
import com.example.notification.enums.NotificationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    // Find notifications by user ID and status
    List<Notification> findByUserIdAndStatus(String userId, NotificationStatus status);

    // Get all notifications for a user ordered by creation time (newest first)
    List<Notification> findByUserIdOrderByCreatedAtDesc(String userId);

    // Find pending notifications ordered by priority and creation time
    @Query("SELECT n FROM Notification n WHERE n.status = :status AND n.scheduledAt <= :now ORDER BY n.priority, n.createdAt")
    List<Notification> findPendingNotificationsByPriority(NotificationStatus status, LocalDateTime now);

    // Find notifications that are eligible for retry
    List<Notification> findByStatusAndRetryCountLessThan(NotificationStatus status, int maxRetries);

    // Find scheduled notifications that are past due (missed scheduled time)
    @Query("SELECT n FROM Notification n WHERE n.status = :status AND n.scheduledAt <= :now")
    List<Notification> findScheduledNotificationsPastDue(
            @Param("status") NotificationStatus status,
            @Param("now") LocalDateTime now
    );

    // Count notifications by status (useful for monitoring)
    long countByStatus(NotificationStatus status);

    // Find scheduled notifications within a specific time range
    @Query("SELECT n FROM Notification n WHERE n.status = :status AND n.scheduledAt BETWEEN :startTime AND :endTime ORDER BY n.scheduledAt")
    List<Notification> findScheduledNotificationsBetween(
            @Param("status") NotificationStatus status,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );

    // Find notifications by multiple statuses (useful for dashboard queries)
    @Query("SELECT n FROM Notification n WHERE n.status IN :statuses ORDER BY n.createdAt DESC")
    List<Notification> findByStatusIn(@Param("statuses") List<NotificationStatus> statuses);

    // Find notifications by user and priority
    List<Notification> findByUserIdAndPriorityOrderByCreatedAtDesc(String userId, NotificationPriority priority);

    // Find notifications created within a time range (useful for analytics)
    @Query("SELECT n FROM Notification n WHERE n.createdAt BETWEEN :startTime AND :endTime ORDER BY n.createdAt DESC")
    List<Notification> findNotificationsBetweenDates(
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );

    // Count notifications by status and date range
    @Query("SELECT COUNT(n) FROM Notification n WHERE n.status = :status AND n.createdAt BETWEEN :startTime AND :endTime")
    long countByStatusAndDateRange(
            @Param("status") NotificationStatus status,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );

    // Find failed notifications that need attention
    @Query("SELECT n FROM Notification n WHERE n.status = 'FAILED' AND n.retryCount >= :maxRetries ORDER BY n.createdAt DESC")
    List<Notification> findFailedNotificationsExceedingRetries(@Param("maxRetries") int maxRetries);
}