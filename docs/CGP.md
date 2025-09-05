# Cartesian Genetic Programming (CGP)

This document describes the Cartesian Genetic Programming (CGP) algorithm implemented in the EDAF framework.

## Overview

Cartesian Genetic Programming is a form of genetic programming that represents programs as directed acyclic graphs (DAGs) in a 2D grid of nodes. This implementation follows the classical model of CGP with a fixed-size genotype and a graph-based phenotype.

A key feature of CGP is its use of "inactive" genes (neutrality), which do not affect the phenotype but can be activated by subsequent mutations. This allows for a more effective exploration of the solution space.

### Genotype to Phenotype Mapping

The core of CGP is the mapping from a linear genotype (an array of integers) to a graph-based phenotype.

```
Genotype: [in1, in2, func, in1, in2, func, ..., out1, out2, ...]
            |---- Node 1 ----|---- Node 2 ----| ... |--- Outputs ---|

     +-------+
x0 --|       |
     | Node 0|--+
x1 --|       |  |
     +-------+  |   +-------+
                +---|       |
     +-------+      | Node 2|--> Phenotype Output 0
x0 --|       |--+---|       |
     | Node 1|      +-------+
x1 --|       |--+
     +-------+  |
                |   +-------+
                +---|       |
                    | Node 3|--> Phenotype Output 1
                +---|       |
     +-------+  |   +-------+
x1 --|       |--+
     | Node 2|
     +-------+
```

Each node in the graph is defined by a set of genes in the genotype that specify its function and its input connections. The outputs of the program are then connected to the outputs of specific nodes in the graph.

## Design Choices

- **Function Primitives**: To maintain consistency with the EDAF framework, this implementation reuses the `com.knezevic.edaf.genotype.tree.primitives.Function` class from the `genotype-tree` module for defining the function set.
- **Framework Integration**: Due to a circular dependency issue in the framework's Maven module structure, the CGP algorithm cannot be integrated into the central `AlgorithmFactoryProvider` without significant refactoring of the framework itself. Therefore, the recommended way to use the CGP algorithm is to instantiate it directly in code.

## Configuration Options

The CGP algorithm is configured via a `CgpConfig` object. If running from a YAML file (e.g., for a custom runner), these parameters would go under `algorithm.parameters`.

| Parameter             | Type          | Description                                                                                             | Default Value   |
|-----------------------|---------------|---------------------------------------------------------------------------------------------------------|-----------------|
| `populationSize`      | `int`         | The number of individuals in the population.                                                            | `200`           |
| `generations`         | `int`         | The maximum number of generations to run (used by `MaxGenerations` termination condition).               | `1000`          |
| `mutationRate`        | `double`      | The probability (0.0-1.0) of mutating each gene in an individual's genotype.                            | `0.02`          |
| `functionSet`         | `List<String>`| A list of function names to be used in the program nodes. Supported: `ADD`, `SUB`, `MUL`, `DIV`, `SIN`, `COS`. | (required)      |
| `rows`                | `int`         | The number of rows in the 2D grid of nodes.                                                             | `1`             |
| `cols`                | `int`         | The number of columns in the 2D grid of nodes.                                                          | `20`            |
| `levelsBack`          | `int`         | The number of previous columns a node can connect to. A value of 0 means any previous node is allowed.  | `5`             |
| `useCrossover`        | `boolean`     | Whether to use crossover in the reproduction phase.                                                     | `false`         |
| `crossoverRate`       | `double`      | The probability (0.0-1.0) of applying crossover to a pair of parents.                                     | `0.8`           |
| `replacementStrategy` | `Enum`        | The replacement strategy to use. Can be `GENERATIONAL` or `STEADY_STATE`.                               | `GENERATIONAL`  |
| `randomSeed`          | `long`        | An optional seed for the random number generator for reproducibility.                                   | (none)          |
| `selection`           | `Object`      | The selection operator configuration (if using the main framework runner).                               | (required)      |

### Example Configuration Snippet for YAML

```yaml
algorithm:
  name: cgp
  parameters:
    populationSize: 50
    generations: 200
    mutationRate: 0.05
    rows: 1
    cols: 20
    functionSet: ["ADD", "SUB", "MUL", "DIV"]
    levelsBack: 10
    useCrossover: false
    replacementStrategy: GENERATIONAL
    randomSeed: 42
    selection:
      name: tournament
      size: 5
  termination:
    max-generations: 200
```

## How to Run CGP

The recommended way to run the CGP algorithm is by instantiating it directly in your code, as shown in the integration test (`CgpIntegrationTest.java`).

Here is a brief example:

```java
// 1. Create a CgpConfig object and set the parameters
CgpConfig config = new CgpConfig();
config.setPopulationSize(50);
config.setGenerations(200);
// ... other parameters

// 2. Create the problem, function set, and other dependencies
SymbolicRegressionProblem problem = new SymbolicRegressionProblem();
List<Function> functionSet = List.of( /* ... function definitions ... */ );
Random random = new Random(config.getRandomSeed());
TerminationCondition<CgpIndividual> terminationCondition = new MaxGenerations<>(config.getGenerations());

// 3. Create the CGP components
CgpDecoder decoder = new CgpDecoder(config, functionSet, problem.getNumInputs(), problem.getNumOutputs());
CgpGenotypeFactory genotypeFactory = new CgpGenotypeFactory(config, functionSet, problem.getNumInputs(), problem.getNumOutputs(), random);
CgpMutationOperator mutation = new CgpMutationOperator(config, functionSet, problem.getNumInputs(), problem.getNumOutputs(), random);
CgpCrossoverOperator crossover = new CgpCrossoverOperator(random);
Selection<CgpIndividual> selection = new TournamentSelection<>(random, 5);

// 4. Create and run the algorithm
CgpAlgorithm algorithm = new CgpAlgorithm(config, problem, decoder, genotypeFactory, selection, mutation, crossover, random, terminationCondition);
algorithm.run();

// 5. Get the results
CgpIndividual best = algorithm.getBest();
```
