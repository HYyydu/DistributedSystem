package com.notification.delivery.service;

import com.notification.delivery.channel.NotificationChannel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChannelFactory {
    
    private final List<NotificationChannel> channels;
    
    public NotificationChannel getChannel(String channelName) {
        return channels.stream()
                .filter(channel -> channel.supportsChannel(channelName))
                .findFirst()
                .orElseThrow(() -> {
                    log.error("No channel implementation found for: {}", channelName);
                    return new IllegalArgumentException("Unsupported channel: " + channelName);
                });
    }
}

