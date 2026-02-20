# Extending the Framework

EDAF v3 is extended through plugins discovered by Java `ServiceLoader`. You can add new representations, problems, models, and algorithm drivers without modifying `ExperimentRunner` orchestration logic.

## 1) Extension Interfaces

From `edaf-core`:

- `RepresentationPlugin<G>`
- `ProblemPlugin<G>`
- `ModelPlugin<G>`
- `AlgorithmPlugin<G>`

Each plugin provides:

- `type()` -> config key used in YAML
- `description()` -> shown in `edaf list ...`
- `create(...)` -> component factory method

## 2) Service Registration Files

To make plugins discoverable, register implementation class names in:

- `META-INF/services/com.knezevic.edaf.v3.core.plugins.RepresentationPlugin`
- `META-INF/services/com.knezevic.edaf.v3.core.plugins.ProblemPlugin`
- `META-INF/services/com.knezevic.edaf.v3.core.plugins.ModelPlugin`
- `META-INF/services/com.knezevic.edaf.v3.core.plugins.AlgorithmPlugin`

One fully qualified class name per line.

## 3) Add a New Problem (Step-by-step)

### Step 1: Implement problem logic

Example file:

`edaf-problems/src/main/java/com/knezevic/edaf/v3/problems/LeadingOnesProblem.java`

```java
package com.knezevic.edaf.v3.problems;

import com.knezevic.edaf.v3.core.api.Fitness;
import com.knezevic.edaf.v3.core.api.ObjectiveSense;
import com.knezevic.edaf.v3.core.api.Problem;
import com.knezevic.edaf.v3.core.api.ScalarFitness;
import com.knezevic.edaf.v3.repr.types.BitString;

public final class LeadingOnesProblem implements Problem<BitString> {

    @Override
    public String name() {
        return "leading-ones";
    }

    @Override
    public ObjectiveSense objectiveSense() {
        return ObjectiveSense.MAXIMIZE;
    }

    @Override
    public Fitness evaluate(BitString genotype) {
        int count = 0;
        for (boolean bit : genotype.genes()) {
            if (bit) {
                count++;
            } else {
                break;
            }
        }
        return new ScalarFitness(count);
    }
}
```

### Step 2: Add plugin wrapper

`edaf-problems/src/main/java/com/knezevic/edaf/v3/problems/plugins/LeadingOnesProblemPlugin.java`

```java
package com.knezevic.edaf.v3.problems.plugins;

import com.knezevic.edaf.v3.core.plugins.ProblemPlugin;
import com.knezevic.edaf.v3.problems.LeadingOnesProblem;
import com.knezevic.edaf.v3.repr.types.BitString;

import java.util.Map;

public final class LeadingOnesProblemPlugin implements ProblemPlugin<BitString> {

    @Override
    public String type() {
        return "leading-ones";
    }

    @Override
    public String description() {
        return "Maximize leading consecutive ones";
    }

    @Override
    public LeadingOnesProblem create(Map<String, Object> params) {
        return new LeadingOnesProblem();
    }
}
```

### Step 3: Register plugin

Append to:

`edaf-problems/src/main/resources/META-INF/services/com.knezevic.edaf.v3.core.plugins.ProblemPlugin`

```text
com.knezevic.edaf.v3.problems.plugins.LeadingOnesProblemPlugin
```

### Step 4: Verify

```bash
./edaf list problems
./edaf config validate path/to/your-config.yml
./edaf run -c path/to/your-config.yml
```

## 4) Add a New Representation

### Step 1: Define genotype type

Add immutable type in `edaf-representations/src/main/java/.../types`.

### Step 2: Implement `Representation<G>`

Responsibilities:

- `random(RngStream)`
- `isValid(G)`
- `repair(G)`
- `summarize(G)`

### Step 3: Implement `RepresentationPlugin<G>`

Parse YAML params from plugin `create(Map<String, Object> params)`.

Use `Params` helper from `edaf-core` to parse robustly.

### Step 4: Register service entry

Add plugin class FQCN to representation service file.

### Step 5: Add tests

- random generation validity
- repair correctness
- property tests for invariants

## 5) Add a New Model

### Contract

Implement `Model<G>`:

- `name()`
- `fit(selected, representation, rng)`
- `sample(count, representation, problem, constraintHandling, rng)`
- optional `diagnostics()`

### Practical recommendations

- ensure deterministic behavior under fixed RNG streams
- guard against sampling before fit
- emit meaningful diagnostics keys
- keep configuration parameters explicit in plugin factory

### Register

Add plugin to corresponding model module service file.

## 6) Add a New Algorithm Driver

### Contract

Implement `Algorithm<G>` directly or extend `AbstractEdaAlgorithm<G>`.

Most cases should extend `AbstractEdaAlgorithm<G>` and override:

- `id()`
- `selectionSize(...)`
- optionally `afterIteration(...)`

### Plugin wrapper

Create `AlgorithmPlugin<G>` that instantiates your algorithm using YAML params.

### Register

Append FQCN to:

`edaf-algorithms/src/main/resources/META-INF/services/com.knezevic.edaf.v3.core.plugins.AlgorithmPlugin`

## 7) Use in YAML

Once registered, reference by type:

```yaml
representation:
  type: your-representation
problem:
  type: leading-ones
algorithm:
  type: your-algorithm
model:
  type: your-model
```

## 8) Integration Checklist

- compile and test module
- plugin appears in list command
- config validates
- run executes end-to-end
- events persist if DB sink enabled
- report generation still works for resulting run

## 9) Common Extension Mistakes

- forgetting service registration file update
- mismatched generic type between representation/problem/model/algorithm
- unbounded repair logic introducing invalid values
- using `Math.random()` instead of provided `RngStream`
- missing diagnostics keys for model observability

## 10) Extending the Boolean-Function Crypto Suite

For cryptographic boolean-function objectives, add new criteria without introducing a new problem type:

1. Implement `CryptoFitnessCriterion` in:
   - `edaf-problems/src/main/java/com/knezevic/edaf/v3/problems/crypto/criteria`
2. Register criterion id aliases in:
   - `CryptoCriteriaFactory#create(...)`
3. Keep scores normalized to `0..1` (higher is better) for stable scalar aggregation.
4. Add unit tests in:
   - `edaf-problems/src/test/java/com/knezevic/edaf/v3/problems/crypto`
5. Document config usage in:
   - `docs/crypto-boolean-problems.md`

## 10) Suggested Test Strategy for New Plugin

- unit tests for local logic
- property tests for invariants (validity, bounds, normalization)
- integration test in `edaf-experiments` with a minimal config assembled in code
- persistence smoke test if plugin changes event payload size/shape expectations
