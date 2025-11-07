# Architecture Diagram

Please save your system architecture diagram as `architecture-diagram.png` in this directory.

The diagram should illustrate:

## Components to Include:
1. **Event Producers** - External systems, user actions, scheduled tasks
2. **Apache Kafka Cluster** - With topics (high/medium/low/dlq)
3. **Notification Service** - Priority Queue Manager & Retry Scheduler
4. **Delivery Workers** - Email, SMS, Push, Webhook workers
5. **PostgreSQL Database** - Notifications, User Preferences, Rate Limits
6. **Redis Cache** - For idempotency and rate limiting
7. **External Services** - SendGrid, Twilio, FCM
8. **Monitoring Stack** - Prometheus, Grafana, Zipkin/Jaeger
9. **Frontend Dashboard** - React/Vue interface

## Recommended Format:
- **Filename**: `architecture-diagram.png`
- **Size**: 1200x800 pixels or similar aspect ratio
- **Format**: PNG with transparent or white background
- **Style**: Clean, professional system architecture diagram

Once you place the image here, it will automatically appear in the architecture.md documentation.

