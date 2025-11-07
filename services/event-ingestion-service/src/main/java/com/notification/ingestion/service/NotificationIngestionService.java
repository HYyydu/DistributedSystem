package com.notification.ingestion.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.notification.ingestion.dto.NotificationRequest;
import com.notification.ingestion.dto.NotificationResponse;
import com.notification.ingestion.dto.RecipientRequest;
import com.notification.ingestion.entity.Notification;
import com.notification.ingestion.exception.NotificationProcessingException;
import com.notification.ingestion.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationIngestionService {
    
    private final NotificationRepository notificationRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ObjectMapper objectMapper;
    
    @Value("${notification.kafka.topic.events}")
    private String eventsTopic;
    
    @Transactional
    public NotificationResponse ingestNotification(NotificationRequest request) {
        log.info("Ingesting notification for event type: {}", request.getEventType());
        
        try {
            // Process each recipient
            for (RecipientRequest recipient : request.getRecipients()) {
                // Save to database
                Notification notification = saveNotification(request, recipient);
                
                // Publish to Kafka
                publishToKafka(notification, recipient);
            }
            
            return NotificationResponse.builder()
                    .status("ACCEPTED")
                    .createdAt(LocalDateTime.now())
                    .message("Notification request accepted and queued for processing")
                    .build();
            
        } catch (Exception e) {
            log.error("Error ingesting notification", e);
            throw new NotificationProcessingException("Failed to ingest notification: " + e.getMessage());
        }
    }
    
    private Notification saveNotification(NotificationRequest request, RecipientRequest recipient) {
        try {
            String dataJson = request.getData() != null ? 
                    objectMapper.writeValueAsString(request.getData()) : null;
            
            Notification notification = Notification.builder()
                    .userId(recipient.getUserId())
                    .eventType(request.getEventType())
                    .priority(request.getPriority().name())
                    .status("PENDING")
                    .templateId(request.getTemplateId())
                    .data(dataJson)
                    .scheduledAt(request.getScheduledAt())
                    .build();
            
            return notificationRepository.save(notification);
        } catch (JsonProcessingException e) {
            throw new NotificationProcessingException("Failed to serialize notification data", e);
        }
    }
    
    private void publishToKafka(Notification notification, RecipientRequest recipient) {
        Map<String, Object> event = new HashMap<>();
        event.put("notificationId", notification.getId().toString());
        event.put("userId", notification.getUserId());
        event.put("eventType", notification.getEventType());
        event.put("priority", notification.getPriority());
        event.put("channels", recipient.getChannels());
        event.put("templateId", notification.getTemplateId());
        event.put("data", notification.getData());
        event.put("scheduledAt", notification.getScheduledAt());
        event.put("createdAt", notification.getCreatedAt());
        
        kafkaTemplate.send(eventsTopic, notification.getId().toString(), event);
        log.info("Published notification event to Kafka: {}", notification.getId());
    }
}

