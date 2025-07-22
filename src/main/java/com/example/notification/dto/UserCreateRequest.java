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

    // Constructors
    public UserCreateRequest() {}

    // Getters and setters
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getFcmToken() { return fcmToken; }
    public void setFcmToken(String fcmToken) { this.fcmToken = fcmToken; }

    public UserType getUserType() { return userType; }
    public void setUserType(UserType userType) { this.userType = userType; }
}
