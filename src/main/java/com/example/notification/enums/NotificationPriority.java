package com.example.notification.enums;

public enum NotificationPriority {
    CRITICAL(1, 0), // Highest priority, no delay
    HIGH(2, 1000),  // 1 second delay
    MEDIUM(3, 5000), // 5 seconds delay
    LOW(4, 15000);   // 15 seconds delay

    private final int level;
    private final long delayMs;

    NotificationPriority(int level, long delayMs) {
        this.level = level;
        this.delayMs = delayMs;
    }

    public int getLevel() { return level; }
    public long getDelayMs() { return delayMs; }
}
