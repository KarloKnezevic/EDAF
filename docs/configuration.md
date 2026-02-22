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

Advanced batch shape for statistical campaigns (multiple repetitions per experiment):

```yaml
defaultRepetitions: 30
defaultSeedStart: 20270000
experiments:
  - config: umda-onemax-v3.yml
    runIdPrefix: stats-umda-onemax-v3
  - config: gaussian-sphere-v3.yml
    repetitions: 20
    seedStart: 303000
    runIdPrefix: stats-gaussian-sphere-v3
```

Batch entry fields:

- `config` (or `path`): experiment YAML path relative to batch file
- `repetitions`: number of repeated runs for stochastic significance studies
- `seedStart`: starting seed for deterministic repetition stream (`seedStart + repetitionIndex`)
- `runIdPrefix`: base run-id prefix; repetitions are auto-suffixed as `-r01`, `-r02`, ...

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

Common `problem.type` values:

- `onemax`
- `knapsack`
- `maxsat`
- `small-tsp`
- `tsplib-tsp`
- `sphere`
- `rosenbrock`
- `rastrigin`
- `cec2014`
- `zdt`
- `dtlz`
- `nguyen-sr`
- `coco-bbob`
- `disjunct-matrix`
- `resolvable-matrix`
- `almost-disjunct-matrix`
- `boolean-function`
- `boolean-function-permutation`
- `boolean-function-tree`
- `boolean-function-mo`

### Disjunct-matrix family parameters

For `disjunct-matrix`, `resolvable-matrix`, `almost-disjunct-matrix`:

- `m` or `rows` (int): number of matrix rows `M`
- `n` or `columns` (int): number of matrix columns `N`
- `t` (int): disjunctness parameter (`1 <= t < N`)
- `f` (int): RM threshold (`0 <= f < N`), used by `resolvable-matrix`
- `epsilon` (double): ADM threshold in `[0,1]`, used by `almost-disjunct-matrix`

Important:

- representation must be `bitstring` with `length = m * n`
- encoding is column-major (`M` bits per column)

### Boolean-function crypto problem parameters

Shared parameters (`boolean-function*`):

- `n` (int): number of input variables (`truth table size = 2^n`)
- `criteria` (string[]): subset/order of `balancedness`, `nonlinearity`, `algebraic-degree`
- `criterionWeights` (map): scalar aggregation weights for configured criteria

Additional parameters:

- `boolean-function-tree`: `maxDepth` (int), max parser recursion depth
- `boolean-function-mo`: `objectiveWeights` (double[]), scalar projection weights for vector fitness

## 4) `stopping` Section

| Field | Type | Default | Description |
| --- | --- | --- | --- |
| `type` | string | `max-iterations` | stopping strategy (`max-iterations` or `budget-or-target`) |
| `maxIterations` | int >= 1 | `100` | hard iteration cap |
| `maxEvaluations` | long >= 1 (optional) | `null` | hard evaluation cap |
| `targetFitness` | double (optional) | `null` | objective target threshold |

Supported runtime behavior:

- `max-iterations`: stops only on `maxIterations`
- `budget-or-target`: stops when any active criterion is reached (`maxIterations`, `maxEvaluations`, or `targetFitness`)

Target comparison follows objective sense:

- minimization problems: stop when `bestFitness <= targetFitness`
- maximization problems: stop when `bestFitness >= targetFitness`

Example:

```yaml
stopping:
  type: budget-or-target
  maxIterations: 10000
  maxEvaluations: 500000
  targetFitness: 0.0
```

## 5) `observability` Section

| Field | Type | Default | Description |
| --- | --- | --- | --- |
| `metricsEveryIterations` | int >= 1 | `1` | console summary cadence |
| `emitModelDiagnostics` | boolean | `true` | include model diagnostics in iteration events |

## 5.1) Latent Insights and Adaptive Control (Algorithm Params)

Latent telemetry and adaptive behavior are configured via `algorithm` params.

Example:

```yaml
algorithm:
  type: pbil
  populationSize: 320
  selectionRatio: 0.25
  latentTopK: 16
  latentDependencyTopK: 20
  latentPairwiseMaxDimensions: 120
  latentPairSampleLimit: 1200
  latentFixationEpsilon: 0.02
  latentDependencyEnabled: true
  adaptiveEnabled: true
  adaptiveEarlyIterationLimit: 60
  adaptiveBinaryEntropyThreshold: 0.70
  adaptiveBinaryFixationThreshold: 0.08
  adaptiveBinaryEntropyDropThreshold: 0.01
  adaptiveExplorationFraction: 0.45
  adaptiveExplorationNoiseRate: 0.12
  adaptiveStagnationGenerations: 6
  adaptiveBinaryDiversityThreshold: 0.10
  adaptivePartialRestartFraction: 0.30
  adaptiveImprovementEpsilon: 1.0e-10
```

Common latent extraction keys:

| Key | Default | Description |
| --- | --- | --- |
| `latentTopK` | `10` | number of top ranked elements saved in insight lists |
| `latentDependencyTopK` | `16` | number of top dependency edges saved |
| `latentPairwiseMaxDimensions` | `64` | dimension cap for pairwise dependency scan |
| `latentPairSampleLimit` | family-specific fallback | max pair samples used in diversity estimates |
| `latentFixationEpsilon` | `0.02` | binary fixation threshold epsilon |
| `latentDependencyEnabled` | `true` | enable dependency scan for binary family |
| `realSigmaCollapseThreshold` | `1e-3` | sigma below threshold counted as collapsed dimension |
| `realNearIdenticalThreshold` | `1e-7` | near-identical threshold for real vector pair ratio |

Adaptive master toggles:

| Key | Default | Description |
| --- | --- | --- |
| `adaptiveEnabled` | `false` | enables adaptive interventions |
| `adaptiveEarlyIterationLimit` | `40` | early-phase window for collapse checks |
| `adaptiveImprovementEpsilon` | `1e-12` | normalized improvement threshold to reset stagnation |
| `adaptiveExplorationFraction` | `0.35` | sampled-offspring fraction to perturb |
| `adaptiveExplorationNoiseRate` | `0.08` | perturbation probability per gene/value |
| `adaptiveRealNoiseScale` | dynamic fallback (`real_sigma_mean` or `0.05`) | gaussian perturbation scale in real family |
| `adaptivePermutationSwaps` | `2` | swaps per perturbed permutation |
| `adaptiveStagnationGenerations` | `10` | stagnation window before restart check |
| `adaptivePartialRestartFraction` | `0.25` | fraction replaced with random samples on partial restart |

Family-specific trigger thresholds:

| Key | Default | Description |
| --- | --- | --- |
| `adaptiveBinaryEntropyThreshold` | `0.28` | low-entropy collapse threshold |
| `adaptiveBinaryFixationThreshold` | `0.6` | high fixation-ratio threshold |
| `adaptiveBinaryEntropyDropThreshold` | `0.1` | rapid entropy-drop threshold |
| `adaptiveBinaryDiversityThreshold` | `0.08` | low elite Hamming diversity threshold |
| `adaptivePermutationEntropyThreshold` | `0.9` | low position-entropy threshold |
| `adaptivePermutationDiversityThreshold` | `0.18` | low elite Kendall diversity threshold |
| `adaptiveRealSigmaThreshold` | `0.04` | low sigma mean threshold |
| `adaptiveRealDiversityThreshold` | `0.12` | low elite Euclidean diversity threshold |

Generated adaptive actions:

- `entropy_collapse` -> `exploration_boost`
- `stagnation_low_diversity` -> `partial_restart`

For detailed interpretation of each signal and chart mapping, see:
- [Latent Insights and Adaptive Control](./latent-insights.md)

## 6) `persistence` Section

| Field | Type | Default | Description |
| --- | --- | --- | --- |
| `enabled` | boolean | `true` | global persistence toggle |
| `bundleArtifacts` | boolean | `true` | write run artifact bundle (`summary.json`, CSV/JSONL traces, matrix artifacts) |
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
| Discrete | `umda-bernoulli`, `pbil-frequency`, `cga-frequency`, `bmda`, `mimic-chow-liu`, `boa-ebna`, `hboa-network`, `token-categorical` |
| Continuous | `gaussian-diag`, `gaussian-full`, `gmm`, `kde`, `copula-baseline`, `snes`, `xnes`, `cma-es`, `normalizing-flow` |
| Permutation | `ehm`, `plackett-luce`, `mallows` |

### Allowed Algorithm Types by Family

| Family | Algorithm types |
| --- | --- |
| Discrete | `umda`, `pbil`, `cga`, `bmda`, `mimic`, `boa`, `ebna`, `hboa`, `mo-eda-skeleton`, `tree-eda` |
| Continuous | `gaussian-eda`, `full-covariance-eda`, `flow-eda`, `gmm-eda`, `kde-eda`, `copula-eda`, `snes`, `xnes`, `cma-es`, `mo-eda-skeleton` |
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
- `configs/benchmarks/sphere-full-cov-v3.yml`
- `configs/benchmarks/sphere-flow-eda-v3.yml`

### Permutation (EHM + small TSP)

- `configs/ehm-tsp-v3.yml`

### Mixed Representation (Copula baseline pipeline)

- `configs/mixed-toy-v3.yml`

### Boolean-function cryptography suite

- `configs/benchmarks/crypto-boolean-umda-v3.yml`
- `configs/benchmarks/crypto-boolean-permutation-ehm-v3.yml`
- `configs/benchmarks/crypto-boolean-tree-eda-v3.yml`
- `configs/benchmarks/crypto-boolean-mo-v3.yml`
- `configs/benchmarks/onemax-hboa-v3.yml`
- batch: `configs/batch-benchmark-crypto-v3.yml`

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

## 16) COCO Campaign Configuration (`schema: "3.0-coco"`)

COCO campaign files are separate from single-run experiment files and are executed with:

```bash
./edaf coco run -c configs/coco/bbob-campaign-v3.yml
```

Core shape:

```yaml
schema: "3.0-coco"

campaign:
  id: coco-bbob-benchmark-v3
  suite: bbob
  functions: [1, 2, 3, 8, 15]
  dimensions: [2, 5, 10]
  instances: [1, 2]
  repetitions: 2
  maxEvaluationsMultiplier: 2000
  targetFitness: 1.0e-8
  databaseUrl: jdbc:sqlite:edaf-v3.db
  outputDirectory: ./results/coco
  reportDirectory: ./reports/coco
  referenceMode: best-online

optimizers:
  - id: gaussian-baseline
    config: optimizers/gaussian-baseline-v3.yml
```

COCO campaign fields:

| Field | Description |
| --- | --- |
| `campaign.id` | campaign primary key persisted in DB |
| `campaign.suite` | currently `bbob` |
| `campaign.functions` | list of BBOB function IDs (`1..24`) |
| `campaign.dimensions` | list of tested dimensions |
| `campaign.instances` | list of BBOB instances |
| `campaign.repetitions` | repeated trials for each `(optimizer,function,dimension,instance)` |
| `campaign.maxEvaluationsMultiplier` | budget multiplier: `budget = multiplier * dimension` |
| `campaign.targetFitness` | success target used by ERT/success metrics |
| `campaign.referenceMode` | `best-online` or `optimizer:<name>` |
| `optimizers[].config` | path to a standard v3 experiment config template |

See `/Users/karloknezevic/Desktop/EDAF/docs/coco-integration.md` for full details.
