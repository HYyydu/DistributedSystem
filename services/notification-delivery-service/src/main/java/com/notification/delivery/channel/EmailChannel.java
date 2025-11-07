package com.notification.delivery.channel;

import com.notification.delivery.dto.DeliveryRequest;
import com.notification.delivery.dto.DeliveryResult;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class EmailChannel implements NotificationChannel {
    
    @Override
    public String getChannelName() {
        return "EMAIL";
    }
    
    @Override
    @CircuitBreaker(name = "emailChannel", fallbackMethod = "deliverFallback")
    public DeliveryResult deliver(DeliveryRequest request) {
        log.info("📧 Delivering EMAIL notification to user: {}", request.getUserId());
        log.info("   NotificationId: {}", request.getNotificationId());
        log.info("   Template: {}", request.getTemplateId());
        log.info("   Data: {}", request.getData());
        
        // Mock email delivery - in production, use SendGrid/AWS SES
        try {
            // Simulate email sending
            Thread.sleep(100); // Simulate API call latency
            
            log.info("✅ EMAIL delivered successfully to user: {}", request.getUserId());
            return DeliveryResult.success("EMAIL", "Email sent successfully via mock SendGrid");
            
        } catch (Exception e) {
            log.error("❌ Failed to deliver EMAIL: {}", e.getMessage());
            return DeliveryResult.failure("EMAIL", e.getMessage());
        }
    }
    
    @Override
    public boolean supportsChannel(String channelName) {
        return "EMAIL".equalsIgnoreCase(channelName);
    }
    
    // Fallback method for circuit breaker
    public DeliveryResult deliverFallback(DeliveryRequest request, Exception e) {
        log.error("🔴 Circuit breaker fallback for EMAIL. Error: {}", e.getMessage());
        return DeliveryResult.failure("EMAIL", "Circuit breaker open: " + e.getMessage());
    }
}

