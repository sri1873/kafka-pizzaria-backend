package com.kafka.learn.dto.request;

import com.kafka.learn.dto.Items;
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
