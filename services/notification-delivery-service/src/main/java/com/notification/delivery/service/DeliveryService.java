package com.notification.delivery.service;

import com.notification.delivery.channel.NotificationChannel;
import com.notification.delivery.dto.DeliveryRequest;
import com.notification.delivery.dto.DeliveryResult;
import com.notification.delivery.entity.DeliveryLog;
import com.notification.delivery.model.ProcessedEvent;
import com.notification.delivery.repository.DeliveryLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class DeliveryService {
    
    private final ChannelFactory channelFactory;
    private final RateLimitService rateLimitService;
    private final DeliveryLogRepository deliveryLogRepository;
    private final WebSocketNotificationService webSocketNotificationService;
    
    public void processDelivery(ProcessedEvent event) {
        log.info("🚀 Processing delivery for notification: {}", event.getNotificationId());
        log.info("   User: {}, Priority: {}, Channels: {}", 
                event.getUserId(), event.getPriority(), event.getChannels());
        
        for (String channelName : event.getChannels()) {
            deliverToChannel(event, channelName);
        }
        
        log.info("✅ Completed delivery processing for notification: {}", event.getNotificationId());
    }
    
    private void deliverToChannel(ProcessedEvent event, String channelName) {
        try {
            // Check rate limit
            if (!rateLimitService.isAllowed(event.getUserId(), channelName)) {
                log.warn("⚠️ Rate limit exceeded for channel: {}. Skipping delivery", channelName);
                logDelivery(event.getNotificationId(), channelName, "RATE_LIMITED", 
                        "Rate limit exceeded", null);
                return;
            }
            
            // Get appropriate channel
            NotificationChannel channel = channelFactory.getChannel(channelName);
            
            // Prepare delivery request
            DeliveryRequest request = DeliveryRequest.builder()
                    .notificationId(event.getNotificationId())
                    .userId(event.getUserId())
                    .channel(channelName)
                    .templateId(event.getTemplateId())
                    .data(event.getData())
                    .eventType(event.getEventType())
                    .build();
            
            // Deliver
            DeliveryResult result = channel.deliver(request);
            
            // Log result
            String status = result.isSuccess() ? "DELIVERED" : "FAILED";
            String errorMessage = result.isSuccess() ? null : result.getErrorDetails();
            LocalDateTime deliveredAt = result.isSuccess() ? LocalDateTime.now() : null;
            
            logDelivery(event.getNotificationId(), channelName, status, errorMessage, deliveredAt);
            
            // Send WebSocket notification for real-time updates
            webSocketNotificationService.sendDeliveryStatus(
                    event.getUserId(),
                    event.getNotificationId(),
                    channelName,
                    status,
                    event.getEventType(),
                    event.getPriority()
            );
            
        } catch (Exception e) {
            log.error("❌ Error delivering to channel {}: {}", channelName, e.getMessage(), e);
            logDelivery(event.getNotificationId(), channelName, "FAILED", e.getMessage(), null);
        }
    }
    
    private void logDelivery(String notificationId, String channel, String status, 
                            String errorMessage, LocalDateTime deliveredAt) {
        DeliveryLog deliveryLog = DeliveryLog.builder()
                .notificationId(UUID.fromString(notificationId))
                .channel(channel)
                .status(status)
                .attemptCount(1)
                .errorMessage(errorMessage)
                .deliveredAt(deliveredAt)
                .build();
        
        deliveryLogRepository.save(deliveryLog);
        log.debug("📝 Logged delivery: {}, Channel: {}, Status: {}", notificationId, channel, status);
    }
}

