# MiniUber - Microservice Architecture for a Ride-Sharing Platform — Prototype

## Overview

This repository contains a small prototype and design for a ride-sharing microservice platform (a simplified Uber-like system). The project models core domain objects (rides, drivers, users, payments), exposes RESTful endpoints, and documents a microservice architecture that uses an API Gateway, Lambda functions for light orchestration, and EC2/ECS for stateful services. Visual diagrams (class diagrams, sequence diagrams, architecture drawings) are available in the `static/` folder.

This README summarizes the design metaphor, domain model, REST resources and URIs, resource representations, architecture decisions, prototype mapping and how to run the project locally.

---

## Design metaphor

Think of the system as a set of cooperative agents:
- API Gateway agents (API Gateway + Lambdas) handle auth, validation, coarse routing and light orchestration.
- Worker agents (stateful microservices running on EC2/ECS) own domain logic and persistence: Ride Service, Driver Service, Payment Service, User Service.
- Messaging agents (SNS/SQS or Kafka) decouple services and propagate events (ride state updates, payment events).
- Real-time agents (WebSocket gateway or dedicated socket service) push driver-location and ride updates to clients.

Visual references: `static/microservices-architecture.png`, `static/class-diagram.png`, `static/sequence-ride-request.png`.

---

## Domain model (summary)

<img width="318" height="392" alt="Class Diagram" src="./blob/main/src/main/resources/static/ClassDiagram.png" />

Key domain objects and important fields (simplified):

- Ride
  - id: string
  - riderId: string
  - driverId: string | null
  - status: enum {REQUESTED, MATCHED, ONGOING, COMPLETED, CANCELLED}
  - origin: {lat, lng}
  - destination: {lat, lng}
  - requestedAt, startedAt, endedAt: timestamps
  - fare: number

- User
  - id, name, phone, email, rating

- Driver
  - id, name, vehicle: {plate, model}, status: enum {AVAILABLE, UNAVAILABLE}, currentLocation

- Payment
  - id, rideId, amount, method, status: enum {PENDING, COMPLETED, FAILED}

Refer to `static/class-diagram.png` for the full class-level diagram implemented in the code.

---

## API: Resource URIs and HTTP methods

Base path: `/api/v1`

Rides
- POST /api/v1/rides — create a new ride request (returns ride resource with `REQUESTED` or `MATCHED` state)
- GET  /api/v1/rides/{rideId} — get ride details
- PUT  /api/v1/rides/{rideId} — update mutable ride fields (cancel ride, update destination before match, etc.)
- GET  /api/v1/rides?userId={userId}&role={rider|driver} — list rides for a user

Drivers
- POST /api/v1/drivers — register a driver
- GET  /api/v1/drivers/{driverId} — driver profile
- PUT  /api/v1/drivers/{driverId}/status — update driver availability
- PUT  /api/v1/drivers/{driverId}/location — update driver location (or push via WebSocket)

Users
- POST /api/v1/users — create a user account
- GET  /api/v1/users/{userId} — get user profile

Payments
- POST /api/v1/payments — initiate payment for a ride
- GET  /api/v1/payments/{paymentId} — get payment status

Real-time (WebSocket)
- WebSocket connect: `wss://.../ws/track` — subscribe to ride and driver-location updates
- Fallback REST location update: `PUT /api/v1/drivers/{driverId}/location`

---

## Resource representations

All resources use JSON for request and response bodies. Timestamps are ISO-8601 (UTC) strings.

Example: Ride creation request

```zsh
{
  "riderId": "user-123",
  "origin": { "lat": 37.7749, "lng": -122.4194 },
  "destination": { "lat": 37.7849, "lng": -122.4094 },
  "paymentMethodId": "card-456"
}
```

Example: Ride response

```zsh
{
  "id": "ride-789",
  "riderId": "user-123",
  "driverId": "driver-111",
  "status": "MATCHED",
  "origin": { "lat": 37.7749, "lng": -122.4194 },
  "destination": { "lat": 37.7849, "lng": -122.4094 },
  "fare": 12.50,
  "requestedAt": "2025-10-31T14:12:00Z"
}
```

---

## Microservice architecture (high level)

- API Gateway (REST + WebSocket) — single public entry point and request validation layer.
- Lambda functions — used for authentication, validation, webhooks and lightweight orchestration.
- EC2/ECS/EKS — run the core microservices:
  - Ride Service — lifecycle management and matching logic
  - Driver Service — driver profiles, availability and location
  - User Service — user profiles and authentication glue
  - Payment Service — integrate with payment provider and reconcile payments
- Messaging/Event bus — SNS/SQS or Kafka to propagate domain events and integrate services asynchronously.
- Datastores — service-specific persistent storage (RDS, DynamoDB) to enforce bounded contexts.

Sequence example (ride request):
1. Client POSTs `/api/v1/rides` to API Gateway
2. Gateway triggers a Lambda for auth/validation then forwards to Ride Service
3. Ride Service persists request and publishes `RideRequested` event to the event bus
4. Driver Service consumes event and notifies nearby available drivers (via push / WebSocket)
5. Driver accepts; Ride Service updates ride `MATCHED` and notifies client and driver via WebSocket
6. Ride proceeds; Payment Service finalizes the charge when ride completes

Refer to `static/sequence-ride-request.png` for details.

---

## What this prototype contains (mapping to repository)

- Java + Maven code under `src/` implementing models, controllers and a simple in-memory or JDBC-backed persistence for demonstration.
- REST controllers exposing the URIs listed above.
- Unit tests under `src/test` covering core domain operations.
- Diagrams in `static/` showing class diagrams and architecture.

Note: This is a teaching/prototype repository. Implementation choices prioritize clarity and demonstration over production scalability and resilience.

---

## Running locally

Build the project and run the service locally (defaults may vary depending on branch/config in the repo):

```zsh
# Build package
./mvnw clean package

# Run with Maven
./mvnw spring-boot:run

# Or run the packaged jar
java -jar target/taller3aygo-0.0.1-SNAPSHOT.jar
```

Default base URL: `http://localhost:8080/api/v1` (confirm in `application.yaml` or `src/main/resources` if changed).

Run tests:

```zsh
./mvnw test
```

---

## Deployment notes (prototype -> cloud)

Suggested deployment pattern used for the design:

- Use API Gateway to expose REST and WebSocket endpoints.
- Use Lambda functions for light orchestration, auth and webhook endpoints.
- Host stateful microservices on EC2, ECS, or EKS (Ride Service / Driver Service / Payment Service).
- Use managed DBs (RDS or DynamoDB) per service to enforce bounded contexts.
- Use SNS/SQS (or Kafka) for decoupled event propagation and to implement eventual consistency.
- Monitor with CloudWatch, set up alarms on error rates and latencies.

Scaling & Real-time considerations:
- For WebSocket scale, prefer a managed WebSocket gateway (API Gateway WebSocket or a socket cluster on ECS/EKS).
- Use location update sampling and rate limits to keep the location stream efficient.

---

## Design tradeoffs and decisions

- Simplicity vs. performance: prototype uses synchronous REST and in-memory stores for clarity. Production must use persistent stores and resilient async flows.
- Real-time: WebSocket provides low-latency updates but requires stateful scaling. Consider push notifications as a fallback.
- Orchestration: Lambdas are useful for small glue logic; core stateful services are better on containers/VMs.

---

## Contributing

Please open issues or PRs to propose changes. Run `./mvnw test` and keep code style aligned with existing conventions. Include unit tests for new behavior.

---

## License

Check repository root for license details.

---

Diagrams and UML: open the `static/` directory to view the architecture and class diagrams referenced above.

