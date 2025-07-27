package com.example.notification.service;

import com.example.notification.entity.User;
import com.example.notification.enums.UserType;
import com.example.notification.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public Optional<User> findById(String userId) {
        return userRepository.findById(userId);
    }

    public User save(User user) {
        return userRepository.save(user);
    }

    public boolean isVIPUser(String userId) {
        return findById(userId)
                .map(user -> user.getUserType() == UserType.VIP || user.getUserType() == UserType.ADMIN)
                .orElse(false);
    }

    // NEW: Check if user is admin
    public boolean isAdminUser(String userId) {
        return findById(userId)
                .map(user -> user.getUserType() == UserType.ADMIN)
                .orElse(false);
    }

    // NEW: Check if user has admin or VIP role
    public boolean hasAdminOrVIPRole(String userId) {
        return findById(userId)
                .map(user -> user.getUserType() == UserType.ADMIN || user.getUserType() == UserType.VIP)
                .orElse(false);
    }

    // NEW: Get user type
    public Optional<UserType> getUserType(String userId) {
        return findById(userId).map(User::getUserType);
    }
}
