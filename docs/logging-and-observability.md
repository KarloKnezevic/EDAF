# Logging and Observability

EDAF observability is event-driven. Algorithm lifecycle events are emitted through `EventBus` and consumed by one or more `EventSink` implementations.

## 1) Event Types

Run events are strongly typed records:

- `RunStartedEvent`
- `IterationCompletedEvent`
- `CheckpointSavedEvent`
- `RunResumedEvent`
- `RunCompletedEvent`
- `RunFailedEvent`

Common fields:

- `runId`
- `timestamp`
- `type`

Iteration payload includes:

- iteration index
- evaluation count
- best/mean/std fitness
- metrics map (from metric collectors)
- model diagnostics map

## 2) Event Bus

`EventBus` fan-outs each event to all registered sinks.

Properties:

- thread-safe sink list (`CopyOnWriteArrayList`)
- graceful close semantics (one sink failure does not stop closing others)

## 3) Built-in Metric Collection

Default collector (`DefaultMetricCollector`) emits:

- `best`
- `mean`
- `std`
- `diversity`
- `entropy`
- `evaluations`
- `iteration`

These values are persisted in iteration rows and used by console/web/reporting.

## 4) Sink Types

### Console (`ConsoleUiSink`)

Features:

- colored run banner
- progress bar (`me.tongfei.progressbar`)
- compact per-iteration summaries
- final run summary with artifacts

Behavior controlled by:

- `logging.verbosity`
- `observability.metricsEveryIterations`

### CSV (`CsvMetricsSink`)

Writes one row per iteration with:

- timestamp
- run id
- iteration
- evaluations
- best/mean/std
- serialized metrics JSON
- serialized diagnostics JSON

### JSONL (`JsonLinesEventSink`)

Writes one JSON event per line, suitable for ingestion/stream processing.

### Rotating File (`RotatingFileEventSink`)

Writes structured event lines and rotates when file exceeds configured byte limit.

### JDBC (`JdbcEventSink`)

Persists full run metadata model:

- canonical config YAML/JSON
- flattened config paths
- run lifecycle updates
- per-iteration metrics
- checkpoints
- raw events

## 5) Logging Modes and Verbosity

Supported modes:

- `console`
- `jsonl`
- `file`
- `db`

Supported verbosity:

- `quiet`
- `normal`
- `verbose`
- `debug`

`LoggingConfigurator` adjusts logback levels for:

- root logger
- `com.knezevic.edaf.v3`
- Hikari
- validator/logging internals

## 6) Sample Configuration

```yaml
observability:
  metricsEveryIterations: 5
  emitModelDiagnostics: true

persistence:
  sinks: [console, csv, jsonl, db]
  outputDirectory: ./results
  database:
    enabled: true
    url: jdbc:sqlite:edaf-v3.db

logging:
  modes: [console, jsonl, file, db]
  verbosity: verbose
  jsonlFile: ./results/experiment-events.jsonl
  logFile: ./edaf-v3.log
```

## 7) Operational Patterns

### Reproducible benchmark run

- fixed `masterSeed`
- sinks: `csv`, `jsonl`, `db`
- verbosity: `normal`

### Deep debugging run

- verbosity: `debug`
- sinks: `console`, `jsonl`, `file`, `db`
- low `metricsEveryIterations` (e.g., `1`)

### CI/automation run

- verbosity: `quiet` or `normal`
- sinks: `jsonl`, `db`

## 8) Failure Visibility

On unhandled runtime exceptions, runner publishes `RunFailedEvent`, and DB sink stores:

- `runs.status = FAILED`
- `runs.error_message`

This keeps run failures queryable from web/API/reporting workflows.
