# Configuration

The framework uses YAML files for configuration.

## Validation
The framework automatically validates the configuration file upon loading. If any required parameters are missing or have invalid values, it will print a clear error message and exit. This prevents runtime errors due to misconfiguration.

## Problem Configuration

The `problem` section of the configuration file defines the problem to be solved.

```yaml
problem:
  class: com.mycompany.myproject.MyProblem
  # ... other problem parameters
```

### Problem Class

The `class` property specifies the fully qualified name of the Java class that implements the `com.knezevic.edaf.core.api.Problem` interface.

### Variable Parameters

You can pass a variable number of parameters to your problem's constructor. The framework will automatically detect the parameters from the configuration file and find a suitable constructor.

For example, if your problem class has a constructor like this:

```java
public MyProblem(int size, double ratio) {
    // ...
}
```

You can specify the parameters in the configuration file like this:

```yaml
problem:
  class: com.mycompany.myproject.MyProblem
  size: 100
  ratio: 0.5
  # ... other problem parameters
```

The framework will automatically match the `size` and `ratio` parameters to the constructor arguments. The supported parameter types are `String`, `Integer`, `Double`, `Float`, `Long`, and `Boolean`.

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
