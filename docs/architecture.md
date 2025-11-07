# System Architecture

## Overview

The Real-time Event-Driven Notification System is a scalable, microservices-based architecture designed to handle high-volume notification delivery across multiple channels (Email, SMS, Push, Webhook).

## Architecture Diagram

![System Architecture](images/architecture-diagram.png)

## Component Details

### 1. Event Producers
**Description**: External systems, user actions, and scheduled tasks that trigger notification events.

**Examples**:
- E-commerce order updates
- User registration confirmations
- Scheduled reminders
- System alerts

### 2. Apache Kafka Cluster
**Purpose**: Message broker for event streaming and priority-based queuing.

**Topics**:
- `notifications.high` - High priority notifications (SLA: < 1 second)
- `notifications.medium` - Medium priority notifications (SLA: < 5 seconds)
- `notifications.low` - Low priority notifications (SLA: < 30 seconds)
- `notifications.dlq` - Dead Letter Queue for failed messages

**Configuration**:
- 3-node Kafka cluster for high availability
- Replication factor: 3
- Min in-sync replicas: 2

### 3. Notification Service (Core Processing)
**Responsibilities**:
- Filter incoming events based on user preferences
- Route events to appropriate delivery channels
- Prioritize notifications based on business rules
- Manage retry logic and dead letter queue

**Components**:
- **Priority Queue Manager**: Handles priority-based message routing
- **Retry Scheduler**: Implements exponential backoff for failed deliveries

**Key Features**:
- Idempotency checking to prevent duplicate notifications
- Rate limiting per user/channel
- Template management
- User preference validation

### 4. Delivery Workers
**Purpose**: Execute actual notification delivery to external platforms.

**Worker Types**:
- **Email Worker**: Integrates with SendGrid for email delivery
- **SMS Worker**: Integrates with Twilio for SMS delivery
- **Push Worker**: Integrates with FCM (Firebase Cloud Messaging) for push notifications
- **Webhook Worker**: Sends HTTP callbacks to external systems

**Features**:
- Concurrent processing with configurable thread pools
- Circuit breaker pattern for external service failures
- Delivery status tracking
- Retry with exponential backoff

### 5. PostgreSQL Database
**Purpose**: Persistent storage for notifications, user preferences, and rate limiting data.

**Tables**:
- `notifications` - Notification records and history
- `user_preferences` - User channel preferences and opt-out settings
- `rate_limits` - Rate limiting counters per user/channel
- `delivery_logs` - Delivery status and tracking information

**Performance Optimizations**:
- Indexed queries on user_id and notification_id
- Partitioning by date for historical data
- Connection pooling with HikariCP

### 6. Redis Cache
**Purpose**: High-performance caching and rate limiting.

**Use Cases**:
- Idempotency token caching
- User preference caching (5-minute TTL)
- Rate limit counters (sliding window)
- Session management for WebSocket connections

### 7. External Delivery Services

#### SendGrid (Email)
- Transactional email delivery
- Template management
- Bounce and complaint handling
- Delivery status webhooks

#### Twilio (SMS)
- SMS delivery worldwide
- Status callbacks
- Fallback phone numbers
- Message segmentation

#### FCM (Push Notifications)
- Android and iOS push notifications
- Topic-based messaging
- Device token management
- Priority messaging

### 8. Status Update System
**Purpose**: Receive and process delivery status updates from external services.

**Status Types**:
- `PENDING` - Queued for delivery
- `SENT` - Successfully delivered to external service
- `DELIVERED` - Confirmed received by end user
- `FAILED` - Delivery failed
- `BOUNCED` - Email bounced or invalid recipient
- `UNSUBSCRIBED` - User opted out

### 9. Monitoring Stack

#### Prometheus
- Metrics collection from all services
- Custom metrics for business KPIs
- Alert rule configuration

**Key Metrics**:
- Notification throughput (per second)
- Delivery success rate by channel
- Average processing time
- Queue depth and lag
- Error rates

#### Grafana
- Real-time dashboards
- Alerting and notifications
- Historical trend analysis

**Dashboards**:
- Service health overview
- Kafka metrics (lag, throughput)
- Database performance
- Delivery success rates by channel

#### Zipkin/Jaeger (Distributed Tracing)
- End-to-end request tracing
- Latency analysis
- Dependency mapping
- Error tracking

### 10. Frontend Dashboard
**Technology**: React/Vue.js

**Features**:
- Real-time notification status updates via WebSocket
- Notification history and search
- User preference management
- Analytics and reporting
- Manual notification triggering

## Data Flow

### Happy Path Flow
1. Event Producer sends notification request to Event Ingestion Service
2. Event Ingestion Service validates and publishes to Kafka topic (based on priority)
3. Notification Processing Service consumes from Kafka
4. Checks user preferences and rate limits (queries PostgreSQL/Redis)
5. Routes to appropriate Delivery Worker
6. Delivery Worker sends to external service (SendGrid/Twilio/FCM)
7. External service delivers and sends status callback
8. Status update is processed and stored in PostgreSQL
9. Frontend Dashboard receives real-time update via WebSocket

### Failure Handling Flow
1. Delivery Worker encounters failure
2. Retry Handler applies exponential backoff
3. After max retries, message sent to Dead Letter Queue (DLQ)
4. Alert triggered in monitoring system
5. Manual intervention or automated recovery process initiated

## Scalability Considerations

### Horizontal Scaling
- **Kafka**: Add more brokers and increase partitions
- **Services**: Deploy multiple instances behind load balancer
- **Database**: Read replicas for query load distribution
- **Redis**: Redis Cluster for distributed caching

### Vertical Scaling
- Increase JVM heap size for services
- Optimize database queries and add indexes
- Tune Kafka producer/consumer configurations

### Performance Targets
- Handle 10,000+ notifications per second
- 99.9% delivery success rate
- < 1 second latency for high-priority notifications
- < 5 minutes for low-priority notifications

## Security

### Authentication & Authorization
- JWT tokens for API authentication
- Role-based access control (RBAC)
- API key management for external integrations

### Data Protection
- TLS/SSL for all external communications
- Encryption at rest for sensitive data
- PII data masking in logs
- Regular security audits

### Network Security
- Private VPC for internal services
- Security groups and network ACLs
- DDoS protection
- Rate limiting per IP/API key

## Disaster Recovery

### Backup Strategy
- PostgreSQL: Daily full backups, hourly incremental
- Kafka: Topic replication across availability zones
- Redis: Snapshot persistence enabled

### Recovery Plan
- RTO (Recovery Time Objective): < 1 hour
- RPO (Recovery Point Objective): < 15 minutes
- Automated failover for critical components
- Regular disaster recovery drills

## Future Enhancements

- [ ] Machine learning-based delivery time optimization
- [ ] A/B testing framework for notification templates
- [ ] Multi-region deployment for global reach
- [ ] Advanced analytics and predictive insights
- [ ] WhatsApp and Slack integration
- [ ] Notification scheduling and batching
- [ ] User engagement scoring

