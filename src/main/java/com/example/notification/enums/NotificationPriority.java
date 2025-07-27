package com.example.notification.enums;

import lombok.Getter;

@Getter
public enum NotificationPriority {
    CRITICAL(1),    // Highest priority
    HIGH(2),        // High priority
    MEDIUM(3),      // Medium priority
    LOW(4);         // Lowest priority

    private final int level;

    NotificationPriority(int level) {
        this.level = level;
    }

    // Remove delayMs - no more artificial delays
    // Priority is now handled purely by queue priority and processing order
}
