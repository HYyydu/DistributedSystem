package com.notification.delivery.channel;

import com.notification.delivery.dto.DeliveryRequest;
import com.notification.delivery.dto.DeliveryResult;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class PushChannel implements NotificationChannel {
    
    @Override
    public String getChannelName() {
        return "PUSH";
    }
    
    @Override
    @CircuitBreaker(name = "pushChannel", fallbackMethod = "deliverFallback")
    public DeliveryResult deliver(DeliveryRequest request) {
        log.info("🔔 Delivering PUSH notification to user: {}", request.getUserId());
        log.info("   NotificationId: {}", request.getNotificationId());
        log.info("   Template: {}", request.getTemplateId());
        log.info("   Data: {}", request.getData());
        
        // Mock Push notification delivery - in production, use Firebase FCM
        try {
            // Simulate push notification sending
            Thread.sleep(60); // Simulate API call latency
            
            log.info("✅ PUSH notification delivered successfully to user: {}", request.getUserId());
            return DeliveryResult.success("PUSH", "Push notification sent successfully via mock FCM");
            
        } catch (Exception e) {
            log.error("❌ Failed to deliver PUSH notification: {}", e.getMessage());
            return DeliveryResult.failure("PUSH", e.getMessage());
        }
    }
    
    @Override
    public boolean supportsChannel(String channelName) {
        return "PUSH".equalsIgnoreCase(channelName);
    }
    
    // Fallback method for circuit breaker
    public DeliveryResult deliverFallback(DeliveryRequest request, Exception e) {
        log.error("🔴 Circuit breaker fallback for PUSH. Error: {}", e.getMessage());
        return DeliveryResult.failure("PUSH", "Circuit breaker open: " + e.getMessage());
    }
}

