package com.example.notification.entity;

import com.example.notification.enums.NotificationChannel;
import com.example.notification.enums.UserType;
import jakarta.persistence.*;
import lombok.Data;

import java.util.Map;

@Data
@Entity
@Table(name = "users")
public class User {
    @Id
    private String userId;

    @Column(nullable = false)
    private String email;

    private String phoneNumber;
    private String fcmToken;

    @Enumerated(EnumType.STRING)
    private UserType userType;

    @ElementCollection
    @CollectionTable(name = "user_preferences")
    private Map<NotificationChannel, Boolean> channelPreferences;

    public User() {}

    public User(String userId, String email, UserType userType) {
        this.userId = userId;
        this.email = email;
        this.userType = userType;
    }


}