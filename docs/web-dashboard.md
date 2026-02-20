# Web Dashboard and API

`edaf-web` is a lightweight monitoring and analysis UI built with Spring Boot + Thymeleaf + vanilla JavaScript.

## 1) Startup

From `/Users/karloknezevic/Desktop/EDAF` run one of:

```bash
EDAF_DB_URL="jdbc:sqlite:$(pwd)/edaf-v3.db" mvn -q -pl edaf-web -am spring-boot:run
```

or

```bash
EDAF_DB_URL="jdbc:sqlite:$(pwd)/edaf-v3.db" mvn -q -f edaf-web/pom.xml spring-boot:run
```

Stop the server with `Ctrl+C`.

Open:

- [http://localhost:7070](http://localhost:7070)

Configuration source:

- `/Users/karloknezevic/Desktop/EDAF/edaf-web/src/main/resources/application.yml`

## 2) UI Pages

### `/` Run Explorer

Features:

- full-text search (`q`)
- filters: algorithm, model, problem, status
- ranges: `from`, `to`, `minBest`, `maxBest`
- sorting: `start_time`, `best_fitness`, `runtime_millis`, `status`
- pagination and page size control
- URL query-state persistence
- link to COCO campaign explorer

### `/runs/{runId}` Run Detail

Features:

- run summary cards
- iteration chart (`best`, `mean`, `std`)
- iteration table
- checkpoints table
- event panel with event type + payload text filter
- YAML/JSON config view
- flattened params table with client-side filtering

### `/coco` COCO Campaign Explorer

Features:

- campaign search (`campaign id`, `name`, `notes`)
- filters: status, suite
- sorting and pagination

### `/coco/{campaignId}` COCO Campaign Detail

Features:

- campaign summary cards
- ERT-ratio-by-dimension chart
- optimizer configuration table
- aggregate metrics table
- trial table with filters (`optimizer`, `functionId`, `dimension`, `reachedTarget`)

## 3) REST API Endpoints

### Run endpoints

- `GET /api/runs`
- `GET /api/runs/{runId}`
- `GET /api/runs/{runId}/iterations`
- `GET /api/runs/{runId}/events`
- `GET /api/runs/{runId}/checkpoints`
- `GET /api/runs/{runId}/params`
- `GET /api/facets`

`GET /api/runs` query params:

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
- `sortBy` in `{start_time,best_fitness,runtime_millis,status}`
- `sortDir` in `{asc,desc}`

### COCO endpoints

- `GET /api/coco/campaigns`
- `GET /api/coco/campaigns/{campaignId}`
- `GET /api/coco/campaigns/{campaignId}/optimizers`
- `GET /api/coco/campaigns/{campaignId}/aggregates`
- `GET /api/coco/campaigns/{campaignId}/trials`

`GET /api/coco/campaigns` query params:

- `q`
- `status`
- `suite`
- `page`
- `size`
- `sortBy` in `{created_at,started_at,finished_at,status,name}`
- `sortDir` in `{asc,desc}`

`GET /api/coco/campaigns/{campaignId}/trials` query params:

- `optimizer`
- `functionId`
- `dimension`
- `reachedTarget`
- `page`
- `size`

## 4) API Examples

```bash
curl "http://localhost:7070/api/runs?page=0&size=25&sortBy=start_time&sortDir=desc"
curl "http://localhost:7070/api/runs?algorithm=umda&problem=onemax&status=COMPLETED"
curl "http://localhost:7070/api/runs?q=problem.genotype.maxDepth"
curl "http://localhost:7070/api/runs/umda-onemax-v3/events?eventType=iteration_completed&q=entropy&page=0&size=20"
curl "http://localhost:7070/api/facets"

curl "http://localhost:7070/api/coco/campaigns?page=0&size=20&sortBy=created_at&sortDir=desc"
curl "http://localhost:7070/api/coco/campaigns/coco-bbob-benchmark-v3"
curl "http://localhost:7070/api/coco/campaigns/coco-bbob-benchmark-v3/aggregates"
curl "http://localhost:7070/api/coco/campaigns/coco-bbob-benchmark-v3/trials?optimizer=gaussian-baseline&dimension=10&page=0&size=25"
```

## 5) MVC + Repository Wiring

- `DashboardController` serves Thymeleaf pages with initial server-rendered data.
- `ApiController` serves JSON polling/filter endpoints.
- `RepositoryConfig` wires and initializes:
  - `RunRepository` (`JdbcRunRepository`)
  - `CocoRepository` (`JdbcCocoRepository`)

## 6) Security and Query Safety

Implemented guards:

- prepared statements for all user-provided filters
- `sortBy` whitelist per endpoint
- restricted `sortDir` normalization (`asc|desc`)

This prevents SQL injection in sorting/filtering paths while keeping dynamic search capability.

## 7) Docker

The default `docker-compose.yml` starts web against PostgreSQL:

- `EDAF_DB_URL=jdbc:postgresql://db:5432/edaf`

See `/Users/karloknezevic/Desktop/EDAF/docs/docker.md` for lifecycle commands.
