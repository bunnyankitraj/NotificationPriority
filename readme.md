# 🔔 Priority-Based Notification System

A comprehensive, scalable notification system built with Spring Boot that supports multiple channels, priority-based processing, scheduled notifications, and real-time delivery.

## 🚀 Features

### Core Features
- **Multi-Channel Support**: Email, SMS, Push, In-App, WebSocket notifications
- **Priority-Based Processing**: CRITICAL, HIGH, MEDIUM, LOW priority levels
- **Scheduled Notifications**: Send notifications at specific future times
- **Real-time Delivery**: WebSocket support for instant notifications
- **VIP User Support**: Automatic priority boost for VIP/Admin users
- **Retry Logic**: Automatic retry with exponential backoff
- **Audit Trail**: Complete tracking of notification lifecycle
- **Load Balancing**: Adaptive processing based on system load

### Advanced Features
- **No Artificial Delays**: Immediate processing with priority-based resource allocation
- **Queue-Based Architecture**: RabbitMQ with separate priority queues
- **Horizontal Scaling**: Multiple consumers per priority level
- **Circuit Breaker**: Graceful handling of high-load scenarios
- **Monitoring & Analytics**: Real-time system statistics and health checks

## 🏗️ Architecture

```
┌─────────────────┐    ┌──────────────────┐    ┌─────────────────────┐
│   Client API    │───▶│  Notification    │───▶│    Priority         │
│                 │    │     Service      │    │    Service          │
└─────────────────┘    └──────────────────┘    └─────────────────────┘
                                │                          │
                                ▼                          ▼
┌─────────────────────────────────────────────────────────────────────┐
│                          Queue Service                              │
│  ┌─────────────┐ ┌─────────────┐ ┌─────────────┐ ┌─────────────┐   │
│  │  CRITICAL   │ │    HIGH     │ │   MEDIUM    │ │     LOW     │   │
│  │   Queue     │ │   Queue     │ │   Queue     │ │   Queue     │   │
│  │(10 workers) │ │(8 workers)  │ │(5 workers)  │ │(3 workers)  │   │
│  └─────────────┘ └─────────────┘ └─────────────┘ └─────────────┘   │
└─────────────────────────────────────────────────────────────────────┘
                                │
                                ▼
┌──────────────────────────────────────────────────────────────────────┐
│                    Notification Processors                          │
│  ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐   │
│  │  Email   │ │   SMS    │ │   Push   │ │  In-App  │ │WebSocket │   │
│  │Processor │ │Processor │ │Processor │ │Processor │ │Processor │   │
│  └──────────┘ └──────────┘ └──────────┘ └──────────┘ └──────────┘   │
└──────────────────────────────────────────────────────────────────────┘
```

## 🔧 Technology Stack

- **Framework**: Spring Boot 3.x
- **Database**: PostgreSQL (with H2 for testing)
- **Message Queue**: RabbitMQ
- **Cache**: Redis
- **WebSocket**: Spring WebSocket
- **Email**: Spring Mail
- **Build Tool**: Maven
- **Java Version**: 17+

## 📦 Installation & Setup

### Prerequisites
- Java 17 or higher
- Maven 3.6+
- Docker & Docker Compose
- PostgreSQL (or use Docker setup)

### Quick Start with Docker

1. **Clone the repository**
```bash
git clone <repository-url>
cd notification-system
```

2. **Start infrastructure services**
```bash
docker-compose up -d
```

This will start:
- PostgreSQL on port 5432
- RabbitMQ on ports 5672 (AMQP) and 15672 (Management UI)
- Redis on port 6379

3. **Configure application properties**
```yaml
# application.yml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/testdb
    username: myuser
    password: mypass
    driver-class-name: org.postgresql.Driver

  jpa:
    database-platform: org.hibernate.dialect.PostgreSQLDialect
    hibernate:
      ddl-auto: update
    show-sql: true

  redis:
    host: localhost
    port: 6379
    timeout: 2000

  rabbitmq:
    host: localhost
    port: 5672
    username: guest
    password: guest
    publisher-confirms: true
    publisher-returns: true

  mail:
    host: smtp.gmail.com
    port: 587
    username: your-email@gmail.com
    password: your-app-password
    properties:
      mail:
        smtp:
          auth: true
          starttls:
            enable: true
```

4. **Build and run the application**
```bash
mvn clean install
mvn spring-boot:run
```

5. **Access the application**
- API: http://localhost:8080
- RabbitMQ Management: http://localhost:15672 (guest/guest)

## 📋 API Reference

### Core Notification APIs

#### Create Immediate Notification
```http
POST /api/notifications
Content-Type: application/json

{
  "userId": "user123",
  "title": "Important Update",
  "message": "Your account has been updated successfully",
  "priority": "HIGH",
  "channel": "EMAIL",
  "metadata": {
    "category": "account",
    "template": "account_update"
  }
}
```

#### Create Scheduled Notification
```http
POST /api/notifications
Content-Type: application/json

{
  "userId": "user123",
  "title": "Meeting Reminder",
  "message": "Don't forget about your meeting at 3 PM",
  "priority": "MEDIUM",
  "channel": "PUSH",
  "scheduledAt": "2024-12-25T15:00:00"
}
```

#### Get User Notifications
```http
GET /api/notifications/user/{userId}
```

#### Get All User Notifications (including scheduled)
```http
GET /api/notifications/user/{userId}/all
```

#### Get Scheduled Notifications
```http
GET /api/notifications/user/{userId}/scheduled
```

#### Cancel Scheduled Notification
```http
DELETE /api/notifications/{notificationId}/cancel?userId={userId}
```

#### Bulk Create Notifications
```http
POST /api/notifications/bulk
Content-Type: application/json

[
  {
    "userId": "user1",
    "title": "Notification 1",
    "message": "Message 1",
    "priority": "HIGH",
    "channel": "EMAIL"
  },
  {
    "userId": "user2",
    "title": "Notification 2",
    "message": "Message 2",
    "priority": "MEDIUM",
    "channel": "SMS"
  }
]
```

### User Management APIs

#### Create User
```http
POST /api/users
Content-Type: application/json

{
  "userId": "user123",
  "email": "user@example.com",
  "phoneNumber": "+1234567890",
  "fcmToken": "fcm_token_here",
  "userType": "VIP"
}
```

#### Update User Preferences
```http
PUT /api/users/{userId}/preferences
Content-Type: application/json

{
  "EMAIL": true,
  "SMS": false,
  "PUSH": true,
  "IN_APP": true,
  "WEBSOCKET": true
}
```

### Monitoring APIs

#### System Statistics
```http
GET /api/monitoring/stats
```

Response:
```json
{
  "pending": {
    "critical": 5,
    "high": 23,
    "medium": 45,
    "low": 120,
    "total": 193
  },
  "totalProcessed": 15234,
  "systemLoad": "NORMAL",
  "timestamp": 1703123456789
}
```

#### Health Check
```http
GET /api/monitoring/health
```

#### Audit Trail (Admin Only)
```http
GET /api/audit/notification/{notificationId}
Authorization: Bearer admin789
X-User-ID: admin789

GET /api/audit/user/{userId}
Authorization: Bearer admin789
X-User-ID: admin789
```

### Security API Examples

#### Create Notification (with authentication)
```http
POST /api/notifications
Content-Type: application/json
X-User-ID: user123

{
  "userId": "user123",
  "title": "Important Update",
  "message": "Your account has been updated successfully",
  "priority": "HIGH",
  "channel": "EMAIL"
}
```

#### Get User Notifications (with ownership verification)
```http
GET /api/notifications/user/user123?userId=user123
```

#### Cancel Scheduled Notification (with ownership verification)
```http
DELETE /api/notifications/123/cancel?userId=user123
```

#### System Statistics (Admin Only)
```http
GET /api/monitoring/stats
Authorization: Bearer admin789
X-User-ID: admin789
```

#### Rate Limit Response Example
```http
HTTP/1.1 429 Too Many Requests
X-RateLimit-Limit: 50
X-RateLimit-Window: 60
X-RateLimit-Reset: 30

Rate limit exceeded. Please try again in 30 seconds.
```

## 🔄 Priority System

### Priority Levels
1. **CRITICAL** (Level 1): Immediate processing, 10 workers
2. **HIGH** (Level 2): High priority, 8 workers
3. **MEDIUM** (Level 3): Normal priority, 5 workers
4. **LOW** (Level 4): Lower priority, 3 workers

### VIP User Priority Boost
VIP and Admin users automatically get priority boost:
- LOW → MEDIUM
- MEDIUM → HIGH
- HIGH → CRITICAL
- CRITICAL → CRITICAL (unchanged)

### Load-Based Processing
- **Normal Load**: All notifications process immediately
- **High Load (>10,000 pending)**: System prioritizes CRITICAL and HIGH only

## 📅 Scheduled Notifications

### Features
- **Future Scheduling**: Schedule notifications for any future date/time
- **Cancellation**: Cancel scheduled notifications before they're sent
- **Fallback Processing**: Automatic handling of missed scheduled notifications
- **Time Zone Support**: Uses system default timezone

### Scheduling Process
1. Notification created with `scheduledAt` in the future
2. Status set to `SCHEDULED`
3. Task scheduled using Spring's TaskScheduler
4. At scheduled time, notification moves to processing queue
5. Processes through normal priority-based system

### Example Use Cases
- Meeting reminders
- Birthday notifications
- Payment due reminders
- Marketing campaigns
- Appointment confirmations

## 🌐 WebSocket Integration

### Connection
```javascript
const ws = new WebSocket('ws://localhost:8080/ws/notifications?userId=user123');

ws.onopen = function(event) {
    console.log('Connected to notification service');
};

ws.onmessage = function(event) {
    const notification = JSON.parse(event.data);
    console.log('Received notification:', notification);
    // Handle real-time notification
};
```

### Message Format
```json
{
  "id": 123,
  "title": "New Message",
  "message": "You have received a new message",
  "priority": "HIGH",
  "channel": "WEBSOCKET",
  "type": "NOTIFICATION"
}
```

## 🔍 Monitoring & Analytics

### System Metrics
- **Queue Depths**: Monitor pending notifications per priority
- **Processing Rates**: Track notifications processed per minute
- **Error Rates**: Monitor failed notifications and retry attempts
- **Channel Performance**: Success rates per notification channel

### Health Checks
- Database connectivity
- RabbitMQ connection status
- Redis availability
- Email service status

### Logging
- Structured logging with correlation IDs
- Performance metrics for each notification
- Audit trail for all status changes
- Error tracking with full stack traces

## 🛠️ Configuration

### Environment Variables
```bash
# Database Configuration
DB_URL=jdbc:postgresql://localhost:5432/testdb
DB_USERNAME=myuser
DB_PASSWORD=mypass

# RabbitMQ Configuration
RABBITMQ_HOST=localhost
RABBITMQ_PORT=5672
RABBITMQ_USERNAME=guest
RABBITMQ_PASSWORD=guest

# Redis Configuration
REDIS_HOST=localhost
REDIS_PORT=6379

# Email Configuration
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=your-email
MAIL_PASSWORD=your-password
```

### Queue Configuration
```yaml
# Custom queue settings
notification:
  queues:
    critical:
      consumers: 10
      maxConcurrent: 30
      priority: 10
    high:
      consumers: 8
      maxConcurrent: 25
      priority: 8
    medium:
      consumers: 5
      maxConcurrent: 15
      priority: 5
    low:
      consumers: 3
      maxConcurrent: 10
      priority: 2
```

## 🧪 Testing

### Unit Tests
```bash
mvn test
```

### Integration Tests
```bash
mvn verify
```

### Load Testing
```bash
# Install Artillery for load testing
npm install -g artillery

# Run load test
artillery run load-test.yml
```

### Test Scenarios
- Priority-based processing verification
- Scheduled notification accuracy
- WebSocket real-time delivery
- Error handling and retry logic
- VIP user priority boost validation

## 🚀 Performance Optimization

### Recommended Settings

#### For High Volume (>100K notifications/day)
```yaml
rabbitmq:
  concurrent-consumers: 20
  max-concurrent-consumers: 50
  prefetch-count: 10

redis:
  connection-pool-size: 20
  timeout: 1000

database:
  connection-pool-size: 25
  max-idle-time: 300000
```

#### For Low Latency (<100ms processing)
```yaml
rabbitmq:
  concurrent-consumers: 15
  max-concurrent-consumers: 30
  prefetch-count: 1

redis:
  timeout: 500

notification:
  batch-size: 1
  immediate-processing: true
```

## 🔐 Security Features

### User Ownership Verification
Users can only access their own notifications. The system automatically verifies ownership before allowing access to notification details or cancellation.

```java
@RequireRole(value = {UserType.REGULAR, UserType.VIP, UserType.ADMIN}, requireOwnership = true)
public ResponseEntity<NotificationResponse> getNotification(@PathVariable Long id)
```

### Role-Based Access Control
Different endpoints require different user roles:

- **REGULAR**: Basic notification operations
- **VIP**: Enhanced features including bulk operations
- **ADMIN**: Full access including monitoring and audit endpoints

```java
// Regular users can create notifications
@RequireRole({UserType.REGULAR, UserType.VIP, UserType.ADMIN})
public ResponseEntity<NotificationResponse> createNotification()

// Only VIP and Admin users can create bulk notifications
@RequireRole({UserType.VIP, UserType.ADMIN})
public ResponseEntity<List<NotificationResponse>> createBulkNotifications(...)

// Only Admin users can access monitoring
@RequireRole({UserType.ADMIN})
public ResponseEntity<Map<String, Object>> getSystemStats(...)
```

### API Rate Limiting
Per-user and global rate limiting with configurable limits:

```java
// User-specific rate limiting
@RateLimit(maxRequests = 50, windowSeconds = 60, endpoint = "create_notification")

// Global rate limiting
@RateLimit(maxRequests = 100, windowSeconds = 60, endpoint = "health_check", global = true)
```

Rate limits are enforced with appropriate HTTP headers:
- `X-RateLimit-Limit`: Maximum requests allowed
- `X-RateLimit-Window`: Time window in seconds
- `X-RateLimit-Reset`: Time until rate limit resets

### Authentication Methods
The system supports multiple ways to provide user identification:

1. **Request Parameter**: `?userId=user123`
2. **Header**: `X-User-ID: user123`
3. **Authorization Header**: `Authorization: Bearer user123` (for JWT tokens)

### Security Considerations

#### Input Validation
- All request payloads are validated using Jakarta Bean Validation
- SQL injection prevention through JPA parameterized queries
- XSS protection for notification content

### Authentication & Authorization
- **User Ownership Verification**: Users can only access their own notifications
- **Role-Based Access Control**: Different endpoints require different user roles (REGULAR, VIP, ADMIN)
- **API Rate Limiting**: Per-user and global rate limiting with configurable limits
- **Admin-Only Endpoints**: Monitoring and audit endpoints restricted to admin users

### Data Privacy
- Personal data encryption in database
- Audit log retention policies
- GDPR compliance features (data deletion)

## 📈 Scaling Strategies

### Horizontal Scaling
1. **Database**: Read replicas for query operations
2. **RabbitMQ**: Cluster setup with high availability
3. **Application**: Multiple instances behind load balancer
4. **Redis**: Redis Cluster for high availability

### Vertical Scaling
1. **Increase worker threads** per priority queue
2. **Optimize database** connection pools
3. **Tune JVM settings** for garbage collection
4. **Increase memory** allocation

### Performance Monitoring
- APM tools integration (New Relic, DataDog)
- Custom metrics with Micrometer
- Database query optimization
- Cache hit ratio monitoring

## 🐛 Troubleshooting

### Common Issues

#### Notifications Not Processing
1. Check RabbitMQ connection
2. Verify queue bindings
3. Check consumer thread pool
4. Review database connectivity

#### Scheduled Notifications Missing
1. Check TaskScheduler configuration
2. Verify timezone settings
3. Review fallback processing logs
4. Check database scheduledAt values

#### WebSocket Connection Issues
1. Verify WebSocket endpoint configuration
2. Check CORS settings
3. Review firewall/proxy settings
4. Validate userId parameter

### Debug Commands
```bash
# Check RabbitMQ queues
docker exec rabbitmq rabbitmqctl list_queues

# Check Redis keys
docker exec redis redis-cli keys "*"

# View application logs
docker logs notification-system -f

# Database connection test
docker exec postgres psql -U myuser -d testdb -c "SELECT COUNT(*) FROM notifications;"
```

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

### Development Guidelines
- Follow Spring Boot best practices
- Write comprehensive unit tests
- Update documentation for new features
- Use conventional commit messages
- Ensure backward compatibility

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 📞 Support

For questions or issues:
- Create an issue on GitHub
- Email: support@yourcompany.com
- Documentation: https://your-docs-site.com

---

## 🎯 Roadmap

### Upcoming Features
- [ ] GraphQL API support
- [ ] Notification templates
- [ ] A/B testing for notifications
- [ ] Machine learning for optimal delivery times
- [ ] Multi-tenant support
- [ ] Notification analytics dashboard
- [ ] Mobile SDK integration
- [ ] Webhook support for external systems

---

*Built with ❤️ using Spring Boot*