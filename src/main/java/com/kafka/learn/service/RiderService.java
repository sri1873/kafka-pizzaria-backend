package com.kafka.learn.service;

import com.kafka.learn.dto.Notification;
import com.kafka.learn.dto.OrderStatus;
import com.kafka.learn.entities.OrderDetails;
import com.kafka.learn.entities.Rider;
import com.kafka.learn.repositories.RestaurantRepository;
import com.kafka.learn.repositories.RiderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.geo.Point;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Service
public class RiderService {
//    TODO
//      Driver Assignment
//      Driver Loaction Tracking
//      Driver accepts or rejects order

    private final Point restaurantLocation = new Point(-6.2603, 53.3498);
    @Autowired
    private KafkaTemplate<String, OrderDetails> kafkaTemplate;
    @Autowired
    private RiderLocationService riderLocationService;
    @Autowired
    private NotificationService notificationService;
    @Autowired
    private RiderRepository riderRepository;
    @Autowired
    private RestaurantRepository restaurantRepository;
    private Map<UUID, CompletableFuture<Boolean>> pendingResponses = new ConcurrentHashMap<>();

    @KafkaListener(topics = "order_info", groupId = "rider-service-group")
    public void consume(OrderDetails order) {
        System.out.println("Consumed Message in rider service: " + order.toString());
        if (order.getStatus() == OrderStatus.READY_FOR_PICKUP) {
            assignRider(order);
        }
    }

    @Async
    private void assignRider(OrderDetails order) {
        List<UUID> nearbyRiders = riderLocationService.findNearbyRiders(restaurantLocation, 5.0);
        for (UUID riderId : nearbyRiders) {
            if (restaurantRepository.findByRiderIdAndStatusNot(riderId).isEmpty()) {
                CompletableFuture<Boolean> future = new CompletableFuture<>();
                pendingResponses.put(order.getOrderId(), future);
                notificationService.sendNotification(riderId,
                        Notification.builder().role("RIDER").orderDetails(order).orderId(order.getOrderId()).message("NEW ORDER ASSIGNMENT").build());
                try {
                    // wait up to 10 seconds for accept/decline
                    Boolean accepted = future.get(10, TimeUnit.SECONDS);
                    if (accepted) {
                        Rider rider = riderRepository.findByRiderId(riderId).orElse(null);
                        rider.setAssigned(true);
                        order.setRider(rider);
                        order.setStatus(OrderStatus.RIDER_ASSIGNED);
                        order.setLastUpdated(Instant.now());
                        riderRepository.save(rider);
                        restaurantRepository.save(order);
                        kafkaTemplate.send("order_info", order.getOrderId().toString(), order);

                        return;
                    }
                    // declined — move to next rider
                } catch (TimeoutException e) {
                    // no response in time — move to next rider
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    pendingResponses.remove(order.getOrderId());
                }
            }
        }
    }

    public void acceptOrder(UUID orderId) {
        CompletableFuture<Boolean> future = pendingResponses.get(orderId);
        if (future != null) {
            future.complete(true); // unblocks the waiting assignRider loop
        }
    }

    public void rejectOrder(UUID orderId) {
        CompletableFuture<Boolean> future = pendingResponses.get(orderId);
        if (future != null) {
            future.complete(false); // unblocks the waiting assignRider loop
        }
    }

    public void deliverOrder(UUID orderId) {

        Optional<OrderDetails> orderDetailsOptional = restaurantRepository.findById(orderId);
        if (orderDetailsOptional.isPresent()) {
            OrderDetails orderDetails = orderDetailsOptional.get();
            orderDetails.setStatus(OrderStatus.DELIVERED);
            orderDetails.setLastUpdated(Instant.now());
            orderDetails.getRider().setAssigned(false);
            restaurantRepository.save(orderDetails);
            riderRepository.save(orderDetails.getRider());
            kafkaTemplate.send("order_info", orderId.toString(), orderDetails);
        }
    }

    public void pickupOrder(UUID orderId) {
        Optional<OrderDetails> orderDetailsOptional = restaurantRepository.findById(orderId);
        if (orderDetailsOptional.isPresent()) {
            OrderDetails orderDetails = orderDetailsOptional.get();
            orderDetails.setStatus(OrderStatus.OUT_FOR_DELIVERY);
            orderDetails.setLastUpdated(Instant.now());
            restaurantRepository.save(orderDetails);
            kafkaTemplate.send("order_info", orderId.toString(), orderDetails);
        }
    }

    public boolean riderAssigned(UUID riderId) {
        Optional<Rider> byRiderId = riderRepository.findByRiderId(riderId);
        return byRiderId.isPresent() && byRiderId.get().isAssigned();
    }
}
