package com.kafka.learn.service;

import com.kafka.learn.dto.Notification;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class NotificationService {

    private final Map<UUID, SseEmitter> emitters = new ConcurrentHashMap<>();
    private final Map<UUID, Notification> lastNotifications = new ConcurrentHashMap<>();

    public SseEmitter subscribe(UUID userId) {
        SseEmitter emitter = new SseEmitter(0L);
        emitters.put(userId, emitter);

        Notification last = lastNotifications.get(userId);
        if (last != null) {
            try {
                emitter.send(SseEmitter.event()
                        .name("order-update")
                        .data(last));
            } catch (Exception e) {
                emitters.remove(userId);
            }
        }
        emitter.onCompletion(() -> emitters.remove(userId));
        emitter.onTimeout(() -> emitters.remove(userId));
        return emitter;
    }

    public void sendNotification(UUID userId, Notification notification) {
        lastNotifications.put(userId, notification);
        SseEmitter emitter = emitters.get(userId);
        if (emitter != null) {
            try {
                emitter.send(SseEmitter.event()
                        .name("order-update")
                        .data(notification));
            } catch (Exception e) {
                emitters.remove(userId);
            }
        }
    }

}
