package com.notification.processing.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationEvent {
    private String notificationId;
    private String userId;
    private String eventType;
    private String priority;  // HIGH, MEDIUM, LOW
    private List<String> channels;  // EMAIL, SMS, PUSH, WEBHOOK
    private String templateId;
    private String data;
    private LocalDateTime scheduledAt;
    private LocalDateTime createdAt;
    private Integer retryCount;  // Track retry attempts
}

