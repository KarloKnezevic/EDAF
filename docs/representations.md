# Representations

Representations define genotype domains, validation, repair behavior, random initialization, and concise summaries used in logs/reports.

Contract: `Representation<G>`.

## 1) Available Representation Types

| Type | Java genotype class | Plugin class | Typical use |
| --- | --- | --- | --- |
| `bitstring` | `BitString` | `BitStringRepresentationPlugin` | binary optimization |
| `int-vector` | `IntVector` | `IntVectorRepresentationPlugin` | bounded integer variables |
| `categorical-vector` | `CategoricalVector` | `CategoricalVectorRepresentationPlugin` | finite symbols |
| `mixed-discrete-vector` | `MixedDiscreteVector` | `MixedDiscreteVectorRepresentationPlugin` | multiple categorical arities |
| `real-vector` | `RealVector` | `RealVectorRepresentationPlugin` | continuous spaces |
| `mixed-real-discrete-vector` | `MixedRealDiscreteVector` | `MixedRealDiscreteVectorRepresentationPlugin` | mixed-variable problems |
| `permutation-vector` | `PermutationVector` | `PermutationVectorRepresentationPlugin` | ranking and routing |
| `variable-length-vector` | `VariableLengthVector<Integer>` | `VariableLengthVectorRepresentationPlugin` | structured variable-length token sequences |

## 2) Parameters by Representation

### 2.1 `bitstring`

| Param | Type | Default |
| --- | --- | --- |
| `length` | int | `64` |

### 2.2 `int-vector`

| Param | Type | Default |
| --- | --- | --- |
| `length` | int | `32` |
| `min` | int | `0` |
| `max` | int | `10` |

### 2.3 `categorical-vector`

| Param | Type | Default |
| --- | --- | --- |
| `length` | int | `16` |
| `symbols` | string[] | `[A, B, C]` |

### 2.4 `mixed-discrete-vector`

Two configuration styles:

1. explicit cardinalities:

```yaml
representation:
  type: mixed-discrete-vector
  cardinalities: [2, 5, 3, 9]
```

2. uniform fallback cardinality:

```yaml
representation:
  type: mixed-discrete-vector
  length: 8
  cardinality: 4
```

### 2.5 `real-vector`

| Param | Type | Default |
| --- | --- | --- |
| `length` | int | `16` |
| `lower` | double | `-5.0` |
| `upper` | double | `5.0` |

### 2.6 `mixed-real-discrete-vector`

| Param | Type | Default |
| --- | --- | --- |
| `realDimensions` | int | `8` |
| `cardinalities` | int[] | `[4, 4]` |
| `lower` | double | `-5.0` |
| `upper` | double | `5.0` |

### 2.7 `permutation-vector`

| Param | Type | Default |
| --- | --- | --- |
| `size` | int | `20` |
| `length` | int | alias for `size` |

### 2.8 `variable-length-vector`

| Param | Type | Default |
| --- | --- | --- |
| `minLength` | int | `2` |
| `maxLength` | int | `16` |
| `maxToken` | int | `64` |

## 3) Domain Semantics and Repair

Each representation guarantees:

- `random(rng)` returns valid genotype in the declared domain.
- `isValid(genotype)` enforces domain constraints.
- `repair(genotype)` maps invalid input into domain.

Examples:

- `real-vector`: clips each dimension into `[lower, upper]`.
- `int-vector`: clamps each value into `[min, max]`.
- `permutation-vector`: invalid permutation repairs to identity fallback.
- `mixed-discrete-vector`: values are wrapped via modulo cardinality.
- `variable-length-vector`: pads/truncates to length bounds and normalizes tokens.

## 4) Distance Utilities

`edaf-representations` also includes distance helpers:

- `HammingDistance`
- `EuclideanDistance`
- `KendallTauDistance`

These are useful for metrics, niching, or custom diagnostics.

## 5) Compatibility with Model Families

Validator-enforced mapping:

- Discrete representations -> discrete models/algorithms
- Continuous representations -> continuous models/algorithms
- Permutation representation -> permutation models/algorithms

See [Configuration Reference](./configuration.md) for exact compatibility lists.

## 6) Example Config Snippets

### Bitstring

```yaml
representation:
  type: bitstring
  length: 128
```

### Real vector

```yaml
representation:
  type: real-vector
  length: 30
  lower: -30.0
  upper: 30.0
```

### Permutation

```yaml
representation:
  type: permutation-vector
  size: 50
```

### Mixed real/discrete

```yaml
representation:
  type: mixed-real-discrete-vector
  realDimensions: 10
  cardinalities: [3, 4, 7]
  lower: -2.0
  upper: 2.0
```

## 7) Adding a New Representation

1. Implement `Representation<YourGenotype>`.
2. Implement `RepresentationPlugin<YourGenotype>`.
3. Register plugin in:

```text
src/main/resources/META-INF/services/com.knezevic.edaf.v3.core.plugins.RepresentationPlugin
```

4. Use `type: your-representation-type` in config.

Detailed walkthrough: [Extending the Framework](./extending-the-framework.md).
