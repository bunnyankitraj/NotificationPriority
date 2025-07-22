package com.example.notification.dto;

import com.example.notification.enums.NotificationChannel;
import com.example.notification.enums.NotificationPriority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Map;

@Data
@AllArgsConstructor
public class NotificationRequest {
    @NotBlank
    private String userId;

    @NotBlank
    private String title;

    @NotBlank
    private String message;

    @NotNull
    private NotificationPriority priority;

    @NotNull
    private NotificationChannel channel;

    private Map<String, String> metadata;

}
