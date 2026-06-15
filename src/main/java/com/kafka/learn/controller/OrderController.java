package com.kafka.learn.controller;


import com.kafka.learn.dto.request.OrderDetailsRequest;
import com.kafka.learn.dto.OrderDetails;
import com.kafka.learn.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class OrderController {

    @Autowired
    OrderService orderService;

    @PostMapping(value = "/order", headers = {"content-type=application/json"})
    public OrderDetails order(@RequestBody OrderDetailsRequest orderDetailsRequest) {
        return orderService.newOrder(orderDetailsRequest);
    }

}
