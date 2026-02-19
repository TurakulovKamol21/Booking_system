# Autoguide Hotel Booking Backend

Reactive hotel booking backend aligned with Java 17 + Spring Boot 3.x requirements.

## Tech Stack
- Java 17
- Spring Boot 3.x (WebFlux, Security, Data R2DBC)
- PostgreSQL (core booking data)
- MongoDB (booking recommendation history)
- Redis (booking response cache)
- Keycloak (OAuth2/OIDC, JWT)
- JUnit 5, Mockito, Testcontainers
- Docker + Docker Compose
- GitLab CI/CD
- OpenAPI/Swagger

## Functional Scope
- Hotel list API with per-user hotel scope
- SUPER_ADMIN role for full cross-hotel access
- Guest management API
- Room management API
- Booking lifecycle API (create/get/list/status update)
- Booking recommendation API

## API Endpoints
- `GET /api/v1/public/home`
- `GET /api/v1/public/hotels`
- `GET /api/v1/public/rooms/highlights`
- `GET /api/v1/hotels`
- `POST /api/v1/guests`
- `GET /api/v1/guests`
- `POST /api/v1/rooms`
- `GET /api/v1/rooms`
- `POST /api/v1/bookings`
- `GET /api/v1/bookings/{id}`
- `PATCH /api/v1/bookings/{id}/status`
- `GET /api/v1/booking-recommendations/{bookingId}`

## Run Locally
### 1) Start infrastructure + backend (Docker)
```bash
docker compose up -d --build
```

Swagger (Docker backend):
- [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)

### 2) Run backend via Maven (optional, local mode)
Default app port from `application.yml` is `8090`:
```bash
mvn spring-boot:run
```

Custom port example:
```bash
mvn spring-boot:run -Dspring-boot.run.arguments="--server.port=18080 --app.security.enabled=false"
```

Swagger (Maven backend):
- [http://localhost:8090/swagger-ui.html](http://localhost:8090/swagger-ui.html)
- [http://localhost:18080/swagger-ui.html](http://localhost:18080/swagger-ui.html) (if custom port is used)

### 3) Get JWT token from Keycloak
```bash
curl -X POST "http://localhost:8081/realms/autoguide/protocol/openid-connect/token" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "client_id=autoguide-api" \
  -d "grant_type=password" \
  -d "username=operator_user" \
  -d "password=operator123"
```

### 4) Example secured API call
```bash
curl -X GET "http://localhost:8090/api/v1/guests" \
  -H "Authorization: Bearer <ACCESS_TOKEN>"
```

## Frontend (Vue, separate project)
Frontend lives in:
- `frontend`

Run:
```bash
cd frontend
cp .env.example .env
npm install
npm run dev
```

Open:
- [http://localhost:5173](http://localhost:5173)

Important:
- In `frontend/.env`, set `VITE_BACKEND_TARGET` to your backend port (`8090` or `18080`).

## Build and Test
```bash
mvn clean verify
```

## Seeded Demo Data
- Flyway `V4__bulk_demo_data.sql` seeds 10+ records for:
  - guests
  - rooms (with internet image URLs)
  - bookings
  - landing amenities
  - landing offers

## Project Structure
- `src/main/java/com/autoguide/backend/config` - security and OpenAPI config
- `src/main/java/com/autoguide/backend/controller` - REST controllers
- `src/main/java/com/autoguide/backend/service` - business logic
- `src/main/java/com/autoguide/backend/repository` - reactive repositories
- `src/main/resources/db/migration` - Flyway SQL migrations
- `src/test/java/com/autoguide/backend` - unit/integration tests
- `frontend` - Vue 3 frontend (dashboard, auth, guests, rooms, bookings)

## Troubleshooting
- `Unsupported Database: PostgreSQL 14.x`:
  `flyway-database-postgresql` dependency is included.
- `Found non-empty schema(s) but no schema history table`:
  `spring.flyway.baseline-on-migrate=true` is enabled.
- `Connection refused localhost:27017`:
  MongoDB service is not running.
- `Port 8090 already in use`:
  Run with another port:
  ```bash
  mvn spring-boot:run -Dspring-boot.run.arguments="--server.port=18080"
  ```
