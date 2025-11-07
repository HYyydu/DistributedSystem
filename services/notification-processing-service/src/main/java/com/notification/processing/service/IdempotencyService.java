package com.notification.processing.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
@Slf4j
public class IdempotencyService {
    
    private final RedisTemplate<String, String> redisTemplate;
    
    @Value("${notification.processing.idempotency.ttl-seconds}")
    private long ttlSeconds;
    
    private static final String KEY_PREFIX = "notification:processed:";
    
    public boolean isAlreadyProcessed(String notificationId) {
        String key = KEY_PREFIX + notificationId;
        Boolean exists = redisTemplate.hasKey(key);
        boolean result = Boolean.TRUE.equals(exists);
        
        if (result) {
            log.info("Notification already processed (duplicate): {}", notificationId);
        }
        
        return result;
    }
    
    public void markAsProcessed(String notificationId) {
        String key = KEY_PREFIX + notificationId;
        redisTemplate.opsForValue().set(key, "PROCESSED", Duration.ofSeconds(ttlSeconds));
        log.debug("Marked notification as processed: {}", notificationId);
    }
    
    public void incrementRetryCount(String notificationId) {
        String key = KEY_PREFIX + notificationId + ":retry";
        redisTemplate.opsForValue().increment(key);
        redisTemplate.expire(key, Duration.ofSeconds(ttlSeconds));
    }
    
    public Long getRetryCount(String notificationId) {
        String key = KEY_PREFIX + notificationId + ":retry";
        String count = redisTemplate.opsForValue().get(key);
        return count != null ? Long.parseLong(count) : 0L;
    }
}

