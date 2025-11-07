package com.notification.delivery.channel;

import com.notification.delivery.dto.DeliveryRequest;
import com.notification.delivery.dto.DeliveryResult;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class SmsChannel implements NotificationChannel {
    
    @Override
    public String getChannelName() {
        return "SMS";
    }
    
    @Override
    @CircuitBreaker(name = "smsChannel", fallbackMethod = "deliverFallback")
    public DeliveryResult deliver(DeliveryRequest request) {
        log.info("📱 Delivering SMS notification to user: {}", request.getUserId());
        log.info("   NotificationId: {}", request.getNotificationId());
        log.info("   Template: {}", request.getTemplateId());
        log.info("   Data: {}", request.getData());
        
        // Mock SMS delivery - in production, use Twilio
        try {
            // Simulate SMS sending
            Thread.sleep(80); // Simulate API call latency
            
            log.info("✅ SMS delivered successfully to user: {}", request.getUserId());
            return DeliveryResult.success("SMS", "SMS sent successfully via mock Twilio");
            
        } catch (Exception e) {
            log.error("❌ Failed to deliver SMS: {}", e.getMessage());
            return DeliveryResult.failure("SMS", e.getMessage());
        }
    }
    
    @Override
    public boolean supportsChannel(String channelName) {
        return "SMS".equalsIgnoreCase(channelName);
    }
    
    // Fallback method for circuit breaker
    public DeliveryResult deliverFallback(DeliveryRequest request, Exception e) {
        log.error("🔴 Circuit breaker fallback for SMS. Error: {}", e.getMessage());
        return DeliveryResult.failure("SMS", "Circuit breaker open: " + e.getMessage());
    }
}

