package com.example.notification.controller;

import com.example.notification.annotation.RateLimit;
import com.example.notification.annotation.RequireRole;
import com.example.notification.entity.NotificationAudit;
import com.example.notification.enums.UserType;
import com.example.notification.repository.NotificationAuditRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/audit")
@CrossOrigin(origins = "*")
public class AuditController {

    @Autowired
    private NotificationAuditRepository auditRepository;

    @GetMapping("/notification/{notificationId}")
    @RateLimit(maxRequests = 50, windowSeconds = 60, endpoint = "audit_notification")
    @RequireRole({UserType.ADMIN})
    public ResponseEntity<List<NotificationAudit>> getNotificationAuditTrail(@PathVariable Long notificationId) {
        try {
            List<NotificationAudit> auditTrail = auditRepository.findByNotificationIdOrderByTimestampDesc(notificationId);
            return ResponseEntity.ok(auditTrail);
        } catch (Exception e) {
            System.err.println("Error fetching audit trail: " + e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/user/{userId}")
    @RateLimit(maxRequests = 50, windowSeconds = 60, endpoint = "audit_user")
    @RequireRole({UserType.ADMIN})
    public ResponseEntity<List<NotificationAudit>> getUserAuditTrail(@PathVariable String userId) {
        try {
            List<NotificationAudit> auditTrail = auditRepository.findByUserIdOrderByTimestampDesc(userId);
            return ResponseEntity.ok(auditTrail);
        } catch (Exception e) {
            System.err.println("Error fetching user audit trail: " + e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
}
