# Algorithms Reference

This document provides detailed descriptions of all algorithms implemented in the EDAF framework.

## Table of Contents

1. [Estimation of Distribution Algorithms (EDAs)](#estimation-of-distribution-algorithms-edas)
   - [UMDA](#umda-univariate-marginal-distribution-algorithm)
   - [PBIL](#pbil-population-based-incremental-learning)
   - [MIMIC](#mimic-mutual-information-maximizing-input-clustering)
   - [BMDA](#bmda-bivariate-marginal-distribution-algorithm)
   - [FDA](#fda-factorized-distribution-algorithm)
   - [CEM](#cem-cross-entropy-method)
   - [BOA](#boa-bayesian-optimization-algorithm)
   - [CGA](#cga-compact-genetic-algorithm)
2. [Genetic Algorithms (GAs)](#genetic-algorithms-gas)
   - [GGA](#gga-generational-genetic-algorithm)
   - [EGA](#ega-eliminative-genetic-algorithm)
   - [LTGA](#ltga-linkage-tree-genetic-algorithm)
3. [Genetic Programming (GP)](#genetic-programming-gp)
   - [GP](#gp-genetic-programming)
   - [CGP](#cgp-cartesian-genetic-programming)

## Estimation of Distribution Algorithms (EDAs)

Estimation of Distribution Algorithms are population-based optimization algorithms that build probabilistic models of promising solutions and sample new candidate solutions from these models. Unlike traditional genetic algorithms, EDAs do not use crossover and mutation operators, but instead learn a probability distribution over the search space.

### UMDA (Univariate Marginal Distribution Algorithm)

**Algorithm ID:** `umda`

**Description:**

UMDA is one of the simplest and most widely used EDAs. It assumes that all variables in the problem are independent, making it computationally efficient but limited to problems where variable dependencies are not critical.

**How It Works:**

1. **Initialization:** Create an initial population of random individuals and evaluate them.
2. **Selection:** Select the best individuals from the current population (typically top 50%).
3. **Model Building:** Estimate univariate marginal probabilities for each variable position based on the selected individuals.
   - For binary problems: Calculate P(x_i = 1) for each bit position i.
   - For continuous problems: Estimate mean and variance for each dimension.
4. **Sampling:** Generate a new population by sampling from the learned univariate distributions.
5. **Evaluation:** Evaluate all new individuals.
6. **Replacement:** Replace the old population with the new one.
7. **Repeat:** Steps 2-6 until termination condition is met.

**Algorithm Pseudocode:**

```
1. Initialize population P
2. Evaluate P
3. While not terminated:
     a. Select best individuals S from P
     b. Estimate P(x_i) for each variable i from S
     c. Sample new population P' from distributions
     d. Evaluate P'
     e. P = P'
```

**Parameters:**

| Parameter | Type | Default | Description |
|-----------|------|---------|-------------|
| `selection.size` | int | population_size / 2 | Number of individuals to select for model building |
| `statistics.type` | string | depends on genotype | Statistics component type (`BitwiseDistribution` for binary, `NormalDistribution` for continuous) |

**Supported Genotypes:**

- `binary` - Binary strings
- `fp` - Floating-point vectors
- `integer` - Integer vectors

**When to Use:**

- **Best for:** Problems with independent or weakly dependent variables
- **Strengths:** Fast, simple, good baseline algorithm
- **Weaknesses:** Cannot capture variable dependencies
- **Examples:** MaxOnes, decomposable problems, separable continuous optimization

**Configuration Example:**

```yaml
algorithm:
  name: umda
  population:
    size: 100
  selection:
    name: tournament
    size: 50
  statistics:
    type: BitwiseDistribution
  termination:
    maxGenerations: 100
```

**Complexity:**

- Time per generation: O(n × m) where n is population size, m is genotype length
- Space: O(n × m)

**References:**

- Larrañaga, P., & Lozano, J. A. (Eds.). (2002). *Estimation of distribution algorithms: A new tool for evolutionary computation*. Springer.

---

### PBIL (Population-Based Incremental Learning)

**Algorithm ID:** `pbil`

**Description:**

PBIL combines concepts from genetic algorithms and competitive learning. It maintains a single probability vector that is updated incrementally based on the best individual found in each generation.

**How It Works:**

1. **Initialization:** Initialize a probability vector (typically uniform: P(x_i = 1) = 0.5 for each bit).
2. **Sampling:** Generate a population by sampling from the current probability vector.
3. **Evaluation:** Evaluate all individuals in the population.
4. **Update:** Update the probability vector towards the best individual:
   ```
   P(x_i) = (1 - α) × P(x_i) + α × best_individual[i]
   ```
   where α is the learning rate.
5. **Repeat:** Steps 2-4 until termination condition is met.

**Algorithm Pseudocode:**

```
1. Initialize probability vector P = [0.5, 0.5, ..., 0.5]
2. While not terminated:
     a. Sample population from P
     b. Evaluate population
     c. Find best individual
     d. Update P towards best: P = (1-α)P + α×best
```

**Parameters:**

| Parameter | Type | Default | Description |
|-----------|------|---------|-------------|
| `population.size` | int | 100 | Population size |
| `parameters.learningRate` | double | 0.1 | Learning rate α (typically 0.01-0.1) |
| `statistics.type` | string | depends on genotype | Statistics component type |

**Supported Genotypes:**

- `binary` - Binary strings
- `fp` - Floating-point vectors

**When to Use:**

- **Best for:** Binary optimization problems, problems with smooth fitness landscapes
- **Strengths:** Simple, memory-efficient (single probability vector), good for binary problems
- **Weaknesses:** May converge prematurely, limited to independent variables
- **Examples:** MaxOnes, binary function optimization

**Configuration Example:**

```yaml
algorithm:
  name: pbil
  population:
    size: 50
  parameters:
    learningRate: 0.1
  statistics:
    type: BitwiseDistribution
  termination:
    maxGenerations: 200
```

**Complexity:**

- Time per generation: O(n × m) where n is population size, m is genotype length
- Space: O(m) - only probability vector stored

**References:**

- Baluja, S. (1994). Population-based incremental learning: A method for integrating genetic search based function optimization and competitive learning (No. CMU-CS-94-163). Carnegie Mellon University.

---

### MIMIC (Mutual-Information-Maximizing Input Clustering)

**Algorithm ID:** `mimic`

**Description:**

MIMIC models the distribution of promising solutions using a chain-like dependency structure, where each variable depends on exactly one other variable (except the first). This captures pairwise dependencies while remaining computationally tractable.

**How It Works:**

1. **Initialization:** Create an initial population and evaluate it.
2. **Selection:** Select the best individuals (typically top 50%).
3. **Model Building:** Build a chain structure that maximizes mutual information:
   - Calculate pairwise mutual information I(X_i, X_j) for all pairs
   - Build chain by greedily adding pairs with highest mutual information
   - Estimate conditional probabilities P(X_i | X_{i-1}) along the chain
4. **Sampling:** Sample new individuals using the chain model:
   - Sample X_1 from marginal P(X_1)
   - For each subsequent variable: Sample X_i from P(X_i | X_{i-1})
5. **Evaluation:** Evaluate new population.
6. **Replacement:** Replace old population with new one.
7. **Repeat:** Steps 2-6 until termination.

**Algorithm Pseudocode:**

```
1. Initialize population P
2. Evaluate P
3. While not terminated:
     a. Select best individuals S from P
     b. Calculate mutual information I(X_i, X_j) for all pairs
     c. Build chain: greedily connect variables with highest MI
     d. Estimate conditional probabilities along chain
     e. Sample new population P' from chain model
     f. Evaluate P'
     g. P = P'
```

**Parameters:**

| Parameter | Type | Default | Description |
|-----------|------|---------|-------------|
| `selection.size` | int | population_size / 2 | Number of individuals for model building |
| `statistics.type` | string | `ChainDistribution` | Statistics component type |

**Supported Genotypes:**

- `binary` - Binary strings only

**When to Use:**

- **Best for:** Binary problems with pairwise dependencies
- **Strengths:** Captures pairwise dependencies, more expressive than UMDA
- **Weaknesses:** Limited to chain structures, binary only
- **Examples:** Binary function optimization with dependencies

**Configuration Example:**

```yaml
algorithm:
  name: mimic
  population:
    size: 100
  selection:
    name: tournament
    size: 50
  statistics:
    type: ChainDistribution
  termination:
    maxGenerations: 100
```

**Complexity:**

- Time per generation: O(n × m²) - mutual information calculation is quadratic in genotype length
- Space: O(m²) - stores pairwise statistics

**References:**

- De Bonet, J. S., Isbell, C. L., & Viola, P. (1997). MIMIC: Finding optima by estimating probability densities. *Advances in neural information processing systems*, 424.

---

### FDA (Factorized Distribution Algorithm)

**Algorithm ID:** `fda`

**Description:**

FDA is a probabilistic model-building evolutionary algorithm that uses a Bayesian network to capture dependencies between variables. It factorizes the joint probability distribution into a product of marginal and conditional probabilities, allowing for efficient modeling of complex variable interactions.

**How It Works:**

1. **Initialization:** Create an initial population of random individuals and evaluate them.
2. **Selection:** Select the best individuals from the current population (typically top 50%).
3. **Model Building:** Learn a Bayesian network structure and parameters:
   - Calculate pairwise mutual information between all variable pairs
   - Build network structure using greedy parent selection (up to 2 parents per variable)
   - Estimate conditional probability tables (CPTs) for each variable given its parents
   - Variables with no parents use marginal probabilities
4. **Sampling:** Generate a new population by sampling from the Bayesian network:
   - Use topological ordering to sample variables
   - Sample variables with no parents from marginals
   - Sample variables with parents from conditional distributions
5. **Evaluation:** Evaluate all new individuals.
6. **Replacement:** Replace the old population with the new one.
7. **Repeat:** Steps 2-6 until termination condition is met.

**Algorithm Pseudocode:**

```
1. Initialize population P
2. Evaluate P
3. While not terminated:
     a. Select best individuals S from P
     b. Calculate mutual information I(X_i, X_j) for all pairs
     c. Build Bayesian network structure (greedy parent selection)
     d. Estimate CPTs: P(X_i | parents(X_i))
     e. Sample new population P' from Bayesian network
     f. Evaluate P'
     g. P = P'
```

**Parameters:**

| Parameter | Type | Default | Description |
|-----------|------|---------|-------------|
| `selection.size` | int | population_size / 2 | Number of individuals to select for model building |
| `statistics.type` | string | `FdaStatistics` | Statistics component type (automatically created for FDA) |

**Supported Genotypes:**

- `binary` - Binary strings only

**When to Use:**

- **Best for:** Binary problems with complex variable dependencies
- **Strengths:** Captures higher-order dependencies, more expressive than MIMIC/BMDA, Bayesian network structure learning
- **Weaknesses:** Higher computational cost than simpler EDAs, binary only, network structure learning can be expensive
- **Examples:** Binary optimization with complex interactions, deceptive problems, problems with building blocks

**Configuration Example:**

```yaml
algorithm:
  name: fda
  population:
    size: 100
  selection:
    name: tournament
    size: 50
  termination:
    maxGenerations: 100
problem:
  genotype:
    type: binary
    length: 100
```

**Complexity:**

- Time per generation: O(n × m² + m² × log m) - mutual information calculation is quadratic, plus network structure learning
- Space: O(m² × 2^k) where k is max parents per variable (typically k=2, so O(m² × 4))

**Implementation Details:**

- Maximum of 2 parents per variable to keep complexity manageable
- Uses cycle detection to ensure acyclic Bayesian network structure
- Laplace smoothing applied when estimating CPTs for robustness
- Topological ordering ensures correct sampling order

**References:**

- Mühlenbein, H., & Mahnig, T. (1999). FDA—A scalable evolutionary algorithm for the optimization of additively decomposed functions. *Evolutionary Computation*, 7(4), 353-376.

---

### CEM (Cross-Entropy Method)

**Algorithm ID:** `cem`

**Description:**

CEM is a stochastic optimization technique that minimizes cross-entropy between a target distribution and a parametric distribution. It iteratively updates the parametric distribution based on elite solutions, bringing it closer to the optimal solution.

**How It Works:**

1. **Initialization:** Initialize parameters of the parametric distribution:
   - For binary problems: Bernoulli distribution with uniform probabilities (0.5)
   - For continuous problems: Gaussian distribution with zero mean and unit variance
2. **Sampling:** Generate a batch of candidate solutions by sampling from the parametric distribution.
3. **Evaluation:** Evaluate all candidate solutions using the fitness function.
4. **Elite Selection:** Select the top-performing solutions (elite fraction, typically 0.1-0.2 of the batch).
5. **Distribution Update:** Update distribution parameters based on elite solutions:
   - For binary: Update probabilities based on frequency of 1s in elite solutions
   - For continuous: Update mean and standard deviation based on elite solutions' statistics
   - Minimize cross-entropy between empirical elite distribution and parametric distribution
6. **Repeat:** Steps 2-5 until termination condition is met.

**Algorithm Pseudocode:**

```
1. Initialize parametric distribution P (Bernoulli or Gaussian)
2. While not terminated:
     a. Sample batch of candidates from P
     b. Evaluate all candidates
     c. Select elite solutions (top fraction)
     d. Update P: minimize cross-entropy with elite distribution
     e. Clip parameters to avoid degenerate distributions
```

**Parameters:**

| Parameter | Type | Default | Description |
|-----------|------|---------|-------------|
| `batchSize` | int | 100 | Number of candidate solutions per iteration |
| `eliteFraction` | double | 0.1 | Proportion of candidates selected as elite (0.1-0.2) |
| `learningRate` | double | 0.7 | Learning rate for distribution update (typically 0.5-0.9) |
| `statistics.type` | string | `CemStatistics` | Statistics component type (automatically created) |

**Supported Genotypes:**

- `binary` - Binary strings (Bernoulli distribution)
- `fp` - Floating-point vectors (Gaussian distribution)

**When to Use:**

- **Best for:** Black-box optimization, problems without gradient information, both continuous and discrete problems
- **Strengths:** No gradient required, works for high-dimensional spaces, handles both binary and continuous problems, adaptive distribution updates
- **Weaknesses:** Requires careful tuning of elite fraction and learning rate, may need many function evaluations
- **Examples:** Continuous optimization, binary optimization, hyperparameter tuning, expensive function evaluation

**Configuration Example:**

```yaml
algorithm:
  name: cem
  population:
    size: 100
  selection:
    name: tournament
    size: 10
  parameters:
    batchSize: 100
    eliteFraction: 0.1
    learningRate: 0.7
  termination:
    max-generations: 100
problem:
  genotype:
    type: binary  # or fp for continuous
    length: 100
```

**Complexity:**

- Time per iteration: O(b × m + e × m) where b is batch size, e is elite size, m is genotype length
- Space: O(m) - stores distribution parameters

**Implementation Details:**

- Probability clipping: [0.05, 0.95] for binary to avoid degenerate distributions
- Minimum standard deviation: 0.01 for continuous to prevent collapse
- Elite fraction typically 0.1-0.2 (top 10-20%)
- Learning rate typically 0.5-0.9 (0.7 is standard)

**References:**

- Rubinstein, R. Y., & Kroese, D. P. (2004). *The cross-entropy method: a unified approach to combinatorial optimization, Monte-Carlo simulation and machine learning*. Springer Science & Business Media.

---

### BMDA (Bivariate Marginal Distribution Algorithm)

**Algorithm ID:** `bmda`

**Description:**

BMDA extends UMDA by considering pairwise dependencies between variables. It builds a bivariate probability model capturing relationships between pairs of variables.

**How It Works:**

1. **Initialization:** Create an initial population and evaluate it.
2. **Selection:** Select the best individuals from the current population.
3. **Model Building:** Estimate bivariate marginal distributions P(X_i, X_j) for all pairs:
   - For binary: Count co-occurrences of bit pairs (00, 01, 10, 11)
   - Normalize to get joint probabilities
4. **Sampling:** Sample new individuals using the bivariate model:
   - Sample first variable from marginal P(X_1)
   - For each subsequent variable: Sample from conditional P(X_i | X_{i-1}) or marginal if no strong dependency
5. **Evaluation:** Evaluate new population.
6. **Replacement:** Replace old population with new one.
7. **Repeat:** Steps 2-6 until termination.

**Parameters:**

| Parameter | Type | Default | Description |
|-----------|------|---------|-------------|
| `selection.size` | int | population_size / 2 | Number of individuals for model building |
| `statistics.type` | string | `BivariateDistribution` | Statistics component type |

**Supported Genotypes:**

- `binary` - Binary strings only

**When to Use:**

- **Best for:** Binary problems with strong pairwise dependencies
- **Strengths:** Captures pairwise interactions, more expressive than UMDA
- **Weaknesses:** Higher computational cost, binary only, does not capture higher-order dependencies
- **Examples:** Binary optimization with local dependencies

**Configuration Example:**

```yaml
algorithm:
  name: bmda
  population:
    size: 100
  selection:
    name: tournament
    size: 50
  statistics:
    type: BivariateDistribution
  termination:
    maxGenerations: 100
```

**Complexity:**

- Time per generation: O(n × m²) - quadratic in genotype length
- Space: O(m²) - stores all pairwise distributions

---

### BOA (Bayesian Optimization Algorithm)

**Algorithm ID:** `boa`

**Description:**

BOA uses Bayesian networks to model dependencies between variables. It learns the structure and parameters of a Bayesian network from promising solutions and samples new solutions from this model.

**Note:** The current implementation in EDAF is a simplified version that uses a Gaussian Process surrogate model for continuous optimization (Bayesian Optimization), which is different from the traditional BOA for discrete problems.

**How It Works (Current Implementation):**

1. **Initialization:** Sample n_init random points and evaluate them to build initial dataset.
2. **Model Building:** Fit a Gaussian Process (GP) surrogate model to the observed data.
3. **Acquisition:** Use an acquisition function (Expected Improvement) to select the next point to evaluate:
   - Find point with maximum expected improvement over current best
4. **Evaluation:** Evaluate the selected point.
5. **Update:** Add the new observation to the dataset and update the GP model.
6. **Repeat:** Steps 3-5 for n_iter iterations.

**Algorithm Pseudocode:**

```
1. Sample n_init random points, evaluate, build dataset D
2. Fit Gaussian Process GP to D
3. For i = 1 to n_iter:
     a. Find x* = argmax EI(x) using GP
     b. Evaluate f(x*)
     c. Add (x*, f(x*)) to D
     d. Refit GP to D
```

**Parameters:**

| Parameter | Type | Default | Description |
|-----------|------|---------|-------------|
| `parameters.nInit` | int | 10 | Number of initial random evaluations |
| `parameters.nIter` | int | 100 | Number of optimization iterations |
| `parameters.min` | double | -10.0 | Lower bound for search space |
| `parameters.max` | double | 10.0 | Upper bound for search space |

**Supported Genotypes:**

- `fp` - Floating-point vectors only

**When to Use:**

- **Best for:** Expensive continuous optimization (few function evaluations)
- **Strengths:** Sample-efficient, good for expensive objective functions
- **Weaknesses:** Limited to continuous problems, requires surrogate model fitting
- **Examples:** Hyperparameter tuning, expensive simulation-based optimization

**Configuration Example:**

```yaml
algorithm:
  name: boa
  parameters:
    nInit: 20
    nIter: 200
    min: -5.0
    max: 5.0
  termination:
    maxGenerations: 200
problem:
  genotype:
    type: fp
    length: 10
    lowerBound: -5.0
    upperBound: 5.0
```

**Complexity:**

- Time per iteration: O(n³) - GP fitting is cubic in number of observations
- Space: O(n²) - GP covariance matrix

**References:**

- Mockus, J. (2012). *Bayesian approach to global optimization: theory and applications*. Springer Science & Business Media.

---

### CGA (Compact Genetic Algorithm)

**Algorithm ID:** `cga`

**Description:**

CGA is a minimalistic EDA that simulates the behavior of a simple genetic algorithm without storing the actual population. It maintains only a probability vector representing the distribution of alleles in the population.

**How It Works:**

1. **Initialization:** Initialize probability vector P = [0.5, 0.5, ..., 0.5] (each bit has equal probability).
2. **Competition:** In each generation:
   - Generate two individuals by sampling from probability vector
   - Evaluate both individuals
   - Identify winner (better fitness) and loser
3. **Update:** Update probability vector towards winner:
   ```
   For each position i where winner[i] ≠ loser[i]:
     if winner[i] = 1: P[i] += 1/n
     else: P[i] -= 1/n
   ```
   where n is the virtual population size.
4. **Boundary:** Clip P[i] to [0, 1] if needed.
5. **Repeat:** Steps 2-4 until termination or convergence.

**Algorithm Pseudocode:**

```
1. Initialize P = [0.5, ..., 0.5]
2. While not terminated:
     a. Sample two individuals from P
     b. Evaluate both
     c. Identify winner
     d. Update P: P[i] += (winner[i] - P[i]) / n
     e. Clip P[i] to [0, 1]
```

**Parameters:**

| Parameter | Type | Default | Description |
|-----------|------|---------|-------------|
| `parameters.n` | int | 100 | Virtual population size (update step size = 1/n) |
| `problem.genotype.length` | int | required | Genotype length |

**Supported Genotypes:**

- `binary` - Binary strings only

**When to Use:**

- **Best for:** Binary optimization with limited memory
- **Strengths:** Extremely memory-efficient, simple, fast
- **Weaknesses:** Binary only, assumes independence, limited exploration
- **Examples:** Binary optimization problems, embedded systems with memory constraints

**Configuration Example:**

```yaml
algorithm:
  name: cga
  parameters:
    n: 100
  termination:
    maxGenerations: 1000
problem:
  genotype:
    type: binary
    length: 100
```

**Complexity:**

- Time per generation: O(m) where m is genotype length
- Space: O(m) - only probability vector

**References:**

- Harik, G. R., Lobo, F. G., & Goldberg, D. E. (1999). The compact genetic algorithm. *IEEE transactions on evolutionary computation*, 3(4), 287-297.

---

## Genetic Algorithms (GAs)

Traditional genetic algorithms use crossover and mutation operators to evolve a population of candidate solutions.

### GGA (Generational Genetic Algorithm)

**Algorithm ID:** `gga`

**Description:**

GGA is the classic generational genetic algorithm where the entire population is replaced in each generation. It follows the traditional evolutionary cycle: selection, crossover, mutation, and replacement.

**How It Works:**

1. **Initialization:** Create an initial random population and evaluate all individuals.
2. **Generational Loop:** While termination condition not met:
   - **Elitism:** Copy the best `elitism` individuals to the new population.
   - **Reproduction:** Until new population is full:
     - Select two parents using selection operator
     - Apply crossover to create offspring
     - Apply mutation to offspring
     - Add offspring to new population
   - **Evaluation:** Evaluate all new individuals.
   - **Replacement:** Replace old population with new population.
   - **Update Best:** Track best individual found so far.

**Algorithm Pseudocode:**

```
1. Initialize population P
2. Evaluate P
3. While not terminated:
     a. Create empty new population P'
     b. Copy best elitism individuals to P'
     c. While |P'| < |P|:
          - Select parents from P
          - Crossover → offspring
          - Mutate offspring
          - Add to P'
     d. Evaluate P'
     e. P = P'
     f. Update best
```

**Parameters:**

| Parameter | Type | Default | Description |
|-----------|------|---------|-------------|
| `elitism` | int | 0 | Number of best individuals to preserve |
| `selection.name` | string | `tournament` | Selection operator |
| `crossing.name` | string | depends on genotype | Crossover operator |
| `mutation.name` | string | depends on genotype | Mutation operator |

**Supported Genotypes:**

- All: `binary`, `fp`, `integer`, `permutation`, `tree`

**When to Use:**

- **Best for:** General-purpose optimization, problems with exploitable structure
- **Strengths:** Flexible, well-understood, works with any genotype
- **Weaknesses:** May lose diversity, requires good operators
- **Examples:** All types of optimization problems

**Configuration Example:**

```yaml
algorithm:
  name: gga
  population:
    size: 100
  elitism: 2
  selection:
    name: tournament
    size: 2
  crossing:
    name: one-point
  mutation:
    name: simple
    probability: 0.01
  termination:
    maxGenerations: 100
```

**Complexity:**

- Time per generation: O(n × m × O(eval)) where n is population size, m is operator cost
- Space: O(n × m)

**References:**

- Goldberg, D. E. (1989). *Genetic algorithms in search, optimization, and machine learning*. Addison-Wesley.

---

### EGA (Eliminative Genetic Algorithm)

**Algorithm ID:** `ega`

**Description:**

EGA is a steady-state genetic algorithm where only one individual is replaced per generation. It maintains constant population size and focuses on gradual improvement.

**How It Works:**

1. **Initialization:** Create an initial random population and evaluate all individuals.
2. **Steady-State Loop:** While termination condition not met:
   - Select two parents using selection operator
   - Apply crossover to create offspring
   - Apply mutation to offspring
   - Evaluate offspring
   - If offspring is better than worst individual in population:
     - Remove worst individual
     - Add offspring to population
   - Update best individual if improved

**Algorithm Pseudocode:**

```
1. Initialize population P
2. Evaluate P
3. While not terminated:
     a. Select two parents from P
     b. Crossover → offspring
     c. Mutate offspring
     d. Evaluate offspring
     e. If offspring better than worst:
          - Remove worst from P
          - Add offspring to P
     f. Update best
```

**Parameters:**

Same as GGA (except no `elitism` parameter needed).

**Supported Genotypes:**

- All: `binary`, `fp`, `integer`, `permutation`, `tree`

**When to Use:**

- **Best for:** Problems where diversity maintenance is important
- **Strengths:** Maintains diversity, gradual convergence, good exploration
- **Weaknesses:** Slower convergence, requires more generations
- **Examples:** Multimodal optimization, complex landscapes

**Configuration Example:**

```yaml
algorithm:
  name: ega
  population:
    size: 100
  selection:
    name: tournament
    size: 2
  crossing:
    name: one-point
  mutation:
    name: simple
    probability: 0.01
  termination:
    maxGenerations: 500
```

**Complexity:**

- Time per generation: O(m × O(eval)) - single evaluation per generation
- Space: O(n × m)

---

### LTGA (Linkage Tree Genetic Algorithm)

**Algorithm ID:** `ltga`

**Description:**

LTGA builds a linkage tree that captures dependencies between variables and uses this tree to guide crossover operations. It uses the Gene Pool Optimal Mixing (GPOM) crossover operator.

**How It Works:**

1. **Initialization:** Create an initial population and evaluate it.
2. **Linkage Tree Building:** In each generation:
   - Calculate univariate probabilities P(x_i) for each variable
   - Calculate pairwise joint probabilities P(x_i, x_j)
   - Calculate mutual information I(X_i, X_j) between all pairs
   - Build hierarchical clustering tree (UPGMA) based on mutual information
3. **Crossover:** Use linkage tree to guide GPOM crossover:
   - Select two parents
   - Traverse linkage tree, copying subtrees from better parent
   - Creates offspring that preserves good building blocks
4. **Evaluation & Replacement:** Evaluate offspring and replace worst if better
5. **Repeat:** Steps 2-4 until termination

**Algorithm Pseudocode:**

```
1. Initialize population P
2. Evaluate P
3. While not terminated:
     a. Build linkage tree from P using mutual information
     b. Select parents from P
     c. Apply GPOM crossover using linkage tree
     d. Mutate offspring
     e. Evaluate offspring
     f. Replace worst if offspring better
     g. Update best
```

**Parameters:**

| Parameter | Type | Default | Description |
|-----------|------|---------|-------------|
| `selection.name` | string | `tournament` | Selection operator |
| `mutation.name` | string | `simple` | Mutation operator |

**Supported Genotypes:**

- `binary` - Binary strings only

**When to Use:**

- **Best for:** Binary problems with complex variable dependencies
- **Strengths:** Automatically discovers linkage, preserves building blocks
- **Weaknesses:** Binary only, computationally expensive (O(m²) per generation)
- **Examples:** Binary function optimization, deceptive problems

**Configuration Example:**

```yaml
algorithm:
  name: ltga
  population:
    size: 100
  selection:
    name: tournament
    size: 2
  mutation:
    name: simple
    probability: 0.01
  termination:
    maxGenerations: 100
problem:
  genotype:
    type: binary
    length: 100
```

**Complexity:**

- Time per generation: O(n × m²) - linkage tree building is quadratic
- Space: O(m²) - linkage tree storage

**References:**

- Thierens, D. (2010). The linkage tree genetic algorithm. *International Conference on Parallel Problem Solving from Nature* (pp. 264-273). Springer.

---

## Genetic Programming (GP)

Genetic Programming evolves computer programs represented as trees.

### GP (Genetic Programming)

**Algorithm ID:** `gp`

**Description:**

Standard Genetic Programming that evolves a population of program trees to solve problems. Programs are represented as syntax trees where internal nodes are functions and leaves are terminals.

**How It Works:**

1. **Initialization:** Create an initial population of random program trees (ramped half-and-half).
2. **Generational Loop:** While termination not met:
   - **Elitism:** Copy best individuals to next generation
   - **Reproduction:**
     - Select parents
     - With probability `crossoverRate`: apply subtree crossover
     - With probability `mutationRate`: apply subtree mutation
     - Otherwise: copy parent
     - Add to new population
   - **Evaluation:** Evaluate all new programs
   - **Replacement:** Replace old population
3. **Termination:** Return best program found

**Parameters:**

| Parameter | Type | Default | Description |
|-----------|------|---------|-------------|
| `elitismSize` | int | 1 | Number of best individuals to preserve |
| `crossoverRate` | double | 0.9 | Probability of applying crossover |
| `mutationRate` | double | 0.1 | Probability of applying mutation |
| `selection.name` | string | `tournament` | Selection operator |

**Supported Genotypes:**

- `tree` - Program trees

**When to Use:**

- **Best for:** Symbolic regression, program synthesis, symbolic expression discovery
- **Strengths:** Flexible representation, discovers novel solutions
- **Weaknesses:** May produce bloated programs, slow evaluation
- **Examples:** Symbolic regression, Santa Fe Ant Trail, multiplexer problems

**Configuration Example:**

```yaml
algorithm:
  name: gp
  population:
    size: 100
  parameters:
    elitismSize: 1
    crossoverRate: 0.9
    mutationRate: 0.1
  selection:
    name: tournament
    size: 2
  termination:
    maxGenerations: 50
problem:
  genotype:
    type: tree
    maxDepth: 10
```

**Complexity:**

- Time per generation: O(n × O(eval)) - evaluation cost depends on tree size
- Space: O(n × d) where d is average tree depth

**References:**

- Koza, J. R. (1992). *Genetic programming: on the programming of computers by means of natural selection*. MIT press.

---

### CGP (Cartesian Genetic Programming)

**Algorithm ID:** `cgp`

**Description:**

Cartesian Genetic Programming is a form of genetic programming that represents programs as directed acyclic graphs (DAGs) in a 2D grid of nodes. This implementation follows the classical model of CGP with a fixed-size genotype and a graph-based phenotype.

Unlike tree-based GP, CGP uses a fixed grid structure where nodes can be reused (multiple outputs can connect to the same node), and typically uses a fixed function set. A key feature of CGP is its use of "inactive" genes (neutrality), which do not affect the phenotype but can be activated by subsequent mutations, allowing for more effective exploration of the solution space.

**How It Works:**

1. **Initialization:** Create an initial population of random CGP individuals (integer arrays representing the graph structure).
2. **Genotype-Phenotype Mapping:** Decode each genotype into an executable program graph (phenotype).
3. **Evaluation:** Evaluate each individual by executing the program graph with problem inputs.
4. **Selection:** Select parents using tournament selection.
5. **Reproduction:**
   - Apply crossover (if enabled) to create offspring
   - Apply mutation to modify genotype genes (function indices, connection genes, output connections)
6. **Replacement:** Use either generational (full replacement) or steady-state (single individual replacement) strategy.
7. **Repeat:** Steps 3-6 until termination condition is met.

**Genotype Structure:**

The CGP genotype is a fixed-length integer array encoding:
- Node genes: For each node in the grid, connection genes (inputs) and function index
- Output genes: Connections to nodes or inputs that form the program outputs

```
Genotype: [in1_0, in2_0, func_0, in1_1, in2_1, func_1, ..., out_0, out_1, ...]
            |---- Node 0 ----|---- Node 1 ----| ... |--- Outputs ---|

Phenotype (graph structure):
     +-------+
x0 --| Node 0|--+
     +-------+  |   +-------+
                +---| Node 2|--> Output 0
     +-------+      +-------+
x1 --| Node 1|--+
     +-------+
```

**Algorithm Pseudocode:**

```
1. Initialize population P of size N
2. For each individual i in P:
    3. Decode genotype to phenotype (graph)
    4. Evaluate phenotype on problem
5. While not terminated:
    6. Sort population by fitness
    7. For each offspring to create:
        8. Select parents (tournament selection)
        9. Apply crossover (if enabled) or copy parent
        10. Mutate genotype (modify connection/function genes)
        11. Decode genotype to phenotype
        12. Evaluate phenotype
    13. Apply replacement strategy (generational or steady-state)
    14. Update best solution
```

**Parameters:**

| Parameter | Type | Description | Default |
|-----------|------|-------------|---------|
| `populationSize` | `int` | Number of individuals in population | `100` |
| `mutationRate` | `double` | Probability of mutating each gene | `0.02` |
| `rows` | `int` | Number of rows in the 2D grid | `1` |
| `cols` | `int` | Number of columns in the 2D grid | `20` |
| `levelsBack` | `int` | Number of previous columns a node can connect to (0 = any) | `5` |
| `useCrossover` | `boolean` | Whether to use crossover | `false` |
| `crossoverRate` | `double` | Probability of applying crossover | `0.8` |
| `replacementStrategy` | `enum` | `GENERATIONAL` or `STEADY_STATE` | `GENERATIONAL` |

**Supported Genotypes:**

- **Integer arrays:** Fixed-length integer arrays encoding graph structure
- **Problems:** Must implement `CgpProblem` interface, which provides:
  - Number of inputs
  - Number of outputs
  - Function set (list of `Function` objects)

**Configuration Example:**

```yaml
problem:
  class: com.knezevic.edaf.algorithm.cgp.problems.CgpSymbolicRegressionProblem
  optimization: min
  genotype:
    type: integer
    length: 100

algorithm:
  name: cgp
  population:
    size: 100
  selection:
    name: tournament
    size: 5
  termination:
    max-generations: 200
  parameters:
    mutationRate: 0.02
    rows: 1
    cols: 20
    levelsBack: 10
    useCrossover: false
    replacementStrategy: GENERATIONAL
```

**When to Use:**

- **Best for:** Symbolic regression, image processing, digital circuit design, problems requiring node reuse
- **Strengths:**
  - Efficient graph representation
  - Handles large programs well
  - Neutral mutations allow effective exploration
  - Fixed structure enables optimization
- **Weaknesses:**
  - More complex than standard GP
  - Fixed grid structure limits flexibility
  - Requires problem to implement `CgpProblem` interface

**Implementation Details:**

- **Function Set:** Reuses `com.knezevic.edaf.genotype.tree.primitives.Function` from `genotype-tree` module
- **Decoder:** Maps genotype (integer array) to executable program graph
- **Mutation:** Modifies connection genes, function indices, and output connections based on `levelsBack` constraint
- **Crossover:** One-point crossover on genotype arrays
- **Replacement:** Supports both generational and steady-state strategies

**Complexity Analysis:**

- **Time:** O(n × g × e) where:
  - n = population size
  - g = grid size (rows × cols)
  - e = evaluation time per individual
- **Space:** O(n × g) for population storage

**References:**

- Miller, J. F., & Thomson, P. (2000). Cartesian genetic programming. *European Conference on Genetic Programming* (pp. 121-132). Springer.

---

## Algorithm Comparison Matrix

| Algorithm | Genotype | Dependencies | Complexity | Memory | Best Use Case |
|-----------|----------|--------------|------------|--------|---------------|
| UMDA | binary, fp, int | None | O(n×m) | O(n×m) | Independent variables |
| PBIL | binary, fp | None | O(n×m) | O(m) | Binary optimization |
| MIMIC | binary | Pairwise | O(n×m²) | O(m²) | Binary with dependencies |
| BMDA | binary | Pairwise | O(n×m²) | O(m²) | Binary pairwise deps |
| FDA | binary | All (Bayesian network) | O(n×m²) | O(m²×4) | Complex dependencies |
| CEM | binary, fp | None | O(b×m) | O(m) | Black-box optimization |
| BOA | fp | All | O(n³) | O(n²) | Expensive continuous |
| CGA | binary | None | O(m) | O(m) | Memory-constrained |
| GGA | all | None | O(n×m) | O(n×m) | General purpose |
| EGA | all | None | O(m) | O(n×m) | Diversity important |
| LTGA | binary | Tree | O(n×m²) | O(m²) | Binary linkage |
| GP | tree | None | O(n×d) | O(n×d) | Program synthesis |
| CGP | graph | None | O(n×grid) | O(n×grid) | Circuit design |

Legend:
- n = population size
- m = genotype length
- d = average tree depth
- grid = rows × cols

---

## Choosing an Algorithm

### For Binary Problems:
- **Independent variables:** UMDA, PBIL, CGA, CEM
- **Pairwise dependencies:** MIMIC, BMDA
- **Complex dependencies:** FDA, LTGA

### For Continuous Problems:
- **Fast optimization:** UMDA, CEM, GGA, EGA
- **Expensive evaluations:** BOA
- **Black-box optimization:** CEM, BOA

### For Permutation Problems:
- **TSP, scheduling:** GGA, EGA (with permutation operators)

### For Program/Tree Problems:
- **Symbolic regression:** GP
- **Circuit design:** CGP

### General Recommendations:
- **Start with:** GGA or UMDA (good baselines)
- **Need diversity:** EGA
- **Memory constrained:** CGA, PBIL
- **Expensive function:** BOA
- **Black-box optimization:** CEM
- **Complex dependencies:** FDA, LTGA, MIMIC, BMDA

---

## Further Reading

- [Getting Started Guide](./getting-started.md) - How to run algorithms
- [Configuration Guide](./configuration.md) - Configuration options
- [Architecture Guide](./architecture.md) - Framework internals
- [Extending the Framework](./extending-the-framework.md) - How to add custom components
