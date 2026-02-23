# Grammar GP Suite

This folder contains runnable grammar-based GP experiments for symbolic regression and symbolic classification.

## Structure

- `boolean/`: automatic grammar mode (`grammar.mode: auto`) on boolean benchmarks.
- `regression/`: automatic grammar mode on Nguyen symbolic regression.
- `classification/`: automatic grammar mode on CSV classification (Iris multiclass + Wine Recognition multiclass).
- `custom_grammar/`: user-defined BNF grammar examples + matching configs.

Classification configs now include:

- `classification-iris-*.yml` (3-class Iris)
- `classification-wine-multiclass-*.yml` (3-class Wine Recognition, 13 features)

## Included algorithms

All configs use discrete EDA drivers compatible with grammar-bitstring encoding:

- `umda`
- `chow-liu-eda`
- `boa`
- `hboa`
- `ebna`

## Running one config

```bash
mvn -q -pl edaf-cli -am package
java -jar /Users/karloknezevic/Desktop/EDAF/edaf-cli/target/edaf-cli.jar run -c /Users/karloknezevic/Desktop/EDAF/configs/grammar_gp_suite/boolean/boolean-xor3-umda.yml
```

## What each config enables

- `run.runCount: 10` for 10 independent runs.
- DB persistence (`jdbc:sqlite:edaf-v3.db`).
- CSV/JSONL telemetry output in `./results`.
- HTML report generation in `./reports`.
- Web-compatible artifacts for run explorer and tree visualization.

## Custom grammar examples

1. `custom_grammar/polynomial-only.bnf`

- Restricts expressions to polynomial-style algebraic forms (`+`, `-`, `*`, constants, `x`, `erc`).
- Used by `custom-polynomial-regression-boa.yml`.

2. `custom_grammar/boolean-only.bnf`

- Restricts expressions to boolean operators (`and`, `or`, `xor`, `not`, `if`) and boolean terminals.
- Used by `custom-boolean-xor-hboa.yml`.
