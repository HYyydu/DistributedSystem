package com.notification.delivery.channel;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.notification.delivery.dto.DeliveryRequest;
import com.notification.delivery.dto.DeliveryResult;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
@RequiredArgsConstructor
public class WebhookChannel implements NotificationChannel {
    
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    
    @Override
    public String getChannelName() {
        return "WEBHOOK";
    }
    
    @Override
    @CircuitBreaker(name = "webhookChannel", fallbackMethod = "deliverFallback")
    @Retry(name = "webhookChannel")
    public DeliveryResult deliver(DeliveryRequest request) {
        log.info("🔗 Delivering WEBHOOK notification");
        log.info("   NotificationId: {}", request.getNotificationId());
        log.info("   User: {}", request.getUserId());
        
        try {
            // Extract webhook URL from data
            Map<String, Object> dataMap = objectMapper.readValue(request.getData(), Map.class);
            String webhookUrl = (String) dataMap.get("webhookUrl");
            
            if (webhookUrl == null || webhookUrl.isEmpty()) {
                log.warn("⚠️ No webhook URL provided, skipping webhook delivery");
                return DeliveryResult.success("WEBHOOK", "No webhook URL provided");
            }
            
            // Prepare webhook payload
            Map<String, Object> payload = new HashMap<>();
            payload.put("notificationId", request.getNotificationId());
            payload.put("userId", request.getUserId());
            payload.put("eventType", request.getEventType());
            payload.put("data", dataMap);
            payload.put("timestamp", System.currentTimeMillis());
            
            // Set headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);
            
            // Make HTTP POST request
            log.info("📤 Sending webhook to: {}", webhookUrl);
            ResponseEntity<String> response = restTemplate.postForEntity(webhookUrl, entity, String.class);
            
            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("✅ WEBHOOK delivered successfully to: {}", webhookUrl);
                return DeliveryResult.success("WEBHOOK", "Webhook delivered to " + webhookUrl);
            } else {
                log.error("❌ Webhook delivery failed with status: {}", response.getStatusCode());
                return DeliveryResult.failure("WEBHOOK", "HTTP " + response.getStatusCode());
            }
            
        } catch (Exception e) {
            log.error("❌ Failed to deliver WEBHOOK: {}", e.getMessage());
            return DeliveryResult.failure("WEBHOOK", e.getMessage());
        }
    }
    
    @Override
    public boolean supportsChannel(String channelName) {
        return "WEBHOOK".equalsIgnoreCase(channelName);
    }
    
    // Fallback method for circuit breaker
    public DeliveryResult deliverFallback(DeliveryRequest request, Exception e) {
        log.error("🔴 Circuit breaker/Retry fallback for WEBHOOK. Error: {}", e.getMessage());
        return DeliveryResult.failure("WEBHOOK", "Circuit breaker open or max retries: " + e.getMessage());
    }
}

