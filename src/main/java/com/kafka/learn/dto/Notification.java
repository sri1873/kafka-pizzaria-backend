package com.kafka.learn.dto;

import com.kafka.learn.entities.OrderDetails;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Notification {
    private UUID orderId;
    private String role;
    private String message;
    private OrderDetails orderDetails;
}
