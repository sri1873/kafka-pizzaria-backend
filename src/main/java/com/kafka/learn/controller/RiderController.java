package com.kafka.learn.controller;

import com.kafka.learn.service.RiderLocationService;
import com.kafka.learn.service.RiderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
public class RiderController {

    @Autowired
    private RiderService riderService;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private RiderLocationService riderLocationService;

    @PutMapping("/redis/updateRiderLocation")
    public String updateRiderLocation(@RequestParam UUID riderId, @RequestParam double latitude, @RequestParam double longitude) {
        riderLocationService.updateLocation(riderId, latitude, longitude);
        return "Location updated";
    }

    @PostMapping("/rider/accept")
    public void acceptOrder(@RequestParam UUID orderId, @RequestParam UUID riderId) {
        riderService.acceptOrder(orderId);
    }

    @GetMapping("/rider/assigned")
    public boolean riderAssigned(@RequestParam UUID riderId) {
        return riderService.riderAssigned(riderId);
    }


    @PostMapping("/rider/reject")
    public void rejectOrder(@RequestParam UUID orderId, @RequestParam UUID riderId) {
        riderService.rejectOrder(orderId);
    }

    @PostMapping("/rider/pickup")
    public void pickupOrder(@RequestParam UUID orderId, @RequestParam UUID riderId) {
        riderService.pickupOrder(orderId);
    }

    @PostMapping("/rider/deliver")
    public void deliverOrder(@RequestParam UUID orderId, @RequestParam UUID riderId) {
        riderService.deliverOrder(orderId);
    }
}
