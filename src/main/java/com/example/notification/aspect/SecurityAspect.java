package com.example.notification.aspect;

import com.example.notification.annotation.RequireRole;
import com.example.notification.enums.UserType;
import com.example.notification.service.NotificationService;
import com.example.notification.service.UserService;
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
import java.util.Arrays;
import java.util.Optional;

@Aspect
@Component
public class SecurityAspect {

    @Autowired
    private UserService userService;

    @Autowired
    private NotificationService notificationService;

    @Around("@annotation(com.example.notification.annotation.RequireRole)")
    public Object checkRoleAccess(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        RequireRole requireRole = method.getAnnotation(RequireRole.class);

        // Get user ID from request parameters or headers
        String userId = getUserIdFromRequest();
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("User ID is required");
        }

        // Check if user exists
        Optional<UserType> userType = userService.getUserType(userId);
        if (userType.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("User not found");
        }

        // Check role access
        UserType[] requiredRoles = requireRole.value();
        if (requiredRoles.length > 0 && !Arrays.asList(requiredRoles).contains(userType.get())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Insufficient privileges. Required roles: " + Arrays.toString(requiredRoles));
        }

        // Check ownership if required
        if (requireRole.requireOwnership()) {
            Long notificationId = getNotificationIdFromRequest();
            if (notificationId != null && !notificationService.verifyNotificationOwnership(notificationId, userId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("Access denied. You can only access your own notifications.");
            }
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

    private Long getNotificationIdFromRequest() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            String pathInfo = request.getPathInfo();
            
            if (pathInfo != null) {
                String[] pathParts = pathInfo.split("/");
                for (int i = 0; i < pathParts.length - 1; i++) {
                    if (pathParts[i].equals("notifications") && i + 1 < pathParts.length) {
                        try {
                            return Long.parseLong(pathParts[i + 1]);
                        } catch (NumberFormatException e) {
                            return null;
                        }
                    }
                }
            }
        }
        return null;
    }
} 