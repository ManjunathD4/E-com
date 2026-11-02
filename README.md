# 🛒 E-Commerce Microservices Architecture (Java 21 & Spring Boot 3)

A production-grade, enterprise microservices platform built for high scalability, fault tolerance, and interview-ready architectural patterns.

---

## 🏛️ System Architecture Overview

```
                      +---------------------------------------+
                      |       Web / Mobile Clients            |
                      +---------------------------------------+
                                          |
                                          | HTTPS / REST
                                          v
                      +---------------------------------------+
                      |         API Gateway (Port: 8080)       |
                      |   - Spring Cloud Gateway (Reactive)   |
                      |   - Reverse Proxy & Routing           |
                      |   - Global Rate Limiting & Auth Filter|
                      +---------------------------------------+
                             /            |            \
       +--------------------+             |             +--------------------+
       |                                  |                                  |
       v                                  v                                  v
+------------------+             +------------------+              +------------------+
|   User Service   |             | Product Service  |<------------ |  Order Service   |
|   (Port: 8081)   |             |   (Port: 8082)   |  OpenFeign   |   (Port: 8083)   |
|                  |             |                  | (Sync Check) |                  |
| - Auth & JWT     |             | - Catalog        |              | - Order State    |
| - Role Security  |             | - Inventory      |              | - Checkout Flow  |
+------------------+             +------------------+              +------------------+
       |                                  |                                  |
   [user_db]                         [product_db]                        [order_db]
       |                                  |                                  |
       |                                  +-------------+                    |
       |                                                |                    |
       |                                                v                    v
       |                                      +------------------------------------+
       |                                      |      Apache Kafka Event Bus        |
       |                                      | Topics: order-events, payment-events|
       |                                      +------------------------------------+
       |                                                        ^
       |                                                        |
       |                                                        v
       |                                              +------------------+
       |                                              | Payment Service  |
       |                                              |   (Port: 8084)   |
       |                                              | - Charge & Ledger|
       |                                              | - Mock Gateway   |
       |                                              +------------------+
       |                                                        |
       |                                                   [payment_db]
       |
       +--------------------------------------------------------+
                                                                |
                                             +------------------+------------------+
                                             |                                     |
                                             v                                     v
                                   +-------------------+                 +-------------------+
                                   | Discovery Server  |                 |   Config Server   |
                                   |  (Eureka Server)  |                 |   (Spring Cloud)  |
                                   |   (Port: 8761)    |                 |    (Port: 8888)   |
                                   +-------------------+                 +-------------------+
```

---

## 📋 Service Registry & Port Mapping

| Service Name | Port | Database | Primary Communication | Key Responsibilities |
|---|---|---|---|---|
| **Config Server** | `8888` | None | Synchronous HTTP | Externalized & centralized configuration repository |
| **Discovery Server (Eureka)** | `8761` | In-Memory Registry | Synchronous HTTP / Heartbeats | Service registration & dynamic client-side lookup |
| **API Gateway** | `8080` | None | Reactive HTTP (WebFlux) | Perimeter entry, routing, auth filter, rate limiting |
| **User Service** | `8081` | `user_db` (Postgres) | Synchronous REST | User registration, authentication, JWT tokens |
| **Product Service** | `8082` | `product_db` (Postgres) | REST (OpenFeign) & Kafka | Catalog browsing, inventory tracking |
| **Order Service** | `8083` | `order_db` (Postgres) | OpenFeign (Sync) & Kafka (Async) | Order placement, state transitions, Saga orchestration |
| **Payment Service** | `8084` | `payment_db` (Postgres) | Kafka (Async Events) | Transaction processing, payment status ledger |

---

## ⚙️ Infrastructure Services

- **PostgreSQL 16**: Port `5432` with isolated databases (`user_db`, `product_db`, `order_db`, `payment_db`).
- **Apache Kafka 7.6.0 (KRaft)**: Port `9092` broker for distributed asynchronous event messaging.
- **Kafka UI**: Port `8085` (maps to internal 8080) for real-time topic and consumer inspection.

---

## 🚀 Running Infrastructure with Docker

```bash
docker compose up -d
```
