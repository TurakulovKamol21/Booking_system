# ADR 0001: Reactive stack with Spring WebFlux + R2DBC

## Status
Accepted

## Context
The service handles hotel booking traffic with concurrent reads/writes across bookings, room inventory, and recommendation data.
The stack must support JWT security, relational consistency, and non-blocking integrations with Redis and MongoDB.

## Decision
Use Spring WebFlux for non-blocking HTTP and Spring Data R2DBC for PostgreSQL access.
Use reactive MongoDB for recommendation history and reactive Redis for caching frequently-read booking responses.

## Consequences
Positive:
- Better resource efficiency under concurrent booking load.
- Consistent reactive model across API, cache, and data layers.

Tradeoffs:
- Higher implementation complexity than imperative MVC + JPA.
- Team needs stronger discipline for reactive pipeline design and testing.
