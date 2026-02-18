# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

EDAF (Estimation of Distribution Algorithms Framework) is a Java 21 multi-module Maven project implementing evolutionary and estimation of distribution algorithms. It uses a plugin-based architecture with Java SPI for component discovery.

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
```

## Requirements

- Java 21+ (LTS)
- Maven 3.9+

## Module Architecture

The project is organized into 22 Maven modules under a parent POM:

**Core layer:**
- `core` — API interfaces (`Algorithm`, `Problem`, `Individual`, `Population`, `Genotype`, `Selection`, `Crossover`, `Mutation`, `TerminationCondition`, `Statistics`), runtime types (`ExecutionContext`, `EventPublisher`, `RandomSource`), and SPI contracts (`AlgorithmProvider`, `GenotypeProvider`, `SelectionProvider`)
- `configuration` — YAML config loading via Jackson, validated with Jakarta Validation / Hibernate Validator
- `factory` — `SpiBackedComponentFactory` uses SPI to discover and instantiate all components from YAML config

**Algorithm modules** (`algorithm-*`): Each implements `AlgorithmProvider` SPI and registers via `META-INF/services/`. Algorithms: GGA, EGA, UMDA, PBIL, MIMIC, BMDA, LTGA, CGA, FDA, CEM, BOA, GP, CGP.

**Genotype modules** (`genotype-*`): Binary, floating-point, integer, permutation, tree representations. Each provides crossover and mutation operators.

**Support modules:**
- `selection` — Tournament and roulette wheel selection (registers `SelectionProvider` via SPI)
- `statistics` — Statistical distribution calculations
- `testing` — Test problems (MaxOnes, Sphere, Rosenbrock, Ackley, Knapsack, TSP, N-Queens, etc.)
- `metrics` — Prometheus and Micrometer event publishers
- `examples` — CLI entry point (`Framework` class using PicoCLI), example YAML configs in `examples/config/`

## Key Patterns

**SPI plugin system:** New algorithms are added by (1) creating a module implementing `AlgorithmProvider`, (2) registering it in `META-INF/services/com.knezevic.edaf.core.spi.AlgorithmProvider`, and (3) adding the module to the parent POM. Same pattern for genotypes and selection operators.

**Configuration-driven:** All experiments are defined in YAML files with three sections: `problem`, `genotype`, and `algorithm`. The `SpiBackedComponentFactory` resolves named components to implementations.

**Package structure:** All code lives under `com.knezevic.edaf.*`. Each module's sources are at `<module>/src/main/java/com/knezevic/edaf/<module-path>/`.

## Testing

- Framework: JUnit 5 + jqwik (property-based testing)
- Tests live in `<module>/src/test/java/`
- Always pass `-Dseed=12345 -Duser.seed=12345` for deterministic results
- CI runs three workflows: build, test, and code-quality (Spotless)

## Code Style

Spotless enforces removal of unused imports. Run `mvn spotless:apply` before committing to auto-fix. No other formatting rules are configured.
