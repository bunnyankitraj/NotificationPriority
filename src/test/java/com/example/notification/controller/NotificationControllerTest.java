package com.example.notification.controller;

import com.example.notification.dto.NotificationRequest;
import com.example.notification.dto.NotificationResponse;
import com.example.notification.enums.NotificationChannel;
import com.example.notification.enums.NotificationPriority;
import com.example.notification.enums.NotificationStatus;
import com.example.notification.service.NotificationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(NotificationController.class)
class NotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private NotificationService notificationService;

    @Autowired
    private ObjectMapper objectMapper;

    private NotificationResponse testNotification;
    private NotificationRequest testRequest;

    @BeforeEach
    void setUp() {
        // Setup test notification response
        testNotification = new NotificationResponse();
        testNotification.setId(1L);
        testNotification.setUserId("user123");
        testNotification.setTitle("Test Notification");
        testNotification.setMessage("This is a test notification");
        testNotification.setPriority(NotificationPriority.HIGH);
        testNotification.setChannel(NotificationChannel.EMAIL);
        testNotification.setStatus(NotificationStatus.SCHEDULED);
        testNotification.setCreatedAt("2024-01-01 10:00:00");
        testNotification.setScheduledAt("2024-01-01 12:00:00");
        testNotification.setScheduled(true);

        // Setup test request
        testRequest = new NotificationRequest();
        testRequest.setUserId("user123");
        testRequest.setTitle("Test Notification");
        testRequest.setMessage("This is a test notification");
        testRequest.setPriority(NotificationPriority.HIGH);
        testRequest.setChannel(NotificationChannel.EMAIL);
        testRequest.setScheduledAt(LocalDateTime.now().plusHours(2));
    }

    @Test
    void testGetScheduledNotifications() throws Exception {
        // Given
        String userId = "user123";
        List<NotificationResponse> scheduledNotifications = Arrays.asList(testNotification);
        when(notificationService.getScheduledNotifications(userId)).thenReturn(scheduledNotifications);

        // When & Then
        mockMvc.perform(get("/api/notifications/user/scheduled/{userId}", userId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].userId").value("user123"))
                .andExpect(jsonPath("$[0].title").value("Test Notification"))
                .andExpect(jsonPath("$[0].status").value("SCHEDULED"))
                .andExpect(jsonPath("$[0].scheduled").value(true));
    }

    @Test
    void testCancelScheduledNotification_Success() throws Exception {
        // Given
        Long notificationId = 1L;
        String userId = "user123";
        when(notificationService.cancelScheduledNotification(notificationId, userId)).thenReturn(true);

        // When & Then
        mockMvc.perform(delete("/api/notifications/{id}/cancel", notificationId)
                        .param("userId", userId))
                .andExpect(status().isOk())
                .andExpect(content().string("Notification cancelled successfully"));
    }

    @Test
    void testCancelScheduledNotification_NotFound() throws Exception {
        // Given
        Long notificationId = 1L;
        String userId = "user123";
        when(notificationService.cancelScheduledNotification(notificationId, userId)).thenReturn(false);

        // When & Then
        mockMvc.perform(delete("/api/notifications/{id}/cancel", notificationId)
                        .param("userId", userId))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Notification not found, not scheduled, or not owned by user"));
    }

    @Test
    void testCreateScheduledNotification() throws Exception {
        // Given
        when(notificationService.createNotification(any(NotificationRequest.class))).thenReturn(testNotification);

        // When & Then
        mockMvc.perform(post("/api/notifications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.userId").value("user123"))
                .andExpect(jsonPath("$.status").value("SCHEDULED"))
                .andExpect(jsonPath("$.scheduled").value(true));
    }

    @Test
    void testGetAllUserNotifications() throws Exception {
        // Given
        String userId = "user123";
        List<NotificationResponse> allNotifications = Arrays.asList(testNotification);
        when(notificationService.getAllUserNotifications(userId)).thenReturn(allNotifications);

        // When & Then
        mockMvc.perform(get("/api/notifications/user/all/{userId}", userId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].userId").value("user123"));
    }

    @Test
    void testGetUserNotifications() throws Exception {
        // Given
        String userId = "user123";
        List<NotificationResponse> sentNotifications = Arrays.asList(testNotification);
        when(notificationService.getUserNotifications(userId)).thenReturn(sentNotifications);

        // When & Then
        mockMvc.perform(get("/api/notifications/user/{userId}", userId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].userId").value("user123"));
    }
} 