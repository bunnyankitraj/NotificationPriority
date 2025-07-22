package com.example.notification.controller;

import com.example.notification.entity.NotificationAudit;
import com.example.notification.repository.NotificationAuditRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;


@RestController
@RequestMapping("/api/audit")
@CrossOrigin(origins = "*")
public class AuditController {

    @Autowired
    private NotificationAuditRepository auditRepository;

    @GetMapping("/notification/{notificationId}")
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
