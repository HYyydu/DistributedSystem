# Demo Guide & Screenshots

This guide provides step-by-step instructions for demonstrating the Real-time Event-Driven Notification System.

## Quick Demo Scenario

### Scenario: E-commerce Order Shipped Notification

This demo shows the complete flow of a notification from creation to multi-channel delivery.

## Step 1: Start the System

```bash
# Start infrastructure
docker-compose up -d

# Wait 30 seconds for Kafka/PostgreSQL/Redis to initialize

# Start all microservices
cd services/event-ingestion-service && mvn spring-boot:run &
cd services/notification-processing-service && mvn spring-boot:run &
cd services/notification-delivery-service && mvn spring-boot:run &
```

## Step 2: Open Monitoring Dashboards

1. **Kafka UI**: http://localhost:8080
   - View topics: `notifications.high`, `notifications.medium`, `notifications.low`
   - Monitor consumer lag and message flow

2. **Real-time Dashboard**: http://localhost:8083/dashboard.html
   - WebSocket connection status
   - Live notification updates

3. **Swagger API**: http://localhost:8081/swagger-ui.html
   - Interactive API documentation

## Step 3: Send Test Notification

### Using cURL:

```bash
curl -X POST http://localhost:8081/api/v1/notifications \
  -H "Content-Type: application/json" \
  -d '{
    "eventType": "ORDER_SHIPPED",
    "recipients": [
      {
        "userId": "user-001",
        "channels": ["EMAIL", "SMS", "PUSH"]
      }
    ],
    "priority": "HIGH",
    "templateId": "order_shipped",
    "data": {
      "orderNumber": "ORD-12345",
      "customerName": "John Doe",
      "trackingNumber": "TRK-789456",
      "trackingUrl": "https://track.example.com/TRK-789456",
      "estimatedDelivery": "2024-12-20"
    }
  }'
```

### Expected Response:

```json
{
  "notificationId": "550e8400-e29b-41d4-a716-446655440000",
  "status": "ACCEPTED",
  "priority": "HIGH",
  "message": "Notification accepted for processing",
  "timestamp": "2024-11-07T10:30:00Z"
}
```

## Step 4: Monitor the Flow

### In Kafka UI (localhost:8080):

1. Navigate to **Topics** → `notifications.high`
2. View the message that was published
3. Check **Consumer Groups** to see processing lag

### In Real-time Dashboard (localhost:8083/dashboard.html):

1. Observe the WebSocket connection status: "Connected"
2. Watch for real-time status updates:
   - PENDING → PROCESSING → SENT → DELIVERED
3. View delivery statistics by channel

### In Application Logs:

**Event Ingestion Service (Terminal 1):**
```
[INFO] Received notification request for eventType: ORDER_SHIPPED
[INFO] Published to Kafka topic: notifications.high
[INFO] Notification ID: 550e8400-e29b-41d4-a716-446655440000
```

**Notification Processing Service (Terminal 2):**
```
[INFO] Consumed message from notifications.high
[INFO] Checking user preferences for userId: user-001
[INFO] User prefers: [EMAIL, SMS, PUSH]
[INFO] Routing to delivery channels
```

**Delivery Service (Terminal 3):**
```
[INFO] Email Worker: Sending to SendGrid...
[INFO] SMS Worker: Sending to Twilio...
[INFO] Push Worker: Sending to FCM...
[SUCCESS] Email delivered successfully
[SUCCESS] SMS delivered successfully
[SUCCESS] Push notification delivered
[INFO] Updated status to DELIVERED
```

## Step 5: Verify Delivery Status

### Query notification status:

```bash
curl http://localhost:8081/api/v1/notifications/550e8400-e29b-41d4-a716-446655440000
```

### Response:

```json
{
  "notificationId": "550e8400-e29b-41d4-a716-446655440000",
  "eventType": "ORDER_SHIPPED",
  "status": "DELIVERED",
  "priority": "HIGH",
  "channels": [
    {
      "type": "EMAIL",
      "status": "DELIVERED",
      "deliveredAt": "2024-11-07T10:30:02Z"
    },
    {
      "type": "SMS",
      "status": "DELIVERED",
      "deliveredAt": "2024-11-07T10:30:03Z"
    },
    {
      "type": "PUSH",
      "status": "DELIVERED",
      "deliveredAt": "2024-11-07T10:30:01Z"
    }
  ],
  "createdAt": "2024-11-07T10:30:00Z",
  "updatedAt": "2024-11-07T10:30:03Z"
}
```

## Advanced Demo Scenarios

### Scenario 2: Priority Handling

Send notifications with different priorities and observe processing order:

```bash
# High priority (processes immediately)
curl -X POST http://localhost:8081/api/v1/notifications \
  -d '{"priority": "HIGH", ...}'

# Low priority (processes in background)
curl -X POST http://localhost:8081/api/v1/notifications \
  -d '{"priority": "LOW", ...}'
```

### Scenario 3: Rate Limiting

Send multiple notifications rapidly to the same user:

```bash
for i in {1..10}; do
  curl -X POST http://localhost:8081/api/v1/notifications \
    -H "Content-Type: application/json" \
    -d '{
      "eventType": "PROMO_ALERT",
      "recipients": [{"userId": "user-001", "channels": ["EMAIL"]}],
      "priority": "LOW"
    }'
done
```

Expected: Rate limiter kicks in after configured threshold (e.g., 5 notifications per minute).

### Scenario 4: Retry Mechanism

Simulate a delivery failure to test retry logic:

1. Stop the external service mock (or configure invalid credentials)
2. Send notification
3. Observe retry attempts with exponential backoff in logs
4. After max retries, message moves to Dead Letter Queue (DLQ)

### Scenario 5: User Preferences

Test user preference filtering:

```bash
# Set user preferences (only EMAIL)
curl -X POST http://localhost:8082/api/v1/preferences \
  -d '{
    "userId": "user-002",
    "channels": ["EMAIL"],
    "optOut": ["SMS", "PUSH"]
  }'

# Send notification to user-002 with multiple channels
curl -X POST http://localhost:8081/api/v1/notifications \
  -d '{
    "recipients": [{"userId": "user-002", "channels": ["EMAIL", "SMS"]}]
  }'
```

Expected: Only EMAIL is delivered; SMS is filtered out.

## Performance Testing

### Load Test with Apache Bench:

```bash
# Generate load test data
cat > notification.json <<EOF
{
  "eventType": "LOAD_TEST",
  "recipients": [{"userId": "load-test-user", "channels": ["EMAIL"]}],
  "priority": "MEDIUM",
  "data": {"message": "Load test notification"}
}
EOF

# Run load test (1000 requests, 10 concurrent)
ab -n 1000 -c 10 -p notification.json -T application/json \
  http://localhost:8081/api/v1/notifications
```

### Monitor Performance:

- **Throughput**: Requests/second
- **Latency**: Response time percentiles (p50, p95, p99)
- **Kafka Lag**: Consumer group lag in Kafka UI
- **Success Rate**: % of successful deliveries

## Cleanup

```bash
# Stop all services
docker-compose down

# Clean up volumes (optional, removes all data)
docker-compose down -v
```

## Troubleshooting Demo Issues

### Issue: Services won't start

**Solution:**
```bash
# Check if ports are in use
lsof -i :8081
lsof -i :8082
lsof -i :8083

# Kill conflicting processes or change ports in application.yml
```

### Issue: Kafka connection errors

**Solution:**
```bash
# Ensure Kafka is healthy
docker-compose ps kafka-1

# Restart Kafka cluster
docker-compose restart kafka-1 kafka-2 kafka-3

# Wait 30 seconds and retry
```

### Issue: No messages in Kafka

**Solution:**
- Check Event Ingestion Service logs for errors
- Verify Kafka topic exists in Kafka UI
- Check network connectivity between services

### Issue: Notifications stuck in PENDING

**Solution:**
- Verify Notification Processing Service is running
- Check consumer group lag in Kafka UI
- Review logs for exceptions

## Screenshot Checklist

For your portfolio/demo video, capture these key screens:

- [ ] Architecture diagram (from docs/images/architecture-diagram.png)
- [ ] Kafka UI showing topics and messages
- [ ] Real-time dashboard with live updates
- [ ] Swagger API documentation
- [ ] Console logs showing complete flow
- [ ] Database tables with notification data
- [ ] Performance metrics (if monitoring is set up)

## Demo Video Script

1. **Introduction** (30 sec)
   - Overview of the system
   - Show architecture diagram

2. **System Startup** (1 min)
   - Start infrastructure with docker-compose
   - Launch microservices
   - Show health checks

3. **Live Demo** (2 min)
   - Send notification via API
   - Show Kafka UI with message flow
   - Display real-time dashboard updates
   - Check delivery logs

4. **Advanced Features** (1 min)
   - Demonstrate priority queuing
   - Show rate limiting
   - Explain retry mechanism

5. **Conclusion** (30 sec)
   - Highlight key achievements
   - Mention scalability and reliability features

Total: 5 minutes

