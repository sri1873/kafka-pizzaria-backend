package com.kafka.learn.service;

import com.kafka.learn.dto.OrderStatus;
import com.kafka.learn.dto.request.OrderDetailsRequest;
import com.kafka.learn.dto.OrderDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class OrderService {

    @Autowired
    private KafkaTemplate<String, OrderDetails> kafkaTemplate;

    public OrderDetails newOrder(OrderDetailsRequest orderDetailsRequest) {
        OrderDetails orderDetails = OrderDetails.builder().status(OrderStatus.PLACED).items(orderDetailsRequest.getItems()).userId(orderDetailsRequest.getUserId()).build();
        kafkaTemplate.send("order_info", orderDetails.getOrderId().toString(), orderDetails);
        return orderDetails;
    }
}
