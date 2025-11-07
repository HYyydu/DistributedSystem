package com.notification.processing;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;

@SpringBootApplication
@EnableKafka
public class NotificationProcessingServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(NotificationProcessingServiceApplication.class, args);
    }
}

