package com.kafka.learn.service;

import com.kafka.learn.dto.OrderStatus;
import com.kafka.learn.entities.OrderDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
public class RiderService {
//    TODO
//      Driver Assignment
//      Driver Loaction Tracking
//      Driver accepts or rejects order

    @Autowired
    private KafkaTemplate<String, OrderDetails> kafkaTemplate;

    @KafkaListener(topics = "order_info", groupId = "rider-service-group")
    public void consume(OrderDetails order) {
        System.out.println("Consumed Message in rider service: " + order.toString());
        if (order.getStatus() == OrderStatus.READY_FOR_PICKUP) {

            UUID assignedRider = assignRider();

//            order.setRiderId(assignedRider);
            order.setStatus(OrderStatus.RIDER_ASSIGNED);
            order.setLastUpdated(Instant.now());

            kafkaTemplate.send("order_info", order.getOrderId().toString(), order);
            System.out.println("Received Message in rider service: " + order.toString());
        }
    }

    public void acceptsOrder(UUID orderId) {
        OrderDetails order = new OrderDetails();
        order.setStatus(OrderStatus.RIDER_ACCEPTED);
        order.setLastUpdated(Instant.now());
        kafkaTemplate.send("order_info", order.getOrderId().toString(), order);
    }

    public void rejectOrder(UUID orderId) {
        OrderDetails order = new OrderDetails();
        order.setStatus(OrderStatus.READY_FOR_PICKUP);
        order.setLastUpdated(Instant.now());
        kafkaTemplate.send("order_info", order.getOrderId().toString(), order);
    }

    public void deliverOrder(UUID orderId) {
        OrderDetails order = new OrderDetails();
        order.setStatus(OrderStatus.DELIVERED);
        order.setLastUpdated(Instant.now());
        kafkaTemplate.send("order_info", order.getOrderId().toString(), order);
    }

    public void pickupOrder(UUID orderId) {
        OrderDetails order = new OrderDetails();
        order.setStatus(OrderStatus.OUT_FOR_DELIVERY);
        order.setLastUpdated(Instant.now());
        kafkaTemplate.send("order_info", order.getOrderId().toString(), order);
    }

    private UUID assignRider() {
        // Simulate rider assignment logic
        return UUID.randomUUID();

    }

}
