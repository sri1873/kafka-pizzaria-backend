package com.kafka.learn.service;

import com.kafka.learn.dto.OrderDetails;
import com.kafka.learn.dto.OrderStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class RestaurentService {
    //    TODO
    //      Restaurent recieves order and accepts or rejects.
    //      Payment received from rider and confirms order to rider.

    @Autowired
    private KafkaTemplate<String, OrderDetails> kafkaTemplate;

    @KafkaListener(topics = "order_info", groupId = "restaurent-service-group")
    public void handleOrderPlaced(OrderDetails order) {
        if (order.getStatus() == OrderStatus.PLACED){
        order.setStatus(OrderStatus.READY_FOR_PICKUP);
        order.setLastUpdated(Instant.now());
        kafkaTemplate.send("order_info", order.getOrderId().toString(), order);
//        orderReadyForPickup(order);
        System.out.println("Received Message in restaurent service: " + order.toString());
        }
    }

    public void orderReadyForPickup(OrderDetails order) {
        order.setStatus(OrderStatus.READY_FOR_PICKUP);
        order.setLastUpdated(Instant.now());
        kafkaTemplate.send("order_info", order.getOrderId().toString(), order);
    }


}
