package com.notification.processing.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserPreferences {
    private List<String> channels;
    private QuietHours quietHours;
    private List<String> blockedEventTypes;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QuietHours {
        private String start;  // e.g., "22:00"
        private String end;    // e.g., "08:00"
    }
}

