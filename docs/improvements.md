# Implementation Status and Roadmap

This document tracks the current maturity of EDAF v3 components and near-term priorities.

## 1) Current Maturity Snapshot

### Core platform

- status: stable
- areas: config validation, typed contracts, RNG determinism, event bus, policy factory

### Vertical slices

- Discrete baseline (`umda` + `umda-bernoulli` + `onemax`): stable
- Continuous baseline (`gaussian-eda` + `gaussian-diag` + `sphere`): stable
- Permutation baseline (`ehm-eda` + `ehm` + `small-tsp`): stable

### Persistence/Web

- status: stable
- areas: normalized DB schema, flattened params search, filtering/sorting/pagination API, dashboard views

### Reporting

- status: stable baseline
- areas: HTML/LaTeX per-run generation from query repository

### Advanced algorithm/model families

- status: implemented baseline
- includes: BMDA, MIMIC, BOA/EBNA, GMM/KDE/Copula EDAs, NES family, CMA-ES, Mallows, MO skeleton

## 2) Prioritized Next Steps

### Priority 1

- replace probabilistic model baselines with more expressive estimators/samplers
- expand multi-objective pipeline from skeleton to Pareto archive + non-dominated sorting
- enrich local search and adaptive restart policies

### Priority 2

- add cross-run comparative reporting (aggregates, confidence intervals)
- add richer dashboard analytics (multi-run overlays, diagnostics panels)
- increase integration/property test coverage for advanced families

### Priority 3

- add scenario packs and benchmark automation templates
- add export adapters for external analytics stacks
- add richer report themes and publication-ready templates

## 3) Quality Gates

Before promoting an algorithm/model family from baseline to stable:

- deterministic reproducibility test under fixed seed
- constraints invariants and sampling validity tests
- integration test with at least one canonical benchmark
- runtime and memory sanity checks
- diagnostics surface reviewed for usefulness

## 4) Documentation Policy

This docs set describes **current** runtime behavior. As families move from baseline to advanced implementation, update:

- `docs/algorithms.md`
- `docs/configuration.md`
- `README.md`

in the same change set.
