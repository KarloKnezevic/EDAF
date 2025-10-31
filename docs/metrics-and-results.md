# Metrics and Results Guide

This document describes how to access and interpret metrics, events, and results from EDAF framework runs.

## Overview

EDAF provides multiple ways to observe algorithm execution:

1. **Console Events** - Human-readable progress information
2. **Micrometer Metrics** - Programmatic metrics collection
3. **Prometheus Endpoint** - HTTP endpoint for metrics scraping
4. **Log Files** - Detailed logs and structured results

## Metrics Collection

### Available Metrics

The framework tracks the following metrics (all prefixed with `edaf.`):

| Metric Name | Type | Description | Tags |
|-------------|------|-------------|------|
| `edaf.algorithm.started` | Counter | Number of algorithm runs started | `algorithm` |
| `edaf.algorithm.terminated` | Counter | Number of algorithm runs completed | `algorithm` |
| `edaf.algorithm.duration` | Timer | Total execution time per algorithm run | `algorithm` |
| `edaf.generation.completed` | Counter | Total number of generations completed | `algorithm` |
| `edaf.generation.duration` | Timer | Time per generation (nanoseconds) | `algorithm` |
| `edaf.evaluations.count` | Counter | Total number of individuals evaluated | `algorithm` |
| `edaf.evaluation.duration` | Timer | Time taken for evaluation batch | `algorithm` |

All metrics include an `algorithm` tag identifying the algorithm instance (e.g., `umda`, `gga`, `pbil`).

## Accessing Metrics

### 1. Console Output (Default)

By default, EDAF uses `ConsoleEventPublisher` which prints events to the console:

```bash
java -jar examples/target/edaf.jar examples/config/gga-max-ones.yaml
```

**Output includes:**
- Algorithm startup messages
- Generation progress with best fitness
- Final results (best individual, fitness)
- Progress bar visualization

**Example console output:**
```
=================================================
   Estimation of Distribution Algorithms Framework (EDAF)   
=================================================
PHASE 1: Loading configuration from 'examples/config/gga-max-ones.yaml'
Configuration loaded successfully.
PHASE 2: Initializing framework components...
Framework components initialized successfully.
PHASE 3: Starting algorithm 'gga'...
Generations |████████████████████| 100/100 Best fitness: 100.0
Algorithm execution finished.
-------------------- RESULTS --------------------
Best individual: BinaryIndividual{genotype=[1, 1, 1, ...], fitness=100.0}
Fitness: 100.0
-----------------------------------------------
```

### 2. Micrometer Metrics (SimpleMeterRegistry)

Enable Micrometer metrics collection with the `--metrics` flag:

```bash
java -jar examples/target/edaf.jar --metrics examples/config/gga-max-ones.yaml
```

**What happens:**
- Framework creates a `SimpleMeterRegistry` and `MicrometerEventPublisher`
- All events are recorded as Micrometer metrics
- Metrics are stored in memory and can be accessed programmatically

**Accessing metrics programmatically:**

If you embed EDAF in your application, you can access metrics like this:

```java
import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import com.knezevic.edaf.metrics.MicrometerEventPublisher;

MeterRegistry registry = new SimpleMeterRegistry();
EventPublisher publisher = new MicrometerEventPublisher(registry, null);

// After running algorithm...
registry.getMeters().forEach(m -> {
    Meter.Id id = m.getId();
    System.out.println(id.getName() + " tags=" + id.getTags());
    m.measure().forEach(ms -> 
        System.out.println("  " + ms.getStatistic() + ": " + ms.getValue())
    );
});
```

**Example output:**
```
edaf.algorithm.started tags=[algorithm=gga]
  COUNT: 1.0
edaf.generation.completed tags=[algorithm=gga]
  COUNT: 100.0
edaf.evaluations.count tags=[algorithm=gga]
  COUNT: 10000.0
edaf.generation.duration tags=[algorithm=gga]
  COUNT: 100.0
  TOTAL_TIME: 2.45
  MAX: 0.028
```

### 3. Prometheus HTTP Endpoint

Expose metrics via HTTP endpoint for Prometheus scraping:

```bash
java -jar examples/target/edaf.jar --prometheus-port 9464 examples/config/gga-max-ones.yaml
```

**What happens:**
- Framework starts an embedded HTTP server on the specified port (default: 9464)
- Metrics are exposed at `http://localhost:9464/metrics`
- Prometheus format (Prometheus text format v0.0.4)

**Accessing metrics:**

1. **Via web browser or curl:**
   ```bash
   curl http://localhost:9464/metrics
   ```

2. **Prometheus scrape configuration:**

   Add to your `prometheus.yml`:
   ```yaml
   scrape_configs:
     - job_name: 'edaf'
       static_configs:
         - targets: ['localhost:9464']
       scrape_interval: 5s
       metrics_path: '/metrics'
   ```

**Example Prometheus output:**
```
# HELP edaf_algorithm_started_total Total number of algorithm runs started
# TYPE edaf_algorithm_started_total counter
edaf_algorithm_started_total{algorithm="gga"} 1.0

# HELP edaf_generation_completed_total Total number of generations completed
# TYPE edaf_generation_completed_total counter
edaf_generation_completed_total{algorithm="gga"} 100.0

# HELP edaf_evaluations_count_total Total number of individuals evaluated
# TYPE edaf_evaluations_count_total counter
edaf_evaluations_count_total{algorithm="gga"} 10000.0

# HELP edaf_generation_duration_seconds Time per generation
# TYPE edaf_generation_duration_seconds summary
edaf_generation_duration_seconds_count{algorithm="gga"} 100.0
edaf_generation_duration_seconds_sum{algorithm="gga"} 2.45
edaf_generation_duration_seconds_max{algorithm="gga"} 0.028
```

**Visualizing with Grafana:**

Once Prometheus is scraping EDAF metrics, you can create Grafana dashboards:

1. Create panels for:
   - Algorithm duration (histogram)
   - Generations completed over time (graph)
   - Evaluation count per algorithm (table)
   - Generation duration percentiles (heatmap)

2. Example Grafana query:
   ```
   rate(edaf_generation_completed_total[1m])
   ```

### 4. Log Files

EDAF writes detailed logs and structured results to files.

#### Log File: `edaf.log`

Location: Current working directory

**Contains:**
- Detailed execution logs
- Debug information
- Error messages

**Format:** Standard logback format with timestamps, thread, level, logger, and message

**Example log entry:**
```
2025-01-15 14:30:22.123 [main] INFO  com.knezevic.edaf.examples.Framework - PHASE 1: Loading configuration from 'examples/config/gga-max-ones.yaml'
2025-01-15 14:30:22.456 [main] INFO  com.knezevic.edaf.examples.Framework - Configuration loaded successfully.
2025-01-15 14:30:22.789 [main] INFO  com.knezevic.edaf.algorithm.gga.gGA - Generation 50: Best fitness = 85.0
```

#### Results File: `results.json`

Location: Current working directory

**Contains:**
- Final results in structured JSON format
- Best individual (serialized)
- Final fitness value
- Timestamps

**Format:** JSON (Logstash encoder format)

**Example results.json:**
```json
{
  "@timestamp": "2025-01-15T14:30:45.123Z",
  "@version": "1",
  "message": "Final result",
  "logger_name": "edaf.results",
  "level": "INFO",
  "best_individual": {
    "genotype": [1, 1, 1, 1, 1, ...],
    "fitness": 100.0
  }
}
```

**Parsing results programmatically:**

```java
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

ObjectMapper mapper = new ObjectMapper();
JsonNode results = mapper.readTree(new File("results.json"));

JsonNode bestIndividual = results.get("best_individual");
double fitness = bestIndividual.get("fitness").asDouble();
int[] genotype = mapper.convertValue(bestIndividual.get("genotype"), int[].class);
```

## Event Types

The framework emits the following event types:

1. **`AlgorithmStarted`** - Emitted when `algorithm.run()` is called
   - Fields: `algorithmId` (String)

2. **`GenerationCompleted`** - Emitted after each generation
   - Fields: `algorithmId`, `generation` (int), `best` (Individual)

3. **`EvaluationCompleted`** - Emitted after evaluating a batch of individuals
   - Fields: `algorithmId`, `evaluatedCount` (int), `durationNanos` (long)

4. **`AlgorithmTerminated`** - Emitted when algorithm finishes
   - Fields: `algorithmId`, `generation` (int)

## Metrics Interpretation

### Understanding Timer Metrics

Timer metrics (`edaf.algorithm.duration`, `edaf.generation.duration`, `edaf.evaluation.duration`) provide:

- **COUNT**: Number of measurements
- **TOTAL_TIME**: Sum of all recorded times
- **MAX**: Maximum recorded time
- **MEAN**: Average time (TOTAL_TIME / COUNT)

**Example interpretation:**
```
edaf.generation.duration{algorithm="gga"}
  COUNT: 100.0           # 100 generations
  TOTAL_TIME: 2.45       # 2.45 seconds total
  MAX: 0.028            # Slowest generation: 28ms
```

Mean generation time = 2.45 / 100 = 24.5ms per generation

### Understanding Counter Metrics

Counter metrics (`edaf.algorithm.started`, `edaf.generation.completed`, `edaf.evaluations.count`) are monotonically increasing.

**For rate calculation in Prometheus:**
```promql
rate(edaf_generation_completed_total[5m])  # Generations per second
rate(edaf_evaluations_count_total[1m])     # Evaluations per second
```

## Best Practices

### 1. For Development and Debugging

Use console output (default) for quick feedback:
```bash
java -jar examples/target/edaf.jar examples/config/my-config.yaml
```

### 2. For Production Monitoring

Use Prometheus endpoint:
```bash
java -jar examples/target/edaf.jar --prometheus-port 9464 examples/config/my-config.yaml
```

Set up Prometheus + Grafana for:
- Real-time monitoring
- Historical data analysis
- Alerting on performance degradation

### 3. For Automated Analysis

Use structured JSON results:
```bash
# Run experiment
java -jar examples/target/edaf.jar examples/config/my-config.yaml

# Parse results.json
python parse_results.py results.json
```

### 4. For Performance Profiling

Enable metrics and analyze:
```bash
java -jar examples/target/edaf.jar --metrics examples/config/my-config.yaml
```

Check `edaf.log` for detailed execution traces.

## Example Workflows

### Workflow 1: Single Experiment Analysis

```bash
# Run with Prometheus metrics
java -jar examples/target/edaf.jar --prometheus-port 9464 examples/config/gga-max-ones.yaml

# In another terminal, scrape metrics
curl http://localhost:9464/metrics > metrics.txt

# After run completes, check results
cat results.json | jq '.best_individual.fitness'
```

### Workflow 2: Batch Experiment Comparison

```bash
# Run multiple experiments with different seeds
for seed in {1..10}; do
  java -Dseed=$seed -jar examples/target/edaf.jar examples/config/gga-max-ones.yaml
  mv results.json results_seed_$seed.json
done

# Aggregate results
python aggregate_results.py results_seed_*.json
```

### Workflow 3: Continuous Monitoring

1. Start EDAF with Prometheus:
   ```bash
   java -jar examples/target/edaf.jar --prometheus-port 9464 examples/config/long-running.yaml
   ```

2. Configure Prometheus to scrape:
   ```yaml
   scrape_configs:
     - job_name: 'edaf'
       static_configs:
         - targets: ['localhost:9464']
   ```

3. Create Grafana dashboard for:
   - Algorithm execution time trends
   - Evaluation throughput
   - Generation duration percentiles

## Troubleshooting

### Metrics Not Appearing

1. **Check if metrics are enabled:**
   ```bash
   # Must use --metrics or --prometheus-port
   java -jar examples/target/edaf.jar --metrics examples/config/my-config.yaml
   ```

2. **Check event publisher:**
   - Verify algorithm implements `SupportsExecutionContext`
   - Check `edaf.log` for event publishing errors

3. **Verify Prometheus endpoint:**
   ```bash
   # Check if server is running
   curl http://localhost:9464/metrics
   ```

### Results File Not Created

- Check write permissions in current directory
- Verify logger configuration in `logback.xml`
- Check `edaf.log` for errors

### Incomplete Metrics

- Some metrics require multiple generations to be meaningful
- Ensure algorithm runs for sufficient generations
- Check termination condition configuration

## Additional Resources

- [Micrometer Documentation](https://micrometer.io/docs)
- [Prometheus Documentation](https://prometheus.io/docs/)
- [Grafana Documentation](https://grafana.com/docs/)
- [Getting Started Guide](./getting-started.md) - Basic usage
- [Configuration Guide](./configuration.md) - Configuration options
