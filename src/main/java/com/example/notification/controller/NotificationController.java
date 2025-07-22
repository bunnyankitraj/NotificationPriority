package com.example.notification.controller;

import com.example.notification.dto.NotificationRequest;
import com.example.notification.dto.NotificationResponse;
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
    public ResponseEntity<List<NotificationResponse>> getUserNotifications(@PathVariable String userId) {
        try {
            List<NotificationResponse> notifications = notificationService.getUserNotifications(userId);
            return ResponseEntity.ok(notifications);
        } catch (Exception e) {
            System.err.println("Error fetching user notifications: " + e.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<NotificationResponse> getNotification(@PathVariable Long id) {
        try {
            Optional<NotificationResponse> notification = notificationService.getNotification(id);
            return notification.map(ResponseEntity::ok)
                    .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
        } catch (Exception e) {
            System.err.println("Error fetching notification: " + e.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/bulk")
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
}
