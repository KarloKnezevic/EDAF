# Architecture

The framework is designed with a modular and extensible architecture. It is built with Java 21 (LTS) and Maven.
The main components are defined by interfaces in the `core` module, and the implementations are provided in separate modules.

## Framework Overview

The EDAF framework is designed to provide a flexible and extensible platform for implementing and experimenting with Estimation of Distribution Algorithms (EDAs) and other evolutionary algorithms. The core idea is to separate the different components of an evolutionary algorithm into a set of well-defined interfaces, allowing for easy substitution and extension.

The main workflow of a typical experiment using the framework is as follows:

1.  **Configuration:** The experiment is defined in a YAML configuration file. This file specifies the algorithm to be used, the problem to be solved, the genotype representation, and the parameters for each component.
2.  **Component Creation:** The `ComponentFactory` reads the configuration file and creates the necessary components (algorithm, problem, population, etc.) using the appropriate factories.
3.  **Algorithm Execution:** The `run()` method of the `Algorithm` instance is called. The algorithm then iteratively evolves a population of individuals to find a solution to the problem.
4.  **Results:** The results of the experiment can be collected through a `ProgressListener` or by inspecting the state of the algorithm after it has finished running.

## Class Diagram

Here is a simplified class diagram showing the main interfaces and classes in the `core` module:

```mermaid
classDiagram
    class Algorithm {
        <<interface>>
        +run()
        +getBest() : Individual
        +getGeneration() : int
        +getPopulation() : Population
        +setProgressListener(ProgressListener)
    }

    class Problem {
        <<interface>>
        +evaluate(Individual)
        +getOptimizationType() : OptimizationType
    }

    class Individual {
        <<interface>>
        +getGenotype() : Object
        +getFitness() : double
        +setFitness(double)
        +copy() : Individual
    }

    class Population {
        <<interface>>
        +add(Individual)
        +remove(Individual)
        +getIndividual(int) : Individual
        +getBest() : Individual
        +getWorst() : Individual
        +getSize() : int
        +sort()
        +getOptimizationType() : OptimizationType
    }

    class Selection {
        <<interface>>
        +select(Population, int) : Population
    }

    class Crossover {
        <<interface>>
        +crossover(Individual, Individual) : Individual
    }

    class Mutation {
        <<interface>>
        +mutate(Individual)
    }

    class TerminationCondition {
        <<interface>>
        +shouldTerminate(Algorithm) : boolean
    }

    class AbstractIndividual {
        #fitness: double
    }

    class SimplePopulation {
        -individuals: List<Individual>
    }

    Algorithm <|-- gGA
    Algorithm <|-- eGA
    Algorithm <|-- cGA
    Algorithm <|-- UMDA
    Algorithm <|-- PBIL
    Algorithm <|-- MIMIC
    Algorithm <|-- LTGA
    Algorithm <|-- BOA
    Algorithm <|-- BMDA

    Individual <|-- AbstractIndividual
    AbstractIndividual <|-- BinaryIndividual
    AbstractIndividual <|-- FpIndividual
    AbstractIndividual <|-- IntIndividual

    Population <|-- SimplePopulation

    Problem <|.. Algorithm
    Population <|.. Algorithm
    Selection <|.. Algorithm
    Crossover <|.. Algorithm
    Mutation <|.. Algorithm
    TerminationCondition <|.. Algorithm
    Individual <|.. Population

```

## Module Dependencies

Here is a diagram of the module dependencies:

```mermaid
graph TD;
    core;
    genotype-binary --> core;
    genotype-fp --> core;
    statistics --> core;
    statistics --> genotype-binary;
    statistics --> genotype-fp;
    testing --> core;
    testing --> genotype-binary;
    testing --> genotype-fp;
    algorithm-umda --> core;
    algorithm-umda --> statistics;
    algorithm-pbil --> core;
    algorithm-pbil --> statistics;
    algorithm-gga --> core;
    algorithm-ega --> core;
    algorithm-cga --> core;
    algorithm-mimic --> core;
    algorithm-mimic --> statistics;
    algorithm-ltga --> core;
    configuration --> core;
    factory --> core;
    factory --> configuration;
    factory --> genotype-binary;
    factory --> genotype-fp;
    factory --> statistics;
    factory --> algorithm-umda;
    factory --> algorithm-pbil;
    factory --> algorithm-gga;
    factory --> algorithm-ega;
    factory --> algorithm-cga;
    factory --> algorithm-mimic;
    factory --> algorithm-ltga;
    examples --> core;
    examples --> factory;
```

## Design Patterns

The framework makes use of several design patterns to achieve its modularity and extensibility.

### Factory Pattern

The `factory` module is the heart of the framework's component creation. It uses a combination of the **Abstract Factory**, **Strategy**, and **Facade** patterns.

*   **`ComponentFactory` (Facade):** The `ComponentFactory` interface and its `DefaultComponentFactory` implementation act as a Facade. It provides a simple interface for creating all the necessary components of an experiment, hiding the complexity of the underlying factory implementations.

*   **`AlgorithmFactory` (Abstract Factory):** The `AlgorithmFactory` is an Abstract Factory that defines the interface for creating a family of related objects: an `Algorithm`, a `Crossover` operator, and a `Mutation` operator. Each concrete algorithm (e.g., `gGA`, `eGA`, `UMDA`) has its own concrete factory that implements this interface.

*   **`GenotypeFactory` and `SelectionFactory` (Strategy):** The `GenotypeFactory` and `SelectionFactory` use the Strategy pattern. Each specific genotype (binary, floating-point, integer) and selection method (tournament, roulette wheel) has its own factory class that implements a common interface. A provider class (`GenotypeFactoryProvider`, `SelectionFactoryProvider`) is used to select the appropriate strategy at runtime based on the configuration.

This design makes it easy to add new algorithms, genotypes, and selection methods without modifying the core framework.

## Plugin model (SPI)

The framework supports runtime discovery of components using Java Service Provider Interface (SPI). This allows algorithms, genotypes and selections to be published by their own modules and discovered without compile-time dependencies in the `factory`.

- `com.knezevic.edaf.core.spi.AlgorithmProvider` – identifies and constructs algorithms
- `com.knezevic.edaf.core.spi.GenotypeProvider` – declares supported genotype families
- `com.knezevic.edaf.core.spi.SelectionProvider` – provides selection strategies

Providers are registered via service descriptors placed under `META-INF/services/<fqcn>`. Example for an algorithm provider:

```
META-INF/services/com.knezevic.edaf.core.spi.AlgorithmProvider
```

Each line in the file contains the fully qualified class name of a provider implementation.

At runtime, the `factory` loads providers using `ServiceLoader` and composes the algorithm according to the YAML configuration.

## Runtime Context, Events and Metrics

### ExecutionContext

The `ExecutionContext` provides:
- **RandomSource**: Seeded, reproducible random number generator
- **ExecutorService**: Virtual threads by default (Java 21 feature) for parallel evaluations
- **EventPublisher**: Publishes structured events throughout algorithm execution

Algorithms implement `SupportsExecutionContext` to receive the context and leverage these capabilities.

### Event System

The framework publishes structured events during execution:

1. **`AlgorithmStarted(algorithmId)`** - Emitted when `algorithm.run()` is called
2. **`GenerationCompleted(algorithmId, generation, best)`** - Emitted after each generation completes
3. **`EvaluationCompleted(algorithmId, evaluatedCount, durationNanos)`** - Emitted after evaluating a batch
4. **`AlgorithmTerminated(algorithmId, generation)`** - Emitted when algorithm finishes

### Metrics Collection

EDAF supports multiple metrics backends:

1. **NoOpEventPublisher** (default)
   - Silent no-op publisher when no metrics are configured

2. **MicrometerEventPublisher** (`--metrics`)
   - Records metrics in `SimpleMeterRegistry`
   - Programmatic access to counters and timers
   - In-memory storage

3. **PrometheusEventPublisher** (`--prometheus-port`)
   - HTTP endpoint at `/metrics`
   - Prometheus text format
   - Integration with monitoring stacks

### Available Metrics

| Metric | Type | Description |
|--------|------|-------------|
| `edaf.algorithm.started` | Counter | Algorithm runs started |
| `edaf.algorithm.terminated` | Counter | Algorithm runs completed |
| `edaf.algorithm.duration` | Timer | Total execution time |
| `edaf.generation.completed` | Counter | Generations completed |
| `edaf.generation.duration` | Timer | Time per generation |
| `edaf.evaluations.count` | Counter | Individuals evaluated |
| `edaf.evaluation.duration` | Timer | Evaluation batch duration |

All metrics include an `algorithm` tag for filtering.

### Results Storage

Results are stored in multiple formats:

1. **Console** - Real-time progress and final summary
2. **`edaf.log`** - Detailed execution logs (Logback)
3. **`results.json`** - Structured JSON with best individual and fitness

For detailed information on accessing and interpreting metrics and results, see the [Metrics and Results Guide](./metrics-and-results.md).
