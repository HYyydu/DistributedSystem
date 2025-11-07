package com.notification.delivery.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.notification.delivery.model.ProcessedEvent;
import com.notification.delivery.service.DeliveryService;
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
public class DeliveryConsumer {
    
    private final DeliveryService deliveryService;
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
            log.info("📬 Received processed notification from partition: {}, offset: {}", partition, offset);
            
            // Convert Map to ProcessedEvent
            ProcessedEvent event = objectMapper.convertValue(message, ProcessedEvent.class);
            
            log.info("📦 Processing delivery for notification: {}", event.getNotificationId());
            
            // Process delivery through all channels
            deliveryService.processDelivery(event);
            
            // Acknowledge message
            acknowledgment.acknowledge();
            
            log.info("✅ Successfully processed and acknowledged delivery. NotificationId: {}", 
                    event.getNotificationId());
            
        } catch (Exception e) {
            log.error("❌ Error consuming message from partition: {}, offset: {}. Error: {}", 
                    partition, offset, e.getMessage(), e);
            
            // Acknowledge anyway to avoid blocking the consumer
            acknowledgment.acknowledge();
        }
    }
}

