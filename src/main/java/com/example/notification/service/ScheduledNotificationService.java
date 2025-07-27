package com.example.notification.service;

import com.example.notification.entity.Notification;
import com.example.notification.enums.NotificationStatus;
import com.example.notification.repository.NotificationRepository;
import org.hibernate.validator.internal.util.stereotypes.Lazy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

@Service
public class ScheduledNotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private NotificationProcessor notificationProcessor;

    @Autowired
    private TaskScheduler taskScheduler;

    private final ConcurrentHashMap<Long, ScheduledFuture<?>> scheduledTasks = new ConcurrentHashMap<>();

    public void scheduleNotification(Notification notification) {
        if (notification.getScheduledAt() != null && notification.getScheduledAt().isAfter(LocalDateTime.now())) {
            Date scheduleTime = Date.from(notification.getScheduledAt()
                    .atZone(ZoneId.systemDefault()).toInstant());

            ScheduledFuture<?> scheduledTask = taskScheduler.schedule(
                    () -> {
                        System.out.println("⏰ Processing scheduled notification: " + notification.getId());
                        notificationProcessor.processScheduledNotification(notification.getId());
                        scheduledTasks.remove(notification.getId());
                    },
                    scheduleTime
            );

            scheduledTasks.put(notification.getId(), scheduledTask);
            System.out.println("📅 Scheduled notification " + notification.getId() +
                    " for " + notification.getScheduledAt());
        }
    }

    public boolean cancelScheduledNotification(Long notificationId) {
        ScheduledFuture<?> scheduledTask = scheduledTasks.remove(notificationId);
        if (scheduledTask != null && !scheduledTask.isDone()) {
            boolean cancelled = scheduledTask.cancel(false);
            System.out.println("❌ Cancelled scheduled notification: " + notificationId);
            return cancelled;
        }
        return false;
    }

    @Scheduled(fixedRate = 60000)
    public void processMissedScheduledNotifications() {
        LocalDateTime now = LocalDateTime.now();
        List<Notification> missedNotifications = notificationRepository
                .findScheduledNotificationsPastDue(NotificationStatus.SCHEDULED, now);

        for (Notification notification : missedNotifications) {
            System.out.println("🔄 Processing missed scheduled notification: " + notification.getId());
            notificationProcessor.processScheduledNotification(notification.getId());
        }

        if (!missedNotifications.isEmpty()) {
            System.out.println("✅ Processed " + missedNotifications.size() + " missed scheduled notifications");
        }
    }

    // Get statistics about scheduled notifications
    public ScheduledStats getScheduledStats() {
        long totalScheduled = notificationRepository.countByStatus(NotificationStatus.SCHEDULED);
        int activeTasks = scheduledTasks.size();

        return new ScheduledStats(totalScheduled, activeTasks);
    }

    public static class ScheduledStats {
        private final long totalScheduled;
        private final int activeTasks;

        public ScheduledStats(long totalScheduled, int activeTasks) {
            this.totalScheduled = totalScheduled;
            this.activeTasks = activeTasks;
        }

        public long getTotalScheduled() { return totalScheduled; }
        public int getActiveTasks() { return activeTasks; }
    }
}
