# Problem Suites (Beyond COCO)

This document describes the built-in optimization suites now supported in EDAF v3 outside COCO campaigns, including recommended configs and extension points.

For deep details on boolean-function cryptographic optimization, see:

- [Boolean Function and Cryptography Suite](./crypto-boolean-problems.md)

## 1) Supported Serious Minimal Set

### Continuous

- `coco-bbob` (campaign workflow in `edaf-coco`)
- `cec2014` (function ids `1..30`, CEC-style deterministic transforms)

### Discrete

- `onemax`
- `knapsack` (0/1 with linear overweight penalty)
- `maxsat` (DIMACS CNF)
- `tsplib-tsp` (TSPLIB `NODE_COORD_SECTION`)
- disjunct-matrix family:
  - `disjunct-matrix` (DM, exact `fit1`)
  - `resolvable-matrix` (RM, exact `fit2`)
  - `almost-disjunct-matrix` (ADM, exact `fit3`)

### Multiobjective

- `zdt` (ids `1,2,3,4,6`)
- `dtlz` (ids `1,2,7`)

### Tree / Structured

- `nguyen-sr` symbolic regression variants `1..8`
- representation uses `variable-length-vector` token tree encoding
- algorithm/model pair: `tree-eda` + `token-categorical`

### Boolean Function / Cryptography

- `boolean-function` (direct truth-table bitstring)
- `boolean-function-permutation` (balanced permutation encoding)
- `boolean-function-tree` (tokenized boolean expression tree)
- `boolean-function-mo` (multi-objective boolean-function variant)
- built-in criteria:
  - `balancedness`
  - `nonlinearity` (Walsh-spectrum based)
  - `algebraic-degree` (ANF/Mobius transform based)

## 2) Package Organization

Problem implementations are grouped by domain:

- `edaf-problems/src/main/java/com/knezevic/edaf/v3/problems/continuous`
- `edaf-problems/src/main/java/com/knezevic/edaf/v3/problems/discrete`
- `edaf-problems/src/main/java/com/knezevic/edaf/v3/problems/discrete/disjunct`
- `edaf-problems/src/main/java/com/knezevic/edaf/v3/problems/multiobjective`
- `edaf-problems/src/main/java/com/knezevic/edaf/v3/problems/permutation`
- `edaf-problems/src/main/java/com/knezevic/edaf/v3/problems/tree`
- `edaf-problems/src/main/java/com/knezevic/edaf/v3/problems/crypto`

Suite-specific helpers:

- `.../continuous/cec/Cec2014Functions.java`
- `.../discrete/maxsat/DimacsCnf.java`
- `.../permutation/tsplib/TsplibInstance.java`
- `.../util/ProblemResourceLoader.java`

Plugin registration is centralized in:

- `edaf-problems/src/main/resources/META-INF/services/com.knezevic.edaf.v3.core.plugins.ProblemPlugin`

## 3) Included Instances and Resources

- MAX-SAT default instance:
  - `edaf-problems/src/main/resources/maxsat/uf20-01.cnf`
- TSPLIB default instance:
  - `edaf-problems/src/main/resources/tsplib/berlin52.tsp`

Use in config:

```yaml
problem:
  type: maxsat
  instance: classpath:maxsat/uf20-01.cnf
```

```yaml
problem:
  type: tsplib-tsp
  instance: classpath:tsplib/berlin52.tsp
```

## 4) Ready-to-Run Benchmark Configs

`configs/benchmarks/` contains one config per suite:

- `cec2014-f10-cma-v3.yml`
- `knapsack-umda-v3.yml`
- `maxsat-umda-v3.yml`
- `tsplib-berlin52-ehm-v3.yml`
- `zdt1-mo-v3.yml`
- `dtlz2-mo-v3.yml`
- `nguyen1-tree-eda-v3.yml`
- `disjunct-matrix-dm-v3.yml`
- `disjunct-matrix-rm-v3.yml`
- `disjunct-matrix-adm-v3.yml`
- `crypto-boolean-umda-v3.yml`
- `crypto-boolean-permutation-ehm-v3.yml`
- `crypto-boolean-tree-eda-v3.yml`
- `crypto-boolean-mo-v3.yml`

Batch config covering the full serious minimal set:

- `configs/batch-benchmark-core-v3.yml`

Batch config for cryptographic boolean-function suite:

- `configs/batch-benchmark-crypto-v3.yml`

Run all in one command:

```bash
./edaf batch -c configs/batch-benchmark-core-v3.yml
./edaf batch -c configs/batch-benchmark-crypto-v3.yml
```

## 5) Multiobjective Observability

`DefaultMetricCollector` now emits:

- `objective_count`
- `best_obj_0`, `best_obj_1`, ...

These are persisted in `run_objectives`, so ZDT/DTLZ objective vectors are queryable from DB and visible in run detail API.

## 6) Result Paths

After batch run:

- per-run CSV/JSONL: `results/benchmarks/`
- per-run HTML reports: `reports/benchmarks/`
- DB summary export:
  - `results/benchmarks/benchmark-core-v3-summary.csv`
  - `results/benchmarks/benchmark-mo-objectives-v3.csv`

## 7) Browser Visibility

Start web app against the repository DB:

```bash
EDAF_DB_URL="jdbc:sqlite:$(pwd)/edaf-v3.db" mvn -q -pl edaf-web -am spring-boot:run
```

Then open:

- [http://localhost:7070](http://localhost:7070)

Use filters:

- `problem=cec2014`
- `problem=knapsack`
- `problem=maxsat`
- `problem=tsplib-tsp`
- `problem=zdt`
- `problem=dtlz`
- `problem=nguyen-sr`
- `problem=disjunct-matrix`
- `problem=resolvable-matrix`
- `problem=almost-disjunct-matrix`
- `problem=boolean-function`
- `problem=boolean-function-permutation`
- `problem=boolean-function-tree`
- `problem=boolean-function-mo`

## 9) Disjunct-Matrix Validation API

EDAF now ships an explicit validator for formal DM/RM/ADM properties with:

- exact enumeration for small `C(N,t)` spaces
- statistically bounded sampling for large spaces
- witness subsets when violations are detected

See dedicated guide:

- [Disjunct Matrix Family (DM/RM/ADM)](./disjunct-matrix-problems.md)

## 8) How to Add a New Problem (clean extension path)

1. Create class implementing `Problem<G>` in domain package.
2. Create `ProblemPlugin<G>` factory in `.../problems/plugins`.
3. Register plugin class in the service file.
4. Add one config example under `configs/benchmarks/`.
5. Add one smoke test in `edaf-problems/src/test/...`.
6. Add one integration slice in `edaf-experiments/src/test/...` (if runnable pipeline exists).
7. Validate with:

```bash
mvn -q -pl edaf-problems,edaf-experiments -am test
./edaf config validate configs/benchmarks/<new-problem>.yml
```

This keeps architecture stable while allowing incremental suite growth.
