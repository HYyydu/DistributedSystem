package com.notification.processing.service;

import com.notification.processing.dto.UserPreferences;
import com.notification.processing.handler.RetryHandler;
import com.notification.processing.model.NotificationEvent;
import com.notification.processing.model.ProcessedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationProcessor {
    
    private final UserPreferenceService userPreferenceService;
    private final IdempotencyService idempotencyService;
    private final RetryHandler retryHandler;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    
    @Value("${notification.kafka.topics.output}")
    private String outputTopic;
    
    public void processNotification(NotificationEvent event) {
        String notificationId = event.getNotificationId();
        
        try {
            log.info("Processing notification: {} with priority: {}", notificationId, event.getPriority());
            
            // Step 1: Check idempotency
            if (idempotencyService.isAlreadyProcessed(notificationId)) {
                log.info("Skipping duplicate notification: {}", notificationId);
                return;
            }
            
            // Step 2: Get user preferences
            UserPreferences preferences = userPreferenceService.getUserPreferences(event.getUserId());
            
            // Step 3: Check if event type is blocked
            if (userPreferenceService.isEventTypeBlocked(preferences, event.getEventType())) {
                log.info("Event type {} is blocked for user: {}", event.getEventType(), event.getUserId());
                publishProcessedEvent(event, "FILTERED", "Event type blocked by user");
                idempotencyService.markAsProcessed(notificationId);
                return;
            }
            
            // Step 4: Check quiet hours (only for LOW and MEDIUM priority)
            if (!"HIGH".equals(event.getPriority()) && 
                userPreferenceService.isInQuietHours(preferences)) {
                log.info("User in quiet hours, delaying notification: {}", notificationId);
                // In a real system, you'd schedule for later
                // For now, we'll just log and continue
            }
            
            // Step 5: Filter channels based on user preferences
            List<String> filteredChannels = userPreferenceService.filterChannels(
                    preferences, event.getChannels());
            
            if (filteredChannels.isEmpty()) {
                log.info("No enabled channels for user: {}", event.getUserId());
                publishProcessedEvent(event, "FILTERED", "All channels disabled by user");
                idempotencyService.markAsProcessed(notificationId);
                return;
            }
            
            // Step 6: Update channels with filtered list
            event.setChannels(filteredChannels);
            
            // Step 7: Process based on priority
            processByPriority(event);
            
            // Step 8: Publish to processed topic
            publishProcessedEvent(event, "PROCESSED", "Successfully processed");
            
            // Step 9: Mark as processed
            idempotencyService.markAsProcessed(notificationId);
            
            log.info("Successfully processed notification: {}", notificationId);
            
        } catch (Exception e) {
            log.error("Error processing notification: {}. Error: {}", notificationId, e.getMessage(), e);
            handleProcessingError(event, e);
        }
    }
    
    private void processByPriority(NotificationEvent event) {
        switch (event.getPriority()) {
            case "HIGH":
                log.info("Processing HIGH priority notification immediately: {}", event.getNotificationId());
                // HIGH priority gets immediate processing
                break;
            case "MEDIUM":
                log.info("Processing MEDIUM priority notification: {}", event.getNotificationId());
                // MEDIUM priority gets normal processing
                break;
            case "LOW":
                log.info("Processing LOW priority notification: {}", event.getNotificationId());
                // LOW priority could be batched or delayed
                break;
            default:
                log.warn("Unknown priority: {}. Treating as MEDIUM", event.getPriority());
        }
    }
    
    private void publishProcessedEvent(NotificationEvent event, String status, String notes) {
        ProcessedEvent processedEvent = ProcessedEvent.builder()
                .notificationId(event.getNotificationId())
                .userId(event.getUserId())
                .eventType(event.getEventType())
                .priority(event.getPriority())
                .channels(event.getChannels())
                .templateId(event.getTemplateId())
                .data(event.getData())
                .processedAt(LocalDateTime.now())
                .status(status)
                .processingNotes(notes)
                .build();
        
        kafkaTemplate.send(outputTopic, event.getNotificationId(), processedEvent);
        log.info("Published processed event for notification: {} with status: {}", 
                event.getNotificationId(), status);
    }
    
    private void handleProcessingError(NotificationEvent event, Exception exception) {
        if (retryHandler.shouldRetry(event)) {
            log.info("Scheduling retry for notification: {}", event.getNotificationId());
            retryHandler.scheduleRetry(event, exception);
        } else {
            log.error("Max retries exceeded, sending to DLQ: {}", event.getNotificationId());
            retryHandler.sendToDLQ(event, exception);
        }
    }
}

