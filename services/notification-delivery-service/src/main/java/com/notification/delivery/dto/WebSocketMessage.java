package com.notification.delivery.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WebSocketMessage {
    private String type;  // DELIVERY_STATUS, METRICS
    private String notificationId;
    private String userId;
    private String channel;
    private String status;
    private String eventType;
    private String priority;
    private LocalDateTime timestamp;
    private Long deliveryTimeMs;
    private Object data;
}

