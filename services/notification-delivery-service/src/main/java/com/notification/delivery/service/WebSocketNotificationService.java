package com.notification.delivery.service;

import com.notification.delivery.dto.WebSocketMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class WebSocketNotificationService {
    
    private final SimpMessagingTemplate messagingTemplate;
    
    public void sendDeliveryStatus(String userId, String notificationId, String channel, 
                                   String status, String eventType, String priority) {
        WebSocketMessage message = WebSocketMessage.builder()
                .type("DELIVERY_STATUS")
                .notificationId(notificationId)
                .userId(userId)
                .channel(channel)
                .status(status)
                .eventType(eventType)
                .priority(priority)
                .timestamp(LocalDateTime.now())
                .build();
        
        // Send to specific user
        String destination = "/queue/notifications";
        messagingTemplate.convertAndSendToUser(userId, destination, message);
        
        // Also broadcast to public topic for dashboard
        messagingTemplate.convertAndSend("/topic/notifications", message);
        
        log.debug("📡 Sent WebSocket update to user: {}, notification: {}, status: {}", 
                userId, notificationId, status);
    }
    
    public void broadcastMetrics(Object metrics) {
        WebSocketMessage message = WebSocketMessage.builder()
                .type("METRICS")
                .timestamp(LocalDateTime.now())
                .data(metrics)
                .build();
        
        messagingTemplate.convertAndSend("/topic/metrics", message);
        log.debug("📊 Broadcasted metrics update");
    }
}

