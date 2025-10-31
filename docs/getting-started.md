# Getting Started

This guide will walk you through the process of building and running the EDAF framework.

## Requirements

- **Java 21 (LTS)** or higher
- **Maven 3.9+** or higher

## Installation

### Step 1: Build the Framework

The project is built with Maven. Run the following command from the root directory:

```bash
mvn clean install
```

This will:
- Compile all modules
- Run unit tests (unless `-DskipTests` is specified)
- Create JAR files in each module's `target` directory
- Create the executable shaded JAR at `examples/target/edaf.jar`

### Step 2: Verify Installation

Check that the executable JAR was created:

```bash
ls -lh examples/target/edaf.jar
```

You should see a file named `edaf.jar` (typically 50-100 MB depending on dependencies).

### Step 3: Run Your First Experiment

Try running a simple example:

```bash
java -jar examples/target/edaf.jar examples/config/umda-max-ones.yaml
```

You should see:
- A progress bar showing generation count
- Best fitness updates
- Final results with the best individual

If you see output, the framework is working correctly!

## How to Run

The framework is run from the command line using the executable shaded JAR produced by the `examples` module.
After building the project with `mvn clean install`, you can find this JAR at `examples/target/edaf.jar`.

The framework now provides a command-line interface (CLI) with several options. You can get help by running:
```
java -jar examples/target/edaf.jar --help
```

### Metrics and Events

EDAF provides comprehensive metrics and event tracking. See the [Metrics and Results Guide](./metrics-and-results.md) for detailed information.

**Quick Start:**

1. **Console output (default):** Progress and results printed to console
2. **Micrometer metrics:** Enable with `--metrics` flag
3. **Prometheus endpoint:** Enable with `--prometheus-port <port>`

**Example - Enable Micrometer metrics:**
```bash
java -jar examples/target/edaf.jar --metrics examples/config/gga-max-ones.yaml
```

**Example - Expose Prometheus metrics:**
```bash
java -jar examples/target/edaf.jar --prometheus-port 9464 examples/config/gga-max-ones.yaml
```

**Available Metrics:**
- `edaf.algorithm.started` - Counter: Number of algorithm runs
- `edaf.algorithm.terminated` - Counter: Completed algorithm runs
- `edaf.algorithm.duration` - Timer: Total execution time
- `edaf.generation.completed` - Counter: Total generations
- `edaf.generation.duration` - Timer: Time per generation
- `edaf.evaluations.count` - Counter: Total individuals evaluated
- `edaf.evaluation.duration` - Timer: Evaluation batch duration

**Accessing Metrics:**

1. **Prometheus endpoint:** `http://localhost:9464/metrics`
2. **Programmatically:** Access `SimpleMeterRegistry` when using `--metrics`
3. **Results file:** See `results.json` for final results in JSON format
4. **Log file:** See `edaf.log` for detailed execution logs

For detailed information on accessing and interpreting metrics, see the [Metrics and Results Guide](./metrics-and-results.md).

### Running an Experiment

To run an experiment, simply provide the path to a valid configuration file:

```bash
java -jar examples/target/edaf.jar examples/config/cga-max-ones.yaml
```

The framework will print its progress to the console, write detailed logs to `edaf.log`, and save final results to `results.json`.

### Example Configurations (by Problem)

- **Binary (MaxOnes):** `examples/config/gga-max-ones.yaml`, `examples/config/bmda-max-ones.yaml`, `examples/config/mimic-max-ones.yaml`, `examples/config/umda-max-ones.yaml`, `examples/config/fda-max-ones.yaml`, `examples/config/cem-max-ones.yaml`
- **Knapsack 0/1:** `examples/config/pbil-knapsack.yaml`, `examples/config/fda-knapsack.yaml`, `examples/config/cem-knapsack.yaml`
- **Permutation (TSP):** `examples/config/problems/tsp-gga.yaml`
- **Floating-point (Sphere):** `examples/config/boa-sphere.yaml`, `examples/config/cem-sphere.yaml`, `examples/config/mimic-sphere.yaml`, `examples/config/fda-sphere.yaml`
- **Floating-point (Ackley):** `examples/config/problems/ackley-umda.yaml`, `examples/config/boa-ackley.yaml`, `examples/config/cem-ackley.yaml`
- **Floating-point (Rosenbrock):** `examples/config/pbil-rosenbrock.yaml`, `examples/config/boa-rosenbrock.yaml`, `examples/config/cem-rosenbrock.yaml`
- **Boolean Functions:** `examples/config/pbil-boolean-function.yaml`, `examples/config/mimic-boolean-function.yaml`, `examples/config/fda-boolean-function.yaml`
- **Deceptive Trap:** `examples/config/pbil-deceptive-trap.yaml`, `examples/config/mimic-deceptive-trap.yaml`, `examples/config/fda-deceptive-trap.yaml`
- **Genetic Programming:** `examples/config/gp/symbolic-regression.yaml`, `examples/config/gp/iris-classification.yaml`, `examples/config/gp/multiplexer.yaml`
- **CGP (Cartesian GP):** `examples/config/cgp-symbolic-regression.yaml`, `examples/config/cgp-multiplexer.yaml`, `examples/config/cgp-parity.yaml`

See [Examples Catalog](../examples/config/README.md) for a complete list.

### Generating a Configuration Template

To make it easier to create new configuration files, you can use the `generate-config` command.
This will generate a template for a given algorithm and print it to the console.

```bash
java -jar examples/target/edaf.jar generate-config --algorithm umda
```

You can redirect the output to a file:

```bash
java -jar examples/target/edaf.jar generate-config -a gga > my-gga-config.yaml
```

**Supported Algorithm Names:**

- `umda`, `pbil`, `mimic`, `ltga`, `bmda`, `boa`, `cga`, `gga`, `ega`, `gp`, `fda`, `cem`, `cgp`

## Logging and Results

The framework uses a comprehensive logging and results system.

### Output Locations

1. **Console Output** (`stdout`)
   - Progress bar with generation count
   - Best fitness per generation
   - Final results summary
   - Human-readable format

2. **Log File** (`edaf.log`)
   - Detailed execution logs
   - Debug information
   - Error messages
   - Timestamped entries

3. **Results File** (`results.json`)
   - Structured JSON format
   - Best individual (serialized)
   - Final fitness value
   - Timestamp
   - Easy to parse programmatically

### Accessing Results

**View console output:**
```bash
java -jar examples/target/edaf.jar examples/config/my-config.yaml
```

**Parse results.json:**
```bash
cat results.json | jq '.best_individual.fitness'
```

**View detailed logs:**
```bash
tail -f edaf.log
```

For comprehensive information on metrics, events, and results, see the [Metrics and Results Guide](./metrics-and-results.md).
