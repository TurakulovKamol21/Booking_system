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

## Run Locally (Step-by-step)
### Variant A (recommended): infra in Docker, backend with Maven, frontend with Vite
1. Start infra services:
```bash
docker compose up -d postgres mongo redis keycloak
```
2. Verify infra is up:
```bash
docker compose ps
```
3. Run backend:
```bash
mvn spring-boot:run
```
4. Check backend:
- [http://localhost:8090/actuator/health](http://localhost:8090/actuator/health)
- [http://localhost:8090/swagger-ui.html](http://localhost:8090/swagger-ui.html)
5. In a new terminal, run frontend:
```bash
cd frontend
cp .env.example .env
npm install
npm run dev
```
6. Open frontend:
- [http://localhost:5173](http://localhost:5173)

Frontend `.env` should point to:
```dotenv
VITE_BACKEND_TARGET=http://localhost:8090
VITE_KEYCLOAK_TARGET=http://localhost:8081
VITE_REQUIRE_AUTH=true
```

### Variant B: backend in Docker Compose
```bash
docker compose up -d --build
```
Swagger:
- [http://localhost:8090/swagger-ui.html](http://localhost:8090/swagger-ui.html)

## Kubernetes
Kubernetes manifests are available in:
- `k8s`

Quick start:
```bash
eval "$(minikube docker-env)"
docker build -t autoguide-backend:latest .
docker build -t autoguide-frontend:latest ./frontend
kubectl apply -k k8s
kubectl -n autoguide port-forward svc/autoguide-frontend 8080:80
```

Then open:
- [http://localhost:8080](http://localhost:8080)

Detailed guide:
- `k8s/README.md`

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
