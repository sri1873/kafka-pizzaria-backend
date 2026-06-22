package com.kafka.learn.service;

import com.kafka.learn.dto.Notification;
import com.kafka.learn.dto.OrderDetailsRequest;
import com.kafka.learn.dto.OrderStatus;
import com.kafka.learn.dto.UpdateOrderStatusDTO;
import com.kafka.learn.entities.OrderDetails;
import com.kafka.learn.entities.User;
import com.kafka.learn.repositories.RestaurantRepository;
import com.kafka.learn.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class RestaurantService {
    //    TODO
    //      Restaurent recieves order and accepts or rejects.
    //      Payment received from rider and confirms order to rider.

    private final UUID restaurantId = UUID.fromString("f3e1c2d4-5b6a-7c8d-9e0f-1a2b3c4d5e6f");
    @Autowired
    private KafkaTemplate<String, OrderDetails> kafkaTemplate;
    @Autowired
    private NotificationService notificationService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RestaurantRepository restaurantRepository;

    public OrderDetails newOrder(OrderDetailsRequest orderDetailsRequest) {
        Optional<User> byId = userRepository.findById(orderDetailsRequest.getUserId());
        OrderDetails orderDetails = OrderDetails.builder().status(OrderStatus.PLACED).items(orderDetailsRequest.getItems())
                .user(byId.get()).build();

        restaurantRepository.save(orderDetails);
        kafkaTemplate.send("order_info", orderDetails.getOrderId().toString(), orderDetails);
        return orderDetails;
    }


    @KafkaListener(topics = "order_info", groupId = "restaurant-service-group")
    public void handleOrderPlaced(OrderDetails order) {
        if (order.getStatus() == OrderStatus.PLACED) {
            notificationService.sendNotification(restaurantId, Notification.builder().orderId(order.getOrderId()).orderDetails(order)
                    .message("New Order Placed").build());
        }
    }

    public void updateOrderStatus(UpdateOrderStatusDTO updateOrderStatusDTO) {
        restaurantRepository.findByOrderIdAndUserId(updateOrderStatusDTO.getOrderId(), updateOrderStatusDTO.getUserId())
                .ifPresent(order -> {
                    order.setStatus(OrderStatus.valueOf(updateOrderStatusDTO.getStatus()));
                    order.setLastUpdated(Instant.now());
                    restaurantRepository.save(order);
                    kafkaTemplate.send("order_info", order.getOrderId().toString(), order);
                });
    }

    public List<OrderDetails> getOrderByStatus(String orderStatus) {
        return restaurantRepository.findByStatus(OrderStatus.valueOf(orderStatus));
    }
}
