package com.example.notification.example;

import com.example.notification.annotation.RateLimit;
import com.example.notification.annotation.RequireRole;
import com.example.notification.enums.UserType;
import org.springframework.web.bind.annotation.*;

/**
 * Example class demonstrating how to use the security features
 * This is for documentation purposes only
 */
@RestController
@RequestMapping("/api/example")
public class SecurityUsageExample {

    /**
     * Example 1: Basic role-based access control
     * Only REGULAR, VIP, and ADMIN users can access this endpoint
     */
    @GetMapping("/basic")
    @RequireRole({UserType.REGULAR, UserType.VIP, UserType.ADMIN})
    @RateLimit(maxRequests = 100, windowSeconds = 60, endpoint = "basic_example")
    public String basicAccess() {
        return "Basic access granted";
    }

    /**
     * Example 2: VIP and Admin only access
     * Only VIP and ADMIN users can access this endpoint
     */
    @GetMapping("/premium")
    @RequireRole({UserType.VIP, UserType.ADMIN})
    @RateLimit(maxRequests = 50, windowSeconds = 60, endpoint = "premium_example")
    public String premiumAccess() {
        return "Premium access granted";
    }

    /**
     * Example 3: Admin only access
     * Only ADMIN users can access this endpoint
     */
    @GetMapping("/admin")
    @RequireRole({UserType.ADMIN})
    @RateLimit(maxRequests = 30, windowSeconds = 60, endpoint = "admin_example")
    public String adminAccess() {
        return "Admin access granted";
    }

    /**
     * Example 4: Ownership verification
     * Users can only access their own resources
     */
    @GetMapping("/user/{userId}/profile")
    @RequireRole(value = {UserType.REGULAR, UserType.VIP, UserType.ADMIN}, requireOwnership = true)
    @RateLimit(maxRequests = 20, windowSeconds = 60, endpoint = "user_profile")
    public String userProfile(@PathVariable String userId) {
        return "User profile for: " + userId;
    }

    /**
     * Example 5: Global rate limiting
     * Global rate limit applies to all users combined
     */
    @GetMapping("/public")
    @RateLimit(maxRequests = 1000, windowSeconds = 60, endpoint = "public_endpoint", global = true)
    public String publicEndpoint() {
        return "Public endpoint - global rate limited";
    }

    /**
     * Example 6: High rate limit for admin operations
     * Admin operations can have higher rate limits
     */
    @PostMapping("/admin/bulk-operation")
    @RequireRole({UserType.ADMIN})
    @RateLimit(maxRequests = 10, windowSeconds = 60, endpoint = "admin_bulk_operation")
    public String adminBulkOperation() {
        return "Admin bulk operation completed";
    }

    /**
     * Example 7: Different rate limits for different operations
     * Read operations can have higher limits than write operations
     */
    @GetMapping("/data")
    @RequireRole({UserType.REGULAR, UserType.VIP, UserType.ADMIN})
    @RateLimit(maxRequests = 200, windowSeconds = 60, endpoint = "read_data")
    public String readData() {
        return "Data read successfully";
    }

    @PostMapping("/data")
    @RequireRole({UserType.REGULAR, UserType.VIP, UserType.ADMIN})
    @RateLimit(maxRequests = 20, windowSeconds = 60, endpoint = "write_data")
    public String writeData() {
        return "Data written successfully";
    }
} 