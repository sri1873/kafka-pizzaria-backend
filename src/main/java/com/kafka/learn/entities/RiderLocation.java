package com.kafka.learn.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class RiderLocation {
    UUID riderId;
    double latitude;
    double longitude;
}
