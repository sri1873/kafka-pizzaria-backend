package com.kafka.learn.dto;

import lombok.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class OrderDetails {
    @Builder.Default
    private UUID orderId = UUID.randomUUID();
    @Builder.Default
    private Instant createdAt = Instant.now();
    @Builder.Default
    private Instant lastUpdated = Instant.now();
    private OrderStatus status;
    private UUID userId;
    private UUID riderId;
    private List<Items> items;
}
