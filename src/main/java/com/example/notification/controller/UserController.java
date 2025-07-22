package com.example.notification.controller;

import com.example.notification.dto.UserCreateRequest;
import com.example.notification.entity.User;
import com.example.notification.enums.NotificationChannel;
import com.example.notification.enums.UserType;
import com.example.notification.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping
    public ResponseEntity<User> createUser(@RequestBody UserCreateRequest request) {
        try {
            User user = new User(request.getUserId(), request.getEmail(), request.getUserType());
            user.setPhoneNumber(request.getPhoneNumber());
            user.setFcmToken(request.getFcmToken());

            // Set default channel preferences
            Map<NotificationChannel, Boolean> preferences = new HashMap<>();
            preferences.put(NotificationChannel.EMAIL, true);
            preferences.put(NotificationChannel.SMS, true);
            preferences.put(NotificationChannel.PUSH, true);
            preferences.put(NotificationChannel.IN_APP, true);
            preferences.put(NotificationChannel.WEBSOCKET, true);
            user.setChannelPreferences(preferences);

            User savedUser = userService.save(user);
            return new ResponseEntity<>(savedUser, HttpStatus.CREATED);
        } catch (Exception e) {
            System.err.println("Error creating user: " + e.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/{userId}")
    public ResponseEntity<User> getUser(@PathVariable String userId) {
        try {
            Optional<User> user = userService.findById(userId);
            return user.map(ResponseEntity::ok)
                    .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
        } catch (Exception e) {
            System.err.println("Error fetching user: " + e.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/{userId}/preferences")
    public ResponseEntity<User> updateChannelPreferences(
            @PathVariable String userId,
            @RequestBody Map<NotificationChannel, Boolean> preferences) {
        try {
            Optional<User> optionalUser = userService.findById(userId);
            if (optionalUser.isPresent()) {
                User user = optionalUser.get();
                user.setChannelPreferences(preferences);
                User updatedUser = userService.save(user);
                return ResponseEntity.ok(updatedUser);
            } else {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            System.err.println("Error updating user preferences: " + e.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
