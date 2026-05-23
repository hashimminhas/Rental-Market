# Üüriturg — Tartu Rental Market Monitor

A personal project I built to track the Tartu (Estonia) rental apartment market in real time.  
It automatically scrapes listings from the three main Estonian rental platforms, aggregates them into a single dashboard, tracks price trends by neighbourhood, and can notify you when a new listing matches your criteria.

---

## What It Does

- **Scrapes** KV.ee, City24, and Rendin every few hours — no manual searching required
- **Deduplicates** listings so the same apartment never shows twice
- **Shows photos** pulled directly from each platform's image CDN
- **Tracks price trends** over time per Tartu neighbourhood (Kesklinn, Karlova, Annelinn, Supilinn, etc.)
- **Alerts** — create a filter (price range, size, neighbourhood) and get notified when a match appears
- **Landlord profiles** — see reviews and reputation scores for landlords
- **Neighbourhood comparison** — compare average rents, price per m², and community scores

---

## Current Data (live)

| Source      | Listings | Images |
|-------------|----------|--------|
| KV.ee       | 150      | Yes    |
| City24      | 137      | Yes    |
| Rendin      | 51       | Yes    |
| **Total**   | **338**  | —      |

All listings are real scraped data — no demo or seed listings.  
Data covers rental apartments in Tartu, Estonia only.

---

## Tech Stack

| Layer       | Technology                              |
|-------------|-----------------------------------------|
| Backend     | Java 21 + Spring Boot 3.4.5             |
| Database    | PostgreSQL 16 (one DB per service)      |
| Messaging   | RabbitMQ 3 (Spring AMQP)               |
| API Gateway | Spring Cloud Gateway                    |
| Frontend    | Vue 3 + Vite + Vue Router + Chart.js    |
| Scraping    | Java HttpClient + Jsoup + wget          |
| Email (dev) | MailHog                                 |
| Containers  | Docker + Docker Compose                 |

---

## Architecture

Nine independent microservices behind a single API Gateway, each with its own database.

```
Browser (Vue 3 :5173)
        │
        ▼
API Gateway (:8080)
        │
   ┌────┴──────────────────────────────────────┐
   │                                           │
scraper-service      →  scraper_db             │
analytics-service    →  analytics_db           │
user-service         →  user_db                │
alert-service        →  alert_db               │
neighborhood-service →  neighborhood_db        │
landlord-service     →  landlord_db            │
listing-service      →  listing_mgmt_db        │
notification-service →  notification_db        │
```

RabbitMQ events flow between services:
- `listing.new` → scraper-service produces → alert-service consumes
- `listing.claimed` → listing-service produces → notification-service consumes
- `review.posted` → landlord-service produces → notification-service consumes

---

## How the Scrapers Work

| Source    | Method                                                                 |
|-----------|------------------------------------------------------------------------|
| KV.ee     | HTML scraping via `wget` subprocess (bypasses Cloudflare TLS block) + Jsoup parsing |
| City24    | Public REST JSON API — `api.city24.ee/et_EE/search/realties`          |
| Rendin    | Firebase callable cloud function — POST to `cloudfunctions.net`        |

Images are fetched from:
- KV.ee → `img-kv.ee/image/object/43/…`
- City24 → `static.img-city24.ee/object/24/…` (format code `24` discovered at runtime)
- Rendin → various CDN fields (`imageUrl`, `coverImage`, `photos[]`, etc.)

---

## Running It

### Prerequisites
- Docker + Docker Compose
- Node.js 18+ (for the frontend dev server)
- Java 21 + Maven (only if running services locally instead of Docker)

### Start everything with Docker

```bash
docker compose up -d
```

This starts all 9 services, PostgreSQL, RabbitMQ, MailHog, and the API Gateway.

### Start the frontend dev server

```bash
cd frontend
npm install
npm run dev
```

Open [http://localhost:5173](http://localhost:5173)

### Trigger a manual scrape

```bash
curl -X POST http://localhost:8080/api/scraper/trigger
```

---

## Service Ports

| Service               | Port  |
|-----------------------|-------|
| API Gateway           | 8080  |
| scraper-service       | 8081  |
| analytics-service     | 8082  |
| user-service          | 8083  |
| alert-service         | 8084  |
| neighborhood-service  | 8085  |
| landlord-service      | 8086  |
| listing-service       | 8087  |
| notification-service  | 8088  |
| Frontend (Vite)       | 5173  |
| PostgreSQL            | 5433  |
| RabbitMQ Management   | 15672 |
| MailHog Web UI        | 8025  |

---

## Key API Endpoints (via Gateway at :8080)

| Endpoint                        | Description                              |
|---------------------------------|------------------------------------------|
| `GET  /api/listings`            | All listings (filter: neighbourhood, price, size) |
| `POST /api/scraper/trigger`     | Manually trigger a scrape run            |
| `GET  /api/scraper/status`      | Last scrape time, counts, job status     |
| `GET  /api/analytics/neighborhoods` | Avg rent & price/m² per neighbourhood |
| `GET  /api/analytics/trends`    | Price trend over time                    |
| `GET  /api/analytics/summary`   | City-wide stats                          |
| `POST /api/users`               | Register user                            |
| `POST /api/alerts`              | Create alert rule                        |
| `GET  /api/neighborhoods`       | Neighbourhoods with scores               |
| `GET  /api/landlords/{id}/reputation` | Landlord reputation score          |

---

## Dev Tools

| Tool               | URL                                      |
|--------------------|------------------------------------------|
| Frontend           | http://localhost:5173                    |
| RabbitMQ UI        | http://localhost:15672 (guest / guest)   |
| MailHog            | http://localhost:8025                    |
| Swagger (scraper)  | http://localhost:8081/swagger-ui.html    |
| Swagger (analytics)| http://localhost:8082/swagger-ui.html    |

---

## Project Structure

```
uuriturg/
├── docker-compose.yml
├── api-gateway/
├── scraper-service/          # KV.ee · City24 · Rendin scrapers
├── analytics-service/        # Price trends, neighbourhood stats
├── user-service/             # User accounts, saved searches
├── alert-service/            # Alert rules & matching
├── neighborhood-service/     # Neighbourhood data & reviews
├── landlord-service/         # Landlord profiles & reviews
├── listing-service/          # Managed listings & ownership claims
├── notification-service/     # Email & in-app notifications
└── frontend/                 # Vue 3 dashboard
```
