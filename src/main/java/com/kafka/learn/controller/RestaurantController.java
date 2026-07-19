package com.kafka.learn.controller;

import com.kafka.learn.dto.OrderDetailsRequest;
import com.kafka.learn.dto.UpdateOrderStatusDTO;
import com.kafka.learn.entities.OrderDetails;
import com.kafka.learn.service.RestaurantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
public class RestaurantController {


    @Autowired
    private RestaurantService restaurantService;

    @PutMapping(value = "/restaurant/update/orderstatus", headers = {"content-type=application/json"})
    public void updateOrderStatus(@RequestBody UpdateOrderStatusDTO updateOrderStatusDTO) {
        restaurantService.updateOrderStatus(updateOrderStatusDTO);
    }

    @GetMapping(value = "/restaurant/orders")
    public List<OrderDetails> findAllOrders(@RequestParam String status) {
        return restaurantService.getOrderByStatus(status);
    }

    @PostMapping(value = "/restaurant/placeorder", headers = {"content-type=application/json"})
    public OrderDetails order(@RequestBody OrderDetailsRequest orderDetailsRequest) {
        return restaurantService.newOrder(orderDetailsRequest);
    }

    @GetMapping("/restaurant/order/byrider")
    public ResponseEntity<OrderDetails> findByRiderId(@RequestParam UUID riderId) {

        return restaurantService.getOrderByRiderId(riderId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/restaurant/order/byuser")
    public List<OrderDetails> findByuserId(@RequestParam UUID userId) {

        return restaurantService.getOrderByUserId(userId);
    }

}
