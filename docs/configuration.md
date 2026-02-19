# Configuration Reference

EDAF v3 uses strict YAML configuration with schema marker:

```yaml
schema: "3.0"
```

Unknown keys are rejected. Structural and semantic validation is performed before execution.

## Validate Configuration

```bash
./edaf config validate configs/umda-onemax-v3.yml
./edaf config validate configs/batch-v3.yml
```

## 1) Top-Level Schema

Required top-level sections for experiment configs:

- `schema`
- `run`
- `representation`
- `problem`
- `algorithm`
- `model`
- `selection`
- `replacement`
- `stopping`
- `constraints`
- `localSearch`
- `restart`
- `niching`
- `observability`
- `persistence`
- `reporting`
- `web`
- `logging`

Batch configs have a different shape:

```yaml
experiments:
  - umda-onemax-v3.yml
  - gaussian-sphere-v3.yml
```

## 2) `run` Section

| Field | Type | Default | Description |
| --- | --- | --- | --- |
| `id` | string | generated UUID-based id | run identifier |
| `name` | string | `EDAF v3 run` | human-readable label |
| `masterSeed` | long | `12345` | root reproducibility seed |
| `deterministicStreams` | boolean | `true` | config-level declaration of deterministic stream intent |
| `checkpointEveryIterations` | int >= 0 | `0` | checkpoint cadence; `0` disables |

## 3) Typed Plugin Sections

The following sections are typed plugin sections:

- `representation`
- `problem`
- `algorithm`
- `model`
- `selection`
- `replacement`
- `constraints`
- `localSearch`
- `restart`
- `niching`

Each uses:

```yaml
<section>:
  type: <plugin-type>
  # additional keys become params map
```

Example:

```yaml
representation:
  type: real-vector
  length: 20
  lower: -5.0
  upper: 5.0
```

`type` is consumed by plugin resolution. Remaining fields become a typed section parameter map.

## 4) `stopping` Section

| Field | Type | Default | Description |
| --- | --- | --- | --- |
| `type` | string | `max-iterations` | currently supported stopping strategy |
| `maxIterations` | int >= 1 | `100` | hard iteration cap |
| `targetFitness` | double (optional) | `null` | accepted but currently not used by default stopping policy |

Current policy factory supports `max-iterations` runtime behavior.

## 5) `observability` Section

| Field | Type | Default | Description |
| --- | --- | --- | --- |
| `metricsEveryIterations` | int >= 1 | `1` | console summary cadence |
| `emitModelDiagnostics` | boolean | `true` | include model diagnostics in iteration events |

## 6) `persistence` Section

| Field | Type | Default | Description |
| --- | --- | --- | --- |
| `enabled` | boolean | `true` | global persistence toggle |
| `sinks` | string[] | `[console, csv, jsonl]` | sink selection |
| `outputDirectory` | string | `./results` | output root for file sinks/checkpoints |
| `database.enabled` | boolean | `false` | JDBC sink toggle |
| `database.url` | string | `jdbc:sqlite:edaf-v3.db` | JDBC URL |
| `database.user` | string | `""` | DB user |
| `database.password` | string | `""` | DB password |

Supported sink values:

- `console`
- `csv`
- `jsonl`
- `file`
- `db`

Validation rule: if `db` sink is requested, `database.enabled` must be `true`.

## 7) `reporting` Section

| Field | Type | Default | Description |
| --- | --- | --- | --- |
| `enabled` | boolean | `true` | report generation toggle |
| `formats` | string[] | `[html]` | requested formats |
| `outputDirectory` | string | `./reports` | report output directory |

Current CLI report formats:

- `html`
- `latex`

## 8) `web` Section

| Field | Type | Default | Description |
| --- | --- | --- | --- |
| `enabled` | boolean | `false` | declarative toggle (web runtime is launched separately) |
| `port` | int | `7070` | preferred UI port |
| `pollSeconds` | int >= 1 | `3` | dashboard polling interval |

## 9) `logging` Section

| Field | Type | Default | Description |
| --- | --- | --- | --- |
| `modes` | string[] | `[console]` | logging output modes |
| `verbosity` | enum | `normal` | `quiet`, `normal`, `verbose`, `debug` |
| `jsonlFile` | string | `./results/run-events.jsonl` | JSONL log path |
| `logFile` | string | `./edaf-v3.log` | rotating file log path |

## 10) Family Compatibility Rules

Semantic validator groups components by representation family.

### Representation Families

- Discrete: `bitstring`, `int-vector`, `categorical-vector`, `mixed-discrete-vector`, `variable-length-vector`
- Continuous: `real-vector`, `mixed-real-discrete-vector`
- Permutation: `permutation-vector`

### Allowed Model Types by Family

| Family | Model types |
| --- | --- |
| Discrete | `umda-bernoulli`, `pbil-frequency`, `cga-frequency`, `bmda`, `mimic-chow-liu`, `boa-ebna` |
| Continuous | `gaussian-diag`, `gaussian-full`, `gmm`, `kde`, `copula-baseline`, `snes`, `xnes`, `cma-es` |
| Permutation | `ehm`, `plackett-luce`, `mallows` |

### Allowed Algorithm Types by Family

| Family | Algorithm types |
| --- | --- |
| Discrete | `umda`, `pbil`, `cga`, `bmda`, `mimic`, `boa`, `ebna`, `mo-eda-skeleton` |
| Continuous | `gaussian-eda`, `gmm-eda`, `kde-eda`, `copula-eda`, `snes`, `xnes`, `cma-es`, `mo-eda-skeleton` |
| Permutation | `ehm-eda`, `plackett-luce-eda`, `mallows-eda`, `mo-eda-skeleton` |

## 11) Policy Types Currently Handled by `PolicyFactory`

### Selection

- `truncation`
- `tournament` (parameter: `k`, default `3`)

### Replacement

- `elitist`
- `generational` (mapped to elitist replacement implementation)

### Constraints

- `identity`
- `repair` (mapped to identity handling)
- `rejection` (parameter: `maxRetries`, default `10`)
- `penalty`

### Restart

- `none`
- `stagnation` (parameter: `patience`, default `1000`)

### Niching

- `none`
- `fitness-sharing`

### Local Search

- currently mapped to no-op implementation

## 12) Full Example Configs

### Discrete (UMDA + OneMax)

- `configs/umda-onemax-v3.yml`

### Continuous (Gaussian EDA + Sphere)

- `configs/gaussian-sphere-v3.yml`

### Permutation (EHM + small TSP)

- `configs/ehm-tsp-v3.yml`

### Mixed Representation (Copula baseline pipeline)

- `configs/mixed-toy-v3.yml`

### Docker/PostgreSQL-ready Example

- `configs/docker/umda-onemax-postgres-v3.yml`

## 13) Converted Sample Set

`configs/converted-v3/` contains curated v3 conversions of selected archived experiment scenarios. These are fully runnable as native v3 configs and useful for parameter sweeps and regressions.

## 14) Configuration Error Model

Validation errors provide:

- precise path (`model.type`, `logging.modes`, etc.)
- concrete message
- actionable hint when available

Examples of enforced errors:

- unknown YAML fields
- unsupported schema
- incompatible representation/model combination
- invalid sink names
- requesting `db` sink with database disabled

## 15) Best Practices

- Always set `run.id` explicitly for reproducible artifact naming.
- Keep `masterSeed` fixed when benchmarking algorithm changes.
- For long runs, enable checkpoints and DB sink.
- Use `metricsEveryIterations` to balance console readability and verbosity.
- Keep `logging.modes` and `persistence.sinks` aligned with analysis needs.
