# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

EDAF (Estimation of Distribution Algorithms Framework) is a Java 21 multi-module Maven project implementing evolutionary and estimation of distribution algorithms. It uses a plugin-based architecture with Java SPI for component discovery, and includes persistence, reporting, and real-time dashboard capabilities.

## Build Commands

```bash
# Build (compile all modules)
mvn clean compile

# Run tests (deterministic seed required for reproducibility)
mvn clean test -Dseed=12345 -Duser.seed=12345

# Run a single module's tests
mvn clean test -pl algorithm-umda -Dseed=12345 -Duser.seed=12345

# Code quality check (Spotless - removes unused imports)
mvn spotless:check

# Auto-fix code quality issues
mvn spotless:apply

# Build executable JAR (output: examples/target/edaf.jar)
mvn clean package -DskipTests

# Run the framework
java -jar examples/target/edaf.jar <config.yaml>

# Run with reproducible seed
java -jar examples/target/edaf.jar --seed 42 <config.yaml>

# Run with file persistence
java -jar examples/target/edaf.jar --output-dir ./results --output-format json <config.yaml>

# Run with SQLite database persistence
java -jar examples/target/edaf.jar --db-url jdbc:sqlite:edaf.db <config.yaml>

# Run with real-time dashboard
java -jar examples/target/edaf.jar --dashboard-port 8080 <config.yaml>

# Run with HTML report generation
java -jar examples/target/edaf.jar --report-format html --report-dir ./reports <config.yaml>

# Run with Prometheus metrics
java -jar examples/target/edaf.jar --prometheus-port 8888 <config.yaml>
```

## Requirements

- Java 21+ (LTS)
- Maven 3.9+

## Module Architecture

The project is organized into 35 Maven modules under a parent POM:

**Core layer:**
- `core` — API interfaces (`Algorithm`, `Problem`, `Individual`, `Population`, `Genotype`, `Selection`, `Crossover`, `Mutation`, `TerminationCondition`, `Statistics`), runtime types (`ExecutionContext`, `EventPublisher`, `CompositeEventPublisher`, `RandomSource`), event classes (`AlgorithmStarted`, `GenerationCompleted`, `EvaluationCompleted`, `AlgorithmTerminated`), and SPI contracts (`AlgorithmProvider`, `GenotypeProvider`, `SelectionProvider`)
- `configuration` — YAML config loading via Jackson, validated with Jakarta Validation / Hibernate Validator. Includes `OutputConfig` for persistence/reporting/dashboard settings.
- `factory` — `SpiBackedComponentFactory` uses SPI to discover and instantiate all components from YAML config

**Algorithm modules** (`algorithm-*`): Each extends `AbstractAlgorithm<T>` (in `core/impl/`) which provides shared best-tracking, event publishing, population evaluation, and `isFirstBetter()` logic. Each implements `AlgorithmProvider` SPI and registers via `META-INF/services/`. Algorithms: GGA, EGA, UMDA, PBIL, MIMIC, BMDA, LTGA, CGA, FDA, CEM, BOA, GP, CGP, NES.

**Genotype modules** (`genotype-*`): Binary, floating-point, integer, permutation, tree, real-valued (with self-adaptive strategy parameters), and categorical representations. Each provides crossover and mutation operators.

**Persistence modules:**
- `persistence-api` — Port interfaces (`ResultSink`, `ResultStore`) and records (`RunMetadata`, `GenerationRecord`, `RunResult`). `PersistenceEventPublisher` bridges core events to sink lifecycle calls.
- `persistence-file` — JSON and CSV file-based persistence. `FileResultSink` delegates to `JsonResultSink` or `CsvResultSink` based on config.
- `persistence-jdbc` — JDBC persistence with HikariCP connection pooling. Supports PostgreSQL and SQLite. `SchemaInitializer` creates tables idempotently.

**Output modules:**
- `reporting` — HTML and Markdown report generators with inline SVG convergence charts. `ReportResultSink` collects data during a run and triggers report generation on completion.
- `dashboard` — Javalin-based web dashboard with SSE for real-time algorithm monitoring. Static frontend with Chart.js for live convergence charts.

**Support modules:**
- `selection` — Tournament and roulette wheel selection (registers `SelectionProvider` via SPI)
- `statistics` — Statistical distribution calculations
- `testing` — Test problems (MaxOnes, Sphere, Rosenbrock, Ackley, Knapsack, TSP, N-Queens, etc.)
- `metrics` — Prometheus and Micrometer event publishers
- `examples` — CLI entry point (`Framework` class using PicoCLI), example YAML configs in `examples/config/`, and real-world examples: `AlgorithmComparisonExample` (multi-algorithm comparison), `TSPExample` (TSP with permutation genotype), `FeatureSelectionExample` (ML feature selection with binary genotype), `HyperparameterTuningExample` (hyperparameter tuning with categorical genotype)

## Event System

All algorithms publish lifecycle events through `CompositeEventPublisher`:
- `AlgorithmStarted` — emitted when `run()` begins
- `EvaluationCompleted` — emitted after each generation's fitness evaluation
- `GenerationCompleted` — emitted after each generation with population statistics
- `AlgorithmTerminated` — emitted when the algorithm finishes

The `CompositeEventPublisher` fans out events to multiple consumers: console, metrics, persistence, reporting, and dashboard.

## Key Patterns

**SPI plugin system:** New algorithms are added by (1) creating a module implementing `AlgorithmProvider`, (2) registering it in `META-INF/services/com.knezevic.edaf.core.spi.AlgorithmProvider`, and (3) adding the module to the parent POM. Same pattern for genotypes and selection operators.

**Configuration-driven:** All experiments are defined in YAML files with sections: `problem`, `algorithm`, and optionally `output` (for persistence/reporting/dashboard). The `SpiBackedComponentFactory` resolves named components to implementations.

**Ports & Adapters for persistence:** `persistence-api` defines write (`ResultSink`) and read (`ResultStore`) port interfaces. Implementations (`persistence-file`, `persistence-jdbc`) are pluggable adapters.

**Package structure:** All code lives under `com.knezevic.edaf.*`. Each module's sources are at `<module>/src/main/java/com/knezevic/edaf/<module-path>/`.

## Testing

- Framework: JUnit 5 + jqwik (property-based testing)
- Tests live in `<module>/src/test/java/`
- Always pass `-Dseed=12345 -Duser.seed=12345` for deterministic results
- jqwik property tests use `*Properties.java` naming (configured in surefire includes)
- Integration tests in `examples` module test end-to-end YAML config → algorithm → persistence pipeline
- JaCoCo coverage: thresholds set at 60% line / 40% branch in parent POM; currently overridden to 0 per-module (tighten as coverage improves). Run `mvn verify` to generate reports.
- CI runs three workflows: build, test, and code-quality (Spotless)

## Code Style

Spotless enforces removal of unused imports. Run `mvn spotless:apply` before committing to auto-fix. No other formatting rules are configured. Java unnamed variables (`_` in pattern matching) require Java 22+; use named variables since the project targets Java 21.
