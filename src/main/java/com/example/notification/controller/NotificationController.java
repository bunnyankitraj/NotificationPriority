package com.example.notification.controller;

import com.example.notification.annotation.RateLimit;
import com.example.notification.annotation.RequireRole;
import com.example.notification.dto.NotificationRequest;
import com.example.notification.dto.NotificationResponse;
import com.example.notification.enums.UserType;
import com.example.notification.service.NotificationService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/notifications")
@CrossOrigin(origins = "*")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    @PostMapping
    @RateLimit(maxRequests = 50, windowSeconds = 60, endpoint = "create_notification")
    @RequireRole({UserType.REGULAR, UserType.VIP, UserType.ADMIN})
    public ResponseEntity<NotificationResponse> createNotification(@Valid @RequestBody NotificationRequest request) {
        try {
            NotificationResponse response = notificationService.createNotification(request);
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (Exception e) {
            System.err.println("Error creating notification: " + e.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/user/{userId}")
    @RateLimit(maxRequests = 100, windowSeconds = 60, endpoint = "get_user_notifications")
    @RequireRole({UserType.REGULAR, UserType.VIP, UserType.ADMIN})
    public ResponseEntity<List<NotificationResponse>> getUserNotifications(@PathVariable String userId) {
        try {
            List<NotificationResponse> notifications = notificationService.getUserNotifications(userId);
            return ResponseEntity.ok(notifications);
        } catch (Exception e) {
            System.err.println("Error fetching user notifications: " + e.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/user/all/{userId}")
    @RateLimit(maxRequests = 50, windowSeconds = 60, endpoint = "get_all_user_notifications")
    @RequireRole({UserType.REGULAR, UserType.VIP, UserType.ADMIN})
    public ResponseEntity<List<NotificationResponse>> getAllUserNotifications(@PathVariable String userId) {
        try {
            List<NotificationResponse> notifications = notificationService.getAllUserNotifications(userId);
            return ResponseEntity.ok(notifications);
        } catch (Exception e) {
            System.err.println("Error fetching user notifications: " + e.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/user/scheduled/{userId}")
    @RateLimit(maxRequests = 30, windowSeconds = 60, endpoint = "get_scheduled_notifications")
    @RequireRole({UserType.REGULAR, UserType.VIP, UserType.ADMIN})
    public ResponseEntity<List<NotificationResponse>> getScheduledNotifications(@PathVariable String userId) {
        try {
            List<NotificationResponse> notifications = notificationService.getScheduledNotifications(userId);
            return ResponseEntity.ok(notifications);
        } catch (Exception e) {
            System.err.println("Error fetching scheduled notifications: " + e.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/{id}/cancel")
    @RateLimit(maxRequests = 20, windowSeconds = 60, endpoint = "cancel_notification")
    @RequireRole(value = {UserType.REGULAR, UserType.VIP, UserType.ADMIN}, requireOwnership = true)
    public ResponseEntity<String> cancelScheduledNotification(@PathVariable Long id, @RequestParam String userId) {
        try {
            boolean cancelled = notificationService.cancelScheduledNotification(id, userId);
            if (cancelled) {
                return ResponseEntity.ok("Notification cancelled successfully");
            } else {
                return ResponseEntity.badRequest().body("Notification not found, not scheduled, or not owned by user");
            }
        } catch (Exception e) {
            System.err.println("Error cancelling notification: " + e.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/{id}")
    @RateLimit(maxRequests = 100, windowSeconds = 60, endpoint = "get_notification")
    @RequireRole(value = {UserType.REGULAR, UserType.VIP, UserType.ADMIN}, requireOwnership = true)
    public ResponseEntity<NotificationResponse> getNotification(@PathVariable Long id) {
        try {
            // Get user ID from request context (handled by aspect)
            String userId = getUserIdFromRequest();
            if (userId == null) {
                return ResponseEntity.badRequest().build();
            }
            
            Optional<NotificationResponse> notification = notificationService.getNotificationWithOwnership(id, userId);
            return notification.map(ResponseEntity::ok)
                    .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
        } catch (Exception e) {
            System.err.println("Error fetching notification: " + e.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/bulk")
    @RateLimit(maxRequests = 10, windowSeconds = 60, endpoint = "bulk_create_notifications")
    @RequireRole({UserType.VIP, UserType.ADMIN})
    public ResponseEntity<List<NotificationResponse>> createBulkNotifications(
            @Valid @RequestBody List<NotificationRequest> requests) {
        try {
            List<NotificationResponse> responses = requests.stream()
                    .map(notificationService::createNotification)
                    .collect(java.util.stream.Collectors.toList());

            return new ResponseEntity<>(responses, HttpStatus.CREATED);
        } catch (Exception e) {
            System.err.println("Error creating bulk notifications: " + e.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Helper method to get user ID from request context
    private String getUserIdFromRequest() {
        // This will be handled by the aspect, but we need it for the getNotification method
        // In a real implementation, you might want to use a more robust approach
        return null; // The aspect will handle this
    }
}
