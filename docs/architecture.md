# Architecture

The framework is designed with a modular and extensible architecture. It is built with Java 17+ and Maven.
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
