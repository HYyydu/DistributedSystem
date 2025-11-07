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
public class ProcessedEvent {
    private String notificationId;
    private String userId;
    private String eventType;
    private String priority;
    private List<String> channels;
    private String templateId;
    private String data;
    private LocalDateTime processedAt;
    private String status;  // PROCESSED, FILTERED, FAILED
    private String processingNotes;
}

