# Notification System with Priority Handling

## Overview

This is a Spring Boot-based notification system that handles different types of notifications (EMAIL, SMS, PUSH, IN_APP, WEBSOCKET) with priority-based processing. The system uses RabbitMQ for message queuing with priority queues, Redis for caching, and PostgreSQL for data persistence.

## Key Features

1. **Priority-based Notification Processing**
   - Four priority levels: CRITICAL, HIGH, MEDIUM, LOW
   - Each priority has different processing delays
   - VIP users get automatic priority boosts

2. **Multiple Notification Channels**
   - Email notifications
   - SMS notifications
   - Push notifications (FCM)
   - In-app notifications
   - WebSocket notifications

3. **Reliable Delivery**
   - Retry mechanism for failed notifications
   - Audit trail for all notification events
   - Status tracking (PENDING, PROCESSING, SENT, FAILED, RETRYING)

4. **Scalable Architecture**
   - RabbitMQ for message queuing with separate queues for each priority
   - WebSocket support for real-time notifications
   - Asynchronous processing

## System Components

### Core Modules

1. **Notification Service**
   - Handles creation and management of notifications
   - Processes notifications based on priority
   - Manages retries for failed notifications

2. **User Service**
   - Manages user information and preferences
   - Handles VIP status checks
   - Stores channel preferences

3. **Priority Service**
   - Calculates final priority (including VIP boosts)
   - Determines routing keys and queue names

4. **Queue Service**
   - Handles sending notifications to appropriate RabbitMQ queues
   - Sets message priorities

### Notification Processors

1. **EmailNotificationProcessor** - Handles email notifications
2. **SMSNotificationProcessor** - Handles SMS notifications
3. **PushNotificationProcessor** - Handles mobile push notifications
4. **InAppNotificationProcessor** - Handles in-app notifications
5. **WebSocketNotificationProcessor** - Handles real-time WebSocket notifications

### Infrastructure

1. **RabbitMQ** - Message broker with priority queues
2. **PostgreSQL** - Primary data store
3. **Redis** - Caching and WebSocket session management
4. **WebSocket** - Real-time notification delivery

## API Endpoints

### Notification Endpoints

- `POST /api/notifications` - Create a new notification
- `GET /api/notifications/user/{userId}` - Get notifications for a user
- `GET /api/notifications/{id}` - Get a specific notification
- `POST /api/notifications/bulk` - Create multiple notifications at once

### User Endpoints

- `POST /api/users` - Create a new user
- `GET /api/users/{userId}` - Get user details
- `PUT /api/users/{userId}/preferences` - Update user notification preferences

### Audit Endpoints

- `GET /api/audit/notification/{notificationId}` - Get audit trail for a notification
- `GET /api/audit/user/{userId}` - Get audit trail for a user

## Setup Instructions

### Prerequisites

- Java 17+
- Docker and Docker Compose
- Maven

### Running the System

1. **Start infrastructure services:**

```bash
docker-compose up -d
```

2. **Build and run the application:**

```bash
mvn clean install
java -jar target/notification-system-0.0.1-SNAPSHOT.jar
```

### Configuration

Update the `application.yml` file with your specific configurations:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/testdb
    username: myuser
    password: mypass

  redis:
    host: localhost
    port: 6379

  rabbitmq:
    host: localhost
    port: 5672
    username: guest
    password: guest

  mail:
    host: smtp.gmail.com
    port: 587
    username: youremail
    password: yourpassword
```

## Priority Handling

The system implements priority-based processing with the following characteristics:

| Priority  | Level | Delay | Queue Priority | Description                          |
|-----------|-------|-------|----------------|--------------------------------------|
| CRITICAL  | 1     | 0ms   | 10             | Highest priority, processed immediately |
| HIGH      | 2     | 1s    | 8              | Processed after 1 second delay       |
| MEDIUM    | 3     | 5s    | 5              | Processed after 5 seconds delay      |
| LOW       | 4     | 15s   | 2              | Processed after 15 seconds delay     |

VIP users receive automatic priority boosts:
- LOW → MEDIUM
- MEDIUM → HIGH
- HIGH → CRITICAL
- CRITICAL remains CRITICAL

## Monitoring

- RabbitMQ Management Console: http://localhost:15672
  - Username: guest
  - Password: guest

## Future Enhancements

1. Add rate limiting for notifications
2. Implement notification templates
3. Add support for scheduled notifications
4. Enhance monitoring and metrics
5. Add support for notification groups and batching
6. Implement dead-letter queue for failed notifications

## Troubleshooting

1. **RabbitMQ connection issues:**
   - Verify RabbitMQ is running (`docker ps`)
   - Check connection details in `application.yml`

2. **Database connection issues:**
   - Verify PostgreSQL container is running
   - Check database credentials in `application.yml`

3. **Notification delivery failures:**
   - Check audit logs via `/api/audit` endpoints
   - Verify channel-specific configurations (e.g., SMTP for email)
