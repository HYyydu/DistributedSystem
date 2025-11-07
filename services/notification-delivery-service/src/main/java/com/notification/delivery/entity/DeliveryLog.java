package com.notification.delivery.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "delivery_logs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryLog {
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;
    
    @Column(name = "notification_id", nullable = false)
    private UUID notificationId;
    
    @Column(nullable = false, length = 50)
    private String channel;
    
    @Column(nullable = false, length = 50)
    private String status;
    
    @Column(name = "attempt_count")
    private Integer attemptCount;
    
    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;
    
    @Column(name = "delivered_at")
    private LocalDateTime deliveredAt;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}

