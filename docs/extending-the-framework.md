# Extending the Framework

The EDAF framework is designed to be easily extensible. You can add your own custom components, such as problems, algorithms, genotypes, and selection methods, without modifying the core framework.

The framework uses Java Service Provider Interface (SPI) for dynamic component discovery. This allows you to create new components in separate modules and have them automatically discovered at runtime.

## Architecture Overview

The framework follows a plugin-based architecture:

1. **Core Interfaces**: Defined in the `core` module
2. **SPI Providers**: Implement provider interfaces to register components
3. **Service Descriptors**: Use `META-INF/services/` to register providers
4. **Component Factory**: Automatically discovers and instantiates components

## Adding a New Problem

### Step 1: Create the Problem Class

Create a new class implementing `com.knezevic.edaf.core.api.Problem<TGenotype>`:

```java
package com.mycompany.myproject;

import com.knezevic.edaf.core.api.Individual;
import com.knezevic.edaf.core.api.Problem;
import com.knezevic.edaf.core.api.OptimizationType;
import java.util.Map;

public class MyProblem implements Problem<int[]> {
    
    private final OptimizationType optimizationType;
    private final int size;
    
    public MyProblem(Map<String, Object> params) {
        this.optimizationType = OptimizationType.valueOf(
            params.getOrDefault("optimization", "min").toString().toUpperCase()
        );
        this.size = (int) params.get("size");
    }
    
    @Override
    public void evaluate(Individual<int[]> individual) {
        int[] genotype = individual.getGenotype();
        double fitness = 0.0;
        
        // Your evaluation logic here
        for (int i = 0; i < genotype.length; i++) {
            fitness += genotype[i];
        }
        
        individual.setFitness(fitness);
    }
    
    @Override
    public OptimizationType getOptimizationType() {
        return optimizationType;
    }
}
```

**Requirements:**
- Must have a constructor that accepts `Map<String, Object>` (for configuration parameters)
- Must implement `evaluate(Individual)` to set fitness
- Must implement `getOptimizationType()` to return `min` or `max`

### Step 2: Use in Configuration

Specify the fully qualified class name in your YAML configuration:

```yaml
problem:
  class: com.mycompany.myproject.MyProblem
  optimization: max
  size: 100
```

### Step 3: Build and Run

Rebuild the project and run:

```bash
mvn clean install
java -jar examples/target/edaf.jar examples/config/my-config.yaml
```

## Adding a New Algorithm (SPI-based)

The modern way to add algorithms is using the SPI mechanism. This allows your algorithm to be discovered automatically without modifying the factory module.

### Step 1: Create the Algorithm Class

Implement `com.knezevic.edaf.core.api.Algorithm<TGenotype>`:

```java
package com.mycompany.myproject;

import com.knezevic.edaf.core.api.*;
import com.knezevic.edaf.core.runtime.SupportsExecutionContext;
import com.knezevic.edaf.core.runtime.ExecutionContext;
import com.knezevic.edaf.core.runtime.GenerationCompleted;

public class MyAlgorithm<T extends Individual<int[]>> implements Algorithm<T>, SupportsExecutionContext {
    
    private final Problem<T> problem;
    private final Population<T> population;
    private final TerminationCondition<T> terminationCondition;
    
    private T best;
    private int generation;
    private ProgressListener listener;
    private ExecutionContext context;
    
    public MyAlgorithm(Problem<T> problem, Population<T> population,
                       TerminationCondition<T> terminationCondition) {
        this.problem = problem;
        this.population = population;
        this.terminationCondition = terminationCondition;
    }
    
    @Override
    public void run() {
        // 1. Initialize
        evaluatePopulation(population);
        population.sort();
        best = (T) population.getBest().copy();
        generation = 0;
        
        // 2. Main loop
        while (!terminationCondition.shouldTerminate(this)) {
            // Your algorithm logic here
            
            generation++;
            if (listener != null) {
                listener.onGenerationDone(generation, population.getBest(), population);
            }
            
            // Publish event
            if (context != null && context.getEvents() != null) {
                context.getEvents().publish(new GenerationCompleted(
                    "my-algorithm", generation, population.getBest(), 
                    null, null, null, null
                ));
            }
        }
    }
    
    private void evaluatePopulation(Population<T> population) {
        // Use ExecutionContext for parallel evaluation if available
        if (context != null && context.getExecutor() != null) {
            // Parallel evaluation with virtual threads
            var tasks = new java.util.ArrayList<java.util.concurrent.Callable<Void>>();
            for (T individual : population) {
                tasks.add(() -> {
                    problem.evaluate(individual);
                    return null;
                });
            }
            try {
                context.getExecutor().invokeAll(tasks);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        } else {
            // Sequential evaluation
            for (T individual : population) {
                problem.evaluate(individual);
            }
        }
    }
    
    @Override
    public T getBest() { return best; }
    
    @Override
    public int getGeneration() { return generation; }
    
    @Override
    public Population<T> getPopulation() { return population; }
    
    @Override
    public void setProgressListener(ProgressListener listener) {
        this.listener = listener;
    }
    
    @Override
    public void setExecutionContext(ExecutionContext context) {
        this.context = context;
    }
}
```

### Step 2: Create the Algorithm Provider

Implement `com.knezevic.edaf.core.spi.AlgorithmProvider`:

```java
package com.mycompany.myproject;

import com.knezevic.edaf.configuration.pojos.Configuration;
import com.knezevic.edaf.core.api.*;
import com.knezevic.edaf.core.spi.AlgorithmProvider;

public class MyAlgorithmProvider implements AlgorithmProvider {
    
    @Override
    public String getName() {
        return "my-algorithm";
    }
    
    @Override
    public boolean supports(String genotypeType) {
        // Return true if this algorithm supports the genotype type
        return genotypeType.equals("integer");
    }
    
    @Override
    public <T extends Individual<?>> Algorithm<T> create(
        Problem<T> problem,
        Population<T> population,
        Selection<T> selection,
        Statistics<T> statistics,
        TerminationCondition<T> terminationCondition,
        java.util.Random random
    ) {
        return new MyAlgorithm<>(problem, population, terminationCondition);
    }
    
    @Override
    public <T extends Individual<?>> Algorithm<T> createWithConfig(
        Configuration config,
        Problem<T> problem,
        Population<T> population,
        Selection<T> selection,
        Statistics<T> statistics,
        TerminationCondition<T> terminationCondition,
        java.util.Random random
    ) {
        // Access configuration parameters if needed
        int maxGenerations = config.getAlgorithm().getTermination().getMaxGenerations();
        
        return create(problem, population, selection, statistics, 
                     terminationCondition, random);
    }
}
```

### Step 3: Register the Provider

Create a service descriptor file:

**File:** `src/main/resources/META-INF/services/com.knezevic.edaf.core.spi.AlgorithmProvider`

**Content:**
```
com.mycompany.myproject.MyAlgorithmProvider
```

### Step 4: Create Maven Module (Optional but Recommended)

Create a new Maven module for your algorithm:

**File:** `algorithm-my-algorithm/pom.xml`:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project>
    <parent>
        <groupId>com.knezevic</groupId>
        <artifactId>edaf</artifactId>
        <version>2.0.0-SNAPSHOT</version>
    </parent>
    <artifactId>algorithm-my-algorithm</artifactId>
    <dependencies>
        <dependency>
            <groupId>com.knezevic</groupId>
            <artifactId>core</artifactId>
        </dependency>
    </dependencies>
</project>
```

Add the module to the parent `pom.xml`:

```xml
<modules>
    ...
    <module>algorithm-my-algorithm</module>
</modules>
```

### Step 5: Use in Configuration

```yaml
algorithm:
  name: my-algorithm
  population:
    size: 100
  termination:
    max-generations: 1000
```

### Step 6: Build and Test

```bash
mvn clean install
java -jar examples/target/edaf.jar examples/config/my-algorithm-config.yaml
```

## Adding a New Algorithm (Legacy Factory-based)

If you need to add an algorithm using the legacy factory approach:

### Step 1: Create Algorithm Class

Same as Step 1 in SPI-based approach.

### Step 2: Create Algorithm Factory

```java
package com.mycompany.myproject;

import com.knezevic.edaf.configuration.pojos.Configuration;
import com.knezevic.edaf.core.api.*;
import com.knezevic.edaf.factory.algorithm.AlgorithmFactory;

public class MyAlgorithmFactory implements AlgorithmFactory {
    
    @Override
    public Algorithm<?> create(Configuration config, Problem<?> problem,
                               Population<?> population, Selection<?> selection,
                               TerminationCondition<?> terminationCondition,
                               java.util.Random random) {
        return new MyAlgorithm((Problem) problem, (Population) population,
                               (TerminationCondition) terminationCondition);
    }
    
    @Override
    public Crossover<?> createCrossover(Configuration config, java.util.Random random) {
        return null; // Not needed for this algorithm
    }
    
    @Override
    public Mutation<?> createMutation(Configuration config, java.util.Random random) {
        return null; // Not needed for this algorithm
    }
}
```

### Step 3: Register in AlgorithmFactoryProvider

Modify `factory/src/main/java/com/knezevic/edaf/factory/algorithm/AlgorithmFactoryProvider.java`:

```java
public static AlgorithmFactory getFactory(String algorithmName) {
    switch (algorithmName.toLowerCase()) {
        // ... existing cases ...
        case "my-algorithm":
            return new MyAlgorithmFactory();
        default:
            throw new IllegalArgumentException("Unknown algorithm: " + algorithmName);
    }
}
```

## Adding a New Genotype

### Step 1: Create Genotype Classes

Create an `Individual` class implementing `com.knezevic.edaf.core.api.Individual<TGenotype>`:

```java
package com.mycompany.myproject;

import com.knezevic.edaf.core.api.Individual;
import com.knezevic.edaf.core.impl.AbstractIndividual;

public class MyIndividual extends AbstractIndividual implements Individual<String> {
    
    private String genotype;
    
    public MyIndividual(String genotype) {
        this.genotype = genotype;
    }
    
    @Override
    public String getGenotype() {
        return genotype;
    }
    
    @Override
    public Individual<String> copy() {
        return new MyIndividual(genotype);
    }
}
```

### Step 2: Create Genotype Factory

```java
package com.mycompany.myproject;

import com.knezevic.edaf.core.api.Genotype;
import com.knezevic.edaf.core.api.Individual;
import com.knezevic.edaf.factory.genotype.GenotypeFactory;
import java.util.Random;

public class MyGenotypeFactory implements GenotypeFactory {
    
    @Override
    public Genotype<?> create(com.knezevic.edaf.configuration.pojos.Configuration config, Random random) {
        int length = config.getProblem().getGenotype().getLength();
        return new MyGenotype(length);
    }
    
    @Override
    public Individual<?> createIndividual(Object genotype) {
        return new MyIndividual((String) genotype);
    }
}
```

### Step 3: Register via SPI

Implement `com.knezevic.edaf.core.spi.GenotypeProvider`:

```java
package com.mycompany.myproject;

import com.knezevic.edaf.core.spi.GenotypeProvider;

public class MyGenotypeProvider implements GenotypeProvider {
    
    @Override
    public String getName() {
        return "string";
    }
    
    @Override
    public boolean supports(String type) {
        return type.equals("string");
    }
}
```

Create service descriptor: `META-INF/services/com.knezevic.edaf.core.spi.GenotypeProvider`:
```
com.mycompany.myproject.MyGenotypeProvider
```

## Adding a New Selection Method

### Step 1: Create Selection Class

```java
package com.mycompany.myproject;

import com.knezevic.edaf.core.api.Individual;
import com.knezevic.edaf.core.api.Population;
import com.knezevic.edaf.core.api.Selection;
import java.util.Random;

public class MySelection<T extends Individual<?>> implements Selection<T> {
    
    private final Random random;
    
    public MySelection(Random random) {
        this.random = random;
    }
    
    @Override
    public Population<T> select(Population<T> population, int size) {
        // Your selection logic here
        Population<T> selected = new com.knezevic.edaf.core.impl.SimplePopulation<>(
            population.getOptimizationType()
        );
        // ... implement selection ...
        return selected;
    }
}
```

### Step 2: Register via SPI

Implement `com.knezevic.edaf.core.spi.SelectionProvider`:

```java
package com.mycompany.myproject;

import com.knezevic.edaf.core.api.Selection;
import com.knezevic.edaf.core.spi.SelectionProvider;
import java.util.Random;

public class MySelectionProvider implements SelectionProvider {
    
    @Override
    public String getName() {
        return "my-selection";
    }
    
    @Override
    public <T extends Individual<?>> Selection<T> create(
        com.knezevic.edaf.configuration.pojos.Configuration config,
        Random random
    ) {
        return new MySelection<>(random);
    }
}
```

Create service descriptor: `META-INF/services/com.knezevic.edaf.core.spi.SelectionProvider`:
```
com.mycompany.myproject.MySelectionProvider
```

### Step 3: Use in Configuration

```yaml
algorithm:
  selection:
    name: my-selection
    size: 50
```

## Best Practices

### 1. Implement SupportsExecutionContext

For better performance and metrics integration:

```java
public class MyAlgorithm implements Algorithm<T>, SupportsExecutionContext {
    private ExecutionContext context;
    
    @Override
    public void setExecutionContext(ExecutionContext context) {
        this.context = context;
    }
    
    // Use context.getExecutor() for parallel evaluation
    // Use context.getEvents() for publishing events
    // Use context.getRandomSource() for reproducible randomness
}
```

### 2. Publish Events

Publish structured events for metrics:

```java
if (context != null && context.getEvents() != null) {
    context.getEvents().publish(new GenerationCompleted(
        "my-algorithm", generation, best,
        stats.best(), stats.worst(), stats.avg(), stats.std()
    ));
}
```

### 3. Implement Elitism

Preserve the best individual across generations:

```java
// Before replacing population
T bestFromCurrent = population.getBest();

// After replacing population
T currentBest = newPopulation.getBest();
if (isFirstBetter(bestFromCurrent, currentBest)) {
    newPopulation.remove(newPopulation.getWorst());
    newPopulation.add((T) bestFromCurrent.copy());
}
```

### 4. Use PopulationStatistics

For consistent statistics calculation:

```java
import com.knezevic.edaf.core.runtime.PopulationStatistics;

PopulationStatistics.Statistics stats = 
    PopulationStatistics.calculate(population);

// Use stats.best(), stats.worst(), stats.avg(), 
// stats.std(), stats.median()
```

## Testing Your Extension

1. **Unit Tests**: Test your component in isolation
2. **Integration Tests**: Create configuration files and test end-to-end
3. **Performance Tests**: Compare with existing algorithms

Example test configuration:

```yaml
problem:
  class: com.mycompany.MyProblem
  optimization: max
  size: 100

genotype:
  type: integer
  length: 100

algorithm:
  name: my-algorithm
  population:
    size: 100
  termination:
    max-generations: 1000
```

Run and verify:

```bash
java -jar examples/target/edaf.jar examples/config/my-test-config.yaml
```

## Troubleshooting

### Algorithm Not Found

- Check service descriptor file location: `META-INF/services/com.knezevic.edaf.core.spi.AlgorithmProvider`
- Verify provider class is on classpath
- Check algorithm name matches configuration (`algorithm.name`)

### ClassNotFoundException

- Ensure module is included in parent `pom.xml`
- Rebuild with `mvn clean install`
- Check shaded JAR includes your module

### Events Not Publishing

- Implement `SupportsExecutionContext`
- Call `setExecutionContext()` in factory/provider
- Verify `context.getEvents()` is not null before publishing

## Additional Resources

- [Architecture Documentation](./architecture.md) - Framework structure
- [Configuration Guide](./configuration.md) - YAML configuration format
- [Metrics and Results](./metrics-and-results.md) - Event publishing and metrics
- [Getting Started](./getting-started.md) - Basic usage
