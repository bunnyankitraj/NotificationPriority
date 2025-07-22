package com.example.notification.dto;

import com.example.notification.enums.UserType;
import lombok.Data;

@Data
public class UserCreateRequest {
    private String userId;
    private String email;
    private String phoneNumber;
    private String fcmToken;
    private UserType userType;
}
