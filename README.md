# Üüriturg — Estonian Rental Market Monitoring Platform

Aggregates rental listings from KV.ee, City24, and Rendin; tracks price trends by Tartu neighborhood; and notifies tenants when matching listings appear.

## Team

| Member     | Services                          |
|------------|-----------------------------------|
| Hashim     | Scraper Service, Analytics Service |
| Sudais     | User Service, Alert Service        |
| Calvin     | Neighborhood Service, Landlord Service |
| Daboikiabo | Listing Service, Notification Service |

## Tech Stack

| Layer        | Technology                         |
|--------------|------------------------------------|
| Language     | Java 21                            |
| Framework    | Spring Boot 3.4.5                  |
| Persistence  | Spring Data JPA + PostgreSQL 16    |
| Messaging    | RabbitMQ 3 (Spring AMQP)          |
| HTTP clients | Spring WebFlux WebClient           |
| Docs         | SpringDoc OpenAPI 2.8.0 (Swagger)  |
| Email (dev)  | MailHog                            |
| Frontend     | Vue 3 + Vite + Vue Router          |
| Containers   | Docker + Docker Compose            |

## Port Reference

| Service               | Port  |
|-----------------------|-------|
| scraper-service       | 8081  |
| analytics-service     | 8082  |
| user-service          | 8083  |
| alert-service         | 8084  |
| neighborhood-service  | 8085  |
| landlord-service      | 8086  |
| listing-service       | 8087  |
| notification-service  | 8088  |
| frontend (Vite)       | 5173  |
| PostgreSQL (host)     | 5433  |
| RabbitMQ AMQP         | 5672  |
| RabbitMQ Management   | 15672 |
| MailHog SMTP          | 1025  |
| MailHog Web UI        | 8025  |

## How to Run

### 1. Start infrastructure
```bash
docker-compose up -d
```

### 2. Run each microservice (separate terminals)
```bash
cd scraper-service && mvn spring-boot:run
cd analytics-service && mvn spring-boot:run
cd user-service && mvn spring-boot:run
cd alert-service && mvn spring-boot:run
cd neighborhood-service && mvn spring-boot:run
cd landlord-service && mvn spring-boot:run
cd listing-service && mvn spring-boot:run
cd notification-service && mvn spring-boot:run
```

### 3. Run the frontend
```bash
cd frontend && npm install && npm run dev
```

## Swagger UI

| Service              | URL                                  |
|----------------------|--------------------------------------|
| scraper-service      | http://localhost:8081/swagger-ui.html |
| analytics-service    | http://localhost:8082/swagger-ui.html |
| user-service         | http://localhost:8083/swagger-ui.html |
| alert-service        | http://localhost:8084/swagger-ui.html |
| neighborhood-service | http://localhost:8085/swagger-ui.html |
| landlord-service     | http://localhost:8086/swagger-ui.html |
| listing-service      | http://localhost:8087/swagger-ui.html |
| notification-service | http://localhost:8088/swagger-ui.html |

## Useful URLs

| Tool               | URL                          |
|--------------------|------------------------------|
| RabbitMQ Management| http://localhost:15672 (guest/guest) |
| MailHog Web UI     | http://localhost:8025        |
| Frontend           | http://localhost:5173        |

## Project Structure

```
uuriturg/
├── docker-compose.yml
├── init-db/init.sql
├── scraper-service/
├── analytics-service/
├── user-service/
├── alert-service/
├── neighborhood-service/
├── landlord-service/
├── listing-service/
├── notification-service/
└── frontend/
```

## Service API Summary

### Scraper Service (8081) — /api/scraper
| Endpoint | Method | Description |
|---|---|---|
| /listings | GET | All active listings (filters: neighborhood, maxPrice, minSize) |
| /listings/latest | GET | Last 50 scraped listings |
| /listings/{listingId} | GET | Single listing by ID |
| /scraper/trigger | POST | Manually trigger a scrape run |
| /scraper/status | GET | Last scrape time, total listings, job status |
| /scraper/jobs | GET | Recent scrape job history |

### Analytics Service (8082) — /api/analytics
| Endpoint | Method | Description |
|---|---|---|
| /analytics/neighborhoods | GET | Avg price & price/m² per neighborhood |
| /analytics/trends | GET | Price trend per day (params: neighborhood, days) |
| /analytics/summary | GET | City-wide stats |
| /analytics/cheapest | GET | Top 10 cheapest (filters: neighborhood, maxPrice) |
| /analytics/compute | POST | Trigger analytics recomputation |

### User Service (8083) — /api/users
| Endpoint | Method | Description |
|---|---|---|
| /users | POST | Register new user |
| /users/{userId} | GET | User profile |
| /users/{userId} | PUT | Update profile |
| /users/{userId} | DELETE | Deactivate account |
| /users/validate/{userId} | GET | Validate user (internal) |
| /users/{userId}/searches | GET | Saved searches |
| /users/{userId}/searches | POST | Create saved search |

### Alert Service (8084) — /api/alerts
| Endpoint | Method | Description |
|---|---|---|
| /alerts | POST | Create alert rule |
| /alerts | GET | List all alerts |
| /alerts/{alertId} | GET | Get alert |
| /alerts/{alertId} | DELETE | Delete alert |
| /alerts/{alertId}/matches | GET | Alert matches |
| /alerts/test/{alertId} | POST | Test fire alert |

### Neighborhood Service (8085) — /api/neighborhoods
| Endpoint | Method | Description |
|---|---|---|
| /neighborhoods | GET | All neighborhoods with scores |
| /neighborhoods/{id} | GET | Neighborhood detail |
| /neighborhoods/{id}/reviews | GET | Reviews |
| /neighborhoods/{id}/reviews | POST | Submit review |
| /neighborhoods/{id}/reviews/{reviewId} | DELETE | Delete review |
| /neighborhoods/compare | GET | Compare neighborhoods |

### Landlord Service (8086) — /api/landlords
| Endpoint | Method | Description |
|---|---|---|
| /landlords | POST | Register landlord |
| /landlords/{landlordId} | GET | Landlord profile |
| /landlords/{landlordId}/reviews | GET | Tenant reviews |
| /landlords/{landlordId}/reviews | POST | Submit review |
| /landlords/{landlordId}/reviews/{reviewId}/respond | PUT | Respond to review |
| /landlords/{landlordId}/reputation | GET | Reputation score |

### Listing Service (8087) — /api/managed-listings
| Endpoint | Method | Description |
|---|---|---|
| /managed-listings | POST | Create direct listing |
| /managed-listings | GET | List managed listings |
| /managed-listings/{id} | GET | Get listing |
| /managed-listings/{id} | PUT | Update listing |
| /managed-listings/claims | POST | Submit claim request |
| /managed-listings/claims/{claimId} | GET | Claim status |
| /managed-listings/claims/{claimId}/approve | PUT | Approve/reject claim |

### Notification Service (8088) — /api/notifications
| Endpoint | Method | Description |
|---|---|---|
| /notifications | POST | Request notification delivery |
| /notifications/{id} | GET | Notification record |
| /notifications/user/{userId} | GET | User notifications |
| /notifications/{id}/status | GET | Delivery status |
| /notifications/templates | GET | List templates |
| /notifications/templates | POST | Create template |
| /notifications/preferences/{userId} | GET | User preferences |
| /notifications/preferences/{userId} | PUT | Update preferences |

## RabbitMQ Events

| Event | Exchange | Producer | Consumer |
|---|---|---|---|
| listing.new | listing.events | scraper-service | alert-service |
| listing.claimed | listing.events | listing-service | notification-service |
| review.posted | landlord.events | landlord-service | notification-service |

## Database per Service

| Service | Database |
|---|---|
| scraper-service | scraper_db |
| analytics-service | analytics_db |
| user-service | user_db |
| alert-service | alert_db |
| neighborhood-service | neighborhood_db |
| landlord-service | landlord_db |
| listing-service | listing_mgmt_db |
| notification-service | notification_db |
