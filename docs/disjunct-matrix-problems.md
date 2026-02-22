# Disjunct Matrix Family (DM/RM/ADM)

This document defines the disjunct-matrix problem family implemented in EDAF v3 and maps each item directly to the paper formulas used in implementation.

Source of truth:

- `submission.pdf` (The Design of (Almost) Disjunct Matrices by Evolutionary Algorithms)
- `notes_almost_disjunct_matrices.pdf`

## 1) Formal Definitions Implemented

Let `A = (x_1^T, ..., x_N^T)` be an `M x N` binary matrix, where each column `x_j in {0,1}^M`.

`supp(x)` is the set of non-zero coordinates of `x`.

For any `t`-subset `S` of columns:

- `delta(S) = |{x_j notin S : supp(x_j) subseteq union_{x_i in S} supp(x_i)}|`

Implemented definitions:

1. `t`-disjunct:
   - for every subset `S` of size `t` and every `x_j notin S`, `supp(x_j) not subseteq union supp(S)`.
2. `(t,f)`-resolvable:
   - for every subset `S` of size `t`, `delta(S) <= f`.
3. `(t,epsilon)`-disjunct (ADM):
   - for every subset `S` of size `t`, `delta(S)/(N-t) <= epsilon`.

## 2) Fitness Functions (Exact)

The framework uses the exact functions from the paper:

1. `fit1(A) = sum_{S in S_t} delta(S)`  
   used by problem type `disjunct-matrix`.
2. `fit2(A) = |{S in S_t : delta(S) > f}|`  
   used by problem type `resolvable-matrix`.
3. `fit3(A) = fit1(A) / (C(N,t) * (N-t))`  
   used by problem type `almost-disjunct-matrix`.

All three are minimization objectives.

Code:

- `edaf-problems/src/main/java/com/knezevic/edaf/v3/problems/discrete/disjunct/DisjunctFitnessFunctions.java`

## 3) Genotype Encoding

Representation is `bitstring`, interpreted as an `M x N` matrix in **column-major** order:

- bits `[j*M, ..., j*M + (M-1)]` correspond to column `j`.
- required bitstring length is exactly `M*N`.

Code:

- `edaf-problems/src/main/java/com/knezevic/edaf/v3/problems/discrete/disjunct/DisjunctMatrix.java`
- `edaf-problems/src/main/java/com/knezevic/edaf/v3/problems/discrete/disjunct/AbstractDisjunctMatrixProblem.java`

## 4) Problem Types and YAML

New built-in problem plugin types:

- `disjunct-matrix` (DM / `fit1`)
- `resolvable-matrix` (RM / `fit2`)
- `almost-disjunct-matrix` (ADM / `fit3`)

Shared parameters:

- `m` or `rows`: number of rows `M`
- `n` or `columns`: number of columns `N`
- `t`: disjunctness parameter (`1 <= t < N`)
- `f`: RM threshold (`0 <= f < N`)
- `epsilon`: ADM threshold (`0 <= epsilon <= 1`)

Examples:

- `configs/benchmarks/disjunct-matrix-dm-v3.yml`
- `configs/benchmarks/disjunct-matrix-rm-v3.yml`
- `configs/benchmarks/disjunct-matrix-adm-v3.yml`

Run:

```bash
./edaf run -c configs/benchmarks/disjunct-matrix-dm-v3.yml
./edaf run -c configs/benchmarks/disjunct-matrix-rm-v3.yml
./edaf run -c configs/benchmarks/disjunct-matrix-adm-v3.yml
```

## 5) Validation Module

Validation API verifies DM/RM/ADM properties directly from definitions.

Main class:

- `edaf-problems/src/main/java/com/knezevic/edaf/v3/problems/discrete/disjunct/DisjunctMatrixValidator.java`

Output model:

- `edaf-problems/src/main/java/com/knezevic/edaf/v3/problems/discrete/disjunct/DisjunctMatrixValidationResult.java`

### 5.1) Exact Mode

For small instances (`C(N,t) <= maxExactSubsets`) validator enumerates all `t`-subsets and returns mathematically exact verdict.

### 5.2) Sampled Mode

For large instances validator samples random `t`-subsets uniformly and reports:

- sampled violation rate estimate
- confidence level
- Hoeffding error bound
- upper bound on true violation rate
- first violating witness subset if found

Sample size can be explicit or derived by:

- `n >= ln(2/alpha) / (2 * eps^2)`, where `alpha = 1 - confidence`.

This gives statistically justified approximation when exhaustive enumeration is infeasible.

### 5.3) Java Usage

```java
DisjunctMatrix matrix = DisjunctMatrix.fromDense(values);
DisjunctMatrixValidationOptions options =
        new DisjunctMatrixValidationOptions(200_000L, 0L, 0.95, 0.02, 12345L);

DisjunctMatrixValidationResult dm =
        DisjunctMatrixValidator.validateDisjunct(matrix, t, options);
DisjunctMatrixValidationResult rm =
        DisjunctMatrixValidator.validateResolvable(matrix, t, f, options);
DisjunctMatrixValidationResult adm =
        DisjunctMatrixValidator.validateAlmostDisjunct(matrix, t, epsilon, options);
```

## 6) Plugin and Registry Wiring

Plugins:

- `edaf-problems/src/main/java/com/knezevic/edaf/v3/problems/plugins/discrete/DisjunctMatrixProblemPlugin.java`
- `edaf-problems/src/main/java/com/knezevic/edaf/v3/problems/plugins/discrete/ResolvableMatrixProblemPlugin.java`
- `edaf-problems/src/main/java/com/knezevic/edaf/v3/problems/plugins/discrete/AlmostDisjunctMatrixProblemPlugin.java`

Service registration:

- `edaf-problems/src/main/resources/META-INF/services/com.knezevic.edaf.v3.core.plugins.ProblemPlugin`

## 7) Tests

Added tests:

- `edaf-problems/src/test/java/com/knezevic/edaf/v3/problems/discrete/disjunct/DisjunctFitnessFunctionsTest.java`
- `edaf-problems/src/test/java/com/knezevic/edaf/v3/problems/discrete/disjunct/DisjunctMatrixValidatorTest.java`

They cover exact values for hand-checkable matrices and sampled-mode behavior.
