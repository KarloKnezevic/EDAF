# EDAF Usage Guide

Comprehensive guide to using the Estimation of Distribution Algorithms Framework (EDAF) v2.0. This document covers all algorithms, configuration options, persistence backends, real-time monitoring, reporting, and the live dashboard.

---

## Table of Contents

1. [Prerequisites](#prerequisites)
2. [Building the Project](#building-the-project)
3. [Running an Experiment](#running-an-experiment)
4. [YAML Configuration Reference](#yaml-configuration-reference)
5. [Algorithms](#algorithms)
6. [Problem Classes](#problem-classes)
7. [Genotype Types and Operators](#genotype-types-and-operators)
8. [Selection Methods](#selection-methods)
9. [Persistence](#persistence)
10. [Reporting](#reporting)
11. [Real-Time Dashboard](#real-time-dashboard)
12. [Metrics and Prometheus](#metrics-and-prometheus)
13. [Console Output](#console-output)
14. [Configuration via YAML (output section)](#configuration-via-yaml-output-section)
15. [Generate Configuration Templates](#generate-configuration-templates)
16. [CLI Reference](#cli-reference)
17. [Database Schema](#database-schema)
18. [Programmatic Usage](#programmatic-usage)
19. [Examples Catalog](#examples-catalog)

---

## Prerequisites

- **Java 21** or later (the framework uses virtual threads and pattern matching)
- **Maven 3.9+**

## Building the Project

```bash
mvn clean install
```

This builds all 33 modules and produces a fat JAR at `examples/target/edaf.jar`.

To skip tests during build:

```bash
mvn clean install -DskipTests
```

## Running an Experiment

The simplest way to run an experiment is with the fat JAR and a YAML configuration file:

```bash
java -jar examples/target/edaf.jar examples/config/cem-sphere.yaml
```

This will:
1. Load the YAML configuration
2. Create the algorithm, problem, genotype, and all components via SPI
3. Run the algorithm with a progress bar and per-generation statistics
4. Print the best fitness at completion

---

## YAML Configuration Reference

Every experiment is defined by a YAML configuration file with two required sections (`problem` and `algorithm`) and one optional section (`output`).

### Minimal Configuration

```yaml
schema-version: "2.0"

problem:
  class: com.knezevic.edaf.testing.problems.Sphere
  optimization: min
  genotype:
    type: fp
    length: 10
    l-bound: -5.0
    u-bound: 5.0

algorithm:
  name: cem
  population:
    size: 100
  selection:
    name: tournament
    size: 3
  termination:
    max-generations: 100
```

### Problem Section

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `class` | String | Yes | Fully-qualified Java class implementing `Problem<T>` |
| `optimization` | String | No | `min` or `max` (default depends on problem) |
| `parameters` | Map | No | Problem-specific parameters (e.g., `benchmarkId`, `dimension`) |
| `genotype` | Object | Yes | Genotype configuration (see below) |

### Genotype Section

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `type` | String | Yes | `binary`, `fp`, `integer`, `permutation`, or `tree` |
| `length` | Integer | Yes | Chromosome/gene length |
| `l-bound` | Double | FP only | Lower bound for floating-point genes |
| `u-bound` | Double | FP only | Upper bound for floating-point genes |
| `min-bound` | Integer | Integer only | Minimum value for integer genes |
| `max-bound` | Integer | Integer only | Maximum value for integer genes |
| `max-depth` | Integer | Tree only | Maximum depth for tree genotype |
| `crossing` | Object | No | Crossover operator configuration |
| `mutation` | Object | No | Mutation operator configuration |

### Crossing (Crossover) Configuration

| Field | Type | Description |
|-------|------|-------------|
| `name` | String | Operator name (see [Genotype Types and Operators](#genotype-types-and-operators)) |
| `probability` | Double | Crossover probability (0.0 - 1.0) |
| `distribution-index` | Double | For SBX crossover (FP genotype) |

### Mutation Configuration

| Field | Type | Description |
|-------|------|-------------|
| `name` | String | Operator name (see [Genotype Types and Operators](#genotype-types-and-operators)) |
| `probability` | Double | Mutation probability (0.0 - 1.0) |
| `distribution-index` | Double | For polynomial mutation (FP genotype) |

### Algorithm Section

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `name` | String | Yes | Algorithm identifier (see [Algorithms](#algorithms)) |
| `population.size` | Integer | Yes | Population size |
| `selection.name` | String | Depends | Selection operator name |
| `selection.size` | Integer | No | Tournament size (for tournament selection) |
| `termination.max-generations` | Integer | Yes | Maximum number of generations |
| `elitism` | Integer | No | Number of elite individuals to preserve |
| `parameters` | Map | No | Algorithm-specific parameters |
| `log-frequency` | Integer | No | Log statistics every N generations |
| `log-directory` | String | No | Directory for log output |

---

## Algorithms

EDAF provides 14 algorithms across four categories. Each algorithm is loaded via SPI using its `name` identifier.

### Estimation of Distribution Algorithms (EDAs)

| Name | ID | Description | Genotype |
|------|----|-------------|----------|
| UMDA | `umda` | Univariate Marginal Distribution Algorithm. Builds independent probability model from selected individuals. | Binary, FP |
| PBIL | `pbil` | Population-Based Incremental Learning. Maintains and updates a probability vector. | Binary |
| MIMIC | `mimic` | Mutual-Information-Maximizing Input Clustering. Models pairwise dependencies. | Binary |
| BMDA | `bmda` | Bivariate Marginal Distribution Algorithm. Models bivariate dependencies via a dependency graph. | Binary |
| FDA | `fda` | Factorized Distribution Algorithm. Uses factorized probability distributions. | Binary, FP |
| BOA | `boa` | Bayesian Optimization Algorithm. Builds a Bayesian network to model variable dependencies. | Binary, FP |
| CEM | `cem` | Cross-Entropy Method. Samples from a Gaussian distribution and updates parameters using elite samples. | FP |
| NES | `nes` | Natural Evolution Strategies (Separable NES). Uses natural gradient updates on mean and sigma vectors. | FP |

### Genetic Algorithms (GAs)

| Name | ID | Description | Genotype |
|------|----|-------------|----------|
| GGA | `gga` | Generational Genetic Algorithm. Standard GA with generational replacement. | Binary, FP, Integer |
| EGA | `ega` | Elitist Genetic Algorithm. GA with elitism (preserves best individuals). | Binary, FP, Integer, Permutation |
| LTGA | `ltga` | Linkage Tree Genetic Algorithm. Uses a linkage tree to identify variable dependencies. | Binary |

### Genetic Programming (GP)

| Name | ID | Description | Genotype |
|------|----|-------------|----------|
| GP | `gp` | Tree-based Genetic Programming. Evolves programs represented as expression trees. | Tree |
| CGP | `cgp` | Cartesian Genetic Programming. Evolves programs on a grid-based graph. | Integer |

### Compact Algorithm

| Name | ID | Description | Genotype |
|------|----|-------------|----------|
| CGA | `cga` | Compact Genetic Algorithm. Maintains a probability vector; extremely memory-efficient. | Binary |

### Algorithm-Specific Parameters

#### UMDA
```yaml
parameters:
  ratio: 0.5  # Selection ratio (fraction of population used for model building)
```

#### CEM
```yaml
parameters:
  batchSize: 100        # Number of samples per generation
  eliteFraction: 0.1    # Top fraction used for distribution update
  learningRate: 0.7     # Smoothing factor for parameter updates
```

#### NES
```yaml
# NES uses adaptive learning rates by default:
#   eta_mu = 1.0
#   eta_sigma = (3 + ln(d)) / (5 * sqrt(d))  where d = dimension
# No additional parameters required.
```

#### BOA
```yaml
parameters:
  n_init: 20    # Initial population size
  n_iter: 200   # Number of iterations
```

#### CGP
```yaml
parameters:
  populationSize: 100
  mutationRate: 0.02
  rows: 1
  cols: 20
  levelsBack: 10
  useCrossover: false
  replacementStrategy: GENERATIONAL  # or MU_PLUS_LAMBDA
```

#### GGA
```yaml
elitism: 1  # Number of elite individuals carried over
```

---

## Problem Classes

EDAF includes built-in test problems across several domains.

### Continuous Optimization (FP Genotype)

| Class | Description | Optimization |
|-------|-------------|-------------|
| `com.knezevic.edaf.testing.problems.Sphere` | Sphere function (unimodal, convex) | min |
| `com.knezevic.edaf.testing.problems.Rosenbrock` | Rosenbrock's valley (narrow curved valley) | min |
| `com.knezevic.edaf.testing.problems.Ackley` | Ackley function (multimodal) | min |
| `com.knezevic.edaf.testing.problems.misc.RastriginProblem` | Rastrigin function (highly multimodal) | min |
| `com.knezevic.edaf.testing.bbob.BBOBProblem` | BBOB benchmark suite (parameterized) | min |

### Binary Optimization (Binary Genotype)

| Class | Description | Optimization |
|-------|-------------|-------------|
| `com.knezevic.edaf.testing.problems.MaxOnes` | Maximize the number of 1-bits | max |
| `com.knezevic.edaf.testing.problems.Knapsack01` | 0/1 Knapsack problem | max |
| `com.knezevic.edaf.testing.problems.misc.DeceptiveTrapProblem` | Deceptive trap function | max |
| `com.knezevic.edaf.testing.problems.crypto.BooleanFunctionProblem` | Boolean function optimization (cryptography) | max |

### Combinatorial Optimization (Permutation Genotype)

| Class | Description | Optimization |
|-------|-------------|-------------|
| `com.knezevic.edaf.testing.problems.TSP` | Traveling Salesman Problem | min |
| `com.knezevic.edaf.testing.problems.misc.NQueensProblem` | N-Queens placement problem | max |
| `com.knezevic.edaf.testing.problems.crypto.BooleanFunctionPermutationProblem` | Boolean function (permutation representation) | max |

### Integer Optimization (Integer Genotype)

| Class | Description | Optimization |
|-------|-------------|-------------|
| `com.knezevic.edaf.testing.problems.IntTarget` | Match a target integer vector | min |

### Genetic Programming (Tree Genotype)

| Class | Description | Optimization |
|-------|-------------|-------------|
| `com.knezevic.edaf.testing.problems.gp.SymbolicRegressionProblem` | Symbolic regression (find formula fitting data) | min |
| `com.knezevic.edaf.testing.problems.gp.MultiplexerProblem` | Boolean multiplexer | max |
| `com.knezevic.edaf.testing.problems.gp.SantaFeAntProblem` | Santa Fe Trail (artificial ant) | max |
| `com.knezevic.edaf.testing.problems.gp.IrisClassificationProblem` | Iris dataset classification | max |
| `com.knezevic.edaf.testing.problems.crypto.BooleanFunctionGPProblem` | Boolean function (GP representation) | max |
| `com.knezevic.edaf.algorithm.cgp.problems.CgpSymbolicRegressionProblem` | Symbolic regression (CGP) | min |

---

## Genotype Types and Operators

### Binary Genotype (`type: binary`)

Represents solutions as bit strings.

**Crossover operators:**
| Name | Description |
|------|-------------|
| `one-point` | Single-point crossover |
| `uniform` | Uniform crossover (each bit independently from a parent) |
| `n-point` | N-point crossover |

**Mutation operators:**
| Name | Description |
|------|-------------|
| `simple` | Bit-flip mutation (each bit flipped with given probability) |

**Example:**
```yaml
genotype:
  type: binary
  length: 20
  crossing:
    name: one-point
    probability: 0.8
  mutation:
    name: simple
    probability: 0.05
```

### Floating-Point Genotype (`type: fp`)

Represents solutions as vectors of real numbers within bounds.

**Crossover operators:**
| Name | Description |
|------|-------------|
| `sbx` | Simulated Binary Crossover (requires `distribution-index`) |
| `discrete` | Discrete crossover (each gene randomly from one parent) |
| `arithmetic` | Arithmetic crossover (weighted average of parents) |

**Mutation operators:**
| Name | Description |
|------|-------------|
| `polynomial` | Polynomial mutation (requires `distribution-index`) |

**Example:**
```yaml
genotype:
  type: fp
  length: 10
  l-bound: -5.0
  u-bound: 5.0
  crossing:
    name: sbx
    distribution-index: 20.0
  mutation:
    name: polynomial
    probability: 0.1
    distribution-index: 20.0
```

### Integer Genotype (`type: integer`)

Represents solutions as vectors of integers within bounds.

**Crossover operators:**
| Name | Description |
|------|-------------|
| `one-point` | Single-point crossover |
| `two-point` | Two-point crossover |

**Mutation operators:**
| Name | Description |
|------|-------------|
| `simple` | Random reset mutation (gene replaced with random value in bounds) |

**Example:**
```yaml
genotype:
  type: integer
  length: 20
  min-bound: 0
  max-bound: 100
  crossing:
    name: one-point
  mutation:
    name: simple
    probability: 0.05
```

### Permutation Genotype (`type: permutation`)

Represents solutions as permutations of integers `[0, 1, ..., length-1]`.

**Crossover operators:**
| Name | Description |
|------|-------------|
| `pmx` | Partially Mapped Crossover |
| `ox` | Order Crossover (OX1) |
| `cx` | Cycle Crossover |

**Mutation operators:**
| Name | Description |
|------|-------------|
| `swap` | Swap two random positions |
| `insert` | Remove a gene and insert it at another position |
| `scramble` | Scramble a random sub-sequence |
| `inversion` | Reverse a random sub-sequence |
| `shift` | Shift a random sub-sequence to a new position |

**Example:**
```yaml
genotype:
  type: permutation
  length: 8
  crossing:
    name: ox
    probability: 0.8
  mutation:
    name: insert
    probability: 0.2
```

### Tree Genotype (`type: tree`)

Represents solutions as expression trees (used with GP).

**Configuration:**
```yaml
genotype:
  type: tree
  max-depth: 5
```

Crossover and mutation are handled internally by the GP algorithm.

---

## Selection Methods

| Name | ID | Description |
|------|----|-------------|
| Tournament Selection | `tournament` | Select the best from `size` randomly chosen individuals |
| Simple Tournament | `simple-tournament` | Simplified tournament variant |

**Configuration:**
```yaml
selection:
  name: tournament
  size: 3      # Tournament size (larger = more selection pressure)
```

---

## Persistence

EDAF supports persisting run results to files (JSON/CSV) or databases (SQLite/PostgreSQL). Persistence captures:
- **Run metadata** — algorithm ID, problem class, genotype type, population size, seed, timestamps
- **Per-generation statistics** — best/worst/avg/std fitness, best individual, evaluation duration
- **Run result** — total generations, best fitness, total duration

### File-Based Persistence (JSON)

**Via CLI flags:**
```bash
java -jar examples/target/edaf.jar \
  --output-dir ./results \
  --output-format json \
  examples/config/cem-sphere.yaml
```

This creates one JSON file per run in `./results/`, named `run-<uuid>.json`.

**Via CLI flags (CSV):**
```bash
java -jar examples/target/edaf.jar \
  --output-dir ./results \
  --output-format csv \
  examples/config/cem-sphere.yaml
```

This creates one CSV file per run with one row per generation.

### Database Persistence (SQLite)

**Via CLI flags:**
```bash
java -jar examples/target/edaf.jar \
  --db-url jdbc:sqlite:edaf-results.db \
  examples/config/cem-sphere.yaml
```

The database schema is automatically initialized on first use. Data is stored in two tables: `runs` and `generation_stats`.

### Database Persistence (PostgreSQL)

```bash
java -jar examples/target/edaf.jar \
  --db-url jdbc:postgresql://localhost:5432/edaf \
  --db-user edaf \
  --db-password secret \
  examples/config/cem-sphere.yaml
```

### Multiple Persistence Backends

You can combine multiple persistence backends in a single run. For example, save to both JSON files and a database:

```bash
java -jar examples/target/edaf.jar \
  --output-dir ./results \
  --output-format json \
  --db-url jdbc:sqlite:edaf-results.db \
  examples/config/cem-sphere.yaml
```

### Querying Persisted Data

After a run, you can query the SQLite database directly:

```bash
# List all runs
sqlite3 edaf-results.db "SELECT run_id, algorithm_id, best_fitness, status FROM runs;"

# View generation statistics for a run
sqlite3 edaf-results.db "SELECT generation, best_fitness, avg_fitness FROM generation_stats WHERE run_id = '<run-id>' ORDER BY generation;"

# Compare multiple runs
sqlite3 edaf-results.db "SELECT algorithm_id, best_fitness, total_duration_ms FROM runs WHERE status = 'COMPLETED' ORDER BY best_fitness;"
```

---

## Reporting

EDAF can generate post-run reports in HTML or Markdown format. Reports include run metadata, a convergence chart (SVG for HTML), and a generation statistics table.

### HTML Report

```bash
java -jar examples/target/edaf.jar \
  --report-format html \
  --report-dir ./reports \
  examples/config/cem-sphere.yaml
```

Output: `./reports/report-<uuid>.html` — a self-contained HTML file with inline CSS and SVG convergence chart. Open directly in any browser.

### Markdown Report

```bash
java -jar examples/target/edaf.jar \
  --report-format md \
  --report-dir ./reports \
  examples/config/cem-sphere.yaml
```

Output: `./reports/report-<uuid>.md` — a Markdown file with tables for run metadata and generation statistics.

### Both Formats

```bash
java -jar examples/target/edaf.jar \
  --report-format both \
  --report-dir ./reports \
  examples/config/cem-sphere.yaml
```

---

## Real-Time Dashboard

The real-time dashboard provides a web-based interface for monitoring algorithm execution as it happens. It uses Server-Sent Events (SSE) to push live updates to a Chart.js frontend.

### Starting the Dashboard

**Via CLI flag:**
```bash
java -jar examples/target/edaf.jar \
  --dashboard-port 7070 \
  examples/config/cem-sphere.yaml
```

Then open `http://localhost:7070` in your browser.

### What the Dashboard Shows

The dashboard displays four sections:

1. **Current Run Stats** — Algorithm name, current generation number, best fitness, average fitness
2. **Convergence Chart** — Live-updating line chart plotting best fitness and average fitness over generations
3. **Generation History** — Scrollable table with per-generation statistics (Gen, Best, Worst, Avg, Std)
4. **Connection Status** — Green "Connected" / Red "Disconnected" badge showing SSE connection state

### How It Works

1. The `DashboardServer` starts a Javalin HTTP server on the specified port
2. The frontend connects to `/sse/events` via `EventSource`
3. The `DashboardEventPublisher` pushes three event types:
   - `algorithmStarted` — `{algorithmId}` — resets the UI for a new run
   - `generationCompleted` — `{algorithmId, generation, bestFitness, worstFitness, avgFitness, stdFitness}` — updates chart and table
   - `algorithmTerminated` — `{algorithmId, generation}` — marks the run as complete
4. The chart updates with zero animation delay for real-time feel

### Dashboard + Persistence

You can combine the dashboard with persistence and reporting in a single command:

```bash
java -jar examples/target/edaf.jar \
  --dashboard-port 7070 \
  --db-url jdbc:sqlite:edaf-results.db \
  --output-dir ./results \
  --output-format json \
  --report-format html \
  examples/config/nes-sphere.yaml
```

This gives you: live monitoring, database storage, JSON file output, and an HTML report.

### API Endpoints

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/` | GET | Dashboard HTML page (static files) |
| `/sse/events` | GET (SSE) | Server-Sent Events stream for live updates |
| `/api/health` | GET | Health check (`{"status": "ok"}`) |

---

## Metrics and Prometheus

EDAF integrates with Micrometer for metrics collection and provides a Prometheus scrape endpoint.

### In-Memory Metrics (Micrometer)

```bash
java -jar examples/target/edaf.jar \
  --metrics \
  examples/config/cem-sphere.yaml
```

Enables `SimpleMeterRegistry` for programmatic access to counters, timers, and gauges within the JVM.

### Prometheus Endpoint

```bash
java -jar examples/target/edaf.jar \
  --prometheus-port 9464 \
  examples/config/cem-sphere.yaml
```

This starts an HTTP server at `http://localhost:9464/metrics` that serves metrics in Prometheus text format.

**Note:** `--prometheus-port` implies `--metrics`.

### Available Metrics

| Metric | Type | Description |
|--------|------|-------------|
| `edaf.algorithm.started` | Counter | Number of algorithm starts |
| `edaf.algorithm.terminated` | Counter | Number of algorithm completions |
| `edaf.algorithm.duration` | Timer | Total algorithm run duration |
| `edaf.generation.completed` | Counter | Number of generations completed |
| `edaf.generation.duration` | Timer | Duration per generation |
| `edaf.evaluations.count` | Counter | Total fitness evaluations |
| `edaf.evaluation.duration` | Timer | Duration of fitness evaluations |
| `edaf_fitness_best` | Gauge | Best fitness in current generation |
| `edaf_fitness_worst` | Gauge | Worst fitness in current generation |
| `edaf_fitness_avg` | Gauge | Average fitness in current generation |
| `edaf_fitness_std` | Gauge | Standard deviation of fitness in current generation |

All metrics are tagged with `algorithm=<algorithmId>`.

### Prometheus Configuration

Add to your `prometheus.yml`:

```yaml
scrape_configs:
  - job_name: 'edaf'
    scrape_interval: 5s
    static_configs:
      - targets: ['localhost:9464']
```

### Grafana Dashboard

After connecting Prometheus to Grafana, you can create panels for:

- **Convergence curve** — `edaf_fitness_best` over time
- **Population diversity** — `edaf_fitness_std` over time
- **Throughput** — rate of `edaf.evaluations.count` (evaluations/sec)
- **Generation timing** — `edaf.generation.duration` histogram

---

## Console Output

By default, EDAF shows a progress bar and periodic statistics table during execution:

```
Generations  40% [========>            ] 40/100 gen  Gen 40 | Best: 0.0234 | Avg: 1.4521 | Std: 2.1034
```

Detailed statistics are printed every 10 generations (and at generation 1):

```
+-------+-----------+-----------+-----------+-----------+
|  Gen  |   Best    |   Worst   |    Avg    |    Std    |
+-------+-----------+-----------+-----------+-----------+
|    10 |    0.1234 |   12.5678 |    3.4567 |    2.8901 |
+-------+-----------+-----------+-----------+-----------+
```

At completion, the best fitness is printed:

```
Best fitness: 0.001234
```

### Log Files

EDAF uses Logback for structured logging:

- `edaf.log` — General execution log (configurable via `logback.xml`)
- Results are also logged via the `edaf.results` logger in JSON format (using Logstash encoder)

---

## Configuration via YAML (output section)

Instead of CLI flags, you can configure persistence, reporting, and dashboard directly in the YAML config file using the optional `output` section:

```yaml
schema-version: "2.0"

problem:
  class: com.knezevic.edaf.testing.problems.Sphere
  optimization: min
  genotype:
    type: fp
    length: 10
    l-bound: -5.0
    u-bound: 5.0

algorithm:
  name: cem
  population:
    size: 100
  selection:
    name: tournament
    size: 3
  termination:
    max-generations: 100
  parameters:
    batchSize: 100
    eliteFraction: 0.1
    learningRate: 0.7

output:
  persistence:
    enabled: true
    type: jdbc                              # "jdbc" or "file"
    jdbc:
      url: "jdbc:sqlite:edaf-results.db"
      # user: "edaf"                        # Optional (for PostgreSQL)
      # password: "secret"                  # Optional (for PostgreSQL)
    file:
      directory: "./results"
      format: json                          # "json" or "csv"
  reporting:
    enabled: true
    format: html                            # "html", "md", or "both"
    output-directory: "./reports"
  dashboard:
    enabled: true
    port: 8080
```

**Note:** CLI flags take precedence over YAML configuration. If both are specified, the CLI flag value is used.

---

## Generate Configuration Templates

Use the `generate-config` subcommand to print a YAML configuration template for any algorithm:

```bash
java -jar examples/target/edaf.jar generate-config --algorithm cem
java -jar examples/target/edaf.jar generate-config --algorithm umda
java -jar examples/target/edaf.jar generate-config --algorithm gga
```

Supported algorithm names for template generation: `cga`, `ega`, `gga`, `umda`, `pbil`, `mimic`, `boa`, `ltga`, `bmda`, `gp`.

The generated template includes all configuration sections with default values — edit as needed.

---

## CLI Reference

```
Usage: edaf [-hV] [--metrics] [--dashboard-port=<port>]
            [--db-password=<password>] [--db-url=<url>] [--db-user=<user>]
            [--output-dir=<dir>] [--output-format=<format>]
            [--prometheus-port=<port>] [--report-dir=<dir>]
            [--report-format=<format>] [<configFile>]
```

### Parameters

| Parameter | Description |
|-----------|-------------|
| `<configFile>` | Path to YAML configuration file |

### Options

| Flag | Description | Default |
|------|-------------|---------|
| `--metrics` | Enable Micrometer metrics (SimpleMeterRegistry) | `false` |
| `--prometheus-port <port>` | Expose Prometheus endpoint on given port (implies `--metrics`) | - |
| `--output-dir <dir>` | Directory for file-based result persistence | - |
| `--output-format <format>` | Output format: `json` or `csv` | `json` |
| `--db-url <url>` | JDBC URL for database persistence | - |
| `--db-user <user>` | Database username | - |
| `--db-password <password>` | Database password | - |
| `--report-format <format>` | Report format: `html`, `md`, or `both` | - |
| `--report-dir <dir>` | Output directory for reports | `./reports` |
| `--dashboard-port <port>` | Start real-time dashboard on given port | - |
| `-h, --help` | Show help message | - |
| `-V, --version` | Print version info | - |

### Subcommands

| Command | Description |
|---------|-------------|
| `generate-config --algorithm <name>` | Generate a YAML configuration template |

---

## Database Schema

When using JDBC persistence, the following tables are automatically created:

### `runs` Table

| Column | Type | Description |
|--------|------|-------------|
| `run_id` | VARCHAR(36) PK | Unique run identifier (UUID) |
| `algorithm_id` | VARCHAR(64) | Algorithm name |
| `problem_class` | VARCHAR(256) | Problem class name |
| `genotype_type` | VARCHAR(64) | Genotype type |
| `population_size` | INTEGER | Population size |
| `max_generations` | INTEGER | Max generations configured |
| `config_hash` | VARCHAR(64) | SHA-256 hash of configuration |
| `seed` | BIGINT | Random seed (nullable) |
| `started_at` | TIMESTAMP | Run start time |
| `completed_at` | TIMESTAMP | Run completion time (nullable) |
| `total_generations` | INTEGER | Total generations executed |
| `best_fitness` | DOUBLE | Best fitness achieved |
| `best_individual` | TEXT | Best individual (JSON string) |
| `total_duration_ms` | BIGINT | Total run duration in milliseconds |
| `status` | VARCHAR(16) | `RUNNING` or `COMPLETED` |

### `generation_stats` Table

| Column | Type | Description |
|--------|------|-------------|
| `id` | INTEGER PK | Auto-increment ID |
| `run_id` | VARCHAR(36) FK | References `runs.run_id` |
| `generation` | INTEGER | Generation number |
| `best_fitness` | DOUBLE | Best fitness in generation |
| `worst_fitness` | DOUBLE | Worst fitness in generation |
| `avg_fitness` | DOUBLE | Average fitness in generation |
| `std_fitness` | DOUBLE | Standard deviation of fitness |
| `best_individual` | TEXT | Best individual (JSON string) |
| `eval_duration_nanos` | BIGINT | Evaluation duration in nanoseconds |
| `recorded_at` | TIMESTAMP | Timestamp when recorded |

Unique constraint: `(run_id, generation)`.

---

## Programmatic Usage

You can use EDAF as a library in your own Java 21 project.

### Maven Dependencies

Add the modules you need to your `pom.xml`:

```xml
<dependency>
    <groupId>com.knezevic.edaf</groupId>
    <artifactId>core</artifactId>
    <version>2.0.0-SNAPSHOT</version>
</dependency>
<dependency>
    <groupId>com.knezevic.edaf</groupId>
    <artifactId>configuration</artifactId>
    <version>2.0.0-SNAPSHOT</version>
</dependency>
<dependency>
    <groupId>com.knezevic.edaf</groupId>
    <artifactId>factory</artifactId>
    <version>2.0.0-SNAPSHOT</version>
</dependency>
<!-- Add algorithm modules as needed -->
<dependency>
    <groupId>com.knezevic.edaf</groupId>
    <artifactId>algorithm-cem</artifactId>
    <version>2.0.0-SNAPSHOT</version>
</dependency>
<!-- Add persistence if needed -->
<dependency>
    <groupId>com.knezevic.edaf</groupId>
    <artifactId>persistence-jdbc</artifactId>
    <version>2.0.0-SNAPSHOT</version>
</dependency>
```

### Running an Algorithm Programmatically

```java
import com.knezevic.edaf.configuration.ConfigurationLoader;
import com.knezevic.edaf.configuration.pojos.Configuration;
import com.knezevic.edaf.core.api.*;
import com.knezevic.edaf.core.runtime.*;
import com.knezevic.edaf.factory.SpiBackedComponentFactory;
import com.knezevic.edaf.persistence.api.PersistenceEventPublisher;
import com.knezevic.edaf.persistence.jdbc.*;

import javax.sql.DataSource;
import java.util.Random;

public class MyExperiment {
    public static void main(String[] args) throws Exception {
        // 1. Load configuration
        ConfigurationLoader loader = new ConfigurationLoader();
        Configuration config = loader.load("my-config.yaml");

        // 2. Create components via SPI factory
        var factory = new SpiBackedComponentFactory();
        Random random = new Random(42);  // Fixed seed for reproducibility

        Problem problem = factory.createProblem(config);
        Genotype genotype = factory.createGenotype(config, random);
        Population population = factory.createPopulation(config, genotype);
        Statistics statistics = factory.createStatistics(config, genotype, random);
        Selection selection = factory.createSelection(config, random);
        TerminationCondition termination = factory.createTerminationCondition(config);
        Algorithm algorithm = factory.createAlgorithm(
            config, problem, population, selection, statistics, termination, random);

        // 3. Set up event publishing with persistence
        CompositeEventPublisher composite = new CompositeEventPublisher();
        composite.addPublisher(new NoOpEventPublisher());

        // Add database persistence
        DataSource ds = DataSourceFactory.create("jdbc:sqlite:results.db", null, null);
        SchemaInitializer.initialize(ds);
        composite.addPublisher(
            new PersistenceEventPublisher(new JdbcResultSink(ds)));

        // 4. Create execution context with virtual threads
        var rs = new SplittableRandomSource(42L);
        var executor = java.util.concurrent.Executors.newVirtualThreadPerTaskExecutor();
        var ctx = new ExecutionContext(rs, executor, composite);

        if (algorithm instanceof SupportsExecutionContext s) {
            s.setExecutionContext(ctx);
        }

        // 5. Run
        algorithm.run();
        System.out.println("Best fitness: " + algorithm.getBest().getFitness());

        // 6. Query results
        JdbcResultStore store = new JdbcResultStore(ds);
        var runs = store.listRuns();
        for (var run : runs) {
            System.out.printf("Run %s: %s, best=%.6f%n",
                run.runId(), run.algorithmId(),
                store.getResult(run.runId()).map(r -> r.bestFitness()).orElse(Double.NaN));
        }

        executor.shutdown();
    }
}
```

---

## Examples Catalog

EDAF ships with 60+ example configuration files in `examples/config/`. Here are some representative ones:

### EDA Examples

| Config File | Algorithm | Problem |
|------------|-----------|---------|
| `cem-sphere.yaml` | CEM | Sphere (FP, 10D, minimize) |
| `cem-rosenbrock.yaml` | CEM | Rosenbrock (FP, minimize) |
| `cem-max-ones.yaml` | CEM | MaxOnes (binary, maximize) |
| `nes-sphere.yaml` | NES | Sphere (FP, 10D, minimize) |
| `nes-rosenbrock.yaml` | NES | Rosenbrock (FP, minimize) |
| `umda-max-ones.yaml` | UMDA | MaxOnes (binary) |
| `umda-knapsack.yaml` | UMDA | 0/1 Knapsack (binary) |
| `pbil-max-ones.yaml` | PBIL | MaxOnes (binary) |
| `mimic-max-ones.yaml` | MIMIC | MaxOnes (binary) |
| `bmda-max-ones.yaml` | BMDA | MaxOnes (binary) |
| `boa-sphere.yaml` | BOA | BBOB Sphere (FP) |
| `fda-max-ones.yaml` | FDA | MaxOnes (binary) |

### GA Examples

| Config File | Algorithm | Problem |
|------------|-----------|---------|
| `gga-max-ones.yaml` | GGA | MaxOnes with crossover/mutation |
| `ega-max-ones.yaml` | EGA | MaxOnes with elitism |
| `ltga-max-ones.yaml` | LTGA | MaxOnes |
| `ga-fp-example.yaml` | GGA | BBOB Sphere (FP + SBX + polynomial) |
| `ga-integer-example.yaml` | GGA | MaxOnes (integer genotype demo) |
| `n-queens.yaml` | EGA | N-Queens (permutation + OX + insert) |

### GP Examples

| Config File | Algorithm | Problem |
|------------|-----------|---------|
| `cgp-symbolic-regression.yaml` | CGP | Symbolic regression |
| `cgp-multiplexer.yaml` | CGP | Boolean multiplexer |
| `cgp-parity.yaml` | CGP | Parity function |
| `cgp-boolean-function.yaml` | CGP | Boolean function |

### Combinatorial Examples

| Config File | Problem Type |
|------------|-------------|
| `deceptive-trap.yaml` | Deceptive trap function |
| `boolean-function-nonlinearity.yaml` | Cryptographic Boolean function |
| `umda-knapsack.yaml` | 0/1 Knapsack |

### Full-Featured Example (All Monitoring)

Run CEM on Sphere with all monitoring features enabled:

```bash
java -jar examples/target/edaf.jar \
  --dashboard-port 7070 \
  --prometheus-port 9464 \
  --db-url jdbc:sqlite:edaf-results.db \
  --output-dir ./results \
  --output-format json \
  --report-format both \
  --report-dir ./reports \
  examples/config/cem-sphere.yaml
```

This simultaneously:
- Displays a progress bar and statistics in the console
- Serves a live dashboard at `http://localhost:7070`
- Exposes Prometheus metrics at `http://localhost:9464/metrics`
- Saves generation data to `edaf-results.db`
- Writes a JSON file to `./results/`
- Generates HTML and Markdown reports in `./reports/`
