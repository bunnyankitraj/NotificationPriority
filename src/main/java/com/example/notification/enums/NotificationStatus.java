package com.example.notification.enums;

public enum NotificationStatus {
    PENDING,
    SCHEDULED,    // NEW: For notifications waiting for scheduled time
    PROCESSING,
    SENT,
    FAILED,
    RETRYING
}
