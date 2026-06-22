package com.kafka.learn.controller;

import com.kafka.learn.dto.OrderDetailsRequest;
import com.kafka.learn.dto.UpdateOrderStatusDTO;
import com.kafka.learn.entities.OrderDetails;
import com.kafka.learn.service.RestaurantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

}
