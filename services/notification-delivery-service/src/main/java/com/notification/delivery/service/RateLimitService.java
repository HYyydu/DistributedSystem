package com.notification.delivery.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
@Slf4j
public class RateLimitService {
    
    private final RedisTemplate<String, String> redisTemplate;
    
    @Value("${notification.delivery.channels.email.rate-limit-per-hour:100}")
    private int emailLimit;
    
    @Value("${notification.delivery.channels.sms.rate-limit-per-hour:50}")
    private int smsLimit;
    
    @Value("${notification.delivery.channels.push.rate-limit-per-hour:200}")
    private int pushLimit;
    
    @Value("${notification.delivery.channels.webhook.rate-limit-per-hour:100}")
    private int webhookLimit;
    
    private static final String KEY_PREFIX = "rate-limit:";
    
    public boolean isAllowed(String userId, String channel) {
        String key = buildKey(userId, channel);
        String countStr = redisTemplate.opsForValue().get(key);
        
        int currentCount = countStr != null ? Integer.parseInt(countStr) : 0;
        int limit = getLimit(channel);
        
        if (currentCount >= limit) {
            log.warn("⚠️ Rate limit exceeded for user: {}, channel: {}. Count: {}, Limit: {}", 
                    userId, channel, currentCount, limit);
            return false;
        }
        
        // Increment counter
        if (currentCount == 0) {
            // First request in this hour, set with TTL
            redisTemplate.opsForValue().set(key, "1", Duration.ofHours(1));
        } else {
            redisTemplate.opsForValue().increment(key);
        }
        
        log.debug("✅ Rate limit check passed for user: {}, channel: {}. Count: {}/{}", 
                userId, channel, currentCount + 1, limit);
        return true;
    }
    
    private String buildKey(String userId, String channel) {
        String hour = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd-HH"));
        return KEY_PREFIX + userId + ":" + channel + ":" + hour;
    }
    
    private int getLimit(String channel) {
        return switch (channel.toUpperCase()) {
            case "EMAIL" -> emailLimit;
            case "SMS" -> smsLimit;
            case "PUSH" -> pushLimit;
            case "WEBHOOK" -> webhookLimit;
            default -> 100; // Default limit
        };
    }
}

