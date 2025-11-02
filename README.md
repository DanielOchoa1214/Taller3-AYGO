# MiniUber - Microservice Architecture for a Ride-Sharing Platform — Prototype

## Overview

This repository contains a small prototype and design for a ride-sharing microservice platform (a simplified Uber-like system). The project models core domain objects (rides, drivers, users, payments), exposes RESTful endpoints, and proposes an initial beta for a microservice architecture that uses an API Gateway, Lambda functions for light orchestration, and some other cloud services.

---

## Design metaphor

Think of the system as a set of cooperative agents:
- API Gateway agents (API Gateway + Lambdas) handle Ride Service, Driver Service, Payment Service and User Service.
- Messaging agents like SNS decouples services and propagate events (ride state updates, payment events).
- Real-time agents (EventBridge) to push driver-location and ride updates to clients.
- Storage agents (DynamoBD) to hold each microservice information as needed.

---

## Domain model

<img alt="Class Diagram" src="https://github.com/DanielOchoa1214/Taller3-AYGO/blob/main/src/main/resources/static/ClassDiagram.png" />

Key domain objects and important fields (simplified):

- Ride
  - id: string
  - user: User
  - driver: Driver
  - from: string
  - to: string

- User
  - id: string
  - name: string
  - email: string
  - score: double

- Driver
  - id, name, vehicle: {plate, model}, status: enum {AVAILABLE, UNAVAILABLE}, currentLocation
 
- Driver
  - id: string
  - name: string
  - email: string
  - state: enum {AVAILABLE, IN_A_RIDE, BANNED}
  - rides: Ride[]

- Payment
  - id, rideId, amount, method, status: enum {PENDING, COMPLETED, FAILED}
- Payment
  - id: string
  - price: double
  - ride: Ride
  - state: enum {PLACED, PAYED, REJECTED}

---

## API: Resource URIs and HTTP methods

Rides
- GET     /ride — get all rides
- GET     /ride/{rideId} — get ride details
- POST    /ride — create a new ride request (returns ride resource location in header)
- PATCH   /ride — update mutable ride fields (cancel ride, update destination before match, etc.)
- DELETE  /ride/{rideId} — deletes the ride registry

Drivers
- GET     /driver — get all drivers
- GET     /driver/{driverId} — get driver details
- POST    /driver — create a new driver profile (returns ride resource location in header)
- PATCH   /driver — update mutable driver fields (update driver availability)
- DELETE  /driver/{driverId} — deletes the driver registry

Users
- GET     /user — get all users
- GET     /user/{userId} — get user details
- POST    /user — create a new user profile (returns ride resource location in header)
- PATCH   /user — update mutable user fields (improving/decreasing in score)
- DELETE  /user/{userId} — deletes the user registry

Payments
- POST /api/v1/payments — initiate payment for a ride
- GET  /api/v1/payments/{paymentId} — get payment status

- GET     /payment — get payments users
- GET     /payment/{paymentId} — get payment details
- POST    /payment — create a new payment registry (returns ride resource location in header)
- PATCH   /payment — update mutable payment fields (updates payment state)
- DELETE  /payment/{paymentId} — deletes the payment registry

---

## Resource representations

All resources use JSON for request and response bodies.

Example: User creation request

```zsh
{
    "name": "Daniel",
    "email": "abc@xyz.com",
    "score": 4.5
}
```

Example: User response

```zsh
{
    "id": 1879023293,
    "name": "Daniel",
    "email": "abc@xyz.com",
    "score": 4.5
}
```

---

## Microservice architecture (high level)

<img alt="Class Diagram" src="https://github.com/DanielOchoa1214/Taller3-AYGO/blob/main/src/main/resources/static/Taller3Arch.png" />

- S3 - 
- CDN -
- Cloudfront - 
- API Gateway (REST + WebSocket) — single public entry point and request validation layer.
- Lambda functions — run the core microservices:
  - Ride Service — lifecycle management and matching logic
  - Driver Service — driver profiles, availability and location
  - User Service — user profiles and authentication glue
  - Payment Service — integrate with payment provider and reconcile payments
- Messaging/Event bus — EventBridge to propagate domain events and integrate services asynchronously.
- Datastores — DynamoDB - service-specific persistent storage to enforce bounded contexts.

Sequence example (ride request):
1. Client POSTs `/api/v1/rides` to API Gateway
2. Gateway triggers a Lambda for auth/validation then forwards to Ride Service
3. Ride Service persists request and publishes `RideRequested` event to the event bus
4. Driver Service consumes event and notifies nearby available drivers (via push / WebSocket)
5. Driver accepts; Ride Service updates ride `MATCHED` and notifies client and driver via WebSocket
6. Ride proceeds; Payment Service finalizes the charge when ride completes

---

## What this prototype contains

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

