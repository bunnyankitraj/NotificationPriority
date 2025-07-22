package com.example.notification.dto;

import lombok.Data;

@Data
public  class WebSocketNotificationMessage {
    private Long id;
    private String title;
    private String message;
    private String priority;
    private String channel;
    private String type;

    public WebSocketNotificationMessage(Long id, String title, String message,
                                        String priority, String channel, String type) {
        this.id = id;
        this.title = title;
        this.message = message;
        this.priority = priority;
        this.channel = channel;
        this.type = type;
    }
}
