package com.example.notification.aspect;

import com.example.notification.annotation.RateLimit;
import com.example.notification.service.RateLimitService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;

@Aspect
@Component
public class RateLimitAspect {

    @Autowired
    private RateLimitService rateLimitService;

    @Around("@annotation(com.example.notification.annotation.RateLimit)")
    public Object checkRateLimit(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        RateLimit rateLimit = method.getAnnotation(RateLimit.class);

        // Get endpoint name
        String endpoint = rateLimit.endpoint();
        if (endpoint.isEmpty()) {
            endpoint = method.getName();
        }

        // Get user ID from request
        String userId = getUserIdFromRequest();
        
        boolean rateLimitExceeded;
        
        if (rateLimit.global()) {
            // Check global rate limit
            rateLimitExceeded = rateLimitService.isGlobalRateLimitExceeded(
                endpoint, rateLimit.maxRequests(), rateLimit.windowSeconds());
        } else {
            // Check user-specific rate limit
            if (userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("User ID is required for rate limiting");
            }
            
            rateLimitExceeded = rateLimitService.isRateLimitExceeded(
                userId, endpoint, rateLimit.maxRequests(), rateLimit.windowSeconds());
        }

        if (rateLimitExceeded) {
            // Add rate limit headers
            ResponseEntity<?> response = ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .header("X-RateLimit-Limit", String.valueOf(rateLimit.maxRequests()))
                    .header("X-RateLimit-Window", String.valueOf(rateLimit.windowSeconds()))
                    .body("Rate limit exceeded. Please try again later.");
            
            // Add remaining time header if user-specific
            if (!rateLimit.global() && userId != null) {
                long timeUntilReset = rateLimitService.getTimeUntilReset(userId, endpoint);
                if (timeUntilReset > 0) {
                    response = ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                            .header("X-RateLimit-Limit", String.valueOf(rateLimit.maxRequests()))
                            .header("X-RateLimit-Window", String.valueOf(rateLimit.windowSeconds()))
                            .header("X-RateLimit-Reset", String.valueOf(timeUntilReset))
                            .body("Rate limit exceeded. Please try again in " + timeUntilReset + " seconds.");
                }
            }
            
            return response;
        }

        // Proceed with the method execution
        return joinPoint.proceed();
    }

    private String getUserIdFromRequest() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            
            // Try to get from request parameter first
            String userId = request.getParameter("userId");
            if (userId != null && !userId.trim().isEmpty()) {
                return userId;
            }

            // Try to get from header
            userId = request.getHeader("X-User-ID");
            if (userId != null && !userId.trim().isEmpty()) {
                return userId;
            }

            // Try to get from Authorization header (Bearer token format)
            String authHeader = request.getHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                // In a real implementation, you would decode the JWT token
                // For now, we'll assume the token contains the user ID
                return authHeader.substring(7); // Remove "Bearer " prefix
            }
        }
        return null;
    }
} 