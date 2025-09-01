# Estimation of Distribution Algorithms Framework (EDAF)

Estimation of distribution algorithms (EDAs), sometimes called probabilistic 
model-building genetic algorithms (PMBGAs), are stochastic optimization methods 
that guide the search for the optimum by building and sampling explicit probabilistic 
models of promising candidate solutions. Optimization is viewed as a series of
incremental updates of a probabilistic model, starting with the model
encoding the uniform distribution over admissible solutions and ending with
the model that generates only the global optima.

This is a completely redesigned and rebuilt Java framework for Estimation of Distribution Algorithms (EDAs), based on the original work by Karlo Knezevic.

## Documentation

For detailed information about the framework, please refer to the documentation:

*   [Getting Started](./docs/getting-started.md)
*   [Architecture](./docs/architecture.md)
*   [Configuration](./docs/configuration.md)
*   [Extending the Framework](./docs/extending-the-framework.md)

## Available Algorithms

The framework includes implementations of several popular evolutionary algorithms:

*   **Generational Genetic Algorithm (gGA):** A traditional GA where the entire population is replaced in each generation.
*   **Eliminative Genetic Algorithm (eGA):** A steady-state GA where one individual is replaced in each generation.
*   **Compact Genetic Algorithm (cGA):** An EDA that simulates the behavior of a simple GA with a large population.
*   **Univariate Marginal Distribution Algorithm (UMDA):** An EDA that assumes the variables are independent.
*   **Population-Based Incremental Learning (PBIL):** An EDA that uses a probability vector to generate new individuals.
*   **MIMIC:** An EDA that uses a chain-like probabilistic model.
*   **Linkage Tree Genetic Algorithm (LTGA):** A GA that uses a linkage tree to guide crossover.
*   **Bayesian Optimization Algorithm (BOA):** An EDA that uses a Bayesian network to model the distribution of promising solutions.
*   **Bivariate Marginal Distribution Algorithm (BMDA):** An EDA that considers pairwise dependencies between variables.

For more details on the architecture and how to use these algorithms, please refer to the [Architecture](./docs/architecture.md) documentation.

## How to Build

The project is built with Maven. To build the project, run the following command from the root directory:

```
mvn clean install
```