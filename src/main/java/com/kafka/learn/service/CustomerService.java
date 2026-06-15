package com.kafka.learn.service;

import com.kafka.learn.dto.Notification;
import com.kafka.learn.dto.OrderDetails;
import com.kafka.learn.utils.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class CustomerService {


    @Autowired
    NotificationService notificationService;

    @KafkaListener(topics = "order_info", groupId = "customer-service-group")
    public void customerOrderUpdate(OrderDetails order) {
        notificationService.sendNotification(order.getUserId(),
                Notification.builder().orderId(order.getOrderId()).role("CUSTOMER").message(order.getStatus().toString() + order.getItems().get(0)).build());
        System.out.println("Received Message in customer service " + order.toString());
    }
}

