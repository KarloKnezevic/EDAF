# Algorithms

This document describes algorithm drivers currently registered in EDAF v3.

## 1) Implementation Status Matrix

| Algorithm type | Family | Driver class | Model expectation | Status |
| --- | --- | --- | --- | --- |
| `umda` | discrete | `UmdaAlgorithm` | `umda-bernoulli` | implemented baseline |
| `pbil` | discrete | `RatioBasedEdaAlgorithm` via plugin | typically `pbil-frequency` | working scaffold |
| `cga` | discrete | `RatioBasedEdaAlgorithm` via plugin | typically `cga-frequency` | working scaffold |
| `bmda` | discrete | `RatioBasedEdaAlgorithm` via plugin | `bmda` | working scaffold |
| `mimic` | discrete | `RatioBasedEdaAlgorithm` via plugin | `mimic-chow-liu` | working scaffold |
| `boa` | discrete | `RatioBasedEdaAlgorithm` via plugin | `boa-ebna` | working baseline |
| `ebna` | discrete | `RatioBasedEdaAlgorithm` via plugin | `boa-ebna` | working scaffold |
| `hboa` | discrete | `HBoaAlgorithm` | `hboa-network` | implemented sparse BN update |
| `gaussian-eda` | continuous | `GaussianDiagEdaAlgorithm` | `gaussian-diag` | implemented baseline |
| `full-covariance-eda` | continuous | `FullCovarianceEdaAlgorithm` | `gaussian-full` | implemented adaptive covariance |
| `flow-eda` | continuous | `FlowEdaAlgorithm` | `normalizing-flow` | implemented nonlinear transport |
| `gmm-eda` | continuous | `RatioBasedEdaAlgorithm` via plugin | `gmm` | working scaffold |
| `kde-eda` | continuous | `RatioBasedEdaAlgorithm` via plugin | `kde` | working scaffold |
| `copula-eda` | continuous | `RatioBasedEdaAlgorithm` via plugin | `copula-baseline` | working scaffold |
| `snes` | continuous | `RatioBasedEdaAlgorithm` via plugin | `snes` | working scaffold |
| `xnes` | continuous | `RatioBasedEdaAlgorithm` via plugin | `xnes` | working scaffold |
| `cma-es` | continuous | `RatioBasedEdaAlgorithm` via plugin | `cma-es` | implemented strategy |
| `ehm-eda` | permutation | `EhmPermutationEdaAlgorithm` | `ehm` | implemented baseline |
| `plackett-luce-eda` | permutation | `RatioBasedEdaAlgorithm` via plugin | `plackett-luce` | working scaffold |
| `mallows-eda` | permutation | `RatioBasedEdaAlgorithm` via plugin | `mallows` | working scaffold |
| `mo-eda-skeleton` | multi-objective | `MoEdaSkeletonAlgorithm` | family-compatible model | skeleton (TODO for Pareto logic) |
| `tree-eda` | structured/tree | `TreeEdaAlgorithm` | `token-categorical` | implemented baseline |

## 2) Shared Driver Pattern

Most algorithms use `AbstractEdaAlgorithm<G>` and follow the same loop:

1. initialize random feasible population
2. select individuals (`SelectionPolicy`)
3. fit model (`Model.fit`)
4. sample offspring (`Model.sample`)
5. enforce constraints + evaluate + local search
6. replace (`ReplacementPolicy`)
7. optional niching and restart
8. emit per-iteration metrics/diagnostics event

`RatioBasedEdaAlgorithm<G>` defines selection size as:

```text
selectionSize = round(populationSize * selectionRatio), clamped to [1, populationSize]
```

## 3) Per-Algorithm Details

### 3.1 UMDA (`umda`)

- Driver: `UmdaAlgorithm`
- Primary use: bitstring + OneMax class of benchmarks
- Typical model: `umda-bernoulli`
- Plugin description: Univariate Marginal Distribution Algorithm

Important parameters:

- `algorithm.populationSize` (int)
- `algorithm.selectionRatio` (double)
- `model.smoothing` (double, passed to Bernoulli model)

Example:

```yaml
algorithm:
  type: umda
  populationSize: 200
  selectionRatio: 0.4
model:
  type: umda-bernoulli
  smoothing: 0.01
```

### 3.2 Gaussian EDA (`gaussian-eda`)

- Driver: `GaussianDiagEdaAlgorithm`
- Primary use: real-vector benchmarks (Sphere, Rosenbrock, Rastrigin)
- Typical model: `gaussian-diag`

Important parameters:

- `algorithm.populationSize`
- `algorithm.selectionRatio`
- `model.minSigma`

### 3.3 EHM EDA (`ehm-eda`)

- Driver: `EhmPermutationEdaAlgorithm`
- Primary use: permutation + `small-tsp`
- Typical model: `ehm`

Important parameters:

- `algorithm.populationSize`
- `algorithm.selectionRatio`
- `model.epsilon`

### 3.4 PBIL (`pbil`)

- Driver: ratio-based scaffold
- Typical model: `pbil-frequency`
- Model implements moving-average update over bit frequencies.

Parameters:

- `algorithm.selectionRatio`
- `model.learningRate`

### 3.5 cGA (`cga`)

- Driver: ratio-based scaffold
- Typical model: `cga-frequency`
- Model maintains probability vector with configurable step updates.

Parameters:

- `algorithm.selectionRatio`
- `model.step`

### 3.6 BMDA (`bmda`)

- Driver: ratio-based scaffold
- Typical model: `bmda`
- Current model uses univariate fallback and exposes diagnostics placeholder for dependency edges.

### 3.7 MIMIC (`mimic`)

- Driver: ratio-based scaffold
- Typical model: `mimic-chow-liu`
- Current model is scaffold with TODO for Chow-Liu structure learning.

### 3.8 BOA / EBNA (`boa`, `ebna`)

- Drivers: ratio-based scaffolds
- Typical model: `boa-ebna`
- Current model is scaffold with TODO for Bayesian network structure learning and ancestral sampling.

### 3.9 hBOA (`hboa`)

- Driver: `HBoaAlgorithm`
- Typical model: `hboa-network`
- Model performs MI-based sparse Bayesian-network learning with one-parent conditionals.

Important parameters:

- `algorithm.selectionRatio`
- `model.smoothing`
- `model.minMutualInformation`
- `model.learningRate`

### 3.10 Full-Covariance EDA (`full-covariance-eda`)

- Driver: `FullCovarianceEdaAlgorithm`
- Typical model: `gaussian-full`
- Model uses empirical covariance with optional EMA learning-rate and diagonal shrinkage.

Important parameters:

- `algorithm.selectionRatio`
- `model.learningRate`
- `model.shrinkage`
- `model.jitter`

### 3.11 Flow EDA (`flow-eda`)

- Driver: `FlowEdaAlgorithm`
- Typical model: `normalizing-flow`
- Model samples from `x = mu + L (z + alpha * tanh(z))`, with skew adaptation from whitened samples.

Important parameters:

- `algorithm.selectionRatio`
- `model.learningRate`
- `model.maxSkew`
- `model.jitter`

### 3.12 GMM-EDA (`gmm-eda`)

- Driver: ratio-based scaffold
- Typical model: `gmm`
- Current model delegates to diagonal Gaussian fallback and reports configured component count.

### 3.13 KDE-EDA (`kde-eda`)

- Driver: ratio-based scaffold
- Typical model: `kde`
- Current model delegates to diagonal Gaussian fallback and reports configured bandwidth.

### 3.14 Copula-EDA (`copula-eda`)

- Driver: ratio-based scaffold
- Typical model: `copula-baseline`
- Current model delegates to full Gaussian fallback with copula diagnostics placeholder.

### 3.15 NES / CMA-ES (`snes`, `xnes`, `cma-es`)

- Drivers: ratio-based scaffolds
- Models: `snes`, `xnes`, `cma-es`
- Current models are scaffolded with fallback samplers and diagnostics placeholders.

### 3.16 Plackett-Luce EDA (`plackett-luce-eda`)

- Driver: ratio-based scaffold
- Typical model: `plackett-luce`
- Model computes item weights from selected rankings and samples permutations sequentially.

### 3.17 Mallows EDA (`mallows-eda`)

- Driver: ratio-based scaffold
- Typical model: `mallows`
- Current model delegates to Plackett-Luce fallback and exposes `mallows_theta` placeholder.

### 3.18 Multi-objective skeleton (`mo-eda-skeleton`)

- Driver: `MoEdaSkeletonAlgorithm`
- Current behavior: scalarized fallback path through shared base algorithm
- TODO in source: Pareto archive, dominance ranking, MO sampling/replacement

### 3.19 Tree EDA (`tree-eda`)

- Driver: `TreeEdaAlgorithm`
- Typical model: `token-categorical`
- Intended pairing: `variable-length-vector` + `nguyen-sr`
- Provides runnable structured-search baseline while keeping the generic EDA runtime contracts.

## 4) Policies That Affect Algorithm Dynamics

Configured independently from algorithm type:

- selection (`truncation`, `tournament`)
- replacement (`elitist`, `generational` -> elitist impl)
- constraints (`identity`, `repair`, `rejection`, `penalty`)
- restart (`none`, `stagnation`)
- niching (`none`, `fitness-sharing`)
- local search (currently no-op)

## 5) Which Combinations to Prefer Today

Recommended stable pipelines:

- discrete benchmark studies: `bitstring` + `onemax` + `umda` + `umda-bernoulli`
- discrete dependency studies: `bitstring` + `onemax` + `hboa` + `hboa-network`
- constrained discrete studies: `bitstring` + `knapsack`/`maxsat` + `umda` + `umda-bernoulli`
- continuous baseline studies: `real-vector` + `sphere` + `gaussian-eda` + `gaussian-diag`
- full-covariance studies: `real-vector` + `sphere` + `full-covariance-eda` + `gaussian-full`
- nonlinear transport studies: `real-vector` + `sphere` + `flow-eda` + `normalizing-flow`
- harder continuous studies: `real-vector` + `cec2014` + `cma-es` + `cma-es`
- permutation baseline studies: `permutation-vector` + `small-tsp` + `ehm-eda` + `ehm`
- TSPLIB studies: `permutation-vector` + `tsplib-tsp` + `ehm-eda` + `ehm`
- tree/structured baseline: `variable-length-vector` + `nguyen-sr` + `tree-eda` + `token-categorical`

For scaffold families, keep clear experiment labels and report that implementation is baseline/scaffold when publishing.

## 6) Discover Algorithms Programmatically

Use CLI:

```bash
./edaf list algorithms
```

Discovery source:

- `edaf-algorithms/src/main/resources/META-INF/services/com.knezevic.edaf.v3.core.plugins.AlgorithmPlugin`
