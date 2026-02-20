# Boolean Function and Cryptography Suite

This document describes the EDAF v3 problem family for optimization of boolean functions with cryptographic criteria.

## 1) Why this suite exists

Many cryptographic constructions (S-box components, combining functions, filter functions) require boolean functions with strong statistical and algebraic properties. EDAF now provides a dedicated suite that keeps those objectives in first-class problem plugins rather than ad-hoc post-processing scripts.

## 2) Problem types

The suite ships four plugin types:

- `boolean-function`
  - direct truth-table optimization
  - genotype: `bitstring`, required length `2^n`
- `boolean-function-permutation`
  - balanced-by-construction encoding
  - genotype: `permutation-vector`, required size `2^n`
  - first `2^(n-1)` permutation positions define truth-table rows set to `1`
- `boolean-function-tree`
  - tokenized boolean expression optimization
  - genotype: `variable-length-vector`
  - tokens are parsed as a prefix boolean expression with bounded recursion depth
- `boolean-function-mo`
  - multi-objective variant
  - genotype: `bitstring`
  - emits `VectorFitness` with one objective per configured criterion

## 3) Criteria and scoring

Each function is scored through pluggable criteria:

- `balancedness`
  - target is exactly half ones in the truth table
  - normalized to `[0,1]` where `1` is strictly balanced
- `nonlinearity`
  - computed from fast Walsh-Hadamard transform
  - score is normalized by theoretical upper bound for the configured `n`
- `algebraic-degree`
  - computed from ANF coefficients via Mobius transform
  - normalized as `degree / n`

Default criterion list when omitted:

- `balancedness`
- `nonlinearity`
- `algebraic-degree`

## 4) Configuration parameters

Shared (`boolean-function*`):

- `n` (int): number of boolean variables, practical range `2..12`
- `criteria` (string[]): criterion ids and order
- `criterionWeights` (map<string,double>): scalar aggregation weights

Type-specific:

- `boolean-function-tree`
  - `maxDepth` (int): parser depth cap for token trees
- `boolean-function-mo`
  - `objectiveWeights` (double[]): scalar projection for vector fitness

## 5) Token grammar for `boolean-function-tree`

Let `variables = n`.

- tokens `< n`: variable references (`x0`, `x1`, ...)
- tokens are normalized by `floorMod(token, n + 9)`
- operator ids (`normalized - n`):
  - `0`: false
  - `1`: true
  - `2`: NOT
  - `3`: AND
  - `4`: OR
  - `5`: XOR
  - `6`: NAND
  - `7`: NOR
  - `8`: XNOR

Example for `n=2`, XOR expression `xor(x0,x1)` in prefix form:

- token sequence: `[7, 0, 1]` (because `n + 5 = 7`)

## 6) Ready benchmark configs

- `configs/benchmarks/crypto-boolean-umda-v3.yml`
- `configs/benchmarks/crypto-boolean-permutation-ehm-v3.yml`
- `configs/benchmarks/crypto-boolean-tree-eda-v3.yml`
- `configs/benchmarks/crypto-boolean-mo-v3.yml`
- batch launcher: `configs/batch-benchmark-crypto-v3.yml`

Run all:

```bash
./edaf batch -c configs/batch-benchmark-crypto-v3.yml
```

Validate one config:

```bash
./edaf config validate configs/benchmarks/crypto-boolean-umda-v3.yml
```

## 7) Persistence and dashboard visibility

With DB sink enabled, these runs are persisted exactly like other suites:

- experiment metadata in `experiments`
- flattened config paths in `experiment_params`
- run summary in `runs`
- per-iteration metrics in `iterations`
- event payloads in `events`

Web/API filtering examples:

```bash
curl "http://localhost:7070/api/runs?problem=boolean-function&status=COMPLETED"
curl "http://localhost:7070/api/runs?problem=boolean-function-tree"
```

## 8) How to add a new crypto criterion

1. Implement `CryptoFitnessCriterion` in:
   - `edaf-problems/src/main/java/com/knezevic/edaf/v3/problems/crypto/criteria`
2. Add criterion mapping in:
   - `CryptoCriteriaFactory.create(...)`
3. Document new criterion id and normalization rule.
4. Add unit tests in:
   - `edaf-problems/src/test/java/com/knezevic/edaf/v3/problems/crypto`
5. Optionally add benchmark config variant under `configs/benchmarks/`.

This keeps the suite open for criteria such as autocorrelation, resiliency, or propagation criteria without changing algorithm orchestration code.
