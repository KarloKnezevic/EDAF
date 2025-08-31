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

## How to Build

The project is built with Maven. To build the project, run the following command from the root directory:

```
mvn clean install
```