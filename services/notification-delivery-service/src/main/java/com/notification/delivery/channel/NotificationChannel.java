package com.notification.delivery.channel;

import com.notification.delivery.dto.DeliveryRequest;
import com.notification.delivery.dto.DeliveryResult;

public interface NotificationChannel {
    String getChannelName();
    DeliveryResult deliver(DeliveryRequest request);
    boolean supportsChannel(String channelName);
}

