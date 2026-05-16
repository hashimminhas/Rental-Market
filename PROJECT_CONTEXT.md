# PROJECT_CONTEXT.md — Üüriturg

> **This file is the single source of truth for any Claude Code session working on this project.**
> Read this file before writing any code. All decisions documented here are final.

---

## 1. Project Identity

- **Name:** Üüriturg (Estonian: "Rental Market")
- **Purpose:** Estonian rental market monitoring platform. Scrapes KV.ee, City24, and Rendin; normalises listings into a unified schema; tracks price trends per Tartu neighbourhood; sends personalised alerts when matching listings appear; provides landlord reputation data and neighbourhood comparisons.
- **Course:** Enterprise System Integration (MTAT.03.229), University of Tartu, Spring 2026
- **Team:**
  - Hashim — scraper-service, analytics-service
  - Sudais — user-service, alert-service
  - Calvin — neighborhood-service, landlord-service
  - Daboikiabo — listing-service, notification-service

---

## 2. Tech Stack Decisions (non-negotiable)

| Decision | Rule |
|---|---|
| Java version | 21 |
| Spring Boot | 3.4.5 |
| Repository base | `CrudRepository<Entity, UUID>` — never `JpaRepository` |
| Test mocking | `@MockitoBean` — never `@MockBean` (removed in Spring Boot 3.4+) |
| DTO mapping | Manual with `@Builder` — no MapStruct |
| CORS | `@CrossOrigin("*")` on every `@RestController` |
| PostgreSQL host port | 5433 (container: 5432) |
| Inter-service HTTP | Spring WebFlux `WebClient` — not RestTemplate, not Feign |
| Frontend HTTP | Native `fetch()` — no Axios |
| HTML scraping | JSoup 1.17.2 — only in scraper-service |
| Email (dev) | MailHog (localhost:1025) — no real SMTP credentials |
| Test datasource | H2 in-memory with `MODE=PostgreSQL` |
| Test RabbitMQ | Excluded via `spring.autoconfigure.exclude` in test application.yml |
| UUID + timestamps | Set in `@PrePersist`; `updatedAt` in `@PreUpdate` |
| Enum defaults | Set in `@PrePersist` if null |
| OpenAPI | SpringDoc 2.8.0 — Swagger UI at `/swagger-ui.html`, API docs at `/api-docs` |

---

## 3. Services — Full Detail

### 3.1 scraper-service

- **Port:** 8081
- **Database:** scraper_db
- **Owner:** Hashim
- **Package root:** `com.uuriturg.scraper`
- **Folders:** `controller/`, `service/`, `repository/`, `domain/`, `dto/`, `exception/`, `config/`, `scraper/`, `messaging/`

**Domain entities:**

```
Listing (Aggregate Root)
  - listingId: UUID (PK, generated)
  - source: Enum {KV_EE, CITY24, RENDIN}
  - externalId: String (not null)
  - title: String
  - price: BigDecimal (EUR)
  - size: BigDecimal (m²)
  - pricePerSqm: BigDecimal (derived: price/size, set on @PrePersist/@PreUpdate)
  - rooms: Integer
  - neighborhood: String
  - street: String
  - city: String
  - postalCode: String
  - url: String
  - scrapedAt: LocalDateTime
  - isActive: Boolean (default true)
  - createdAt: LocalDateTime (@PrePersist)

ScrapeJob (Entity)
  - jobId: UUID (PK, generated)
  - source: Enum {KV_EE, CITY24, RENDIN}
  - startedAt: LocalDateTime
  - completedAt: LocalDateTime (nullable)
  - status: Enum {RUNNING, COMPLETED, FAILED}
  - listingsFound: Integer (default 0)
  - newListings: Integer (default 0)
```

**Deduplication rule:** Before saving a scraped listing, check `externalId + source`. If exists: update `price`, `isActive=true`, `scrapedAt`. If not: insert new.

**pricePerSqm:** Always computed as `price.divide(size, 2, RoundingMode.HALF_UP)` — never stored from source.

**REST endpoints (base `/api/scraper`):**
```
GET  /listings                  query params: neighborhood, maxPrice, minSize
GET  /listings/latest           returns last 50 by scrapedAt desc
GET  /listings/{listingId}      404 if not found
POST /scraper/trigger           triggers scrape run, returns 202 with jobId
GET  /scraper/status            last scrape time, total active listings, current job status
GET  /scraper/jobs              list recent ScrapeJob records
```

**RabbitMQ role:** PUBLISHER
- Exchange: `listing.events` (topic)
- Routing key: `listing.new`
- Payload: `{ listingId, title, price, neighborhood, size, rooms, url, scrapedAt }`
- Publish after each new listing is saved

**Scraping notes:**
- KV.ee Tartu rentals URL: `https://www.kv.ee/kinnisvara/uusobjektid/?deal_type=2&county=18&parish=1061`
- City24 Tartu URL: `https://city24.ee/en/real-estate-search/apartments-for-rent/tartu`
- Use JSoup with `userAgent("Mozilla/5.0 ...")` to avoid blocks
- Scheduled: `@Scheduled(cron = "${scraper.schedule.cron}")` default every 6 hours

**Extra pom.xml dependencies:**
```xml
<dependency>
    <groupId>org.jsoup</groupId>
    <artifactId>jsoup</artifactId>
    <version>1.17.2</version>
</dependency>
```

---

### 3.2 analytics-service

- **Port:** 8082
- **Database:** analytics_db
- **Owner:** Hashim
- **Package root:** `com.uuriturg.analytics`
- **Folders:** `controller/`, `service/`, `repository/`, `domain/`, `dto/`, `exception/`, `config/`, `client/`

**Domain entities:**

```
NeighborhoodSnapshot (Aggregate Root)
  - snapshotId: UUID (PK, generated)
  - neighborhood: String (not null)
  - date: LocalDate (not null)
  - averagePrice: BigDecimal
  - averagePricePerSqm: BigDecimal
  - medianPrice: BigDecimal
  - listingCount: Integer
  - priceChangePercent: BigDecimal (nullable — null on first snapshot)
  - createdAt: LocalDateTime (@PrePersist)
```

**REST endpoints (base `/api/analytics`):**
```
GET  /analytics/neighborhoods   returns latest snapshot per neighborhood (current prices)
GET  /analytics/trends          params: neighborhood (required), days (default 30)
GET  /analytics/summary         city-wide: totalListings, cheapestPrice, mostExpensivePrice, averagePrice, averageSize
GET  /analytics/cheapest        top 10 cheapest — params: neighborhood (optional), maxPrice (optional)
POST /analytics/compute         triggers recomputation — calls scraper-service for each tracked neighborhood
```

**Outbound HTTP calls:**
- `GET http://localhost:8081/listings?neighborhood={name}` — via WebClient
- Config property: `scraper.service.base-url`

**RabbitMQ role:** NONE

**Extra pom.xml dependencies:**
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-webflux</artifactId>
</dependency>
```

---

### 3.3 user-service

- **Port:** 8083
- **Database:** user_db
- **Owner:** Sudais
- **Package root:** `com.uuriturg.user`
- **Folders:** `controller/`, `service/`, `repository/`, `domain/`, `dto/`, `exception/`, `config/`

**Domain entities:**

```
User (Aggregate Root)
  - userId: UUID (PK, generated)
  - firstName: String (not null)
  - lastName: String (not null)
  - email: String (not null, unique)
  - phone: String (nullable)
  - role: Enum {TENANT, LANDLORD, ADMIN} (default TENANT)
  - createdAt: LocalDateTime (@PrePersist)
  - updatedAt: LocalDateTime (@PreUpdate)

SavedSearch (Entity)
  - searchId: UUID (PK, generated)
  - userId: UUID (FK → User.userId)
  - neighborhood: String (nullable)
  - maxPrice: BigDecimal (nullable)
  - minSize: BigDecimal (nullable)
  - minRooms: Integer (nullable)
  - createdAt: LocalDateTime (@PrePersist)
```

**REST endpoints (base `/api/users`):**
```
POST   /users                      body: { firstName, lastName, email, phone, role }
GET    /users/{userId}             404 if not found
PUT    /users/{userId}             body: updatable fields
DELETE /users/{userId}             soft-delete: set role to ADMIN? or mark inactive
GET    /users/validate/{userId}    returns { userId, email, role, active:true } or 404
GET    /users/{userId}/searches    list saved searches
POST   /users/{userId}/searches    body: { neighborhood, maxPrice, minSize, minRooms }
```

**Outbound HTTP calls:** NONE — user-service has no upstream dependencies

**RabbitMQ role:** NONE

---

### 3.4 alert-service

- **Port:** 8084
- **Database:** alert_db
- **Owner:** Sudais
- **Package root:** `com.uuriturg.alert`
- **Folders:** `controller/`, `service/`, `repository/`, `domain/`, `dto/`, `exception/`, `config/`, `messaging/`, `client/`

**Domain entities:**

```
AlertRule (Aggregate Root)
  - alertId: UUID (PK, generated)
  - userId: UUID (cross-service ref to User — no FK)
  - neighborhood: String (nullable — null means any)
  - maxPrice: BigDecimal (not null)
  - minSize: BigDecimal (nullable)
  - minRooms: Integer (nullable)
  - isActive: Boolean (default true)
  - createdAt: LocalDateTime (@PrePersist)

AlertMatch (Entity)
  - matchId: UUID (PK, generated)
  - alertId: UUID (FK → AlertRule.alertId)
  - listingId: UUID (cross-service ref to Listing — no FK)
  - matchedAt: LocalDateTime (@PrePersist)
  - notified: Boolean (default false)
```

**REST endpoints (base `/api/alerts`):**
```
POST /alerts                      body: { userId, neighborhood, maxPrice, minSize, minRooms }
GET  /alerts                      list all alert rules
GET  /alerts/{alertId}            get specific alert
DELETE /alerts/{alertId}          deactivate (isActive=false)
GET  /alerts/{alertId}/matches    list AlertMatch records for an alert
POST /alerts/test/{alertId}       manually test-fire the alert (for demo)
```

**Outbound HTTP calls:**
- `GET http://localhost:8083/users/validate/{userId}` — verify user before creating alert
- `POST http://localhost:8088/notifications` — trigger notification on match

**RabbitMQ role:** CONSUMER
- Queue: `alert.listing.queue`
- Exchange: `listing.events`
- Routing key: `listing.new`
- On receive: evaluate all active AlertRules against incoming listing payload; create AlertMatch; call user-service to validate; call notification-service

**Extra pom.xml dependencies:**
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-webflux</artifactId>
</dependency>
```

---

### 3.5 neighborhood-service

- **Port:** 8085
- **Database:** neighborhood_db
- **Owner:** Calvin
- **Package root:** `com.uuriturg.neighborhood`
- **Folders:** `controller/`, `service/`, `repository/`, `domain/`, `dto/`, `exception/`, `config/`, `client/`

**Domain entities:**

```
Neighborhood (Aggregate Root)
  - neighborhoodId: UUID (PK, generated)
  - name: String (not null, unique)
  - city: String (not null, default "Tartu")
  - description: String (nullable)
  - latitude: Double
  - longitude: Double
  - amenityScore: BigDecimal (nullable)
  - transportScore: BigDecimal (nullable)
  - safetyRating: BigDecimal (nullable)
  - createdAt: LocalDateTime (@PrePersist)

NeighborhoodReview (Entity)
  - reviewId: UUID (PK, generated)
  - neighborhoodId: UUID (FK → Neighborhood.neighborhoodId)
  - userId: UUID (cross-service ref — no FK)
  - rating: Integer (1-5)
  - text: String
  - createdAt: LocalDateTime (@PrePersist)
```

**REST endpoints (base `/api/neighborhoods`):**
```
GET    /neighborhoods                           list all with scores
GET    /neighborhoods/{id}                      detail with reviews + analytics price data
GET    /neighborhoods/{id}/reviews              list reviews
POST   /neighborhoods/{id}/reviews              body: { userId, rating, text } — verifies user
DELETE /neighborhoods/{id}/reviews/{reviewId}   admin or author only
GET    /neighborhoods/compare                   param: ids=id1,id2,id3
```

**Outbound HTTP calls:**
- `GET http://localhost:8083/users/validate/{userId}` — verify reviewer
- `GET http://localhost:8082/analytics/neighborhoods` — fetch price data for profile

**RabbitMQ role:** NONE

**Extra pom.xml dependencies:**
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-webflux</artifactId>
</dependency>
```

---

### 3.6 landlord-service

- **Port:** 8086
- **Database:** landlord_db
- **Owner:** Calvin
- **Package root:** `com.uuriturg.landlord`
- **Folders:** `controller/`, `service/`, `repository/`, `domain/`, `dto/`, `exception/`, `config/`, `messaging/`, `client/`

**Domain entities:**

```
LandlordProfile (Aggregate Root)
  - landlordId: UUID (PK, generated)
  - userId: UUID (cross-service ref to User — no FK)
  - displayName: String
  - verificationStatus: Enum {PENDING, VERIFIED, REJECTED} (default PENDING)
  - averageRating: BigDecimal (derived, recalculated on each review)
  - totalReviews: Integer (derived, default 0)
  - createdAt: LocalDateTime (@PrePersist)

TenantReview (Entity)
  - reviewId: UUID (PK, generated)
  - landlordId: UUID (FK → LandlordProfile.landlordId)
  - reviewerUserId: UUID (cross-service ref — no FK)
  - rating: Integer (1-5)
  - text: String
  - landlordResponse: String (nullable)
  - createdAt: LocalDateTime (@PrePersist)
```

**REST endpoints (base `/api/landlords`):**
```
POST /landlords                                      body: { userId, displayName }
GET  /landlords/{landlordId}                         profile with reputation
GET  /landlords/{landlordId}/reviews                 list tenant reviews
POST /landlords/{landlordId}/reviews                 body: { reviewerUserId, rating, text }
PUT  /landlords/{landlordId}/reviews/{reviewId}/respond   body: { response }
GET  /landlords/{landlordId}/reputation              computed score
```

**Outbound HTTP calls:**
- `GET http://localhost:8083/users/validate/{userId}` — verify landlord and reviewer

**RabbitMQ role:** PUBLISHER
- Exchange: `landlord.events` (topic)
- Routing key: `review.posted`
- Payload: `{ reviewId, landlordId, reviewerUserId, rating }`
- Publish after a new TenantReview is saved

**Extra pom.xml dependencies:**
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-webflux</artifactId>
</dependency>
```

---

### 3.7 listing-service

- **Port:** 8087
- **Database:** listing_mgmt_db
- **Owner:** Daboikiabo
- **Package root:** `com.uuriturg.listing`
- **Folders:** `controller/`, `service/`, `repository/`, `domain/`, `dto/`, `exception/`, `config/`, `messaging/`, `client/`

**Domain entities:**

```
ManagedListing (Aggregate Root)
  - managedListingId: UUID (PK, generated)
  - landlordId: UUID (cross-service ref to LandlordProfile — no FK)
  - scrapedListingId: UUID (nullable, cross-service ref to Listing — no FK)
  - title: String
  - description: String (nullable)
  - price: BigDecimal
  - size: BigDecimal
  - rooms: Integer
  - address: String
  - status: Enum {AVAILABLE, RENTED, WITHDRAWN} (default AVAILABLE)
  - isVerified: Boolean (default false)
  - createdAt: LocalDateTime (@PrePersist)
  - updatedAt: LocalDateTime (@PreUpdate)

ClaimRequest (Entity)
  - claimId: UUID (PK, generated)
  - landlordId: UUID (not null)
  - scrapedListingId: UUID (not null, cross-service ref — no FK)
  - status: Enum {PENDING, APPROVED, REJECTED} (default PENDING)
  - verificationDetails: String
  - submittedAt: LocalDateTime (@PrePersist)
  - resolvedAt: LocalDateTime (nullable)
```

**REST endpoints (base `/api/managed-listings`):**
```
POST /managed-listings                          body: { landlordId, title, description, price, size, rooms, address }
GET  /managed-listings                          params: status, landlordId
GET  /managed-listings/{id}                     get specific
PUT  /managed-listings/{id}                     update listing or status
POST /managed-listings/claims                   body: { landlordId, scrapedListingId, verificationDetails }
GET  /managed-listings/claims/{claimId}         claim status
PUT  /managed-listings/claims/{claimId}/approve body: { approved: true/false }
```

**Outbound HTTP calls:**
- `GET http://localhost:8083/users/validate/{userId}` — verify landlord role
- `GET http://localhost:8081/listings/{id}` — retrieve scraped listing for claim
- `GET http://localhost:8086/landlords/{id}` — retrieve landlord profile

**RabbitMQ role:** PUBLISHER
- Exchange: `listing.events` (topic)
- Routing key: `listing.claimed`
- Payload: `{ claimId, landlordId, scrapedListingId, status, submittedAt }`
- Publish after claim is submitted

**Extra pom.xml dependencies:**
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-webflux</artifactId>
</dependency>
```

---

### 3.8 notification-service

- **Port:** 8088
- **Database:** notification_db
- **Owner:** Daboikiabo
- **Package root:** `com.uuriturg.notification`
- **Folders:** `controller/`, `service/`, `repository/`, `domain/`, `dto/`, `exception/`, `config/`, `messaging/`, `client/`

**Domain entities:**

```
Notification (Aggregate Root)
  - notificationId: UUID (PK, generated)
  - recipientUserId: UUID (cross-service ref — no FK)
  - channel: Enum {EMAIL, IN_APP} (default EMAIL)
  - subject: String
  - body: String
  - status: Enum {PENDING, SENT, FAILED} (default PENDING)
  - sentAt: LocalDateTime (nullable — set when delivered)
  - createdAt: LocalDateTime (@PrePersist)

NotificationTemplate (Entity)
  - templateId: UUID (PK, generated)
  - name: String (unique)
  - subjectTemplate: String
  - bodyTemplate: String

NotificationPreference (Entity)
  - preferenceId: UUID (PK, generated)
  - userId: UUID (cross-service ref — no FK)
  - channel: Enum {EMAIL, IN_APP}
  - enabled: Boolean (default true)
```

**REST endpoints (base `/api/notifications`):**
```
POST /notifications                          body: { recipientUserId, channel, subject, body, templateName? }
GET  /notifications/{id}                     get notification record
GET  /notifications/user/{userId}            all notifications for user
GET  /notifications/{id}/status              delivery status
GET  /notifications/templates                list templates
POST /notifications/templates                body: { name, subjectTemplate, bodyTemplate }
GET  /notifications/preferences/{userId}     user preferences
PUT  /notifications/preferences/{userId}     body: { channel, enabled }
```

**Outbound HTTP calls:**
- `GET http://localhost:8083/users/{userId}` — retrieve contact info for delivery
- Uses `JavaMailSender` → MailHog (SMTP localhost:1025)

**RabbitMQ role:** CONSUMER (two queues)
- Queue 1: `notification.listing.queue` → exchange `listing.events`, routing key `listing.claimed`
- Queue 2: `notification.landlord.queue` → exchange `landlord.events`, routing key `review.posted`

**Extra pom.xml dependencies:**
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-webflux</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-mail</artifactId>
</dependency>
```

---

## 4. Domain Model Summary

| Service (Owner) | Aggregate Root | Entities | Value Objects | Repository |
|---|---|---|---|---|
| Scraper (Hashim) | Listing | ScrapeJob | Money, Address | IListingRepository |
| Analytics (Hashim) | NeighborhoodSnapshot | — | DateRange | INeighborhoodSnapshotRepository |
| User (Sudais) | User | SavedSearch | ContactInfo | IUserRepository |
| Alert (Sudais) | AlertRule | AlertMatch | — | IAlertRuleRepository |
| Neighborhood (Calvin) | Neighborhood | NeighborhoodReview | GeoCoordinate | INeighborhoodRepository |
| Landlord (Calvin) | LandlordProfile | TenantReview | — | ILandlordProfileRepository |
| Listing (Daboikiabo) | ManagedListing | ClaimRequest | Money, Address | IManagedListingRepository |
| Notification (Daboikiabo) | Notification | NotificationTemplate, NotificationPreference | — | INotificationRepository |

---

## 5. Cross-Service Relationships

All references use UUID only — no shared databases, no cross-database foreign keys.

| From | To | Relationship | Reference |
|---|---|---|---|
| AlertRule [alert] | User [user] | Alert belongs to User | userId (UUID) |
| AlertMatch [alert] | Listing [scraper] | Match references Listing | listingId (UUID) |
| NeighborhoodReview [neighborhood] | User [user] | Review written by User | userId (UUID) |
| Neighborhood [neighborhood] | NeighborhoodSnapshot [analytics] | Profile displays price data | neighborhood name (String) |
| LandlordProfile [landlord] | User [user] | Landlord linked to User | userId (UUID) |
| TenantReview [landlord] | User [user] | Review written by User | reviewerUserId (UUID) |
| ManagedListing [listing] | LandlordProfile [landlord] | Listing owned by Landlord | landlordId (UUID) |
| ManagedListing [listing] | Listing [scraper] | Claimed scraped listing | scrapedListingId (UUID) |
| Notification [notification] | User [user] | Notification sent to User | recipientUserId (UUID) |
| NotificationPreference [notification] | User [user] | Preference owned by User | userId (UUID) |

---

## 6. RabbitMQ Event Catalogue

| Event | Exchange | Routing Key | Producer | Consumer | Payload fields |
|---|---|---|---|---|---|
| listing.new | listing.events | listing.new | scraper-service | alert-service | listingId, title, price, neighborhood, size, rooms, url, scrapedAt |
| listing.claimed | listing.events | listing.claimed | listing-service | notification-service | claimId, landlordId, scrapedListingId, status, submittedAt |
| review.posted | landlord.events | review.posted | landlord-service | notification-service | reviewId, landlordId, reviewerUserId, rating |

**Queue bindings:**
- `alert.listing.queue` → `listing.events` exchange, routing key `listing.new` (declared in alert-service RabbitMQ config)
- `notification.listing.queue` → `listing.events` exchange, routing key `listing.claimed` (declared in notification-service)
- `notification.landlord.queue` → `landlord.events` exchange, routing key `review.posted` (declared in notification-service)

**Exchange type:** `TopicExchange` for all exchanges.

**RabbitMQ Config pattern (each service that publishes/consumes):**
```java
@Configuration
public class RabbitMQConfig {
    @Bean
    public TopicExchange listingExchange() { return new TopicExchange("listing.events"); }
    @Bean
    public Queue alertListingQueue() { return new Queue("alert.listing.queue", true); }
    @Bean
    public Binding alertListingBinding(Queue alertListingQueue, TopicExchange listingExchange) {
        return BindingBuilder.bind(alertListingQueue).to(listingExchange).with("listing.new");
    }
    @Bean
    public MessageConverter jsonMessageConverter() { return new Jackson2JsonMessageConverter(); }
    @Bean
    public AmqpTemplate amqpTemplate(ConnectionFactory cf) {
        RabbitTemplate t = new RabbitTemplate(cf);
        t.setMessageConverter(jsonMessageConverter());
        return t;
    }
}
```

---

## 7. System Workflows

### Workflow 1 — New Listing Alert Notification (Sync + Async)

| Step | From | To | Action |
|---|---|---|---|
| 1 | Scheduler | scraper-service | Cron triggers scrape; new listings stored in scraper_db |
| 2 | scraper-service | RabbitMQ | Publishes `listing.new` to `listing.events` |
| 3 | alert-service | RabbitMQ | Consumes `listing.new` [ASYNC] |
| 4 | alert-service | alert_db | Evaluates active AlertRules; creates AlertMatch record |
| 5 | alert-service | user-service | GET /users/validate/{userId} [SYNC] |
| 6 | user-service | alert-service | Returns 200 { userId, email, role } |
| 7 | alert-service | notification-service | POST /notifications [SYNC] |
| 8 | notification-service | user-service | GET /users/{userId} for contact info [SYNC] |
| 9 | notification-service | notification_db | Creates Notification record; sends email via MailHog |

### Workflow 2 — Landlord Listing Claim & Verification (Sync + Async)

| Step | From | To | Action |
|---|---|---|---|
| 1 | Landlord | listing-service | POST /managed-listings/claims |
| 2 | listing-service | user-service | GET /users/validate/{userId} [SYNC] |
| 3 | user-service | listing-service | Returns 200 { userId, role: LANDLORD } |
| 4 | listing-service | scraper-service | GET /listings/{scrapedListingId} [SYNC] |
| 5 | scraper-service | listing-service | Returns 200 { listing details } |
| 6 | listing-service | listing_mgmt_db | Creates ClaimRequest (PENDING) + ManagedListing |
| 7 | listing-service | RabbitMQ | Publishes `listing.claimed` to `listing.events` |
| 8 | notification-service | RabbitMQ | Consumes `listing.claimed` [ASYNC] |
| 9 | notification-service | user-service | GET /users/{landlordUserId} [SYNC] |
| 10 | notification-service | notification_db | Creates Notification; sends claim confirmation |

### Workflow 3 — Neighborhood Review Submission (Synchronous)

| Step | From | To | Action |
|---|---|---|---|
| 1 | User | neighborhood-service | POST /neighborhoods/{id}/reviews |
| 2 | neighborhood-service | user-service | GET /users/validate/{userId} [SYNC] |
| 3 | user-service | neighborhood-service | Returns 200 { userId, role } |
| 4 | neighborhood-service | neighborhood_db | Creates NeighborhoodReview; recalculates scores |
| 5 | neighborhood-service | analytics-service | GET /analytics/neighborhoods [SYNC] |
| 6 | analytics-service | neighborhood-service | Returns price snapshot |
| 7 | neighborhood-service | User | Returns 201 { reviewId, updated profile } |

### Workflow 4 — Analytics Recomputation (Synchronous)

| Step | From | To | Action |
|---|---|---|---|
| 1 | Scheduler/Admin | analytics-service | POST /analytics/compute |
| 2 | analytics-service | scraper-service | GET /listings?neighborhood=X for each tracked neighborhood [SYNC] |
| 3 | scraper-service | analytics-service | Returns listing data |
| 4 | analytics-service | analytics_db | Creates/updates NeighborhoodSnapshot records |
| 5 | analytics-service | Scheduler | Returns 200 { snapshotsCreated } |

---

## 8. Coding Conventions

### Controller
```java
@RestController
@RequestMapping("/listings")
@CrossOrigin("*")
@Tag(name = "Listings", description = "Listing management endpoints")
public class ListingController {

    @Operation(summary = "Get all active listings")
    @GetMapping
    public ResponseEntity<List<ListingResponse>> getAllListings(...) { ... }
}
```

### Service — interface + impl pattern
```java
public interface ListingService {
    List<ListingResponse> findAll(String neighborhood, BigDecimal maxPrice, BigDecimal minSize);
}

@Service
@RequiredArgsConstructor
public class ListingServiceImpl implements ListingService { ... }
```

### Repository
```java
public interface ListingRepository extends CrudRepository<Listing, UUID> {
    List<Listing> findByIsActiveTrue();
    Optional<Listing> findBySourceAndExternalId(Source source, String externalId);
}
```

### Entity
```java
@Entity
@Table(name = "listings")
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class Listing {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID listingId;

    @Enumerated(EnumType.STRING)
    private Source source;

    private boolean isActive;
    private LocalDateTime createdAt;

    @PrePersist
    void prePersist() {
        if (createdAt == null) createdAt = LocalDateTime.now();
        if (!isActive) isActive = true;
        if (pricePerSqm == null && price != null && size != null && size.compareTo(BigDecimal.ZERO) != 0)
            pricePerSqm = price.divide(size, 2, RoundingMode.HALF_UP);
    }
}
```

### DTO
```java
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class CreateListingRequest {
    @NotBlank private String title;
    @NotNull private BigDecimal price;
}
```

### Exception Handler
```java
@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(NoSuchElementException ex) {
        return ResponseEntity.status(404).body(Map.of(
            "status", 404,
            "error", "Not Found",
            "message", ex.getMessage()
        ));
    }
}
```

### Tests
```java
@WebMvcTest(ListingController.class)
class ListingControllerTest {
    @Autowired MockMvc mockMvc;
    @MockitoBean ListingService listingService;   // NOT @MockBean

    @Test
    void shouldReturnListings() throws Exception {
        mockMvc.perform(get("/listings"))
               .andExpect(status().isOk());
    }
}
```

### OpenAPI Config (every service)
```java
@Configuration
public class OpenApiConfig {
    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("Scraper Service API")
                .version("1.0")
                .description("Scrapes Estonian rental portals"));
    }
}
```

### WebClient Config (services that call others)
```java
@Configuration
public class WebClientConfig {
    @Bean
    public WebClient scraperWebClient(@Value("${scraper.service.base-url}") String baseUrl) {
        return WebClient.builder().baseUrl(baseUrl).build();
    }
}
```

---

## 9. Tartu Neighborhoods

The 10 tracked neighborhoods:
`Kesklinn`, `Ülejõe`, `Tammelinn`, `Annelinn`, `Karlova`, `Veeriku`, `Tähtvere`, `Supilinn`, `Ränilinn`, `Maarjamõisa`

---

## 10. Frontend Pages

| Route | View file | Data sources |
|---|---|---|
| `/` | DashboardView.vue | GET /api/analytics/summary, GET /api/analytics/neighborhoods |
| `/listings` | ListingsView.vue | GET /api/scraper/listings?neighborhood=&maxPrice=&minSize= |
| `/trends` | TrendsView.vue | GET /api/analytics/trends?neighborhood=X&days=30 (Chart.js line chart) |
| `/alerts` | AlertsView.vue | POST /api/alerts, GET /api/alerts |

**Frontend conventions:**
- Native `fetch()` only — no Axios
- Vite proxy rewrites `/api/scraper` → `http://localhost:8081` (no `/api/scraper` prefix reaches the service)
- All views in `src/views/`, router lazy-loads them
- `App.vue` has sticky nav bar with links to all 4 pages

---

## 11. Demo Flow (90 seconds, competition)

1. Open Dashboard (http://localhost:5173) — show real scraped Tartu listings, neighborhood price comparison table
2. Go to Listings — filter by Kesklinn, max price 600 EUR — show matching results with price/m²
3. Go to Trends — select Tammelinn — Chart.js line chart shows 30-day price history
4. Go to Alerts — register alert: email, Annelinn, max 500 EUR
5. Trigger scrape: `POST http://localhost:8081/scraper/trigger`
6. Open MailHog (http://localhost:8025) — show alert email that arrived

---

## 12. Common pom.xml Dependencies (all 8 services)

```xml
<parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>3.4.5</version>
</parent>

<properties>
    <java.version>21</java.version>
</properties>

<dependencies>
    <dependency><groupId>org.springframework.boot</groupId><artifactId>spring-boot-starter-web</artifactId></dependency>
    <dependency><groupId>org.springframework.boot</groupId><artifactId>spring-boot-starter-data-jpa</artifactId></dependency>
    <dependency><groupId>org.springframework.boot</groupId><artifactId>spring-boot-starter-validation</artifactId></dependency>
    <dependency><groupId>org.springframework.boot</groupId><artifactId>spring-boot-starter-amqp</artifactId></dependency>
    <dependency><groupId>org.springdoc</groupId><artifactId>springdoc-openapi-starter-webmvc-ui</artifactId><version>2.8.0</version></dependency>
    <dependency><groupId>org.postgresql</groupId><artifactId>postgresql</artifactId><scope>runtime</scope></dependency>
    <dependency><groupId>com.h2database</groupId><artifactId>h2</artifactId><scope>test</scope></dependency>
    <dependency><groupId>org.projectlombok</groupId><artifactId>lombok</artifactId><optional>true</optional></dependency>
    <dependency><groupId>org.springframework.boot</groupId><artifactId>spring-boot-starter-test</artifactId><scope>test</scope></dependency>
</dependencies>
```

**Additional for analytics, alert, neighborhood, landlord, listing, notification:**
```xml
<dependency><groupId>org.springframework.boot</groupId><artifactId>spring-boot-starter-webflux</artifactId></dependency>
```

**Additional for scraper only:**
```xml
<dependency><groupId>org.jsoup</groupId><artifactId>jsoup</artifactId><version>1.17.2</version></dependency>
```

**Additional for notification only:**
```xml
<dependency><groupId>org.springframework.boot</groupId><artifactId>spring-boot-starter-mail</artifactId></dependency>
```
