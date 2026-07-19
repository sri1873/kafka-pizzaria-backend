package com.kafka.learn.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "rider")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Rider {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID riderId;
    private String name;
    private boolean assigned;


}
