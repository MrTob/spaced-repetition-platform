# Spaced Repetition Service

A flashcard app that schedules reviews using spaced repetition algorithms (SM-2 and FSRS).
Built with Spring Boot, React, and PostgreSQL.

## Quick Start

The fastest way to run the app — no cloning, no build tools, just Docker.

1. **Download the two required files**

   ```bash
   curl -O https://raw.githubusercontent.com/mrtob/spaced-repetition-service/main/docker-compose.yml
   curl -O https://raw.githubusercontent.com/mrtob/spaced-repetition-service/main/.env.example
   ```

2. **Configure and start**

   ```bash
   cp .env.example .env
   # Edit .env with your preferred database credentials
   docker compose up -d
   ```

That's it. Open `http://localhost` in your browser.

## Docker Compose Variants

| File | Purpose | Command |
|------|---------|---------|
| `docker-compose.yml` | **Quickstart** — pulls pre-built images from GHCR | `docker compose up -d` |
| `docker-compose.build.yml` | **Local build** — builds frontend & backend from source | `docker compose -f docker-compose.build.yml up -d --build` |
| `docker-compose.dev.yml` | **Development** — database only, run apps locally | `docker compose -f docker-compose.dev.yml up -d` |

## Development Setup

Use `docker-compose.dev.yml` to run only the database, then start backend and frontend locally:

1. **Start the database**

   ```bash
   docker compose -f docker-compose.dev.yml up -d
   ```

2. **Start the backend** (dev profile is active by default, seeds 7 demo cards)

   ```bash
   ./mvnw spring-boot:run
   ```

   Or run `SpacedRepetitionServiceApplication` from your IDE.
   The API is available at `http://localhost:8080`.

3. **Start the frontend**

   ```bash
   cd frontend
   bun install
   bun run dev
   ```

   Open `http://localhost:5173`. The Vite dev server proxies `/api` to the backend.

## Tech Stack

**Backend:** Java 21, Spring Boot 4.0.2, Spring Data JPA, PostgreSQL 17, Flyway, MapStruct, Maven, GraalVM Native Image

**Frontend:** React 19, TypeScript, Vite 8, Tailwind CSS 4, shadcn/ui, Framer Motion, SWR, Bun

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
