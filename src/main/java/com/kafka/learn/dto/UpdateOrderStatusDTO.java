package com.kafka.learn.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class UpdateOrderStatusDTO {
    private UUID orderId;
    private UUID userId;
    private String status;

}
