package com.kafka.learn.utils;

import com.kafka.learn.entities.RiderLocation;
import com.kafka.learn.service.RiderLocationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import tools.jackson.databind.ObjectMapper;

import java.util.UUID;

@Component
public class RiderLocationHandler extends TextWebSocketHandler {

    @Autowired
    private RiderLocationService riderLocationService;

    private ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        RiderLocation location = objectMapper.readValue(message.getPayload(), RiderLocation.class);
        session.getAttributes().put("riderId", location.getRiderId());
        riderLocationService.updateLocation(
                location.getRiderId(),
                location.getLatitude(),
                location.getLongitude()
        );

    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        Object riderId = session.getAttributes().get("riderId");

        if (riderId != null) {
            riderLocationService.removeRider(UUID.fromString(riderId.toString()));
        }

    }
}
