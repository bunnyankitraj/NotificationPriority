package com.example.notification.repository;

import com.example.notification.entity.NotificationAudit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationAuditRepository extends JpaRepository<NotificationAudit, Long> {
    List<NotificationAudit> findByNotificationIdOrderByTimestampDesc(Long notificationId);
    List<NotificationAudit> findByUserIdOrderByTimestampDesc(String userId);
}
