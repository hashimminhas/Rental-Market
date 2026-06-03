# Üüriturg — Technical Project Brief
**For: Muhammad Haris Irfan | UT ICS Student Project Contest — June 3, 2026**

---

## ⚡ Key Numbers to Remember (memorise these)

| Fact | Value |
|------|-------|
| Live listings | **500+** |
| Data sources | **4** (KV.ee, City24, Rendin, Kinnisvara24) |
| Scrape frequency | **Every 6 hours**, auto-scheduled |
| Alert email speed | **Under 30 seconds** from new listing to email |
| Microservices | **4** (Scraper, Analytics, Alert, Notification) |
| Backend language | **Java 21 + Spring Boot 3.4** |
| Frontend | **Vue 3** |
| Message broker | **RabbitMQ** |
| Deployment | **Docker Compose on UT HPC server** |
| Analytics tool | **Umami** (self-hosted, cookie-free, GDPR compliant) |
| Live URL | **https://uuriturg.cs.ut.ee** |

---

## 🛠️ Tech Stack at a Glance

| Layer | Technology | Why |
|-------|-----------|-----|
| Backend | Java 21 + Spring Boot 3.4 | Mature, production-grade, familiar |
| API Gateway | Spring Cloud Gateway | Routing, CORS, circuit breakers in one place |
| Message Queue | RabbitMQ | Decouples scraper from alert matching |
| Database | PostgreSQL (4 separate DBs) | One DB per service = true isolation |
| Frontend | Vue 3 (Composition API) | Lightweight, reactive, fast to build |
| Map | Leaflet.js | Open-source, no API key needed |
| Charts | Chart.js | Simple, clean price trend visualisation |
| Email | Brevo SMTP | Free transactional email, reliable delivery |
| Containerisation | Docker + Docker Compose | Reproducible, easy to deploy |
| Web scraping | JSoup + wget subprocess | JSoup for HTML parsing; wget bypasses Cloudflare |
| Reverse proxy | nginx | Serves frontend, proxies /api to gateway |
| Visitor analytics | Umami | Privacy-first, self-hosted, no cookies |

---

## 1. What Is This Project?

**Üüriturg** (Estonian for "rental market") is a real-time rental listing aggregator for Tartu, Estonia. It solves a very specific and painful problem: students and newcomers looking for apartments in Tartu have to manually check multiple portals every day — KV.ee, City24, Rendin — because no single platform shows everything in one place.

We built a system that:
- Automatically scrapes all three portals every 6 hours
- Stores every listing in a central database
- Shows everything on an interactive map with filters
- Lets users create price alerts and get emailed the moment a match appears
- Shows market analytics — average rent per neighborhood, price trends over time

The site is live at **https://uuriturg.cs.ut.ee** with 500+ real listings.

---

## 2. The Problem (Why We Built This)

Tartu has a competitive rental market, especially for students. The pain points:
- **Fragmented data**: listings are spread across KV.ee, City24, Rendin, Kinnisvara24
- **No alerts**: none of the portals send you an email when a new listing matching your criteria appears
- **No map view**: you cannot see all listings on a map across portals
- **No price analytics**: no way to know if a neighborhood is getting more expensive

Our solution aggregates everything automatically, with zero manual effort from the user.

---

## 3. System Architecture — Microservices Overview

The system is built as a **microservices architecture** using Java 21 + Spring Boot 3.4. Each service is an independent application with its own database, its own responsibility, and its own port. They communicate via **REST HTTP** and **RabbitMQ message queue**.

```
Browser / Frontend (Vue 3)
        │
        ▼
  API Gateway :8080          ← single entry point, routes all traffic
  ┌──────────────────────────────────────────────────────┐
  │  /api/listings/**  →  Scraper Service  :8081         │
  │  /api/analytics/** →  Analytics Service :8082        │
  │  /api/alerts/**    →  Alert Service    :8084         │
  │  /api/notifications/** → Notification Service :8088  │
  └──────────────────────────────────────────────────────┘
        │
        ▼
  RabbitMQ (message broker)
  Scraper publishes "listing.new" → Alert Service consumes it

  PostgreSQL (one database per service)
  scraper_db | analytics_db | alert_db | notification_db
```

Everything runs in **Docker containers** on a single Ubuntu server at UT HPC (193.40.153.151). Managed with `docker-compose.prod.yml`.

---

## 4. Service-by-Service Breakdown

---

### 4.1 Scraper Service (Port 8081) — THE CORE

**What it does:** This is the heart of the system. It scrapes rental listings from four portals every 6 hours, deduplicates them, stores them in PostgreSQL, and publishes a RabbitMQ event for every new listing so the Alert Service can react in real time.

**How scraping works — each source:**

#### KV.ee Scraper
- KV.ee uses **Cloudflare TLS fingerprinting** to detect and block automated HTTP clients (like Java's `HttpURLConnection` or OkHttp). Standard HTTP libraries get 403 blocked.
- **Solution**: We use a `wget` subprocess. `wget` has a different TLS fingerprint that Cloudflare doesn't block. We call `wget` from Java using `ProcessBuilder`, capture the HTML output, then parse it with **JSoup**.
- We extract: title, price, size, rooms, street address, neighborhood, URL, image URL.

#### City24 Scraper
- City24 has a **public REST/JSON API** — no scraping required.
- We call their API endpoint with query parameters (city=Tartu, deal_type=rent), parse the JSON response directly.
- City24 responses include GPS coordinates (latitude/longitude), which is why the map pins for City24 listings are more accurate.

#### Rendin Scraper
- Rendin uses **Firebase Realtime Database** as its backend. Their mobile app fetches data from Firebase.
- We call the Firebase REST endpoint directly (`https://rendin-production.firebaseio.com/...`), which returns JSON.
- Rendin listings tend to be verified landlord listings with better quality data.

#### Kinnisvara24 Scraper
- Standard HTML page scraping using **JSoup** (Java HTML parser).
- We load the listing page, find the relevant CSS selectors, extract the data.

**Neighborhood detection:** Not all portals include a neighborhood field. We built a rule-based `detectNeighborhood()` method that looks for known Tartu neighborhood names in the listing address and title (e.g. "Kesklinn", "Annelinn", "Karlova") and assigns the neighborhood automatically.

**Deduplication:** Each listing has a `source` (KV_EE, CITY24, RENDIN, K24) and an `externalId` (the listing's ID on the original portal). Before saving, we check if that combination already exists — if yes, we update it; if no, we insert it.

**Scheduling:** A Spring `@Scheduled` job runs every 6 hours automatically. Can also be triggered manually via `POST /api/scraper/trigger`.

**RabbitMQ event:** After saving a new listing, we publish a `listing.new` event to the `listing.events` exchange with the listing data (id, price, neighborhood, size, rooms, URL). The Alert Service listens for this.

**Key Endpoints:**
```
GET  /api/listings                  → all listings (filter by neighborhood, maxPrice, minSize)
GET  /api/listings/{id}             → single listing
GET  /api/listings/{id}/price-history → price change history
POST /api/scraper/trigger           → manually trigger a scrape
GET  /api/scraper/status            → is scrape running?
GET  /api/scraper/jobs              → scrape job history
```

---

### 4.2 Analytics Service (Port 8082)

**What it does:** Computes price statistics per Tartu neighborhood and stores daily snapshots. Powers the "Market Insights" page — price trends charts, city summary stats, cheapest available listings.

**How it works:**
- Has a scheduled job (runs every 6 hours, offset from scraper) that calls the Scraper Service via REST to get all listings per neighborhood.
- For each of the 10 Tartu neighborhoods it computes: average price, median price, price per m², listing count, min price, max price, and % change vs previous day.
- Saves a `NeighborhoodSnapshot` row to `analytics_db` for each neighborhood.
- The landing page stats (€512 avg, cheapest, most expensive, 549 listings) all come from these stored snapshots — no live computation on page load.

**Key Endpoints:**
```
GET  /api/analytics/neighborhoods   → latest snapshot per neighborhood
GET  /api/analytics/trends?neighborhood=Kesklinn&days=30  → price trend over time
GET  /api/analytics/summary         → city-wide stats (avg, min, max, total listings)
GET  /api/analytics/cheapest        → cheapest current listings
POST /api/analytics/compute         → manually trigger snapshot computation
```

---

### 4.3 Alert Service (Port 8084)

**What it does:** Lets users create price alert rules (email + criteria). When a new listing arrives via RabbitMQ, it checks all active rules and sends an email if there's a match.

**How the alert flow works end-to-end:**
1. User creates an alert via the frontend: email, neighborhood, max price, min size, min rooms
2. Alert rule is saved to `alert_db`
3. Immediately after creation, the service does a background scan of all existing listings that might already match (so the user gets notified of current matches too)
4. When a new listing arrives (RabbitMQ `listing.new` event), the Alert Service evaluates it against ALL active alert rules
5. If a rule matches AND this listing hasn't been matched before → saves an `AlertMatch` record, then calls Notification Service via REST
6. Deduplication: `alertMatchRepository.existsByAlertIdAndListingId()` — each listing is only sent once per alert

**Matching logic:** A listing matches a rule if:
- Neighborhood matches (if rule has one set)
- Price ≥ rule's minPrice (if set)
- Price ≤ rule's maxPrice (if set)
- Size ≥ rule's minSize (if set)
- Rooms ≥ rule's minRooms (if set)

**Key Endpoints:**
```
POST /api/alerts                    → create a new alert rule
GET  /api/alerts                    → list all alerts
GET  /api/alerts/{id}               → get single alert
PUT  /api/alerts/{id}               → update / toggle active
DELETE /api/alerts/{id}             → delete alert
GET  /api/alerts/{id}/matches       → listings matched by this alert
```

---

### 4.4 Notification Service (Port 8088)

**What it does:** Handles email delivery. Called by the Alert Service via REST when a match is found. Uses **Brevo SMTP** (transactional email provider) to send real emails.

**How it works:**
1. Alert Service calls `POST /api/notifications` with recipient email, subject, body
2. Notification Service saves the notification record to `notification_db` (for audit trail)
3. Sends the email via Spring `JavaMailSender` → Brevo SMTP relay (`smtp-relay.brevo.com:587`)
4. Updates the notification status to SENT or FAILED

**Why a separate service?** Decoupling. If email delivery fails, we can retry without re-triggering the alert logic. We also have a full audit log of every email ever sent.

**Key Endpoints:**
```
POST /api/notifications                     → send a notification
GET  /api/notifications/{id}               → get notification by ID
GET  /api/notifications/status/pending     → all unsent notifications
POST /api/notifications/{id}/retry         → retry a failed notification
```

---

### 4.5 API Gateway (Port 8080)

**What it does:** Single entry point for all traffic. The frontend only ever talks to port 8080 — it never directly contacts individual services.

**Responsibilities:**
- **Routing**: maps `/api/listings/**` → Scraper, `/api/analytics/**` → Analytics, etc.
- **CORS**: allows requests from `uuriturg.cs.ut.ee` and `localhost:5173`
- **Circuit breakers**: using Resilience4j — if a service is down, returns a fallback response instead of hanging
- **StripPrefix**: removes the `/api` prefix before forwarding (e.g. `/api/listings` becomes `/listings` when it hits the Scraper Service)

---

### 4.6 Frontend (Vue 3)

**What it does:** Single-page application. Built with Vue 3 (Composition API) and served via nginx.

**Pages:**
- `/` — Landing page with live stats and "how it works"
- `/listings` — Interactive Leaflet map + table with all listings, filter by neighborhood/price/size
- `/insights` — Market analytics charts (Chart.js), price trends per neighborhood
- `/alerts` — Create/manage price alerts

**Analytics:** Umami (self-hosted, privacy-friendly, no cookies) — tracks page views, visitor countries, session duration.

---

## 5. How Services Connect to Each Other

```
┌─────────────┐    REST     ┌──────────────────┐
│   Frontend  │ ──────────► │   API Gateway    │
└─────────────┘             └────────┬─────────┘
                                     │ routes
              ┌──────────────────────┼───────────────────┐
              ▼                      ▼                   ▼
     ┌────────────────┐   ┌─────────────────┐  ┌──────────────────┐
     │ Scraper Service│   │Analytics Service│  │  Alert Service   │
     │    :8081       │   │    :8082        │  │     :8084        │
     └───────┬────────┘   └────────┬────────┘  └────────┬─────────┘
             │ RabbitMQ            │ REST calls           │ REST calls
             │ listing.new         │ scraper:8081         │ notification:8088
             │                     │                      ▼
             └────────────────────►└──────────►  ┌──────────────────┐
             (Alert Service                       │Notification Svc  │
              consumes events)                    │    :8088         │
                                                  └──────────────────┘
                                                         │ SMTP
                                                         ▼
                                                  Brevo → User Email
```

**Two communication patterns:**
1. **REST (synchronous)**: Frontend → Gateway → Services. Also Analytics → Scraper. Also Alert → Notification.
2. **RabbitMQ (asynchronous)**: Scraper publishes `listing.new` event → Alert Service consumes it. This is event-driven — Scraper doesn't need to know Alert Service exists.

---

## 6. GDPR Compliance

### What personal data do we collect?

| Data | Where | Why |
|------|-------|-----|
| Email address | Alert rules table (`alert_db`) | To send matching listing notifications |
| Email address | Notifications table (`notification_db`) | Audit log of sent emails |

### What we do right:
- **Minimal data**: We only collect an email. No name, no phone, no account required.
- **Purpose limitation**: Email is used only to send alerts the user explicitly requested.
- **User control**: Users can delete their alert at any time via the UI — this removes their email from the system completely.
- **Privacy notice**: The alerts page shows an ⓘ tooltip explaining exactly what we store and why.
- **No third-party sharing**: Email is only sent to Brevo (our SMTP relay) for delivery purposes.
- **No tracking cookies**: Umami analytics is cookie-free and privacy-compliant.

### What we could improve (honest):
- No formal privacy policy page yet
- No explicit "I agree" checkbox on alert creation
- Email is not encrypted at rest in the database
- No automatic data deletion after X months of inactivity

---

## 7. Pros and Cons of Our Architecture

### Pros
| Advantage | Explanation |
|-----------|-------------|
| **Independent scaling** | If KV.ee scraping gets heavy, only the Scraper Service needs more resources |
| **Fault isolation** | If Analytics Service crashes, listings still work fine — services don't share a process |
| **Independent deployment** | Can rebuild and redeploy one service without touching others |
| **Technology flexibility** | Each service could theoretically use a different language/framework |
| **Event-driven alerts** | RabbitMQ means new listings trigger alerts in under 30 seconds without polling |

### Cons
| Disadvantage | Explanation |
|--------------|-------------|
| **Complexity** | 4 services + gateway + queue + 4 databases vs. one monolith that would do the same |
| **Network latency** | REST calls between services add overhead (e.g. Analytics calling Scraper adds ~10ms) |
| **Harder to debug** | A bug can span multiple services; need to check multiple logs |
| **Resource usage** | Each Spring Boot service uses ~256MB RAM. Total: ~1.5GB just for Java processes |
| **Over-engineered for current scale** | With 500 listings and ~50 users/day, a monolith would have been sufficient |

**When would microservices be justified here?** If the scraping needed to run on a separate high-memory machine, or if the alert matching needed to scale independently during peak demand (e.g. start of semester when everyone searches at once).

---

## 8. What We Would Do Differently / Future Work

### Short term
- **Email unsubscribe link** in every alert email (one-click unsubscribe without logging in)
- **Alert statistics**: show the user how many listings were found since they created the alert
- **Price drop alerts**: notify when a listing they've seen drops in price

### Medium term
- **More data sources**: Kinnisvaraportaal.ee, Facebook Marketplace housing groups
- **Saved searches**: remember filter state between sessions
- **Mobile app**: React Native wrapper around the existing API

### Long term
- **Machine learning price prediction**: "Is this listing cheap or expensive for its neighborhood?"
- **Landlord profiles**: verified landlords with review scores (we had this, removed it to simplify)
- **Multi-city support**: Tallinn, Pärnu — same architecture, new scraper configurations

---

## 9. Judge Questions — Model Answers

These are the questions judges most commonly ask. Read these out loud with Harris tonight.

---

**Q: Why microservices and not a simple monolith?**
> Each component has a different failure mode and scaling need. The scraper can hang or fail without affecting the alert system. The analytics can be down without breaking listing display. In a monolith, one crash kills everything. Also, it's a university project — demonstrating distributed systems architecture is part of the point.

---

**Q: How does the alert system work exactly?**
> When the scraper saves a new listing, it publishes a `listing.new` event to RabbitMQ. The Alert Service is subscribed to that queue. It receives the event, loops through all active alert rules, and checks if the listing matches the criteria — neighborhood, price range, min size, min rooms. If it matches and hasn't been sent before, it calls the Notification Service via REST, which sends the email through Brevo SMTP. End to end: under 30 seconds.

---

**Q: How do you handle KV.ee's bot protection?**
> KV.ee uses Cloudflare TLS fingerprinting — it identifies Java HTTP clients by their TLS handshake signature and returns 403. We bypass this by calling `wget` as a subprocess from Java using `ProcessBuilder`. `wget` has a different TLS fingerprint that Cloudflare doesn't flag. The HTML comes back to Java, we parse it with JSoup.

---

**Q: What happens if a scraper or service goes down?**
> The API Gateway uses Resilience4j circuit breakers. If a service fails repeatedly, the circuit opens and requests get a fast fallback response instead of timing out. Services are also independent — if Analytics crashes, listings still work. Docker has `restart: unless-stopped` so containers recover automatically after failures.

---

**Q: Is the data you're scraping legal?**
> We display publicly available listing data for informational and research purposes. This is similar to how Google indexes public web pages. We don't store personal data from listings (no landlord names, no phone numbers). The scraped data is used to help tenants find housing, which is in the public interest. Long term, we would seek data partnership agreements with the portals.

---

**Q: How is GDPR handled?**
> We only collect one piece of personal data: email address, and only when the user explicitly creates an alert. It's used solely to send them the alerts they asked for. Users can delete their alert at any time, which removes their email from the database. We show a privacy notice on the form. Analytics (Umami) is cookie-free.

---

**Q: What was the hardest technical problem?**
> KV.ee's Cloudflare block was the biggest challenge — standard Java HTTP clients all got blocked. The wget subprocess solution was non-obvious. The second hardest was ensuring alert deduplication: a new listing must only trigger one email per alert rule, even if the scraper runs multiple times. We solved that with a composite unique check on `(alertId, listingId)` in the match table.

---

**Q: How many real users does it have?**
> We launched recently but have real visitors from Estonia tracked via Umami analytics. The site is publicly accessible and indexed. The alert system has sent real emails to real users.

---

**Q: Could this scale to Tallinn or other cities?**
> Yes — the architecture supports it. You'd add new scraper implementations for Tallinn-specific listings, add the city as a filter parameter, and the rest of the pipeline (alerts, analytics, notifications) works unchanged. The neighborhood detection would need a Tallinn neighborhood dictionary.

---

**Q: Why not just use an existing aggregator?**
> There isn't one for Tartu. KV.ee, City24 and Rendin are competitors — they don't share data. No existing tool shows all Tartu rental listings in one place with a map and email alerts. That gap is exactly why we built this.

---

## 10. Quick Reference — Live URLs (open these on your laptop before you walk in)

| What | URL |
|------|-----|
| Main site | https://uuriturg.cs.ut.ee |
| Listings map | https://uuriturg.cs.ut.ee/listings |
| Market insights | https://uuriturg.cs.ut.ee/insights |
| Set an alert | https://uuriturg.cs.ut.ee/alerts |
| Trigger scrape | POST https://uuriturg.cs.ut.ee/api/scraper/trigger |
| Trigger analytics compute | POST https://uuriturg.cs.ut.ee/api/analytics/compute |

---

## 11. One-Minute Pitch (memorise this)

> "Tartu students waste time checking KV.ee, City24 and Rendin separately every day for apartments. We built Üüriturg — one platform that automatically scrapes all three portals every 6 hours, shows 500+ listings on an interactive map, and emails you within 30 seconds when a new listing matches your criteria. It's built as microservices on Java Spring Boot, deployed with Docker on a UT server, and it's live right now."

---

*Document prepared for UT ICS Student Project Contest — June 3, 2026*
*Authors: Hashim Ali & Muhammad Haris Irfan*
