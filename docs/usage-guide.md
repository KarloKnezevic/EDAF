# Usage Guide

This guide is a practical cookbook for common EDAF workflows.

## 1) Build and Validate Environment

```bash
mvn -q clean test
./edaf --help
```

## 2) Run Recipes

### Recipe A: Discrete baseline (UMDA + OneMax)

```bash
./edaf run -c configs/umda-onemax-v3.yml
```

### Recipe B: Continuous baseline with checkpoints

```bash
./edaf run -c configs/gaussian-sphere-v3.yml
```

Resume:

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

## 3) Configuration Validation Recipes

```bash
./edaf config validate configs/umda-onemax-v3.yml
./edaf config validate configs/gaussian-sphere-v3.yml
./edaf config validate configs/ehm-tsp-v3.yml
./edaf config validate configs/batch-v3.yml
```

## 4) Plugin Discovery Recipes

```bash
./edaf list algorithms
./edaf list models
./edaf list problems
```

## 5) Reporting Recipes

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

## 6) Web Dashboard Recipes

Run locally on SQLite:

```bash
EDAF_DB_URL=jdbc:sqlite:edaf-v3.db mvn -q -pl edaf-web -am spring-boot:run
```

Open:

- [http://localhost:7070](http://localhost:7070)

## 7) API Query Recipes

### Run list with pagination

```bash
curl "http://localhost:7070/api/runs?page=0&size=25&sortBy=start_time&sortDir=desc"
```

### Filter by algorithm/problem/status

```bash
curl "http://localhost:7070/api/runs?algorithm=umda&problem=onemax&status=COMPLETED"
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

### Filter facets

```bash
curl "http://localhost:7070/api/facets"
```

## 8) Docker Recipes

Start everything:

```bash
docker compose up --build
```

Tail web logs:

```bash
docker compose logs -f web
```

Shutdown:

```bash
docker compose down
```

Destroy DB volume:

```bash
docker compose down -v
```

## 9) Artifact Verification Recipes

Check generated files:

```bash
ls -la results
ls -la reports
```

Inspect SQLite quickly:

```bash
sqlite3 edaf-v3.db "SELECT run_id,status,best_fitness,start_time FROM runs ORDER BY start_time DESC LIMIT 10;"
```

Inspect flattened params:

```bash
sqlite3 edaf-v3.db "SELECT param_path,value_type,COALESCE(value_text,value_json) FROM experiment_params LIMIT 20;"
```

## 10) Common Pitfalls

- DB sink configured but `persistence.database.enabled` is false.
- Incompatible representation/model/algorithm families.
- Running web against a different DB than runner writes to.
- Missing checkpoints when `checkpointEveryIterations` is `0`.

## 11) Recommended Workflow for New Experiments

1. duplicate a nearest config in `configs/`
2. adjust run id/name and seeds
3. run `edaf config validate`
4. run experiment with `edaf run`
5. inspect web/API and generate report
6. version-control config and report metadata
