package com.notification.ingestion.controller;

import com.notification.ingestion.dto.NotificationRequest;
import com.notification.ingestion.dto.NotificationResponse;
import com.notification.ingestion.service.NotificationIngestionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
@Tag(name = "Notification Ingestion", description = "APIs for ingesting notification requests")
public class NotificationController {
    
    private final NotificationIngestionService notificationIngestionService;
    
    @PostMapping
    @Operation(summary = "Send notification", description = "Submit a new notification request")
    public ResponseEntity<NotificationResponse> sendNotification(
            @Valid @RequestBody NotificationRequest request) {
        NotificationResponse response = notificationIngestionService.ingestNotification(request);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }
    
    @GetMapping("/health")
    @Operation(summary = "Health check", description = "Check if the service is healthy")
    public ResponseEntity<Map<String, String>> healthCheck() {
        return ResponseEntity.ok(Map.of("status", "UP", "service", "event-ingestion-service"));
    }
}

