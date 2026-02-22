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

Latent-knowledge additions are merged into the same numeric map:

- binary examples:
  - `binary_mean_entropy`
  - `binary_fixation_ratio`
  - `drift_binary_prob_l2`
  - `diversity_hamming_population`
- permutation examples:
  - `perm_position_entropy_mean`
  - `drift_consensus_kendall`
  - `diversity_kendall_population`
- real examples:
  - `real_sigma_mean`
  - `drift_gaussian_kl_diag`
  - `diversity_euclidean_population`
- adaptive:
  - `adaptive_event_count`

## 3) Model Diagnostics

`Model.diagnostics()` returns numeric key-value pairs.

Examples by model family:

- Bernoulli UMDA: model entropy, mean probability
- Diagonal Gaussian: sigma min/max, condition number
- Full Gaussian: covariance condition number
- EHM: edge entropy
- advanced models: diagnostics include family-specific metrics

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
- population_size
- elite_size
- best_fitness
- mean_fitness
- std_fitness
- metrics_json
- diagnostics_json
- latent_json
- adaptive_actions_json

### JSONL

File: `results/<run-id>.jsonl` (or configured path)

- one event JSON per line
- suitable for stream ingestion and replay tools

Per-run artifact bundle (`results/.../runs/<runId>/`) includes:

- `telemetry.jsonl` (one row per generation with latent payload)
- `events.jsonl` (all events including `adaptive_action`)
- `metrics.csv` (compact numeric series for quick import)
- `summary.json` (highlights and artifact pointers)
- `report.html` (static charted report)

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

`iterations.diagnostics_json` contains:

- `populationSize`
- `eliteSize`
- `modelDiagnostics`
- `latentTelemetry`
- `adaptiveActions`

## 5) Recommended Analysis Patterns

### Convergence trend

Use `iterations.best_fitness` over iteration index.

### Stability and diversity

Use:

- `std_fitness`
- `metrics_json.diversity`
- `metrics_json.entropy`
- `metrics_json.drift_*`
- `metrics_json.diversity_*`
- `diagnostics_json.latentTelemetry.metrics`

### Failure diagnosis

Use:

- `runs.status`
- `runs.error_message`
- tail of `events.payload_json`

### Adaptive behavior diagnosis

Use:

- `events.event_type = 'adaptive_action'`
- `events.payload_json` (`trigger`, `actionType`, `reason`, `details`)
- `iterations.diagnostics_json.adaptiveActions`
- `metrics_json.adaptive_event_count`

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

Adaptive events for one run:

```sql
SELECT created_at, event_type, payload_json
FROM events
WHERE run_id = 'latent-adaptive-showcase-onemax'
  AND event_type = 'adaptive_action'
ORDER BY created_at;
```

Extract latent family and fixation ratio from diagnostics:

```sql
SELECT
  iteration,
  json_extract(diagnostics_json, '$.latentTelemetry.representationFamily') AS family,
  json_extract(diagnostics_json, '$.latentTelemetry.metrics.binary_fixation_ratio') AS fixation_ratio
FROM iterations
WHERE run_id = 'latent-adaptive-showcase-onemax'
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

- Advanced model families expose concrete diagnostics (dependency, covariance, entropy, gradient and rank-dependence metrics).
- `run_objectives` currently stores final scalar-like metrics from the latest iteration payload.
- Some heavy latent computations (for example dependency edges) are configurable; disable or reduce top-K/dimension caps for very high-dimensional runs.

## 10) Signal Interpretation Quick Guide

- rising `binary_fixation_ratio` very early:
  - model is committing quickly; consider enabling adaptive exploration boost
- near-zero `diversity_*` + flat best fitness:
  - likely stagnation; partial restart threshold is too weak or disabled
- repeated high `drift_*` spikes:
  - model update is unstable; consider larger elite, smoothing, or reduced learning pressure
- real family low `real_sigma_mean` too early:
  - premature collapse in sampling distribution; use sigma/adaptive thresholds

For full metric semantics and YAML controls:
- [Latent Insights and Adaptive Control](./latent-insights.md)
