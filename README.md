# Workout Tracker

Workout Tracker is a Spring Boot backend for creating workout plans, logging workout sessions, and storing set-level training data. It also includes a scheduled weekly summary job, health and metrics endpoints, local Docker-based infrastructure, and a CI pipeline.

## What The App Does

The current version supports:

- creating and listing exercises
- creating workout plans
- adding exercises to a workout plan
- creating workout sessions
- logging set entries with reps and weight
- generating weekly workout summaries in the background
- exposing health, metrics, readiness/liveness, and Prometheus scrape endpoints

The API is intentionally small. The goal so far has been to build a clean backend baseline before adding more product features.

## Architecture Overview

The application follows a standard layered Spring Boot structure:

- `api`: controllers, DTOs, exception handling, and centralized endpoint paths
- `service`: business logic and transaction boundaries
- `repository`: Spring Data JPA repositories
- `domain`: JPA entities and shared persistence concerns
- `config`: security, JPA, scheduling, clock, and HTTP logging setup
- `job`: scheduled background jobs

High-level request flow:

```text
HTTP Request
   |
   v
Controller
   |
   v
Service
   |
   v
Repository
   |
   v
PostgreSQL
```

Background flow:

```text
@Scheduled job
   |
   v
WeeklyWorkoutSummaryService
   |
   v
Aggregate sessions + sets
   |
   v
Persist weekly_workout_summary
```

## How To Run Locally

### Prerequisites

- Java 24
- Docker and Docker Compose

### Option 1: Run With Local Profile

This uses your local PostgreSQL on `localhost:5432`.

1. Create a local database named `workout_tracker`.
2. Start the app with the local profile:

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

The `local` profile is defined in [application-local.yaml](/Users/ahmetduser/IdeaProjects/workout-tracker/src/main/resources/application-local.yaml).

Local defaults:

- database: `jdbc:postgresql://localhost:5432/workout_tracker`
- username: `postgres`
- password: `postgres`
- basic auth username: `workout-admin`
- basic auth password: `change-me`

### Option 2: Run The Full Local Stack With Docker Compose

This is the easiest way to run the app together with PostgreSQL, Prometheus, and Grafana.

```bash
docker compose up --build
```

Available local endpoints:

- app: `http://localhost:8080`
- Prometheus: `http://localhost:9090`
- Grafana: `http://localhost:3000`
- health: `http://localhost:8080/actuator/health`
- metrics: `http://localhost:8080/actuator/metrics`
- prometheus scrape: `http://localhost:8080/actuator/prometheus`

The compose file is local-development-only and should not be treated as a production deployment model.

### Authentication

Current security is intentionally simple:

- `GET /api/**` is public
- `GET /actuator/**` is public
- non-GET requests require HTTP Basic auth

Example:

```bash
curl -u workout-admin:change-me \
  -H "Content-Type: application/json" \
  -d '{"name":"Bench Press"}' \
  http://localhost:8080/api/exercises
```

## How To Run Tests

Run the full test suite:

```bash
./mvnw test
```

Run a single test class:

```bash
./mvnw -Dtest=WeeklyWorkoutSummaryServiceIntegrationTest test
```

Test characteristics:

- unit tests cover service logic
- integration tests cover controllers and actuator endpoints
- tests use H2 in memory
- Liquibase migrations run during test startup


### Clear Config Boundaries

- [application.yaml](/Users/ahmetduser/IdeaProjects/workout-tracker/src/main/resources/application.yaml) is deployment-oriented and environment-driven
- [application-local.yaml](/Users/ahmetduser/IdeaProjects/workout-tracker/src/main/resources/application-local.yaml) contains local developer defaults
- [docker-compose.yml](/Users/ahmetduser/IdeaProjects/workout-tracker/docker-compose.yml) is strictly for local development

## CI Pipeline

GitHub Actions runs on pushes to `main` and `master`, and on pull requests.

The pipeline currently:

- runs tests
- builds the application jar
- builds the Docker image

See [ci.yml](/Users/ahmetduser/IdeaProjects/workout-tracker/.github/workflows/ci.yml).