package com.kafka.learn.controller;

import com.kafka.learn.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.UUID;

@RestController
public class NotificationController {
    @Autowired
    private NotificationService notificationService;

    @GetMapping(value = "/restaurant/events/{userId}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter restaurantEvents(@PathVariable UUID userId) {
        return notificationService.subscribe(userId);
    }

    @GetMapping(value = "/customer/events/{userId}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter customerEvents(@PathVariable UUID userId) {
        return notificationService.subscribe(userId);
    }
}
