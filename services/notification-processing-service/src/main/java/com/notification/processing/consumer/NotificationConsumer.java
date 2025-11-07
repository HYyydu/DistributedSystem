package com.notification.processing.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.notification.processing.model.NotificationEvent;
import com.notification.processing.service.NotificationProcessor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationConsumer {
    
    private final NotificationProcessor notificationProcessor;
    private final ObjectMapper objectMapper;
    
    @KafkaListener(
        topics = "${notification.kafka.topics.input}",
        groupId = "${spring.kafka.consumer.group-id}",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void consume(
            @Payload Map<String, Object> message,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment acknowledgment) {
        
        try {
            log.info("Received message from partition: {}, offset: {}", partition, offset);
            log.debug("Message content: {}", message);
            
            // Convert Map to NotificationEvent
            NotificationEvent event = convertToNotificationEvent(message);
            
            // Process the notification
            notificationProcessor.processNotification(event);
            
            // Manually acknowledge the message
            acknowledgment.acknowledge();
            
            log.info("Successfully processed and acknowledged message. NotificationId: {}", 
                    event.getNotificationId());
            
        } catch (Exception e) {
            log.error("Error consuming message from partition: {}, offset: {}. Error: {}", 
                    partition, offset, e.getMessage(), e);
            
            // Acknowledge anyway to avoid blocking the consumer
            // The retry logic is handled within the processor
            acknowledgment.acknowledge();
        }
    }
    
    private NotificationEvent convertToNotificationEvent(Map<String, Object> message) {
        try {
            // Convert the Map to NotificationEvent using ObjectMapper
            NotificationEvent event = objectMapper.convertValue(message, NotificationEvent.class);
            
            // Initialize retry count if not present
            if (event.getRetryCount() == null) {
                event.setRetryCount(0);
            }
            
            return event;
        } catch (Exception e) {
            log.error("Error converting message to NotificationEvent: {}", e.getMessage());
            throw new RuntimeException("Failed to parse notification event", e);
        }
    }
}

