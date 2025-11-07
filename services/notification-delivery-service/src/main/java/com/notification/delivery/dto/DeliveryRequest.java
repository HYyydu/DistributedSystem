package com.notification.delivery.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryRequest {
    private String notificationId;
    private String userId;
    private String channel;
    private String templateId;
    private String data;
    private String eventType;
}

