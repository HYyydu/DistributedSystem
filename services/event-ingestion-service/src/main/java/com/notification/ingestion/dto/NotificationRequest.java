package com.notification.ingestion.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationRequest {
    
    @NotBlank(message = "Event type is required")
    private String eventType;
    
    @NotEmpty(message = "At least one recipient is required")
    private List<RecipientRequest> recipients;
    
    @NotNull(message = "Priority is required")
    private Priority priority;
    
    private String templateId;
    
    private Map<String, Object> data;
    
    private LocalDateTime scheduledAt;
    
    public enum Priority {
        HIGH, MEDIUM, LOW
    }
}

