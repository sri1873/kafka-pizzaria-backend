# Pizza Kafka — Backend

An event-driven food delivery backend built on Apache Kafka, showcasing real-time order orchestration across four independent Spring Boot microservices, live rider geolocation with Redis, and real-time client updates via Server-Sent Events and WebSocket.

**Frontend repo:** [pizza-kafka-frontend](https://github.com/sri1873/kafka-pizzeria-frontend)

---

## Motivation

This project was built to model how a real delivery platform (think Deliveroo, Uber Eats) coordinates **multiple independent services that don't call each other directly**, but instead react to a shared stream of events. The goal was to get hands-on with the patterns that actually show up at that scale: event-driven architecture, geospatial matching, sequential offer-based assignment, and multiple real-time transport protocols each used for the job they're actually suited to.

Also, as a side note, to learn and try out Kafka.

---

## Architecture

```
                                                        ┌───────────────────┐
                                                        │   Apache Kafka    │
                                                        │  (KRaft, no ZK)   │
                                                        │  topic: order_info│
                                                        └────────▲──────────┘
                                              produces/consumes  │  produces/consumes
                                  ┌───────────────────┬─────────┴─────────┬───────────────────┐
                                  │                   │                   │                   │
                          ┌───────▼────────┐   ┌───────▼────────┐  ┌───────▼────────┐  ┌───────▼───────┐
                          │ Order Service  │   │Restaurant Svc  │  │ Customer Svc   │  │  Rider Svc    │
                          │                │   │                │  │                │  │               │
                          │ places new     │   │ accepts order, │  │ pushes status  │  │ finds nearby  │
                          │ orders         │   │ marks ready    │  │ via SSE        │  │ riders (Redis)│
                          └────────────────┘   └────────────────┘  └────────────────┘  │ sequential    │
                                                                                       │ offer + accept│
                                                                                       │ WebSocket loc │
                                                                                       └───────┬───────┘
                                                                                               │
                                                                                       ┌───────▼────────┐
                                                                                       │     Redis      │
                                                                                       │  GEOADD/RADIUS │
                                                                                       │  live rider    │
                                                                                       │  positions     │
                                                                                       └────────────────┘
```

Every service is a Kafka consumer and, where relevant, a producer on the same `order_info` topic — a service reacts only to the order statuses it cares about, updates the order, and republishes the new state. No service calls another service's REST API directly for order state changes; the topic is the single source of truth for "what happened."

---

## Order lifecycle

```
PLACED → ORDER_ACCEPTED → READY_FOR_PICKUP → RIDER_ASSIGNED → OUT_FOR_DELIVERY → DELIVERED
```

Each transition is triggered by a different actor (customer, restaurant, or rider), published back to Kafka, and consumed by whichever services need to react — the restaurant dashboard, the customer's live tracker, and the rider-matching engine all move independently off the same event stream.

---

## Key engineering decisions

### 1. One topic, status-driven routing
Rather than a topic per event type, all order lifecycle events flow through a single `order_info` topic keyed by `orderId`. Keying by order ID guarantees Kafka delivers all of an order's events to the same partition — so updates for a given order are always processed **in order**, never racing each other across partitions.

### 2. Sequential rider assignment (no race conditions by design)
When an order is ready, nearby riders are found via Redis `GEORADIUS` and offered the job **one at a time**, closest first — not broadcast to everyone at once. Each offer is backed by a `CompletableFuture` with a 10-second timeout:

```java
Boolean accepted = future.get(10, TimeUnit.SECONDS);
```

If the rider accepts, the future completes, and the order is locked to them immediately — no other rider is ever notified, so there's no possibility of a double-accept race condition. If they decline or time out, the loop moves to the next-closest rider. This trades a small amount of latency (worst case: 10s × number of riders tried) for correctness without needing distributed locks.

*Production note:* this in-memory approach is appropriate for a single instance. At multi-instance scale, the same pattern would move to a database-backed offer table with a scheduled expiry sweep, so state survives restarts and works across horizontally scaled instances.

### 3. Redis for live geolocation — not the database
Rider positions update every few seconds. Writing that to a relational table would mean constant churn on rows nobody needs to keep historically. Redis's native `GEOADD`/`GEORADIUS` commands do exactly this job — fast in-memory writes, radius queries in a single command, and TTL-based expiry so a rider who drops offline disappears from the matching pool automatically.

### 4. Three real-time transports, each used for what it's good at
| Transport | Direction | Used for |
|---|---|---|
| **Kafka** | service ↔ service | Order state changes, decoupled and durable |
| **SSE** | server → browser | Pushing order status + rider-assignment offers to the UI |
| **WebSocket** | browser → server | Streaming rider location updates every few seconds |

This wasn't arbitrary — SSE is one-directional and lighter-weight than WebSocket for server-push notifications; WebSocket is used specifically where the browser needs to continuously *send* data (location), which SSE cannot do.

---

## Tech stack

- **Java 17** / **Spring Boot 3.3.2**
- **Apache Kafka** (KRaft mode — no Zookeeper), Dockerized
- **Redis** — geospatial commands for rider tracking
- **H2** (file-based) — order/user/rider persistence
- **Spring Data JPA** / Hibernate
- **Spring WebSocket** — rider location ingestion
- **Server-Sent Events** (`SseEmitter`) — client push notifications, with heartbeat keep-alive to prevent idle timeout
- **Lombok**

---

## Getting started

### Prerequisites
- Java 17
- Docker + Docker Compose
- Maven

### 1. Start Kafka + Redis
```bash
docker compose up -d
```

This brings up:
- Kafka broker (KRaft mode) on `localhost:9092`
- Redis on `localhost:6379`

### 2. Run the application
```bash
mvn clean install
mvn spring-boot:run
```

The app starts on `localhost:8080` and connects to Kafka/Redis automatically. H2 file-based storage persists to `./data` between restarts.

### 3. Verify
```bash
# Kafka topic exists, and broker is reachable
docker exec -it broker kafka-topics.sh --bootstrap-server localhost:9092 --list

# Redis is reachable
docker exec -it redis redis-cli ping   # → PONG
```

---

## Project structure

```
src/main/java/com/kafka/learn/
├── entities/          # OrderDetails, User, Rider, Items — JPA entities
├── dto/                # Notification, OrderStatus, RiderLocationDto
├── repositories/       # Spring Data JPA repositories
├── service/
│   ├── RiderService.java            # sequential assignment, accept/reject/pickup/deliver
│   ├── RiderLocationService.java    # Redis GEOADD/GEORADIUS operations
│   ├── NotificationService.java     # SSE emitter management, per-user notification cache
│   └── ...,
├── config/
│   ├── WebSocketConfig.java         # rider location ingestion endpoint
│   └── KafkaConfig.java
└── controller/         # REST endpoints per role (customer/restaurant/rider)
```

---

## Drawbacks to Build on

- Currently only the latest state is retained -> Persist full order status history to support order timelines surviving a page refresh
- Move rider-offer state from in-memory `CompletableFuture` to a DB-backed table with scheduled expiry, for multi-instance correctness
- Fixed demo coordinates used -> Geocode arbitrary delivery addresses currently.
- Swap H2 for Postgres and containerise the full stack for deployment

---

## Related

Frontend built in React + Vite + Tailwind, consuming this backend via REST, SSE, and WebSocket — see [pizza-kafka-frontend](https://github.com/sri1873/kafka-pizzeria-frontend).



## Acknowledgement and Side note

Most of the README and the ideation process included the use of AI. I forced myself to use original documentation for Redis, Kafka and Mapbox, as I wanted to learn these technologies but used AI to speed up the dev process, mostly CSS and JSX.
