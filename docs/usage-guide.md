# Usage Guide

This guide is a practical cookbook for common EDAF workflows.

## 1) Build and Validate Environment

```bash
cd /Users/karloknezevic/Desktop/EDAF
mvn -q clean test
./edaf --help
```

## 2) Core Run Recipes

### Recipe A: Discrete baseline (UMDA + OneMax)

```bash
./edaf run -c configs/umda-onemax-v3.yml
```

### Recipe B: Continuous baseline with checkpoints

```bash
./edaf run -c configs/gaussian-sphere-v3.yml
```

Resume from checkpoint:

```bash
./edaf resume --checkpoint results/checkpoints/gaussian-sphere-v3-iter-50.ckpt.yaml
```

### Recipe C: Permutation baseline (EHM + small TSP)

```bash
./edaf run -c configs/ehm-tsp-v3.yml
```

### Recipe D: Batch execution

```bash
./edaf batch -c configs/batch-v3.yml
```

### Recipe D2: 30-run statistical batch per experiment

```bash
./edaf batch -c configs/batch-stat-sample-v3.yml
```

This file uses per-entry `repetitions`, `seedStart`, and `runIdPrefix` so one logical experiment produces many runs suitable for statistical tests.

### Recipe E: Full cross-domain benchmark set (non-COCO)

```bash
./edaf batch -c configs/batch-benchmark-core-v3.yml
```

This batch runs and persists:

- OneMax
- Knapsack
- MAX-SAT
- TSPLIB TSP (Berlin52)
- CEC2014
- ZDT
- DTLZ
- Nguyen symbolic regression

### Recipe F: Boolean-function cryptography benchmark set

```bash
./edaf batch -c configs/batch-benchmark-crypto-v3.yml
```

This batch runs and persists:

- truth-table boolean-function optimization
- balanced permutation boolean-function optimization
- token-tree boolean-function optimization
- multi-objective boolean-function baseline

### Recipe G: Significance campaign (same problem, multiple algorithms, 30x runs)

```bash
./edaf batch -c configs/batch-significance-onemax-v3.yml
```

This campaign runs `umda` and `pbil` on `onemax` with identical deterministic seed schedules,
so `/api/analysis/problem/onemax` can compute paired Friedman/Wilcoxon-style statistics.

## 3) COCO/BBOB Recipes

### Build and import fuller reference rows (recommended)

Build importer CSV from official COCO ppdata:

```bash
./scripts/coco/build_reference_from_ppdata.py \
  --functions 1,2,3,8,15 \
  --dimensions 2,5,10,20 \
  --target-label 1e-7 \
  --target-value 1e-7 \
  --out configs/coco/reference/coco-reference-bbob-ppdata-2009-2023-f1-2-3-8-15-d2-5-10-20-t1e-7.csv
```

Import into DB:

```bash
sqlite3 edaf-v3.db "DELETE FROM coco_reference_results WHERE suite='bbob';"
./edaf coco import-reference \
  --csv configs/coco/reference/coco-reference-bbob-ppdata-2009-2023-f1-2-3-8-15-d2-5-10-20-t1e-7.csv \
  --suite bbob \
  --source-url https://numbbo.github.io/ppdata-archive/bbob/ \
  --db-url jdbc:sqlite:edaf-v3.db
```

### Run smoke campaign

```bash
./edaf coco run -c configs/coco/bbob-smoke-v3.yml
```

### Run full campaign

```bash
./edaf coco run -c configs/coco/bbob-campaign-v3.yml
```

### Run larger publishable campaign

```bash
./edaf coco run -c configs/coco/bbob-publishable-v4.yml
```

### Run CMA-ES comparison campaign

```bash
./edaf coco run -c configs/coco/bbob-cma-compare-v3.yml
```

### Rebuild campaign report from DB

```bash
./edaf coco report \
  --campaign-id coco-bbob-publishable-v4 \
  --out reports/coco \
  --db-url jdbc:sqlite:edaf-v3.db
```

## 4) Configuration Validation Recipes

```bash
./edaf config validate configs/umda-onemax-v3.yml
./edaf config validate configs/gaussian-sphere-v3.yml
./edaf config validate configs/ehm-tsp-v3.yml
./edaf config validate configs/batch-v3.yml
```

## 5) Plugin Discovery Recipes

```bash
./edaf list algorithms
./edaf list models
./edaf list problems
```

## 6) Reporting Recipes

SQLite:

```bash
./edaf report --run-id umda-onemax-v3 --out reports --db-url jdbc:sqlite:edaf-v3.db
```

PostgreSQL:

```bash
./edaf report --run-id docker-umda-onemax-v3 --out reports --db-url jdbc:postgresql://localhost:5432/edaf --db-user edaf --db-password edaf
```

Multiple formats:

```bash
./edaf report --run-id umda-onemax-v3 --out reports --formats html,latex
```

## 7) Web Dashboard Recipes

Open a terminal in `/Users/karloknezevic/Desktop/EDAF` and run one of these commands:

```bash
EDAF_DB_URL="jdbc:sqlite:$(pwd)/edaf-v3.db" mvn -q -pl edaf-web -am spring-boot:run
```

or

```bash
EDAF_DB_URL="jdbc:sqlite:$(pwd)/edaf-v3.db" mvn -q -f edaf-web/pom.xml spring-boot:run
```

If plugin prefix resolution fails, use fully-qualified goal:

```bash
EDAF_DB_URL="jdbc:sqlite:$(pwd)/edaf-v3.db" mvn -q -pl edaf-web -am org.springframework.boot:spring-boot-maven-plugin:run
```

Stop web server with `Ctrl+C` in the same terminal.

Open:

- [http://localhost:7070](http://localhost:7070)

UI pages:

- `/` run explorer
- `/experiments` experiment explorer (multi-run grouped view)
- `/runs/{runId}` run detail
- `/experiments/{experimentId}` experiment detail (multi-run analytics)
- `/coco` campaign explorer
- `/coco/{campaignId}` campaign detail

## 8) API Query Recipes

### Run list with pagination

```bash
curl "http://localhost:7070/api/runs?page=0&size=25&sortBy=start_time&sortDir=desc"
```

### Experiment list with pagination

```bash
curl "http://localhost:7070/api/experiments?page=0&size=25&sortBy=created_at&sortDir=desc"
curl "http://localhost:7070/api/experiments?algorithm=umda&problem=onemax&q=maxDepth"
```

### Filter by algorithm/problem/status

```bash
curl "http://localhost:7070/api/runs?algorithm=umda&problem=onemax&status=COMPLETED"
curl "http://localhost:7070/api/runs?problem=knapsack&status=COMPLETED"
curl "http://localhost:7070/api/runs?problem=cec2014&status=COMPLETED"
curl "http://localhost:7070/api/runs?problem=zdt&status=COMPLETED"
curl "http://localhost:7070/api/runs?problem=boolean-function&status=COMPLETED"
```

### Full-text-like search across run and flattened params

```bash
curl "http://localhost:7070/api/runs?q=problem.genotype.maxDepth"
```

### Range filtering

```bash
curl "http://localhost:7070/api/runs?from=2026-02-01T00:00:00Z&to=2026-02-20T00:00:00Z&minBest=20&maxBest=100"
```

### Run detail resources

```bash
curl "http://localhost:7070/api/runs/umda-onemax-v3"
curl "http://localhost:7070/api/runs/umda-onemax-v3/iterations"
curl "http://localhost:7070/api/runs/umda-onemax-v3/checkpoints"
curl "http://localhost:7070/api/runs/umda-onemax-v3/params"
curl "http://localhost:7070/api/runs/umda-onemax-v3/events?eventType=iteration_completed&q=entropy&page=0&size=20"
```

### Experiment-level analytics and LaTeX export

```bash
curl "http://localhost:7070/api/experiments/<experimentId>"
curl "http://localhost:7070/api/experiments/<experimentId>/runs?page=0&size=50&sortBy=start_time&sortDir=desc"
curl "http://localhost:7070/api/experiments/<experimentId>/analysis?direction=max&target=60"
curl "http://localhost:7070/api/experiments/<experimentId>/latex?direction=max&target=60"
```

### Same-problem algorithm significance analysis

```bash
curl "http://localhost:7070/api/analysis/problem/onemax?direction=max&target=60"
curl "http://localhost:7070/api/analysis/problem/onemax/latex?direction=max&target=60"
```

### Advanced algorithm benchmarks

```bash
./edaf run -c configs/benchmarks/onemax-hboa-v3.yml
./edaf run -c configs/benchmarks/sphere-full-cov-v3.yml
./edaf run -c configs/benchmarks/sphere-flow-eda-v3.yml
```

Resume checkpoints:

```bash
./edaf resume --checkpoint results/benchmarks/checkpoints/benchmark-sphere-full-cov-v3-iter-100.ckpt.yaml
./edaf resume --checkpoint results/benchmarks/checkpoints/benchmark-sphere-flow-eda-v3-iter-100.ckpt.yaml
```

For browser workflow:

1. Open one run from `/`.
2. Click `Experiment` in run detail header.
3. On experiment page inspect:
   - box-plot and histogram over all repeated runs
   - data/performance profiles
   - Wilcoxon/Holm pairwise table and Friedman summary.

### COCO campaign resources

```bash
curl "http://localhost:7070/api/coco/campaigns?page=0&size=20"
curl "http://localhost:7070/api/coco/campaigns/coco-bbob-benchmark-v3"
curl "http://localhost:7070/api/coco/campaigns/coco-bbob-benchmark-v3/optimizers"
curl "http://localhost:7070/api/coco/campaigns/coco-bbob-benchmark-v3/aggregates"
curl "http://localhost:7070/api/coco/campaigns/coco-bbob-benchmark-v3/trials?optimizer=gaussian-baseline&dimension=10&page=0&size=25"
```

### Filter facets

```bash
curl "http://localhost:7070/api/facets"
```

## 9) Docker Recipes

Start stack:

```bash
docker compose up --build
```

Run detached:

```bash
docker compose up -d --build
```

Inspect:

```bash
docker compose ps
docker compose logs -f web
docker compose logs -f runner
docker compose logs -f db
```

Stop:

```bash
docker compose stop
docker compose down
```

Destroy DB volume:

```bash
docker compose down -v
```

## 10) Artifact Verification Recipes

Check generated files:

```bash
ls -la results
ls -la reports
```

Inspect SQLite quickly:

```bash
sqlite3 edaf-v3.db "SELECT run_id,status,best_fitness,start_time FROM runs ORDER BY start_time DESC LIMIT 10;"
sqlite3 edaf-v3.db "SELECT campaign_id,status,created_at FROM coco_campaigns ORDER BY created_at DESC LIMIT 10;"
```

Inspect flattened params:

```bash
sqlite3 edaf-v3.db "SELECT param_path,value_type,COALESCE(value_text,value_json) FROM experiment_params LIMIT 20;"
```

Inspect COCO aggregates:

```bash
sqlite3 edaf-v3.db "SELECT campaign_id,optimizer_id,dimension,success_rate,ert_ratio FROM coco_aggregates ORDER BY campaign_id,optimizer_id,dimension;"
```

## 11) Common Pitfalls

- DB sink configured but `persistence.database.enabled` is false.
- Incompatible representation/model/algorithm families.
- Running web against a different DB than runner writes to.
- Missing checkpoints when `checkpointEveryIterations` is `0`.
- Starting web with a relative DB URL (for example `jdbc:sqlite:edaf-v3.db`) can point to the module working directory instead of repo root; prefer `jdbc:sqlite:$(pwd)/edaf-v3.db`.

## 12) Recommended Workflow for New Experiments

1. duplicate nearest config in `configs/`
2. adjust run id/name and seed
3. run `./edaf config validate ...`
4. run experiment with `./edaf run ...`
5. inspect web/API and generate report
6. commit config + report metadata for reproducibility
