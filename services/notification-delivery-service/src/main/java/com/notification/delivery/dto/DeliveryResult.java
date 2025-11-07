package com.notification.delivery.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryResult {
    private boolean success;
    private String channel;
    private String message;
    private String errorDetails;
    
    public static DeliveryResult success(String channel, String message) {
        return DeliveryResult.builder()
                .success(true)
                .channel(channel)
                .message(message)
                .build();
    }
    
    public static DeliveryResult failure(String channel, String errorDetails) {
        return DeliveryResult.builder()
                .success(false)
                .channel(channel)
                .errorDetails(errorDetails)
                .build();
    }
}

