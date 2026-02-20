# Getting Started

This guide walks through local execution, artifact inspection, reporting, and dashboard usage with the current EDAF v3 stack.

## 1) Prerequisites

- Java 21+
- Maven 3.9+
- Optional: Docker + Docker Compose (for containerized stack)

Verify:

```bash
java -version
mvn -version
docker --version
docker compose version
```

## 2) Build Everything

From repository root:

```bash
mvn -q clean test
```

If you only need CLI packaging quickly:

```bash
mvn -q -pl edaf-cli -am package
```

## 3) Understand the Wrapper

`./edaf` is the primary entrypoint.

- It checks if `edaf-cli/target/edaf-cli.jar` exists.
- If missing or stale relative to source changes, it rebuilds `edaf-cli` and dependencies.
- It then launches the CLI jar.

Usage:

```bash
./edaf --help
```

## 4) Run Your First Experiment

Start with OneMax + UMDA:

```bash
./edaf run -c configs/umda-onemax-v3.yml
```

Expected console behavior:

- banner with run id, algorithm, model, problem, seed
- progress bar by iteration
- periodic summary rows controlled by `observability.metricsEveryIterations`
- final summary (best fitness, best genotype summary, artifacts)

## 5) Inspect Artifacts

Typical outputs:

- `results/<run-id>.csv` (iteration metrics)
- `results/<run-id>.jsonl` (event stream)
- `edaf-v3.log` (rotating structured file log)
- `results/checkpoints/...` (if checkpointing enabled)
- DB rows (if `db` sink enabled)

Example quick checks:

```bash
ls -la results
head -n 5 results/umda-onemax-v3.csv
head -n 5 results/umda-onemax-v3.jsonl
```

## 6) Generate Reports

From persisted DB state:

```bash
./edaf report --run-id umda-onemax-v3 --out reports --db-url jdbc:sqlite:edaf-v3.db
```

Formats:

```bash
./edaf report --run-id umda-onemax-v3 --out reports --formats html,latex
```

## 7) Validate Config Before Running

```bash
./edaf config validate configs/umda-onemax-v3.yml
./edaf config validate configs/batch-v3.yml
```

Validation includes:

- strict YAML field validation (unknown fields rejected)
- bean constraints (required fields, min values)
- semantic compatibility checks (representation/model/algorithm families)

## 8) Run Multiple Experiments via Batch

```bash
./edaf batch -c configs/batch-v3.yml
```

Batch file format:

```yaml
experiments:
  - umda-onemax-v3.yml
  - gaussian-sphere-v3.yml
  - ehm-tsp-v3.yml
```

Paths are resolved relative to the batch file location.

## 9) Resume from Checkpoint

Enable checkpoints in config:

```yaml
run:
  checkpointEveryIterations: 10
```

Then resume:

```bash
./edaf resume --checkpoint results/checkpoints/gaussian-sphere-v3-iter-50.ckpt.yaml
```

Checkpoint payload stores:

- config snapshot
- run/iteration/evaluation metadata
- population
- model state (for supported models)
- RNG snapshot (deterministic replay)

## 10) Start Web Dashboard Locally

By default web uses SQLite DB at `jdbc:sqlite:edaf-v3.db`.
Run this from a terminal in `/Users/karloknezevic/Desktop/EDAF`:

```bash
EDAF_DB_URL="jdbc:sqlite:$(pwd)/edaf-v3.db" mvn -q -pl edaf-web -am spring-boot:run
```

Stop the server with `Ctrl+C` in that terminal.

Open:

- [http://localhost:7070](http://localhost:7070)

Alternative command if Maven prefix resolution fails:

```bash
EDAF_DB_URL="jdbc:sqlite:$(pwd)/edaf-v3.db" mvn -q -f edaf-web/pom.xml spring-boot:run
```

## 11) Run COCO Smoke Campaign

```bash
./edaf coco run -c configs/coco/bbob-smoke-v3.yml
```

Then inspect:

- campaign rows in DB (`coco_campaigns`, `coco_trials`, `coco_aggregates`)
- campaign pages in web UI (`/coco`, `/coco/{campaignId}`)
- generated campaign report in `reports/coco/`

## 12) Run Full Docker Stack

```bash
docker compose up --build
```

This starts:

- PostgreSQL (`db`)
- dashboard (`web`)
- one runner process (`runner`) using `configs/docker/umda-onemax-postgres-v3.yml`

Stop:

```bash
docker compose down
```

Stop and remove volumes:

```bash
docker compose down -v
```

## Next Recommended Reads

- [Architecture](./architecture.md)
- [Configuration Reference](./configuration.md)
- [CLI Reference](./cli-reference.md)
- [Database Schema](./database-schema.md)
- [Web Dashboard and API](./web-dashboard.md)
- [COCO Integration Guide](./coco-integration.md)
