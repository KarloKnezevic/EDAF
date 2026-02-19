# Metrics and Results

This document explains what EDAF records, where it records it, and how to interpret it.

## 1) Event-Centric Data Model

Each run emits events. Sinks serialize these events into different storage backends.

Main lifecycle sequence:

1. `run_started`
2. multiple `iteration_completed`
3. optional `checkpoint_saved`
4. optional `run_resumed`
5. `run_completed` or `run_failed`

## 2) Iteration Metrics

Default per-iteration scalar metrics:

- `best`
- `mean`
- `std`
- `diversity`
- `entropy`
- `evaluations`
- `iteration`

These are emitted by `DefaultMetricCollector`.

## 3) Model Diagnostics

`Model.diagnostics()` returns numeric key-value pairs.

Examples by model family:

- Bernoulli UMDA: model entropy, mean probability
- Diagonal Gaussian: sigma min/max, condition number
- Full Gaussian: covariance condition number
- EHM: edge entropy
- scaffold models: placeholder diagnostics signaling TODO fields

## 4) Sink Outputs

### Console

- real-time progress bar
- compact summaries
- final run summary

### CSV

File: `results/<run-id>.csv`

Columns:

- timestamp
- run_id
- iteration
- evaluations
- best_fitness
- mean_fitness
- std_fitness
- metrics_json
- diagnostics_json

### JSONL

File: `results/<run-id>.jsonl` (or configured path)

- one event JSON per line
- suitable for stream ingestion and replay tools

### Rotating File

File: configured `logging.logFile`

- line-oriented structured event records
- automatic size-based rotation

### JDBC

Tables:

- `experiments`
- `experiment_params`
- `runs`
- `run_objectives`
- `iterations`
- `checkpoints`
- `events`

See [Database Schema](./database-schema.md).

## 5) Recommended Analysis Patterns

### Convergence trend

Use `iterations.best_fitness` over iteration index.

### Stability and diversity

Use:

- `std_fitness`
- `metrics_json.diversity`
- `metrics_json.entropy`

### Failure diagnosis

Use:

- `runs.status`
- `runs.error_message`
- tail of `events.payload_json`

### Parameter trace

Use flattened config:

- `experiment_params.param_path`
- `experiment_params.value_*`

## 6) Example Queries

Top runs by best fitness:

```sql
SELECT run_id, status, best_fitness, runtime_millis
FROM runs
ORDER BY best_fitness DESC
LIMIT 20;
```

Iterations for one run:

```sql
SELECT iteration, evaluations, best_fitness, mean_fitness, std_fitness
FROM iterations
WHERE run_id = 'umda-onemax-v3'
ORDER BY iteration;
```

Search configs containing `maxDepth`:

```sql
SELECT experiment_id, param_path, value_text, value_json
FROM experiment_params
WHERE param_path LIKE '%maxDepth%';
```

## 7) Report Generation Flow

`ReportService` query path:

1. fetch `RunSummary` from repository
2. fetch iteration series
3. render requested format generators

Current generators:

- `HtmlReportGenerator`
- `LatexReportGenerator`

## 8) Reproducibility Recommendations

- lock `masterSeed`
- persist DB + JSONL for auditability
- keep config snapshots in DB (`config_yaml`, `config_json`)
- use checkpoints for long-running or interruptible jobs

## 9) Caveats

- Some advanced model families are scaffold implementations and expose diagnostic placeholders.
- `run_objectives` currently stores final scalar-like metrics from the latest iteration payload.
