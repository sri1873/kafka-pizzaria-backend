package com.kafka.learn.entities;

import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "rider")
public class Rider {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID riderId;


}
