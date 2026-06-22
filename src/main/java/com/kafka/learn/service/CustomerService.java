package com.kafka.learn.service;

import com.kafka.learn.dto.Notification;
import com.kafka.learn.entities.OrderDetails;
import com.kafka.learn.entities.User;
import com.kafka.learn.repositories.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class CustomerService {


    @Autowired
    NotificationService notificationService;

    @Autowired
    UserRepository userRepository;

    @KafkaListener(topics = "order_info", groupId = "customer-service-group")
    public void customerOrderUpdate(OrderDetails order) {
        notificationService.sendNotification(order.getUser().getUserId(),
                Notification.builder().orderId(order.getOrderId()).orderDetails(order).role("CUSTOMER").message(order.getItems().toString()).build());
        System.out.println("Received Message in customer service " + order.toString());
    }

    @Transactional
    public void createUser(UUID userId) {
        userRepository.save(User.builder().build());
    }
}

