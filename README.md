# Spaced Repetition Service

A REST API built with Spring Boot that schedules flashcard reviews using spaced repetition algorithms.
The service supports two algorithm strategies — **SM-2** and **FSRS** — selectable via configuration.

## Tech Stack

- Java 21
- Spring Boot 4.0.2
- Spring Data JPA (Hibernate)
- PostgreSQL 17
- Flyway (database migrations)
- MapStruct (DTO mapping)
- Maven

## Prerequisites

- Java 21+
- Docker & Docker Compose
- Maven 3.9+ (or use the included `mvnw` wrapper)

## Quick Start

1. **Clone and configure**

   ```bash
   cp .env.example .env
   # Edit .env with your preferred database credentials
   ```

2. **Start PostgreSQL**

   ```bash
   docker-compose up -d
   ```

3. **Run the application** (dev profile is active by default)

   ```bash
   ./mvnw spring-boot:run
   ```

   The API is available at `http://localhost:8080`.
   In dev mode, 7 demo cards are seeded automatically.

4. **Run with production profile**

   ```bash
   ./mvnw spring-boot:run -Dspring-boot.run.profiles=prod
   ```

## Choosing an Algorithm

Set the `srs.algorithm` property in `application.yaml` or via environment variable:

```yaml
srs:
  algorithm: fsrs   # or sm2
```

FSRS is the default. The active algorithm is selected at startup via `@ConditionalOnProperty` —
only one implementation is registered as a Spring bean.

## API

### Create a card

```
POST /cards
Content-Type: application/json

{
  "front": "What is spaced repetition?",
  "back": "A learning technique that reviews material at increasing intervals."
}
```

**Response** `201 Created`
```json
{
  "id": "a1b2c3d4-...",
  "front": "What is spaced repetition?",
  "back": "A learning technique that reviews material at increasing intervals.",
  "nextReview": "2025-01-15T10:30:00Z",
  "createdAt": "2025-01-15T10:30:00Z"
}
```

### List cards

```
GET /cards?page=0&size=10&sortBy=createdAt&sortDir=desc&search=keyword
```

Returns a paginated list of cards. All parameters are optional:

| Parameter | Default     | Description                          |
|-----------|-------------|--------------------------------------|
| `page`    | `0`         | Page number (zero-based)             |
| `size`    | `10`        | Number of cards per page             |
| `sortBy`  | `createdAt` | Field to sort by                     |
| `sortDir` | `desc`      | Sort direction (`asc` or `desc`)     |
| `search`  | —           | Case-insensitive search in front/back|

**Response** `200 OK`
```json
{
  "content": [ { "id": "...", "front": "...", "back": "...", "nextReview": "...", "createdAt": "..." } ],
  "page": 0,
  "size": 10,
  "totalElements": 42,
  "totalPages": 5,
  "last": false
}
```

### Get cards due for review

```
GET /cards/due
```

Returns all cards whose `nextReview` timestamp is in the past.

### Update a card

```
PUT /cards/{id}
Content-Type: application/json

{
  "front": "Updated question",
  "back": "Updated answer"
}
```

**Response** `200 OK` — returns the updated card.

### Delete a card

```
DELETE /cards/{id}
```

**Response** `204 No Content`

### Review a card

```
POST /cards/{id}/review?quality=4
```

Submits a review with a quality score (0-5). The algorithm recalculates the card's next review date.

| Score | Meaning            |
|-------|--------------------|
| 0     | Complete blackout  |
| 1     | Wrong, remembered after seeing answer |
| 2     | Wrong, but felt close |
| 3     | Correct with difficulty |
| 4     | Correct with hesitation |
| 5     | Perfect response   |

## Architecture

```
controller/        REST endpoints (DTOs in, DTOs out)
  └─ CardController

dto/               Request/response records + MapStruct mapper
  ├─ CardCreateRequest   (Java record)
  ├─ CardUpdateRequest   (Java record)
  ├─ CardResponse        (Java record)
  ├─ PageResponse        (generic pagination wrapper)
  └─ CardMapper          (MapStruct interface)

service/           Business logic
  ├─ CardService         Card CRUD
  └─ ReviewService       Delegates to the active algorithm

algorithm/         Strategy pattern + conditional config
  ├─ SpacedRepetitionAlgorithm   (interface)
  ├─ SM2Algorithm
  ├─ FSRSAlgorithm
  └─ AlgorithmConfig             (selects bean via srs.algorithm property)

entity/            JPA entities
  └─ Card

config/            Application setup
  ├─ WebConfig           CORS configuration (WebMvcConfigurer)
  ├─ CorsProperties      Binds srs.cors.allowed-origins
  └─ DevDataSeeder       Seeds demo cards in dev profile
```

The algorithm layer uses the **Strategy pattern**: `ReviewService` depends on the `SpacedRepetitionAlgorithm`
interface, and `AlgorithmConfig` conditionally registers the chosen implementation as a Spring bean.

## Algorithms

### SM-2

The SuperMemo 2 algorithm (Piotr Wozniak, 1987) tracks three values per card:

- **Easiness Factor** (starts at 2.5) — how easy the card is to recall
- **Interval** — days until the next review
- **Repetitions** — consecutive correct recalls

On correct response (quality >= 3): interval grows via `interval * easinessFactor`.
On incorrect response (quality < 3): repetitions and interval reset to the beginning.

### FSRS (Free Spaced Repetition Scheduler)

FSRS v4 models memory with two parameters:

- **Stability (S)** — the interval in days at which recall probability is 90%
- **Difficulty (D)** — how hard the card is to learn (range 1-10)

Uses a power-law forgetting curve `R(t) = (1 + t/(9S))^(-0.5)` and 15 optimized weights
to calculate the next review interval.

## Profiles

| Profile | Behavior |
|---------|----------|
| `dev` (default) | Loads `.env` file, enables SQL logging, seeds demo cards on startup |
| `prod` | Expects env vars from deployment environment, disables open-in-view |

## Database

Migrations are managed by Flyway and located in `src/main/resources/db/migration/`.

| Migration | Description |
|---|---|
| V1 | Create `cards` table with SM-2 and FSRS fields |
| V2 | Add timezone support (`TIMESTAMPTZ`), defaults, NOT NULL constraints, index on `next_review` |
