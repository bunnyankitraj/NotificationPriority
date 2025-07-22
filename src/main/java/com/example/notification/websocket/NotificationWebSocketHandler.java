package com.example.notification.websocket;

import com.example.notification.dto.WebSocketNotificationMessage;
import com.example.notification.entity.Notification;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
public class NotificationWebSocketHandler implements WebSocketHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ConcurrentHashMap<String, CopyOnWriteArrayList<WebSocketSession>> userSessions = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String userId = getUserIdFromSession(session);
        if (userId != null) {
            userSessions.computeIfAbsent(userId, k -> new CopyOnWriteArrayList<>()).add(session);
            System.out.println("WebSocket connection established for user: " + userId);
        }
    }

    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
        // Handle incoming messages if needed
        System.out.println("Received WebSocket message: " + message.getPayload());
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        System.err.println("WebSocket transport error: " + exception.getMessage());
        removeSession(session);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
        removeSession(session);
        String userId = getUserIdFromSession(session);
        System.out.println("WebSocket connection closed for user: " + userId);
    }

    @Override
    public boolean supportsPartialMessages() {
        return false;
    }

    public boolean sendNotificationToUser(String userId, Notification notification) {
        CopyOnWriteArrayList<WebSocketSession> sessions = userSessions.get(userId);
        if (sessions == null || sessions.isEmpty()) {
            System.out.println("No active WebSocket sessions for user: " + userId);
            return false;
        }

        try {
            String messageJson = objectMapper.writeValueAsString(createWebSocketMessage(notification));
            TextMessage textMessage = new TextMessage(messageJson);

            boolean sentToAtLeastOne = false;
            for (WebSocketSession session : sessions) {
                if (session.isOpen()) {
                    session.sendMessage(textMessage);
                    sentToAtLeastOne = true;
                } else {
                    sessions.remove(session);
                }
            }

            return sentToAtLeastOne;
        } catch (IOException e) {
            System.err.println("Error sending WebSocket notification: " + e.getMessage());
            return false;
        }
    }

    private void removeSession(WebSocketSession session) {
        String userId = getUserIdFromSession(session);
        if (userId != null) {
            CopyOnWriteArrayList<WebSocketSession> sessions = userSessions.get(userId);
            if (sessions != null) {
                sessions.remove(session);
                if (sessions.isEmpty()) {
                    userSessions.remove(userId);
                }
            }
        }
    }

    private String getUserIdFromSession(WebSocketSession session) {
        // Extract userId from session attributes or query parameters
        // This is a simplified version - in real implementation, you'd handle authentication
        String query = session.getUri().getQuery();
        if (query != null && query.startsWith("userId=")) {
            return query.substring(7);
        }
        return null;
    }

    private WebSocketNotificationMessage createWebSocketMessage(Notification notification) {
        return new WebSocketNotificationMessage(
                notification.getId(),
                notification.getTitle(),
                notification.getMessage(),
                notification.getPriority().name(),
                notification.getChannel().name(),
                "NOTIFICATION"
        );
    }

}
