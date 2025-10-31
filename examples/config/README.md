Examples catalog

Run via the shaded CLI jar, e.g.:

```bash
java -jar examples/target/edaf.jar examples/config/<file>.yaml --seed 12345
```

General
- bmda-max-ones.yaml: BMDA on binary MaxOnes
- boa-sphere.yaml: BOA on FP Sphere
- cga-max-ones.yaml: cGA on binary MaxOnes (length-aware)
- ega-max-ones.yaml: Steady-state GA on MaxOnes
- fda-max-ones.yaml: FDA on binary MaxOnes (Bayesian network)
- cem-max-ones.yaml: CEM on binary MaxOnes (Cross-Entropy Method)
- cem-sphere.yaml: CEM on FP Sphere (continuous optimization)
- gga-max-ones.yaml: Generational GA on MaxOnes
- gga-mu-comma-lambda.yaml: GGA (mu,lambda) variant
- gga-mu-plus-lambda.yaml: GGA (mu+lambda) variant
- ltga-max-ones.yaml: LTGA on MaxOnes
- mimic-max-ones.yaml: MIMIC on MaxOnes
- pbil-max-ones.yaml: PBIL on MaxOnes
- rastrigin.yaml: GA on FP Rastrigin
- rastrigin-umda.yaml: UMDA on FP Rastrigin
- umda-max-ones.yaml: UMDA on MaxOnes

Problems
- problems/ackley-umda.yaml: UMDA on Ackley (FP)
- problems/knapsack-bmda.yaml: BMDA on 0/1 Knapsack (binary)
- problems/tsp-gga.yaml: GGA on TSP (permutation)

GP (Genetic Programming)
- gp/boolean-function-gp.yaml: GP evolving boolean function
- gp/multiplexer.yaml: GP solving 6-bit multiplexer
- gp/santa-fe-ant.yaml: GP Santa Fe Ant Trail
- gp/symbolic-regression.yaml: GP symbolic regression (y = x^4 + x^3 + x^2 + x)
- gp/iris-classification.yaml: GP classification on Iris dataset

CGP (Cartesian Genetic Programming)
- cgp-symbolic-regression.yaml: CGP on symbolic regression problem
- cgp-multiplexer.yaml: CGP on 6-bit multiplexer
- cgp-max-ones.yaml: CGP on MaxOnes problem
- cgp-parity.yaml: CGP on Parity problem
- cgp-boolean-function.yaml: CGP on Boolean function optimization

Notes
- All problems resolve to `com.knezevic.edaf.testing.problems.*` or `com.knezevic.edaf.algorithm.cgp.problems.*` classes.
- Metrics: add `--metrics` or `--prometheus-port <port>` to enable metrics.
- Use `--seed <number>` for reproducible runs.

