package com.example.notification.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class RateLimitService {

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    private static final String RATE_LIMIT_PREFIX = "rate_limit:";
    private static final String USER_RATE_LIMIT_PREFIX = "user_rate_limit:";

    /**
     * Check if user has exceeded rate limit for a specific endpoint
     * @param userId User ID
     * @param endpoint Endpoint name (e.g., "create_notification", "get_notifications")
     * @param maxRequests Maximum requests allowed
     * @param windowSeconds Time window in seconds
     * @return true if rate limit is exceeded, false otherwise
     */
    public boolean isRateLimitExceeded(String userId, String endpoint, int maxRequests, int windowSeconds) {
        String key = USER_RATE_LIMIT_PREFIX + userId + ":" + endpoint;
        
        // Get current count
        String currentCount = redisTemplate.opsForValue().get(key);
        int count = currentCount != null ? Integer.parseInt(currentCount) : 0;
        
        if (count >= maxRequests) {
            return true; // Rate limit exceeded
        }
        
        // Increment count
        if (count == 0) {
            // First request in this window, set with expiration
            redisTemplate.opsForValue().set(key, "1", windowSeconds, TimeUnit.SECONDS);
        } else {
            // Increment existing count
            redisTemplate.opsForValue().increment(key);
        }
        
        return false;
    }

    /**
     * Check global rate limit for an endpoint
     * @param endpoint Endpoint name
     * @param maxRequests Maximum requests allowed
     * @param windowSeconds Time window in seconds
     * @return true if rate limit is exceeded, false otherwise
     */
    public boolean isGlobalRateLimitExceeded(String endpoint, int maxRequests, int windowSeconds) {
        String key = RATE_LIMIT_PREFIX + endpoint;
        
        // Get current count
        String currentCount = redisTemplate.opsForValue().get(key);
        int count = currentCount != null ? Integer.parseInt(currentCount) : 0;
        
        if (count >= maxRequests) {
            return true; // Rate limit exceeded
        }
        
        // Increment count
        if (count == 0) {
            // First request in this window, set with expiration
            redisTemplate.opsForValue().set(key, "1", windowSeconds, TimeUnit.SECONDS);
        } else {
            // Increment existing count
            redisTemplate.opsForValue().increment(key);
        }
        
        return false;
    }

    /**
     * Get remaining requests for a user on a specific endpoint
     * @param userId User ID
     * @param endpoint Endpoint name
     * @param maxRequests Maximum requests allowed
     * @return Number of remaining requests
     */
    public int getRemainingRequests(String userId, String endpoint, int maxRequests) {
        String key = USER_RATE_LIMIT_PREFIX + userId + ":" + endpoint;
        String currentCount = redisTemplate.opsForValue().get(key);
        int count = currentCount != null ? Integer.parseInt(currentCount) : 0;
        return Math.max(0, maxRequests - count);
    }

    /**
     * Get time until rate limit resets for a user
     * @param userId User ID
     * @param endpoint Endpoint name
     * @return Time in seconds until reset, or -1 if no rate limit is active
     */
    public long getTimeUntilReset(String userId, String endpoint) {
        String key = USER_RATE_LIMIT_PREFIX + userId + ":" + endpoint;
        Long ttl = redisTemplate.getExpire(key, TimeUnit.SECONDS);
        return ttl != null ? ttl : -1;
    }

    /**
     * Reset rate limit for a user on a specific endpoint
     * @param userId User ID
     * @param endpoint Endpoint name
     */
    public void resetRateLimit(String userId, String endpoint) {
        String key = USER_RATE_LIMIT_PREFIX + userId + ":" + endpoint;
        redisTemplate.delete(key);
    }

    /**
     * Reset global rate limit for an endpoint
     * @param endpoint Endpoint name
     */
    public void resetGlobalRateLimit(String endpoint) {
        String key = RATE_LIMIT_PREFIX + endpoint;
        redisTemplate.delete(key);
    }
} 