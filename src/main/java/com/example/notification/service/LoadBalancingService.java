package com.example.notification.service;

import com.example.notification.entity.Notification;
import com.example.notification.enums.NotificationPriority;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class LoadBalancingService {

    private final AtomicInteger pendingCritical = new AtomicInteger(0);
    private final AtomicInteger pendingHigh = new AtomicInteger(0);
    private final AtomicInteger pendingMedium = new AtomicInteger(0);
    private final AtomicInteger pendingLow = new AtomicInteger(0);

    private final AtomicLong totalProcessed = new AtomicLong(0);

    @Autowired
    private QueueService queueService;

    public boolean shouldProcessImmediately(Notification notification) {
        // Always process CRITICAL immediately
        if (notification.getPriority() == NotificationPriority.CRITICAL) {
            pendingCritical.incrementAndGet();
            return true;
        }

        // Check load - if system is under heavy load, prioritize more aggressively
        int totalPending = getTotalPendingCount();

        if (totalPending > 10000) { // Heavy load threshold
            // Under heavy load, only process CRITICAL and HIGH immediately
            if (notification.getPriority() == NotificationPriority.HIGH) {
                pendingHigh.incrementAndGet();
                return true;
            }
            return false; // Queue MEDIUM and LOW
        }

        // Normal load - process all
        updatePendingCount(notification.getPriority(), 1);
        return true;
    }

    public void markProcessed(Notification notification) {
        updatePendingCount(notification.getPriority(), -1);
        totalProcessed.incrementAndGet();
    }

    private void updatePendingCount(NotificationPriority priority, int delta) {
        switch (priority) {
            case CRITICAL -> pendingCritical.addAndGet(delta);
            case HIGH -> pendingHigh.addAndGet(delta);
            case MEDIUM -> pendingMedium.addAndGet(delta);
            case LOW -> pendingLow.addAndGet(delta);
        }
    }

    private int getTotalPendingCount() {
        return pendingCritical.get() + pendingHigh.get() +
                pendingMedium.get() + pendingLow.get();
    }

    public LoadStats getLoadStats() {
        return new LoadStats(
                pendingCritical.get(),
                pendingHigh.get(),
                pendingMedium.get(),
                pendingLow.get(),
                totalProcessed.get()
        );
    }

    public static class LoadStats {
        private final int pendingCritical;
        private final int pendingHigh;
        private final int pendingMedium;
        private final int pendingLow;
        private final long totalProcessed;

        public LoadStats(int pendingCritical, int pendingHigh, int pendingMedium,
                         int pendingLow, long totalProcessed) {
            this.pendingCritical = pendingCritical;
            this.pendingHigh = pendingHigh;
            this.pendingMedium = pendingMedium;
            this.pendingLow = pendingLow;
            this.totalProcessed = totalProcessed;
        }

        // Getters
        public int getPendingCritical() { return pendingCritical; }
        public int getPendingHigh() { return pendingHigh; }
        public int getPendingMedium() { return pendingMedium; }
        public int getPendingLow() { return pendingLow; }
        public long getTotalProcessed() { return totalProcessed; }
        public int getTotalPending() { return pendingCritical + pendingHigh + pendingMedium + pendingLow; }
    }
}
