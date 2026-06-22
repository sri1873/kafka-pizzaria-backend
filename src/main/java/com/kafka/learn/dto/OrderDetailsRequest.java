package com.kafka.learn.dto;

import com.kafka.learn.entities.Items;
import lombok.*;

import java.util.List;
import java.util.UUID;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class OrderDetailsRequest {
    private UUID userId;
    private List<Items> items;
    private String destination;
    private String deliveryAddress;
}
