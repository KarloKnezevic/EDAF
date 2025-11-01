# EDAF Usage Guide

## Quick Start

### Running EDAF

**Basic usage:**
```bash
java -jar examples/target/edaf.jar examples/config/umda-max-ones.yaml
```

**With reproducible seed:**
```bash
java -jar examples/target/edaf.jar --seed 12345 examples/config/umda-max-ones.yaml
```

**With Prometheus metrics:**
```bash
java -jar examples/target/edaf.jar --prometheus-port 8888 examples/config/umda-max-ones.yaml
```

## Console Output

EDAF provides real-time progress information in the console:

### Progress Bar

A visual progress bar shows algorithm execution:

```
Generations   45% [████████████          ]      45/100 gen (0:00:23 / 0:00:51)
Gen 45 | Best: 87.5000 | Avg: 82.3400 | Std: 3.2100
```

The progress bar displays:
- **Generation progress**: Current generation / Total generations
- **Estimated time**: Elapsed time / Estimated total time
- **Compact statistics**: Generation number, best fitness, average, standard deviation

### Statistics Table

Every 10 generations (and on generation 1), a detailed statistics table is displayed:

```
╔══════════════════════════════════════════════════════════════════╗
║  Generation Statistics                                           ║
╠══════════════════════════════════════════════════════════════════╣
║  Generation:                1                                   ║
╠══════════════════════════════════════════════════════════════════╣
║  Best Fitness              87.500000                             ║
║  Worst Fitness             45.000000                             ║
║  Average (μ)                72.340000                             ║
║  Std Dev (σ)                8.210000                              ║
║  Median                     73.000000                             ║
╚══════════════════════════════════════════════════════════════════╝
```

The table includes:
- **Best Fitness**: Highest fitness value in population (for max optimization)
- **Worst Fitness**: Lowest fitness value in population
- **Average (μ)**: Mean fitness across all individuals
- **Std Dev (σ)**: Standard deviation of fitness values
- **Median**: Median fitness value

**Note**: The table uses ANSI color codes for better readability. Colors are automatically disabled if the terminal doesn't support them.

### Final Results

At the end of execution, the final result is displayed:

```
Best fitness: 100.0000
```

## Running Prometheus

Prometheus enables real-time monitoring and visualization of algorithm metrics.

### Step 1: Start EDAF with Prometheus

```bash
java -jar examples/target/edaf.jar --prometheus-port 8888 examples/config/umda-max-ones.yaml
```

**Important**: Use `--prometheus-port` instead of `--metrics` to expose an HTTP endpoint.

### Step 2: Create Prometheus Configuration

Create `$HOME/prometheus.yml` (or use full path - Prometheus doesn't expand `~`):

```yaml
global:
  scrape_interval: 5s

scrape_configs:
  - job_name: 'edaf'
    static_configs:
      - targets: ['localhost:8888']
```

**Key points:**
- **Port**: Must match the port used with `--prometheus-port` (e.g., `8888`)
- **Path**: `/metrics` is the default endpoint (no need to specify)
- **Interval**: `5s` provides near-real-time updates

### Step 3: Start Prometheus

**Option A: Homebrew (macOS)**
```bash
# Use $HOME instead of ~ (Prometheus doesn't expand ~)
prometheus --config.file=$HOME/prometheus.yml --web.listen-address=:9090

# Or use full path
prometheus --config.file=/Users/$(whoami)/prometheus.yml --web.listen-address=:9090
```

**Option B: Docker**
```bash
docker run --rm -p 9090:9090 \
  -v $HOME/prometheus.yml:/etc/prometheus/prometheus.yml \
  prom/prometheus
```

**Option C: Download Binary**

Download from [prometheus.io](https://prometheus.io/download/):

```bash
# Extract
tar xvfz prometheus-*.tar.gz
cd prometheus-*

# Run
./prometheus --config.file=$HOME/prometheus.yml --web.listen-address=:9090
```

### Step 4: Access Prometheus UI

1. Open browser: http://localhost:9090
2. Go to **Graph** tab
3. Enter queries (see [Metrics and Results Guide](./docs/metrics-and-results.md))

**Example queries:**
```
# Best fitness over time
edaf_fitness_best{algorithm="umda"}

# Generations completed
edaf_generation_completed_total{algorithm="umda"}

# Evaluation rate
rate(edaf_evaluations_count_total{algorithm="umda"}[30s])
```

### Step 5: Visualize with Grafana (Optional)

1. Install Grafana: `brew install grafana` or use Docker
2. Start Grafana: `grafana-server` or `docker run -p 3000:3000 grafana/grafana`
3. Add Prometheus as data source: http://localhost:9090
4. Create dashboards with panels for:
   - Fitness trends (line graph)
   - Evaluation throughput (graph)
   - Generation duration (histogram)

## Configuration Files

All configuration files are in `examples/config/`. See `examples/config/README.md` for a complete list of available examples.

### Example Configurations by Problem Type

- **Binary (MaxOnes)**: `umda-max-ones.yaml`, `pbil-max-ones.yaml`, `fda-max-ones.yaml`
- **Knapsack 0/1**: `pbil-knapsack.yaml`, `fda-knapsack.yaml`, `cem-knapsack.yaml`
- **Floating-point (Sphere)**: `cem-sphere.yaml`, `umda-sphere.yaml`
- **Floating-point (Rosenbrock)**: `pbil-rosenbrock.yaml`, `boa-rosenbrock.yaml`, `cem-rosenbrock.yaml`
- **Floating-point (Ackley)**: `boa-ackley.yaml`, `cem-ackley.yaml`
- **Boolean Functions**: `pbil-boolean-function.yaml`, `fda-boolean-function.yaml`
- **Genetic Programming**: `gp/symbolic-regression.yaml`, `gp/iris-classification.yaml`
- **CGP**: `cgp-symbolic-regression.yaml`, `cgp-multiplexer.yaml`

## Metrics Access

### HTTP Endpoint (Prometheus)

When running with `--prometheus-port`, metrics are available at:
- **Endpoint**: http://localhost:8888/metrics
- **Format**: Prometheus text format
- **Update frequency**: Updated after each generation

**Example:**
```bash
curl http://localhost:8888/metrics
```

**Output:**
```
# HELP edaf_generation_completed_total Total number of generations completed
# TYPE edaf_generation_completed_total counter
edaf_generation_completed_total{algorithm="umda"} 45.0

# HELP edaf_fitness_best Current best fitness value
# TYPE edaf_fitness_best gauge
edaf_fitness_best{algorithm="umda"} 87.5

# HELP edaf_evaluations_count_total Total number of individuals evaluated
# TYPE edaf_evaluations_count_total counter
edaf_evaluations_count_total{algorithm="umda"} 4500.0
```

### Available Metrics

| Metric | Type | Description |
|--------|------|-------------|
| `edaf_algorithm_started_total` | Counter | Algorithm runs started |
| `edaf_algorithm_terminated_total` | Counter | Algorithm runs completed |
| `edaf_algorithm_duration_seconds` | Timer | Total execution time |
| `edaf_generation_completed_total` | Counter | Generations completed |
| `edaf_generation_duration_seconds` | Timer | Time per generation |
| `edaf_evaluations_count_total` | Counter | Individuals evaluated |
| `edaf_evaluation_duration_seconds` | Timer | Evaluation batch duration |
| `edaf_fitness_best` | Gauge | Current best fitness |
| `edaf_fitness_worst` | Gauge | Current worst fitness |
| `edaf_fitness_avg` | Gauge | Average fitness |
| `edaf_fitness_std` | Gauge | Standard deviation of fitness |

All metrics include an `algorithm` tag (e.g., `algorithm="umda"`).

See [Metrics and Results Guide](./docs/metrics-and-results.md) for detailed query examples.

## Log Files

### Execution Log (`edaf.log`)

Detailed execution logs are written to `edaf.log`:

**Location**: Current working directory

**Contains:**
- Timestamped log entries
- Configuration loading status
- Component initialization
- Algorithm execution details
- Error messages and stack traces

**Example:**
```
2025-01-15 14:30:22.123 [main] INFO  com.knezevic.edaf.examples.Framework - Configuration loaded successfully.
2025-01-15 14:30:22.789 [main] INFO  com.knezevic.edaf.algorithm.umda.Umda - Generation 50: Best fitness = 85.0
```

**Log Level**: Controlled by `examples/src/main/resources/logback.xml`. Default: `WARN` (reduces console noise).

### Results File (`results.json`)

Final results are written to `results.json` in structured JSON format:

**Location**: Current working directory

**Contains:**
- Best individual (serialized)
- Final fitness value
- Timestamp
- Algorithm metadata

**Example:**
```json
{
  "@timestamp": "2025-01-15T14:30:45.123Z",
  "@version": "1",
  "message": "Final result",
  "logger_name": "edaf.results",
  "level": "INFO",
  "best_individual": {
    "genotype": [1, 1, 1, 1, ...],
    "fitness": 100.0
  }
}
```

**Parsing:**
```bash
# Extract best fitness
cat results.json | jq '.best_individual.fitness'

# Extract genotype
cat results.json | jq '.best_individual.genotype'
```

## Troubleshooting

### Port Already in Use

**Error**: `BindException: Address already in use`

**Solution:**
```bash
# Find process using the port
lsof -i :8888

# Kill the process
lsof -ti :8888 | xargs kill

# Or use a different port
java -jar examples/target/edaf.jar --prometheus-port 8889 examples/config/umda-max-ones.yaml
```

**Note**: Make sure to update `prometheus.yml` if you change the port.

### Prometheus Can't Find Config File

**Error**: `Error loading config (--config.file=~/prometheus.yml)`

**Solution**: Use `$HOME` instead of `~` (Prometheus doesn't expand `~`):

```bash
prometheus --config.file=$HOME/prometheus.yml --web.listen-address=:9090
```

Or use full path:
```bash
prometheus --config.file=/Users/$(whoami)/prometheus.yml --web.listen-address=:9090
```

### No Metrics in Prometheus

**Symptoms**: 
- `curl http://localhost:8888/metrics` returns nothing
- Prometheus UI shows "Empty query result"

**Solutions:**
1. **Check if EDAF is running with Prometheus:**
   ```bash
   # Must use --prometheus-port flag
   java -jar examples/target/edaf.jar --prometheus-port 8888 examples/config/umda-max-ones.yaml
   ```

2. **Check if algorithm implements `SupportsExecutionContext`:**
   - Modern algorithms (FDA, CEM, CGP, UMDA, etc.) support this
   - Legacy algorithms may not publish events

3. **Check port in Prometheus config:**
   ```yaml
   # prometheus.yml must match EDAF port
   - targets: ['localhost:8888']  # Must match --prometheus-port
   ```

4. **Wait for first generation:**
   - Metrics are published after each generation
   - Some metrics (like `edaf_fitness_best`) only appear after generation 1

5. **Check EDAF logs:**
   ```bash
   tail -f edaf.log
   ```
   Look for errors about PrometheusEventPublisher or ExecutionContext.

### Build Errors

**Error**: `Unable to access jarfile examples/target/edaf.jar`

**Solution:**
```bash
# Build the project
mvn clean package -DskipTests

# Verify JAR exists
ls -lh examples/target/edaf.jar
```

### Configuration Errors

**Error**: `UnrecognizedPropertyException` or `JsonMappingException`

**Common causes:**
- Incorrect YAML indentation
- Wrong property names (e.g., `lowerBound` vs `l-bound`)
- Missing required fields

**Solution**: Check configuration file against examples in `examples/config/`.

### Algorithm Not Found

**Error**: `IllegalArgumentException: Unknown algorithm: xyz`

**Solution:**
1. Check algorithm name in configuration matches available algorithms
2. Verify algorithm module is included in build
3. Check algorithm provider is registered via SPI

## Stopping Processes

### Stop EDAF

```bash
# Find EDAF process
ps aux | grep "edaf.jar" | grep -v grep

# Kill by PID
kill <PID>

# Or kill by port
lsof -ti :8888 | xargs kill
```

### Stop Prometheus

```bash
# Find Prometheus process
ps aux | grep prometheus | grep -v grep

# Kill by PID
kill <PID>

# Or kill by port
lsof -ti :9090 | xargs kill
```

## Additional Resources

- [Getting Started Guide](./docs/getting-started.md) - Installation and basic usage
- [Configuration Guide](./docs/configuration.md) - YAML configuration format
- [Metrics and Results Guide](./docs/metrics-and-results.md) - Detailed metrics documentation
- [Algorithms Reference](./docs/algorithms.md) - Algorithm descriptions and parameters
- [Extending the Framework](./docs/extending-the-framework.md) - Adding custom components
