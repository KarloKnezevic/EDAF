# Configuration

The framework uses YAML files for configuration.

## Validation
The framework automatically validates the configuration file upon loading. If any required parameters are missing or have invalid values, it will print a clear error message and exit. This prevents runtime errors due to misconfiguration.

## Problem Configuration

The `problem` section of the configuration file defines the problem to be solved.

```yaml
problem:
  class: com.mycompany.myproject.MyProblem
  optimization: MAXIMIZE
  # ... other problem parameters
```

### `class`
The `class` property specifies the fully qualified name of the Java class that implements the `com.knezevic.edaf.core.api.Problem` interface.

### `optimization`
The `optimization` property defines the goal of the optimization. It can be set to `MINIMIZE` or `MAXIMIZE`. If omitted, it defaults to `MINIMIZE` for backward compatibility.

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
  optimization: MINIMIZE
  size: 100
  ratio: 0.5
```

## Available Components

Here is a list of the currently available components that can be specified in the configuration file.

**Genotypes (`genotype.type`)**
| Name      | Description                               |
|-----------|-------------------------------------------|
| `binary`  | A genotype represented by a binary string.  |
| `fp`      | A genotype represented by floating-point numbers. |
| `integer` | A genotype represented by integers.       |

**Selection (`selection.name`)**
| Name            | Description                               |
|-----------------|-------------------------------------------|
| `tournament`    | Tournament selection.                     |
| `rouletteWheel` | Roulette wheel selection.                 |

**Crossover (`crossing.name`)**
| Genotype  | Name          | Description                   | Parameters |
|-----------|---------------|-------------------------------|------------|
| `binary`  | `onePoint`    | One-point crossover.          | - |
| `binary`  | `uniform`     | Uniform crossover.            | - |
| `integer` | `onePoint`    | One-point crossover.          | - |
| `integer` | `twoPoint`    | Two-point crossover.          | - |
| `fp`      | `sbx`         | Simulated Binary Crossover.   | `distribution-index` |

**Mutation (`mutation.name`)**
| Genotype  | Name         | Description                  | Parameters |
|-----------|--------------|------------------------------|------------|
| `binary`  | `simple`     | Simple bit-flip mutation.    | `probability` |
| `integer` | `simple`     | Simple integer mutation.     | `probability` |
| `fp`      | `polynomial` | Polynomial mutation.         | `probability`, `distribution-index` |
