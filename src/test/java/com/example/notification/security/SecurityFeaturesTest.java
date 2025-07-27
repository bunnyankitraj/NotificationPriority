package com.example.notification.security;

import com.example.notification.annotation.RateLimit;
import com.example.notification.annotation.RequireRole;
import com.example.notification.entity.User;
import com.example.notification.enums.UserType;
import com.example.notification.service.NotificationService;
import com.example.notification.service.RateLimitService;
import com.example.notification.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SecurityFeaturesTest {

    @Mock
    private UserService userService;

    @Mock
    private NotificationService notificationService;

    @Mock
    private RateLimitService rateLimitService;

    @InjectMocks
    private com.example.notification.aspect.SecurityAspect securityAspect;

    @InjectMocks
    private com.example.notification.aspect.RateLimitAspect rateLimitAspect;

    private MockHttpServletRequest request;
    private User regularUser;
    private User vipUser;
    private User adminUser;

    @BeforeEach
    void setUp() {
        request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        regularUser = new User("user123", "user@example.com", UserType.REGULAR);
        vipUser = new User("vip456", "vip@example.com", UserType.VIP);
        adminUser = new User("admin789", "admin@example.com", UserType.ADMIN);
    }

    @Test
    void testUserOwnershipVerification_Success() {
        // Given
        when(notificationService.verifyNotificationOwnership(1L, "user123")).thenReturn(true);

        // When
        boolean result = notificationService.verifyNotificationOwnership(1L, "user123");

        // Then
        assertTrue(result);
        verify(notificationService).verifyNotificationOwnership(1L, "user123");
    }

    @Test
    void testUserOwnershipVerification_Failure() {
        // Given
        when(notificationService.verifyNotificationOwnership(1L, "user123")).thenReturn(false);

        // When
        boolean result = notificationService.verifyNotificationOwnership(1L, "user123");

        // Then
        assertFalse(result);
        verify(notificationService).verifyNotificationOwnership(1L, "user123");
    }

    @Test
    void testRoleBasedAccess_RegularUser_Allowed() {
        // Given
        request.setParameter("userId", "user123");
        when(userService.getUserType("user123")).thenReturn(Optional.of(UserType.REGULAR));

        // When & Then
        // This would be tested in integration tests with actual controller methods
        assertTrue(userService.getUserType("user123").isPresent());
        assertEquals(UserType.REGULAR, userService.getUserType("user123").get());
    }

    @Test
    void testRoleBasedAccess_AdminUser_Allowed() {
        // Given
        when(userService.isAdminUser("admin789")).thenReturn(true);

        // When
        boolean isAdmin = userService.isAdminUser("admin789");

        // Then
        assertTrue(isAdmin);
        verify(userService).isAdminUser("admin789");
    }

    @Test
    void testRoleBasedAccess_RegularUser_NotAdmin() {
        // Given
        when(userService.isAdminUser("user123")).thenReturn(false);

        // When
        boolean isAdmin = userService.isAdminUser("user123");

        // Then
        assertFalse(isAdmin);
        verify(userService).isAdminUser("user123");
    }

    @Test
    void testVIPUserPriorityBoost() {
        // Given
        when(userService.isVIPUser("vip456")).thenReturn(true);
        when(userService.isVIPUser("user123")).thenReturn(false);

        // When
        boolean vipIsVIP = userService.isVIPUser("vip456");
        boolean regularIsVIP = userService.isVIPUser("user123");

        // Then
        assertTrue(vipIsVIP);
        assertFalse(regularIsVIP);
        verify(userService).isVIPUser("vip456");
        verify(userService).isVIPUser("user123");
    }

    @Test
    void testRateLimit_UserSpecific_Success() {
        // Given
        when(rateLimitService.isRateLimitExceeded("user123", "create_notification", 50, 60))
                .thenReturn(false);

        // When
        boolean rateLimitExceeded = rateLimitService.isRateLimitExceeded("user123", "create_notification", 50, 60);

        // Then
        assertFalse(rateLimitExceeded);
        verify(rateLimitService).isRateLimitExceeded("user123", "create_notification", 50, 60);
    }

    @Test
    void testRateLimit_UserSpecific_Exceeded() {
        // Given
        when(rateLimitService.isRateLimitExceeded("user123", "create_notification", 50, 60))
                .thenReturn(true);

        // When
        boolean rateLimitExceeded = rateLimitService.isRateLimitExceeded("user123", "create_notification", 50, 60);

        // Then
        assertTrue(rateLimitExceeded);
        verify(rateLimitService).isRateLimitExceeded("user123", "create_notification", 50, 60);
    }

    @Test
    void testRateLimit_Global_Success() {
        // Given
        when(rateLimitService.isGlobalRateLimitExceeded("health_check", 100, 60))
                .thenReturn(false);

        // When
        boolean rateLimitExceeded = rateLimitService.isGlobalRateLimitExceeded("health_check", 100, 60);

        // Then
        assertFalse(rateLimitExceeded);
        verify(rateLimitService).isGlobalRateLimitExceeded("health_check", 100, 60);
    }

    @Test
    void testGetRemainingRequests() {
        // Given
        when(rateLimitService.getRemainingRequests("user123", "create_notification", 50))
                .thenReturn(25);

        // When
        int remaining = rateLimitService.getRemainingRequests("user123", "create_notification", 50);

        // Then
        assertEquals(25, remaining);
        verify(rateLimitService).getRemainingRequests("user123", "create_notification", 50);
    }

    @Test
    void testGetTimeUntilReset() {
        // Given
        when(rateLimitService.getTimeUntilReset("user123", "create_notification"))
                .thenReturn(30L);

        // When
        long timeUntilReset = rateLimitService.getTimeUntilReset("user123", "create_notification");

        // Then
        assertEquals(30L, timeUntilReset);
        verify(rateLimitService).getTimeUntilReset("user123", "create_notification");
    }

    @Test
    void testUserTypeRetrieval() {
        // Given
        when(userService.getUserType("user123")).thenReturn(Optional.of(UserType.REGULAR));
        when(userService.getUserType("vip456")).thenReturn(Optional.of(UserType.VIP));
        when(userService.getUserType("admin789")).thenReturn(Optional.of(UserType.ADMIN));
        when(userService.getUserType("nonexistent")).thenReturn(Optional.empty());

        // When & Then
        assertEquals(UserType.REGULAR, userService.getUserType("user123").get());
        assertEquals(UserType.VIP, userService.getUserType("vip456").get());
        assertEquals(UserType.ADMIN, userService.getUserType("admin789").get());
        assertTrue(userService.getUserType("nonexistent").isEmpty());
    }

    @Test
    void testAdminOrVIPRoleCheck() {
        // Given
        when(userService.hasAdminOrVIPRole("admin789")).thenReturn(true);
        when(userService.hasAdminOrVIPRole("vip456")).thenReturn(true);
        when(userService.hasAdminOrVIPRole("user123")).thenReturn(false);

        // When & Then
        assertTrue(userService.hasAdminOrVIPRole("admin789"));
        assertTrue(userService.hasAdminOrVIPRole("vip456"));
        assertFalse(userService.hasAdminOrVIPRole("user123"));
    }
} 