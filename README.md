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

```yml
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
  - id: string
  - price: double
  - ride: Ride
  - state: enum {PLACED, PAYED, REJECTED}
```

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

<img alt="Architecture Diagram" src="https://github.com/DanielOchoa1214/Taller3-AYGO/blob/main/src/main/resources/static/Taller3Arch.png" />

This section describes the cloud components and responsibilities used in the proposed architecture.

- S3 — object storage for static assets (diagrams, client bundle, logs backups).
- CDN (CloudFront) — edge caching for static assets and optional API caching to reduce latency for common GETs.
- CloudWatch — centralized observability platform for the stack. Responsibilities:
  - Collect logs (CloudWatch Logs) from Lambda, API Gateway access logs, ECS/EC2 agents and custom app logs.
- API Gateway (REST + WebSocket) — single public entry point for client traffic; routes REST calls to services/Lambdas and manages WebSocket connections for real-time updates.
- Lambda functions — lightweight services that own business logic and data. Example services:
  - Ride Service — manages ride lifecycle, state transitions and matching logic.
  - Driver Service — driver profiles, availability state and location management.
  - User Service — user profiles, account management and basic auth integration.
  - Payment Service — payment initiation, reconciliation and integration with a payment provider.
- Messaging / Event bus — EventBridge to publish domain events (RideRequested, RideMatched, PaymentCompleted) and decouple services for asynchronous processing.
- Datastores — DynamoDB service-specific databases following the bounded-context principle.

Sequence example (ride request):
1. Client POSTs `/ride` to API Gateway.
2. API Gateway invokes a Lambda the Ride Service.
3. Ride Service persists the request and publishes a `RideRequested` event to the event bus.
4. Driver Service consumes the event and notifies nearby available drivers (via push notifications).
5. A driver accepts; Driver Service confirms acceptance and Ride Service updates the ride to `MATCHED` and notifies both client and driver.
6. Ride completes; Payment Service charges the rider and publishes `PaymentCompleted`.

---

## What this prototype contains

- Java + Maven code implementing domain models, controllers and a simple persistence layer for demonstration.
- REST controllers that expose the routes described earlier.

This repository is a prototype and educational artifact — it prioritizes clarity over production-ready concerns such as security, multi-region deployment, and hardened observability.

---

## Running locally

Build the project and run the service locally (defaults may vary depending on branch/config in the repo):

```zsh
# Build package
mvn clean install

# Run with Docker Compose
docker compose up -d
```

Default base URL: `http://localhost:8080`

---

## Deployment notes (prototype -> cloud)

Suggested deployment pattern used for the design:

- Use API Gateway to expose REST endpoints.
- Use managed DBs (DynamoDB) per service to enforce bounded contexts.
- Use EventBridge for decoupled event propagation and to implement eventual consistency.
- Monitor with CloudWatch, set up alarms on error rates and latencies.


