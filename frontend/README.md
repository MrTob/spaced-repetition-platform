# SRS Frontend

React frontend for the Spaced Repetition Service — a flashcard app that schedules reviews using the SM-2 algorithm.

## Tech Stack

- **React 19** with TypeScript
- **Vite 8** (build tool & dev server)
- **Tailwind CSS 4** with **shadcn/ui** (New York style)
- **Framer Motion** (card flip animations)
- **SWR** (data fetching)
- **Bun** (package manager & runtime)

## Development

Make sure the database and backend are running first (see the [root README](../README.md)).

```bash
bun install
bun run dev
```

The dev server starts at `http://localhost:5173` and proxies `/api/*` to `http://localhost:8080`.

## Scripts

| Command | Description |
|---------|-------------|
| `bun run dev` | Start Vite dev server with HMR |
| `bun run build` | Build for production |
| `bun run preview` | Preview production build locally |
| `bun run lint` | Run ESLint |

## Project Structure

```
src/
├── components/
│   ├── ui/                  # shadcn/ui primitives
│   ├── kokonutui/           # Third-party components (card-flip)
│   ├── review-card.tsx      # Flashcard review with 3D flip animation
│   ├── create-card-form.tsx # New card form
│   └── card-table.tsx       # Card management table with inline editing
├── lib/
│   ├── api.ts               # Backend API client
│   └── utils.ts             # Utility functions
├── App.tsx                  # Main application component
├── main.tsx                 # Entry point
└── index.css                # Global styles & Tailwind theme
```

## API

All requests go through `/api`, proxied to the backend in both dev (Vite proxy) and production (Nginx).

| Method | Endpoint                          | Description                  |
|--------|-----------------------------------|------------------------------|
| GET    | `/api/cards/due`                  | Fetch cards due for review   |
| GET    | `/api/cards?page=&size=&sort=`    | Fetch all cards (paginated)  |
| POST   | `/api/cards`                      | Create a new card            |
| PUT    | `/api/cards/{id}`                 | Update a card                |
| DELETE | `/api/cards/{id}`                 | Delete a card                |
| POST   | `/api/cards/{id}/review?quality=` | Review a card (quality 0-5)  |

## Docker

The Dockerfile uses a multi-stage build:

1. **Build stage** (`oven/bun:1`) — installs dependencies and runs `bun run build`
2. **Runtime stage** (`nginx:alpine`) — serves the static bundle and proxies `/api` to the backend

The `API_URL` environment variable (set in `docker-compose.yml`) controls where Nginx forwards API requests.

A pre-built image is available: `ghcr.io/mrtob/srs-frontend:latest`
