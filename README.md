# Real-time Event-Driven Notification System

A scalable microservices-based notification system built with Spring Boot, Apache Kafka, and Redis.

## Architecture

- **Event Ingestion Service**: Receives and validates notification requests
- **Notification Processing Service**: Processes events, applies business logic
- **Delivery Service**: Handles multi-channel notification delivery

## Technology Stack

- Java 17
- Spring Boot 3.2
- Apache Kafka
- PostgreSQL
- Redis
- Docker

## Getting Started

### Prerequisites

- Java 17+
- Docker & Docker Compose
- Maven 3.8+

### Run Locally

1. Start infrastructure:
   ```bash
   docker-compose up -d
   ```

2. Run services:
   ```bash
   # Terminal 1: Event Ingestion Service
   cd services/event-ingestion-service
   mvn spring-boot:run

   # Terminal 2: Notification Processing Service
   cd services/notification-processing-service
   mvn spring-boot:run

   # Terminal 3: Delivery Service
   cd services/delivery-service
   mvn spring-boot:run
   ```

## API Documentation

Once services are running, access Swagger UI at:
- Event Ingestion Service: http://localhost:8081/swagger-ui.html
- Notification Processing Service: http://localhost:8082/swagger-ui.html
- Delivery Service: http://localhost:8083/swagger-ui.html

## Infrastructure Components

### Kafka UI
Access Kafka UI to monitor topics and messages:
- URL: http://localhost:8080

### PostgreSQL Database
- Host: localhost
- Port: 5432
- Database: notification_db
- Username: notification_user
- Password: notification_pass

### Redis Cache
- Host: localhost
- Port: 6379

## API Examples

### Send Notification

```bash
curl -X POST http://localhost:8081/api/v1/notifications \
  -H "Content-Type: application/json" \
  -d '{
    "eventType": "ORDER_SHIPPED",
    "recipients": [
      {
        "userId": "user-001",
        "channels": ["EMAIL", "SMS"]
      }
    ],
    "priority": "HIGH",
    "templateId": "order_shipped",
    "data": {
      "orderNumber": "ORD-12345",
      "trackingUrl": "https://track.example.com/ORD-12345"
    }
  }'
```

### Health Check

```bash
curl http://localhost:8081/api/v1/notifications/health
```

## Development

### Build All Services

```bash
# From root directory
cd services/event-ingestion-service && mvn clean install && cd ../..
cd services/notification-processing-service && mvn clean install && cd ../..
cd services/delivery-service && mvn clean install && cd ../..
```

### Stop All Services

```bash
# Stop infrastructure
docker-compose down

# Stop Spring Boot services (Ctrl+C in each terminal)
```

### View Logs

```bash
# View all Docker logs
docker-compose logs -f

# View specific service logs
docker-compose logs -f kafka-1
docker-compose logs -f postgres
```

## Troubleshooting

### Kafka Connection Issues

```bash
# Check Kafka is running
docker-compose ps kafka-1 kafka-2 kafka-3

# Restart Kafka
docker-compose restart kafka-1 kafka-2 kafka-3
```

### Database Connection Issues

```bash
# Check PostgreSQL
docker-compose ps postgres

# Connect to database
docker exec -it notification-postgres psql -U notification_user -d notification_db

# View tables
\dt
```

### Port Already in Use

```bash
# Find process using port
lsof -i :8081

# Kill process
kill -9 <PID>
```

## Project Status

- [x] Phase 1: Core Architecture Setup
- [ ] Phase 2: Event Processing & Priority Queues
- [ ] Phase 3: Multi-Channel Delivery
- [ ] Phase 4: Real-time Updates with WebSocket
- [ ] Phase 5: Monitoring & Observability
- [ ] Phase 6: Security & Authentication
- [ ] Phase 7: Testing & CI/CD
- [ ] Phase 8: Performance & Scalability

## Project Structure

```
notification-system/
├── docker-compose.yml
├── README.md
├── .gitignore
├── docs/
│   ├── architecture.md
│   └── api-specification.yaml
├── infrastructure/
│   ├── docker/
│   │   └── init-db.sql
│   └── scripts/
├── services/
│   ├── event-ingestion-service/
│   ├── notification-processing-service/
│   └── delivery-service/
└── monitoring/
```

## Contributing

This is a learning project for SDE skill development.

## License

MIT License

## Contact

For questions or feedback, please open an issue.

