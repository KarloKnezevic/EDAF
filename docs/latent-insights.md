# Latent Insights and Adaptive Control

This document explains the new latent-knowledge layer in EDAF v3:

- what is computed per generation,
- which YAML keys control it,
- where signals are persisted,
- how to read them in web UI and static reports,
- how adaptive controls react to these signals.

Use this together with:

- [Configuration Reference](./configuration.md)
- [Usage Guide](./usage-guide.md)
- [Web Dashboard and API](./web-dashboard.md)
- [Metrics and Results](./metrics-and-results.md)

## 1) What "latent insights" means in EDAF

For every generation, EDAF extracts knowledge from:

- current population,
- selected elite,
- model state (when available).

The output is `latentTelemetry` with:

- `representationFamily` (`binary`, `permutation`, `real`, or `unknown`)
- `metrics` (compact scalar features)
- `insights` (structured payload for heatmaps/tables)
- `drift` (change from previous generation)
- `diversity` (population/elite spread signals)

EDAF stores this on every `iteration_completed` event and uses it for adaptive actions.

## 2) What gets computed by representation family

## Binary (`bitstring`)

- bit marginals `p_i = P(X_i=1)` (estimated from elite)
- per-bit entropy and aggregate entropy stats
- fixation ratio (`p_i < eps` or `p_i > 1-eps`)
- top decided bits and top uncertain bits
- dependency edges (`MI` + correlation) and linkage clusters
- drift vs previous generation:
  - `binary_prob_l1`
  - `binary_prob_l2`
  - `binary_prob_kl`
  - `binary_entropy_delta`
- diversity:
  - `hamming_population`
  - `hamming_elite`

## Permutation (`permutation-vector`)

- item-position distribution `P(pos(item)=k)` (from elite)
- per-item position entropy
- consensus permutation (Borda-like rank aggregation)
- top adjacency edges with trend vs previous generation
- drift:
  - `consensus_kendall`
- diversity:
  - `kendall_population`
  - `kendall_elite`

## Real-valued (`real-vector`, mixed continuous)

- mean vector, sigma vector (from model when exposed, otherwise estimated)
- covariance summary and eigen spectrum
- uncertainty signals:
  - `real_entropy_proxy_mean`
  - `real_differential_entropy`
- drift:
  - `mean_l2`
  - `sigma_l2`
  - `gaussian_kl_diag`
- diversity:
  - `euclidean_population`
  - `euclidean_elite`
  - `near_identical_ratio`
  - `covariance_trace`
- collapse indicators:
  - `real_collapsed_dim_ratio`
  - `collapsedDimensions` list

## 3) YAML keys you can tune now

All keys below are configured in `algorithm` section as typed params.

Example pattern:

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

## Latent extraction controls

| Key | Default | Meaning |
| --- | --- | --- |
| `latentTopK` | `10` | top items to keep in ranked insight lists |
| `latentDependencyTopK` | `16` | top dependency/edge pairs to store |
| `latentPairwiseMaxDimensions` | `64` | max dimensions used in pairwise dependency scan |
| `latentPairSampleLimit` | family-specific fallback | max pair samples for diversity distance estimation |
| `latentFixationEpsilon` | `0.02` | binary fixation threshold epsilon |
| `latentDependencyEnabled` | `true` | enable binary dependency discovery |
| `realSigmaCollapseThreshold` | `1e-3` | sigma below this counts as collapsed dim |
| `realNearIdenticalThreshold` | `1e-7` | euclidean threshold for near-identical sample ratio |

## Adaptive control toggles

| Key | Default | Meaning |
| --- | --- | --- |
| `adaptiveEnabled` | `false` | master switch for adaptive actions |
| `adaptiveEarlyIterationLimit` | `40` | early-phase window for collapse checks |
| `adaptiveImprovementEpsilon` | `1e-12` | minimum normalized improvement to reset stagnation counter |
| `adaptiveExplorationFraction` | `0.35` | fraction of sampled population to perturb on collapse |
| `adaptiveExplorationNoiseRate` | `0.08` | perturbation rate for gene/value edits |
| `adaptiveRealNoiseScale` | dynamic fallback (`real_sigma_mean` or `0.05`) | gaussian noise amplitude for real vectors |
| `adaptivePermutationSwaps` | `2` | swaps applied during permutation perturbation |
| `adaptiveStagnationGenerations` | `10` | stagnation generations before low-diversity restart |
| `adaptivePartialRestartFraction` | `0.25` | fraction replaced by random samples on restart action |

## Family-specific adaptive thresholds

| Key | Default | Meaning |
| --- | --- | --- |
| `adaptiveBinaryEntropyThreshold` | `0.28` | low entropy threshold (binary collapse) |
| `adaptiveBinaryFixationThreshold` | `0.6` | high fixation threshold (binary collapse) |
| `adaptiveBinaryEntropyDropThreshold` | `0.1` | sudden entropy drop threshold |
| `adaptiveBinaryDiversityThreshold` | `0.08` | low hamming elite diversity threshold |
| `adaptivePermutationEntropyThreshold` | `0.9` | low position-entropy threshold |
| `adaptivePermutationDiversityThreshold` | `0.18` | low kendall elite diversity threshold |
| `adaptiveRealSigmaThreshold` | `0.04` | low sigma mean threshold (real collapse) |
| `adaptiveRealDiversityThreshold` | `0.12` | low euclidean elite diversity threshold |

## 4) Adaptive actions that can trigger

EDAF emits adaptive actions as events and applies countermeasures in sampling path.

## `entropy_collapse` -> `exploration_boost`

Triggered in early phase if family-specific collapse criteria fire.

Countermeasures:

- binary: bit perturbation/noise
- real: gaussian value perturbation
- permutation: additional swaps/random mixing

## `stagnation_low_diversity` -> `partial_restart`

Triggered when:

- best fitness has not improved for configured stagnation window, and
- diversity is below family-specific threshold.

Countermeasure:

- replace tail fraction of sampled offspring with fresh random individuals.

## 5) Where to see these insights

## Console

Verbose/debug modes now include:

- `adaptive=<count>` in per-iteration line
- one line per triggered adaptive action:
  - trigger
  - action type
  - reason

## Run artifact directory

`results/.../runs/<runId>/` includes:

- `telemetry.jsonl`: per-generation latent payload and adaptive actions
- `events.jsonl`: all event payloads, including `adaptive_action`
- `metrics.csv`: compact numeric summary including drift/diversity/adaptive count
- `summary.json`: run summary + latent highlights
- `report.html`: static report with representation-specific insight section

## Database (`db` sink)

Per generation:

- `iterations.metrics_json`
- `iterations.diagnostics_json` with:
  - `populationSize`
  - `eliteSize`
  - `modelDiagnostics`
  - `latentTelemetry`
  - `adaptiveActions`

Event stream:

- `events` table stores `adaptive_action` events and full payload.

## Web UI (`/runs/{runId}`)

Tabs now expose:

- Fitness
- Diversity
- Drift
- Insights
  - binary: entropy heatmap, probability trajectories, fixation curve, dependencies
  - permutation: position heatmap, consensus drift, adjacency edges
  - real: sigma heatmap, mean trajectories, eigen spectrum
- Iterations
- Events (adaptive timeline + raw event stream)
- Configuration

## 6) Practical workflow

1. Start with one latent demo config from `configs/latent-insights/`.
2. Run:
   - `./edaf run -c configs/latent-insights/<file>.yml`
3. Open static report:
   - `results/latent-insights/runs/<runId>/report.html`
4. Start web UI and open `/runs/<runId>` to inspect live/interactive charts.
5. Tune thresholds:
   - reduce entropy/fixation thresholds to trigger later,
   - increase for earlier exploration boost,
   - adjust `adaptivePartialRestartFraction` for stronger restarts.
6. Re-run with same `masterSeed` to compare behavior reproducibly.

## 7) Ready-made configs

`configs/latent-insights/` currently provides:

- binary:
  - `binary-onemax-umda-latent.yml`
  - `binary-onemax-pbil-latent.yml`
  - `binary-knapsack-bmda-latent.yml`
- permutation:
  - `permutation-smalltsp-ehm-latent.yml`
  - `permutation-smalltsp-mallows-latent.yml`
  - `permutation-tsplib-ehbsa-latent.yml`
- real:
  - `real-sphere-gaussian-latent.yml`
  - `real-rastrigin-snes-latent.yml`
  - `real-rosenbrock-cma-latent.yml`
- adaptive showcase:
  - `adaptive-showcase-onemax-collapse.yml` (intentionally triggers adaptive events)

