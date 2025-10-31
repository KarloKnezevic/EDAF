# Configuration

The framework uses YAML files for configuration.

## Validation
The framework automatically validates the configuration file upon loading. If any required parameters are missing or have invalid values, it will print a clear error message and exit. This prevents runtime errors due to misconfiguration.

## Problem Configuration

The `problem` section of the configuration file defines the problem to be solved.

```yaml
problem:
  class: com.mycompany.myproject.MyProblem
  optimization: max
  # ... other problem parameters
```

### `class`
The `class` property specifies the fully qualified name of the Java class that implements the `com.knezevic.edaf.core.api.Problem` interface.

### `optimization`
The `optimization` property defines the goal of the optimization. It can be set to `min` or `max`. If omitted, it defaults to `min` for backward compatibility.

### Variable Parameters
All other parameters in the `problem` section are collected into a `Map<String, Object>` and passed to the constructor of your problem class. Your problem class must have a public constructor that accepts a single `Map<String, Object>` argument.

For example, if your problem class has a constructor like this:
```java
public MyProblem(Map<String, Object> params) {
    this.size = (int) params.get("size");
    this.ratio = (double) params.get("ratio");
}
```

You can specify the parameters in the configuration file like this:
```yaml
problem:
  class: com.mycompany.myproject.MyProblem
  optimization: min
  size: 100
  ratio: 0.5
```

## Available Components
**Algorithms (`algorithm.name`)**
| Name   | Description                                      |
|--------|--------------------------------------------------|
| `umda` | Univariate Marginal Distribution Algorithm       |
| `pbil` | Population-Based Incremental Learning            |
| `gga`  | Generational Genetic Algorithm                   |
| `ega`  | Eliminative GA (steady-state)                    |
| `ltga` | Linkage Tree Genetic Algorithm                   |
| `bmda` | Bivariate Marginal Distribution Algorithm        |
| `mimic`| Mutual-Information-Maximizing Input Clustering   |
| `fda`  | Factorized Distribution Algorithm (Bayesian network)|
| `cem`  | Cross-Entropy Method (black-box optimization)        |
| `boa`  | Bayesian Optimization Algorithm (surrogate-based)|
| `gp`   | Genetic Programming                              |
| `cgp`  | Cartesian Genetic Programming (graph-based)      |
| `cga`  | Compact Genetic Algorithm                        |
| `ega`  | Eliminative GA (steady-state)                    |


Here is a list of the currently available components that can be specified in the configuration file.

**Genotypes (`genotype.type`)**
| Name      | Description                               |
|-----------|-------------------------------------------|
| `binary`  | A genotype represented by a binary string.  |
| `fp`      | A genotype represented by floating-point numbers. |
| `integer` | A genotype represented by integers.       |
| `permutation` | A genotype represented by a permutation of integers. |
| `tree`    | A genotype represented by a tree structure (for GP). |

**Selection (`selection.name`)**
| Name                 | Description                               |
|----------------------|-------------------------------------------|
| `simple-tournament`  | Simple tournament (k=2).                   |
| `tournament`         | Tournament selection (default k=2).        |
| `rouletteWheel`      | Roulette wheel selection.                  |

**Crossover (`crossing.name`)**
| Genotype  | Name               | Description                   | Parameters |
|-----------|--------------------|-------------------------------|------------|
| `binary`  | `one-point`        | One-point crossover.          | - |
| `binary`  | `uniform`          | Uniform crossover.            | - |
| `integer` | `one-point`        | One-point crossover.          | - |
| `integer` | `two-point`        | Two-point crossover.          | - |
| `fp`      | `sbx`              | Simulated Binary Crossover.   | `distribution-index` |
| `fp`      | `discrete`         | Discrete recombination.       | - |
| `fp`      | `simple-arithmetic`| Simple arithmetic recombination. | `probability` |
| `fp`      | `whole-arithmetic` | Whole arithmetic recombination.  | `probability` |
| `permutation` | `pmx`           | Partially Mapped Crossover.   | - |
| `permutation` | `ox`            | Order Crossover.              | - |
| `permutation` | `cx`            | Cycle Crossover.              | - |

**Mutation (`mutation.name`)**
| Genotype  | Name           | Description                  | Parameters |
|-----------|----------------|------------------------------|------------|
| `binary`  | `simple`       | Simple bit-flip mutation.    | `probability` |
| `integer` | `simple`       | Simple integer mutation.     | `probability` |
| `fp`      | `polynomial`   | Polynomial mutation.         | `probability`, `distribution-index` |
| `permutation` | `swap`      | Swap two positions.          | `probability` |
| `permutation` | `insert`    | Insert element at position.  | `probability` |
| `permutation` | `inversion` | Invert a segment.            | `probability` |
| `permutation` | `scramble`  | Scramble a segment.          | `probability` |
| `permutation` | `shift`     | Shift a segment.             | `probability` |

### Operator name aliases (backward compatibility)

- `onePoint` -> `one-point`
- `twoPoint` -> `two-point`

Selection names supported: `tournament`, `simple-tournament`, `mu-comma-lambda`, `mu-plus-lambda`, `rouletteWheel`.
