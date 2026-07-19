package com.kafka.learn.repositories;

import com.kafka.learn.dto.OrderStatus;
import com.kafka.learn.entities.OrderDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RestaurantRepository extends JpaRepository<OrderDetails, UUID> {

    @Query(value = "SELECT o FROM OrderDetails o WHERE o.orderId = :orderId AND o.user.userId = :userId")
    Optional<OrderDetails> findByOrderIdAndUserId(UUID orderId, UUID userId);

    @Query(value = "SELECT o FROM OrderDetails o WHERE o.status = :status")
    List<OrderDetails> findByStatus(OrderStatus status);

    @Query(value = "SELECT o FROM OrderDetails o WHERE o.rider.riderId = :riderId and o.status != 'DELIVERED'")
    Optional<OrderDetails> findByRiderId(UUID riderId);

    @Query(value = "SELECT o FROM OrderDetails o WHERE o.user.userId = :userId ORDER BY o.lastUpdated DESC LIMIT 1")
    List<OrderDetails> findByUserId(UUID userId);

    @Query(value = "SELECT o FROM OrderDetails o WHERE o.rider.riderId = :riderId and o.status != 'DELIVERED'")
    List<OrderDetails> findByRiderIdAndStatusNot(UUID riderId);

}

