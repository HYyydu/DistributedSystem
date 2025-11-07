package com.notification.processing.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.notification.processing.dto.UserPreferences;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserPreferenceService {
    
    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;
    
    public UserPreferences getUserPreferences(String userId) {
        try {
            String sql = "SELECT preferences FROM users WHERE id = ?";
            String preferencesJson = jdbcTemplate.queryForObject(sql, String.class, userId);
            
            if (preferencesJson != null) {
                return objectMapper.readValue(preferencesJson, UserPreferences.class);
            }
        } catch (Exception e) {
            log.warn("Could not fetch preferences for user: {}. Using defaults. Error: {}", 
                    userId, e.getMessage());
        }
        
        // Return default preferences
        return UserPreferences.builder()
                .channels(List.of("EMAIL", "SMS", "PUSH", "WEBHOOK"))
                .build();
    }
    
    public boolean isChannelEnabled(UserPreferences preferences, String channel) {
        if (preferences == null || preferences.getChannels() == null) {
            return true;  // Default: all channels enabled
        }
        return preferences.getChannels().contains(channel);
    }
    
    public boolean isInQuietHours(UserPreferences preferences) {
        if (preferences == null || preferences.getQuietHours() == null) {
            return false;  // No quiet hours configured
        }
        
        try {
            UserPreferences.QuietHours quietHours = preferences.getQuietHours();
            LocalTime now = LocalTime.now();
            LocalTime start = LocalTime.parse(quietHours.getStart(), DateTimeFormatter.ofPattern("HH:mm"));
            LocalTime end = LocalTime.parse(quietHours.getEnd(), DateTimeFormatter.ofPattern("HH:mm"));
            
            // Handle quiet hours that span midnight
            if (start.isBefore(end)) {
                return now.isAfter(start) && now.isBefore(end);
            } else {
                return now.isAfter(start) || now.isBefore(end);
            }
        } catch (Exception e) {
            log.warn("Error checking quiet hours: {}", e.getMessage());
            return false;
        }
    }
    
    public boolean isEventTypeBlocked(UserPreferences preferences, String eventType) {
        if (preferences == null || preferences.getBlockedEventTypes() == null) {
            return false;
        }
        return preferences.getBlockedEventTypes().contains(eventType);
    }
    
    public List<String> filterChannels(UserPreferences preferences, List<String> requestedChannels) {
        if (preferences == null || preferences.getChannels() == null) {
            return requestedChannels;
        }
        
        return requestedChannels.stream()
                .filter(channel -> preferences.getChannels().contains(channel))
                .toList();
    }
}

