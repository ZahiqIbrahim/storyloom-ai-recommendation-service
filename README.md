# Storyloom AI Recommendation Service

An AI-powered book and movie recommendation microservice that uses **Google Gemini 2.5 Flash** to generate personalized suggestions and enriches them with real data from Open Library and TMDB via the Storyloom Catalog Service.

---

## Architecture Overview

This service is part of the **Storyloom** microservices ecosystem. It acts as the intelligent recommendation layer — the client sends a natural language prompt, and this service returns fully populated book or movie objects without the client needing to call any other service.

```
Client
  │
  ▼
POST /recommend/books  or  POST /recommend/movies   (plain text prompt)
  │
  ▼
GeminiController
  │
  ▼
GeminiService
  ├── 1. Sends prompt to Google Gemini 2.5 Flash  ──►  [Gemini API]
  │       └─ Returns JSON array of titles
  │
  └── 2. Sends titles to Catalog Service  ──►  [STORYLOOM-CATALOG-SERVICE]
          └─ Returns enriched Book[] / Movie[] objects
  │
  ▼
Response back to client
```

### Internal Service Dependencies

| Dependency | How it communicates | What it's for |
|---|---|---|
| **Google Gemini API** | REST (via `GeminiClient` Feign client) | Generates AI-powered title recommendations |
| **Storyloom Catalog Service** | REST (via `StoryloomCatalogService` Feign client) | Fetches full book/movie details from Open Library & TMDB |

Both external calls use **Spring Cloud OpenFeign**. The Catalog Service is discovered through **Netflix Eureka** service registry.

---

## Tech Stack

| Technology | Version |
|---|---|
| Java | 21 |
| Spring Boot | 4.1.0 |
| Spring Cloud | 2025.1.2 |
| Spring Cloud OpenFeign | (managed by Spring Cloud) |
| Netflix Eureka Client | (managed by Spring Cloud) |
| Google Gemini 2.5 Flash | (external AI API) |
| Maven | build tool |

---

## Configuration

All configuration lives in `src/main/resources/application.properties`:

```properties
spring.application.name=storyloom-ai-recommendation-service

server.port=8081

gemini.api.key=YOUR_GEMINI_API_KEY_HERE
gemini.api.base-url=https://generativelanguage.googleapis.com
```

### Required Properties

| Property | Description |
|---|---|
| `gemini.api.key` | Your Google Gemini API key (obtainable from [Google AI Studio](https://aistudio.google.com/)) |
| `gemini.api.base-url` | Base URL for the Gemini API (defaults to `https://generativelanguage.googleapis.com`) |

---

## Getting Started

### Prerequisites

- **Java 21** installed
- **Maven** installed
- A valid **Google Gemini API key**
- The **Storyloom Service Registry** (Eureka) must be running
- The **Storyloom Catalog Service** must be running and registered in Eureka

### Running the Service

```bash
# Clone the repository
git clone <repository-url>
cd storyloom-ai-recommendation-service

# Build and run
./mvnw spring-boot:run
```

The service will start on **port 8081** and register itself with Eureka as `STORYLOOM-AI-RECOMMENDATION-SERVICE`.

---

## API Endpoints

This service exposes exactly **two endpoints**, both under the `/recommend` base path.

> **Important:** Both endpoints accept **raw plain text** in the request body — not JSON. Do not wrap your input in quotes or JSON objects.

---

### `POST /recommend/books`

Recommends books based on a natural language description. Returns enriched book data from Open Library.

**Request**

```http
POST /recommend/books HTTP/1.1
Content-Type: text/plain

I loved Dune and Ender's Game, recommend something similar
```

**Success Response (`200 OK`)** — JSON array of Book objects:

```json
[
  {
    "id": 1,
    "title": "Clean Code",
    "authorName": ["Robert C. Martin"],
    "subtitle": "A Handbook of Agile Software Craftsmanship",
    "firstPublishYear": "2008",
    "key": "/works/OL17618370W",
    "cover": "https://covers.openlibrary.org/b/id/8065615-L.jpg"
  }
]
```

**Book Object Fields**

| Field | Type | Description |
|---|---|---|
| `id` | `Long` | Auto-generated database ID |
| `title` | `String` | Book title |
| `authorName` | `List<String>` | List of author names |
| `subtitle` | `String` (nullable) | Book subtitle |
| `firstPublishYear` | `String` | Year of first publication |
| `key` | `String` | Open Library work key (e.g. `/works/OL17618370W`) |
| `cover` | `String` | Full URL to the cover image from Open Library |

**Error Response (`400 Bad Request`)**

```json
{ "Error": "Failed to parse Gemini Book response: ..." }
```

---

### `POST /recommend/movies`

Recommends movies based on a natural language description. Returns enriched movie data from TMDB.

**Request**

```http
POST /recommend/movies HTTP/1.1
Content-Type: text/plain

I want mind-bending sci-fi movies like Inception and Interstellar
```

**Success Response (`200 OK`)** — JSON array of Movie objects:

```json
[
  {
    "id": 1,
    "title": "Interstellar",
    "overview": "In a future where Earth is dying, a team of explorers travel through a wormhole...",
    "posterPath": "https://image.tmdb.org/t/p/w500/gEU2QniE6E77NI6lCU6MxlNBvIx.jpg",
    "releaseDate": "2014-11-05",
    "voteAverage": "8.661"
  }
]
```

**Movie Object Fields**

| Field | Type | Description |
|---|---|---|
| `id` | `Long` | Auto-generated database ID |
| `title` | `String` | Movie title |
| `overview` | `String` | Plot synopsis / summary |
| `posterPath` | `String` (nullable) | Full URL to the movie poster from TMDB (500px width) |
| `releaseDate` | `String` | Release date in `YYYY-MM-DD` format |
| `voteAverage` | `String` | TMDB user rating (out of 10) |

**Error Response (`400 Bad Request`)**

```json
{ "Error": "Failed to parse Gemini movie response: ..." }
```

---

### Quick cURL Tests

```bash
# Recommend books
curl -X POST http://localhost:8081/recommend/books \
  -H "Content-Type: text/plain" \
  -d "I love fantasy books with dragons and magic"

# Recommend movies
curl -X POST http://localhost:8081/recommend/movies \
  -H "Content-Type: text/plain" \
  -d "Funny animated movies like Shrek"
```

---

## Project Structure

```
src/main/java/com/example/storyloom_ai_recommendation_service/
├── StoryloomAiRecommendationServiceApplication.java   # Entry point + @EnableFeignClients
├── controller/
│   └── GeminiController.java                          # REST endpoints (books & movies)
├── service/
│   └── GeminiService.java                             # Business logic + Gemini prompt engineering
└── externalInterfaces/
    ├── GeminiClient.java                              # Feign client → Google Gemini API
    └── StoryloomCatalogService.java                   # Feign client → Catalog Service
```

### Layer Breakdown

| Layer | File | Responsibility |
|---|---|---|
| **Controller** | `GeminiController` | Receives HTTP requests, returns responses, handles error mapping to `400` |
| **Service** | `GeminiService` | Builds Gemini prompts, parses AI response into title lists, calls Catalog Service |
| **Feign Client** | `GeminiClient` | Calls Gemini `generateContent` API with API key as query param |
| **Feign Client** | `StoryloomCatalogService` | Calls Catalog Service endpoints to get full book/movie data |

---

## Important Notes

- **AI responses are non-deterministic.** Gemini may return different titles and counts for the same prompt on each call.
- **Plain text input only.** The `@RequestBody String text` annotation means the entire request body is read as a raw string — do not send JSON-wrapped input.
- **Movie lookups are fault-tolerant.** If a single movie title can't be found by the Catalog Service, it is silently skipped and the remaining results are still returned.
- **Book lookups are not fault-tolerant.** A single failed book lookup will throw an exception and return a `400` for the entire request.
- **This service has no database.** It is stateless — all data comes from Gemini and the Catalog Service on each request.

---

## License

This project is part of the Storyloom platform.
