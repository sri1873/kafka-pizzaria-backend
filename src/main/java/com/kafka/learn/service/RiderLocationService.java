package com.kafka.learn.service;

import com.kafka.learn.entities.RiderLocation;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.geo.Circle;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.core.GeoOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.domain.geo.Metrics;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class RiderLocationService {

    private static final String RIDERS_KEY = "active_riders";
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    private GeoOperations<String, Object> geoOperations;

    @PostConstruct
    public void init() {
        geoOperations = redisTemplate.opsForGeo();
    }

    public void updateLocation(UUID riderId, double lat, double lng) {
        geoOperations.add(RIDERS_KEY, new Point(lng, lat), riderId);
        redisTemplate.expire(riderId.toString(), 30, TimeUnit.SECONDS); // auto-expire offline riders
    }

    public RiderLocation getLocation(UUID riderId) {
        Point point = geoOperations.position(RIDERS_KEY, riderId).get(0);
        if (point != null) {
            return RiderLocation.builder().longitude(point.getX()).latitude(point.getY()).build();
        }
        return null;
    }

    public List<UUID> findNearbyRiders(Point restaurantLocation, double radiusKm) {
        RedisGeoCommands.GeoRadiusCommandArgs args = RedisGeoCommands.GeoRadiusCommandArgs.newGeoRadiusArgs().sortAscending();
        return geoOperations.radius(RIDERS_KEY, new Circle(restaurantLocation, new Distance(radiusKm, Metrics.KILOMETERS)), args)
                .getContent()
                .stream()
                .map(geoResult -> UUID.fromString(geoResult.getContent().getName().toString()))
                .toList();


    }

    // 3. remove a rider when they go offline
    public void removeRider(UUID riderId) {
        geoOperations.remove(RIDERS_KEY, riderId);
    }
}