# Web Dashboard and API

`edaf-web` provides a lightweight monitoring and analysis UI powered by Spring Boot + Thymeleaf + vanilla JavaScript.

## 1) Startup

Default local command:

```bash
EDAF_DB_URL=jdbc:sqlite:edaf-v3.db mvn -q -pl edaf-web -am spring-boot:run
```

Open:

- [http://localhost:7070](http://localhost:7070)

Configuration source:

- `edaf-web/src/main/resources/application.yml`

Default datasource URL fallback:

- `jdbc:sqlite:edaf-v3.db`

## 2) UI Pages

### `/` Run Explorer

Features:

- search box
- algorithm/model/problem/status filters
- datetime range filters (`from`, `to`)
- best-fitness range filters (`minBest`, `maxBest`)
- sort controls (`sortBy`, `sortDir`)
- page size and pagination controls
- URL query-state persistence for refresh/shareability

### `/runs/{runId}` Run Detail

Features:

- summary cards (status, algorithm, model, problem, seed, runtime, hash)
- best/mean/std chart across iterations
- iterations table
- checkpoints table
- events panel with event-type and text filtering
- config YAML and pretty JSON view
- flattened params table with client-side search
- periodic refresh polling

## 3) REST API Endpoints

### `GET /api/runs`

Query parameters:

- `q`
- `algorithm`
- `model`
- `problem`
- `status`
- `from`
- `to`
- `minBest`
- `maxBest`
- `page`
- `size`
- `sortBy` (`start_time`, `best_fitness`, `runtime_millis`, `status`)
- `sortDir` (`asc`, `desc`)

Response envelope:

```json
{
  "items": [ ... ],
  "page": 0,
  "size": 25,
  "total": 123,
  "totalPages": 5
}
```

### `GET /api/runs/{runId}`

Returns full run+experiment detail projection.

### `GET /api/runs/{runId}/iterations`

Returns ordered iteration series.

### `GET /api/runs/{runId}/events`

Additional params:

- `eventType` (exact type)
- `q` (search in event type/payload)
- `page`
- `size`

### `GET /api/runs/{runId}/checkpoints`

Returns checkpoint rows ordered by iteration.

### `GET /api/runs/{runId}/params`

Returns flattened experiment params associated with run experiment.

### `GET /api/facets`

Returns distinct values for:

- algorithms
- models
- problems
- statuses

## 4) API Examples

```bash
curl "http://localhost:7070/api/runs?page=0&size=25&sortBy=start_time&sortDir=desc"
curl "http://localhost:7070/api/runs?algorithm=umda&problem=onemax&status=COMPLETED"
curl "http://localhost:7070/api/runs?q=maxDepth&sortBy=best_fitness&sortDir=desc"
curl "http://localhost:7070/api/runs/gp-nested-smoke-v3"
curl "http://localhost:7070/api/runs/gp-nested-smoke-v3/events?eventType=iteration_completed&q=entropy&page=0&size=20"
curl "http://localhost:7070/api/facets"
```

## 5) MVC and Repository Wiring

- `DashboardController` serves Thymeleaf pages with initial data.
- `ApiController` serves JSON endpoints.
- `RepositoryConfig` creates `RunRepository` bean and initializes schema at startup.

Repository implementation:

- `JdbcRunRepository`

## 6) Security Notes

Current app is intended for trusted internal/research deployment.

Implemented query safeguards:

- prepared statements for all filter values
- sort column whitelist
- strict sort direction normalization

If deploying publicly, add authentication/authorization and HTTP hardening.

## 7) Performance Notes

- run list and events are paged
- indexes support filter combinations and time-based queries
- event payloads are stored as JSON text; avoid unbounded payload growth in custom sinks

## 8) Docker Deployment

The default `docker-compose.yml` starts `web` against PostgreSQL at:

- `EDAF_DB_URL=jdbc:postgresql://db:5432/edaf`

See [Docker Guide](./docker.md) for lifecycle commands.
