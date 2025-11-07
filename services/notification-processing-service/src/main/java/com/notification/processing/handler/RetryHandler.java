package com.notification.processing.handler;

import com.notification.processing.model.NotificationEvent;
import com.notification.processing.service.IdempotencyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class RetryHandler {
    
    private final IdempotencyService idempotencyService;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    
    @Value("${notification.processing.retry.max-attempts}")
    private int maxAttempts;
    
    @Value("${notification.processing.retry.initial-interval-ms}")
    private long initialIntervalMs;
    
    @Value("${notification.processing.retry.multiplier}")
    private double multiplier;
    
    @Value("${notification.processing.retry.max-interval-ms}")
    private long maxIntervalMs;
    
    @Value("${notification.kafka.topics.input}")
    private String retryTopic;
    
    @Value("${notification.kafka.topics.dlq}")
    private String dlqTopic;
    
    public boolean shouldRetry(NotificationEvent event) {
        int currentRetryCount = event.getRetryCount() != null ? event.getRetryCount() : 0;
        boolean shouldRetry = currentRetryCount < maxAttempts;
        
        if (!shouldRetry) {
            log.warn("Max retry attempts ({}) reached for notification: {}", 
                    maxAttempts, event.getNotificationId());
        }
        
        return shouldRetry;
    }
    
    public void scheduleRetry(NotificationEvent event, Exception exception) {
        int currentRetryCount = event.getRetryCount() != null ? event.getRetryCount() : 0;
        int nextRetryCount = currentRetryCount + 1;
        
        if (nextRetryCount >= maxAttempts) {
            sendToDLQ(event, exception);
            return;
        }
        
        long waitTime = calculateBackoffTime(nextRetryCount);
        
        log.info("Scheduling retry {} of {} for notification {} after {}ms. Error: {}", 
                nextRetryCount, maxAttempts, event.getNotificationId(), waitTime, exception.getMessage());
        
        // Update retry count
        event.setRetryCount(nextRetryCount);
        idempotencyService.incrementRetryCount(event.getNotificationId());
        
        // In a real system, you'd use a scheduled retry mechanism
        // For simplicity, we'll re-publish to the same topic after a delay
        try {
            Thread.sleep(waitTime);
            kafkaTemplate.send(retryTopic, event.getNotificationId(), event);
            log.info("Retry scheduled successfully for notification: {}", event.getNotificationId());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Retry scheduling interrupted for notification: {}", event.getNotificationId());
            sendToDLQ(event, e);
        }
    }
    
    public void sendToDLQ(NotificationEvent event, Exception exception) {
        log.error("Sending notification to DLQ after {} retries. NotificationId: {}, Error: {}", 
                event.getRetryCount(), event.getNotificationId(), exception.getMessage());
        
        // Add error information to the event
        event.setRetryCount(event.getRetryCount() != null ? event.getRetryCount() : 0);
        
        kafkaTemplate.send(dlqTopic, event.getNotificationId(), event);
        log.info("Notification sent to DLQ: {}", event.getNotificationId());
    }
    
    private long calculateBackoffTime(int retryCount) {
        long backoff = (long) (initialIntervalMs * Math.pow(multiplier, retryCount - 1));
        return Math.min(backoff, maxIntervalMs);
    }
}

