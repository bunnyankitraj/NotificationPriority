package com.example.notification.controller;

import com.example.notification.service.LoadBalancingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/monitoring")
@CrossOrigin(origins = "*")
public class MonitoringController {

    @Autowired
    private LoadBalancingService loadBalancingService;

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getSystemStats() {
        LoadBalancingService.LoadStats stats = loadBalancingService.getLoadStats();

        Map<String, Object> response = new HashMap<>();
        response.put("pending", Map.of(
                "critical", stats.getPendingCritical(),
                "high", stats.getPendingHigh(),
                "medium", stats.getPendingMedium(),
                "low", stats.getPendingLow(),
                "total", stats.getTotalPending()
        ));
        response.put("totalProcessed", stats.getTotalProcessed());
        response.put("systemLoad", stats.getTotalPending() > 10000 ? "HIGH" : "NORMAL");
        response.put("timestamp", System.currentTimeMillis());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> healthCheck() {
        Map<String, String> health = new HashMap<>();
        health.put("status", "UP");
        health.put("service", "notification-system");
        health.put("timestamp", String.valueOf(System.currentTimeMillis()));
        return ResponseEntity.ok(health);
    }
}