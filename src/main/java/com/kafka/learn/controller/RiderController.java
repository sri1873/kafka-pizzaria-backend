package com.kafka.learn.controller;

import com.kafka.learn.service.RiderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
public class RiderController {

    @Autowired
    private RiderService riderService;

    @GetMapping("/rider/accept")
    public void acceptOrder(@RequestParam UUID orderId) {
        riderService.acceptsOrder(orderId);
    }

    @GetMapping("/rider/reject")
    public void rejectOrder(@RequestParam UUID orderId) {
        riderService.rejectOrder(orderId);
    }

    @GetMapping("/rider/pickup")
    public void pickupOrder(@RequestParam UUID orderId) {
        riderService.pickupOrder(orderId);
    }

    @GetMapping("/rider/deliver")
    public void deliverOrder(@RequestParam UUID orderId) {
        riderService.deliverOrder(orderId);
        }
}
