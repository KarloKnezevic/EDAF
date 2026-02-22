# Web Dashboard and API

`edaf-web` is a lightweight monitoring and analysis UI built with Spring Boot + Thymeleaf + vanilla JavaScript.

## 1) Startup

From `/Users/karloknezevic/Desktop/EDAF` run:

```bash
EDAF_DB_URL="jdbc:sqlite:$(pwd)/edaf-v3.db" mvn -q -pl edaf-web -am spring-boot:run
```

If Maven cannot resolve `spring-boot` prefix, use fully-qualified goal:

```bash
EDAF_DB_URL="jdbc:sqlite:$(pwd)/edaf-v3.db" mvn -q -pl edaf-web -am org.springframework.boot:spring-boot-maven-plugin:run
```

Use `-pl edaf-web -am` from repo root to ensure sibling modules are on classpath.

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
- tabs for:
  - fitness (`best`, `mean`, `std`)
  - diversity
  - drift
  - representation insights
  - iterations/checkpoints
  - events
  - configuration
- representation-specific insights:
  - binary: entropy heatmap, probability trajectories, fixation curve, dependency edges
  - permutation: item-position heatmap, consensus drift, adjacency trends
  - real: sigma heatmap, mean trajectories, eigen summary
- collapsible iteration/checkpoint section
- collapsible events section with large payload preview + expand view
- collapsible configuration section (YAML/JSON + flattened params)
- responsive layout with overflow-safe containers
- adaptive timeline table from `adaptive_action` events

### `/experiments` Experiment Explorer

Features:

- one row per experiment (`experiment_id`) with grouped run counters
- search over experiment ids, run ids, config hash, and flattened params
- filters: algorithm/model/problem/date range
- sorting + pagination for large benchmark campaigns

### `/experiments/{experimentId}` Experiment Detail

Features:

- experiment metadata and run counters
- run table for all repetitions in one experiment
- run-level box-plot and histogram (best fitness distribution)
- data profile and performance profile charts
- success rate, ERT, SP1 summary
- cross-algorithm same-problem section:
  - Wilcoxon pairwise tests
  - Holm correction
  - Friedman omnibus ranking
- one-click LaTeX export buttons
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

- `GET /api/experiments`
- `GET /api/runs`
- `GET /api/runs/{runId}`
- `GET /api/runs/{runId}/iterations`
- `GET /api/runs/{runId}/events`
- `GET /api/runs/{runId}/checkpoints`
- `GET /api/runs/{runId}/params`
- `GET /api/facets`
- `GET /api/experiments/{experimentId}`
- `GET /api/experiments/{experimentId}/runs`
- `GET /api/experiments/{experimentId}/analysis`
- `GET /api/experiments/{experimentId}/latex`
- `GET /api/analysis/problem/{problemType}`
- `GET /api/analysis/problem/{problemType}/latex`

Analysis query params:

- `direction` in `{min,max}`
- `target` (optional success threshold)
- `algorithm` (repeatable; optional subset for problem comparison)

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

`GET /api/runs/{runId}/events` query params:

- `eventType` (for example `adaptive_action`)
- `q` (payload text search)
- `page`
- `size`

`GET /api/experiments` query params:

- `q`
- `algorithm`
- `model`
- `problem`
- `from`
- `to`
- `page`
- `size`
- `sortBy` in `{created_at,total_runs,best_fitness,algorithm_type,model_type,problem_type}`
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
curl "http://localhost:7070/api/runs/latent-adaptive-showcase-onemax/events?eventType=adaptive_action&page=0&size=20"
curl "http://localhost:7070/api/facets"
curl "http://localhost:7070/api/experiments/<experimentId>/analysis?direction=max&target=60"
curl "http://localhost:7070/api/analysis/problem/onemax?direction=max&target=60"
curl "http://localhost:7070/api/analysis/problem/onemax/latex?direction=max&target=60"

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

## 8) Latent Insights Workflow in UI

1. Run one of configs from `configs/latent-insights/`.
2. Open `/runs/<runId>`.
3. In `Insights` tab:
   - verify family-specific charts are rendered.
4. In `Events` tab:
   - filter `eventType=adaptive_action` to inspect triggers and actions.
5. In `Configuration` tab:
   - inspect flattened params and validate threshold values used in run.
6. Cross-check with static report:
   - `results/.../runs/<runId>/report.html`.

## 9) Docker

The default `docker-compose.yml` starts web against PostgreSQL:

- `EDAF_DB_URL=jdbc:postgresql://db:5432/edaf`

See `/Users/karloknezevic/Desktop/EDAF/docs/docker.md` for lifecycle commands.
