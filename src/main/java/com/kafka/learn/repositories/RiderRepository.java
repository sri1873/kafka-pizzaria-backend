package com.kafka.learn.repositories;

import com.kafka.learn.entities.Rider;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface RiderRepository extends JpaRepository<Rider, UUID> {

    @Query(value = "SELECT r FROM Rider r WHERE r.riderId = :riderId")
    Optional<Rider> findByRiderId(UUID riderId);

}
