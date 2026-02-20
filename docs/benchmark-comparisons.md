# Benchmark Comparisons

This document records comparable experiment results produced directly from EDAF v3 campaign runs.

## Core Non-COCO Benchmark Set (Cross-Domain)

### Reproducibility Inputs

- Batch config: `/Users/karloknezevic/Desktop/EDAF/configs/batch-benchmark-core-v3.yml`
- Experiment configs:
  - `/Users/karloknezevic/Desktop/EDAF/configs/umda-onemax-v3.yml`
  - `/Users/karloknezevic/Desktop/EDAF/configs/benchmarks/knapsack-umda-v3.yml`
  - `/Users/karloknezevic/Desktop/EDAF/configs/benchmarks/maxsat-umda-v3.yml`
  - `/Users/karloknezevic/Desktop/EDAF/configs/benchmarks/tsplib-berlin52-ehm-v3.yml`
  - `/Users/karloknezevic/Desktop/EDAF/configs/benchmarks/cec2014-f10-cma-v3.yml`
  - `/Users/karloknezevic/Desktop/EDAF/configs/benchmarks/zdt1-mo-v3.yml`
  - `/Users/karloknezevic/Desktop/EDAF/configs/benchmarks/dtlz2-mo-v3.yml`
  - `/Users/karloknezevic/Desktop/EDAF/configs/benchmarks/nguyen1-tree-eda-v3.yml`

### Run Summary

| Run ID | Algorithm | Model | Problem | Best Fitness |
| --- | --- | --- | --- | ---: |
| umda-onemax-v3 | umda | umda-bernoulli | onemax | 64.0 |
| benchmark-knapsack-umda-v3 | umda | umda-bernoulli | knapsack | 159.0 |
| benchmark-maxsat-umda-v3 | umda | umda-bernoulli | maxsat | 60.0 |
| benchmark-tsplib-berlin52-ehm-v3 | ehm-eda | ehm | tsplib-tsp | 14146.0 |
| benchmark-cec2014-f10-cma-v3 | cma-es | cma-es | cec2014 | 4.115598e-4 |
| benchmark-zdt1-mo-v3 | mo-eda-skeleton | gaussian-diag | zdt | 0.375729 |
| benchmark-dtlz2-mo-v3 | mo-eda-skeleton | gaussian-diag | dtlz | 0.33 |
| benchmark-nguyen1-tree-eda-v3 | tree-eda | token-categorical | nguyen-sr | 0.024960 |

### Multiobjective Objective Snapshots

- `benchmark-zdt1-mo-v3`: `best_obj_0=0.2895337996`, `best_obj_1=0.4619246723`
- `benchmark-dtlz2-mo-v3`: `best_obj_0≈0`, `best_obj_1≈0`, `best_obj_2=1.0`

### Generated Artifacts

- DB summary CSV:
  - `/Users/karloknezevic/Desktop/EDAF/results/benchmarks/benchmark-core-v3-summary.csv`
- MO objectives CSV:
  - `/Users/karloknezevic/Desktop/EDAF/results/benchmarks/benchmark-mo-objectives-v3.csv`
- Per-run HTML reports:
  - `/Users/karloknezevic/Desktop/EDAF/reports/benchmarks/report-benchmark-cec2014-f10-cma-v3.html`
  - `/Users/karloknezevic/Desktop/EDAF/reports/benchmarks/report-benchmark-knapsack-umda-v3.html`
  - `/Users/karloknezevic/Desktop/EDAF/reports/benchmarks/report-benchmark-maxsat-umda-v3.html`
  - `/Users/karloknezevic/Desktop/EDAF/reports/benchmarks/report-benchmark-tsplib-berlin52-ehm-v3.html`
  - `/Users/karloknezevic/Desktop/EDAF/reports/benchmarks/report-benchmark-zdt1-mo-v3.html`
  - `/Users/karloknezevic/Desktop/EDAF/reports/benchmarks/report-benchmark-dtlz2-mo-v3.html`
  - `/Users/karloknezevic/Desktop/EDAF/reports/benchmarks/report-benchmark-nguyen1-tree-eda-v3.html`

### Regeneration Commands

```bash
cd /Users/karloknezevic/Desktop/EDAF
./edaf batch -c configs/batch-benchmark-core-v3.yml
```

## COCO BBOB D=2 Comparison

### Reproducibility Inputs

- Campaign config: `/Users/karloknezevic/Desktop/EDAF/configs/coco/bbob-compare-d2-v3.yml`
- Optimizers:
  - `/Users/karloknezevic/Desktop/EDAF/configs/coco/optimizers/gaussian-baseline-v3.yml`
  - `/Users/karloknezevic/Desktop/EDAF/configs/coco/optimizers/gaussian-aggressive-v3.yml`
- Reference CSV imported: `/Users/karloknezevic/Desktop/EDAF/configs/coco/reference/coco-reference-template.csv`
- DB: `jdbc:sqlite:edaf-v3.db`
- Campaign id: `coco-bbob-compare-d2-v3`

### Scope

- Functions: `1,2,3,8,15`
- Dimension: `2`
- Instances: `1,2`
- Repetitions: `3`
- Total trials: `60`

### Overall Comparable Metrics

| Optimizer | Success Rate | Mean Evals to Target | EDAF ERT | Reference ERT | ERT Ratio |
| --- | ---: | ---: | ---: | ---: | ---: |
| gaussian-aggressive | 76.67% | 1276.52 | 2189.57 | 716.00 | 3.058 |
| gaussian-baseline | 46.67% | 1877.14 | 5305.71 | 716.00 | 7.410 |

Interpretation:

- lower `ERT Ratio` is better (`EDAF ERT / reference ERT`)
- for this campaign, `gaussian-aggressive` outperformed `gaussian-baseline` on the aggregate target-reaching metrics

### Function-Level Detail

| Function | Optimizer | Trials | Successes | Avg Evals To Target | Avg Best Fitness |
| ---: | --- | ---: | ---: | ---: | ---: |
| 1 | gaussian-aggressive | 6 | 6 | 706.67 | 1.36782e-19 |
| 1 | gaussian-baseline | 6 | 6 | 1360.00 | 2.10961e-18 |
| 2 | gaussian-aggressive | 6 | 6 | 1013.33 | 5.90171e-15 |
| 2 | gaussian-baseline | 6 | 6 | 2000.00 | 1.48257e-13 |
| 3 | gaussian-aggressive | 6 | 5 | 1776.00 | 0.165827 |
| 3 | gaussian-baseline | 6 | 1 | 3120.00 | 0.000826579 |
| 8 | gaussian-aggressive | 6 | 0 | - | 0.0646566 |
| 8 | gaussian-baseline | 6 | 0 | - | 0.061192 |
| 15 | gaussian-aggressive | 6 | 6 | 1693.33 | 0 |
| 15 | gaussian-baseline | 6 | 1 | 3000.00 | 0.0324831 |

### Generated Artifacts

- Campaign HTML report:
  - `/Users/karloknezevic/Desktop/EDAF/reports/coco/coco-campaign-coco-bbob-compare-d2-v3.html`
- Comparable CSV exports:
  - `/Users/karloknezevic/Desktop/EDAF/results/coco/coco-bbob-compare-d2-v3-aggregates.csv`
  - `/Users/karloknezevic/Desktop/EDAF/results/coco/coco-bbob-compare-d2-v3-by-function.csv`

### Regeneration Commands

```bash
cd /Users/karloknezevic/Desktop/EDAF
./edaf coco import-reference --csv configs/coco/reference/coco-reference-template.csv --suite bbob --db-url jdbc:sqlite:edaf-v3.db
./edaf coco run -c configs/coco/bbob-compare-d2-v3.yml
./edaf coco report --campaign-id coco-bbob-compare-d2-v3 --out reports/coco --db-url jdbc:sqlite:edaf-v3.db
```

## COCO BBOB Publishable Comparison (D=2,5,10,20)

### Reproducibility Inputs

- Campaign config: `/Users/karloknezevic/Desktop/EDAF/configs/coco/bbob-publishable-v3.yml`
- Optimizers:
  - `/Users/karloknezevic/Desktop/EDAF/configs/coco/optimizers/gaussian-baseline-v3.yml`
  - `/Users/karloknezevic/Desktop/EDAF/configs/coco/optimizers/gaussian-aggressive-v3.yml`
- Reference extractor script:
  - `/Users/karloknezevic/Desktop/EDAF/scripts/coco/build_reference_from_ppdata.py`
- Imported reference CSV:
  - `/Users/karloknezevic/Desktop/EDAF/configs/coco/reference/coco-reference-bbob-ppdata-2009-2023-f1-2-3-8-15-d2-5-10-20-t1e-7.csv`
- Reference source URL: `https://numbbo.github.io/ppdata-archive/`
- Imported rows: `4665`
- DB: `jdbc:sqlite:edaf-v3.db`
- Campaign id: `coco-bbob-publishable-v3`

### Scope

- Functions: `1,2,3,8,15`
- Dimensions: `2,5,10,20`
- Instances: `1,2`
- Repetitions: `5`
- Total trials: `400`
- Successful trials: `156`

### Aggregate Metrics by Dimension

| Optimizer | Dimension | Success Rate | Mean Evals to Target | EDAF ERT | Reference ERT | ERT Ratio |
| --- | ---: | ---: | ---: | ---: | ---: | ---: |
| gaussian-aggressive | 2 | 0.72 | 1195.56 | 2128.89 | 205.97 | 10.3358 |
| gaussian-aggressive | 5 | 0.52 | 2092.31 | 7630.77 | 2076.36 | 3.6751 |
| gaussian-aggressive | 10 | 0.22 | 2552.73 | 45098.18 | 6583.80 | 6.8499 |
| gaussian-aggressive | 20 | 0.08 | 3820.00 | 279820.00 | 18923.74 | 14.7867 |
| gaussian-baseline | 2 | 0.42 | 1560.00 | 4874.29 | 205.97 | 23.6647 |
| gaussian-baseline | 5 | 0.46 | 3553.04 | 10596.52 | 2076.36 | 5.1034 |
| gaussian-baseline | 10 | 0.40 | 5616.00 | 23616.00 | 6583.80 | 3.5870 |
| gaussian-baseline | 20 | 0.30 | 7864.00 | 63864.00 | 18923.74 | 3.3748 |

Interpretation:

- lower `ERT Ratio` is better (`EDAF ERT / reference ERT`)
- `gaussian-aggressive` was stronger in lower dimensions (`2`, `5`)
- `gaussian-baseline` was stronger in higher dimensions (`10`, `20`) for this campaign setup

### Publishable Bundle

- Bundle directory:
  - `/Users/karloknezevic/Desktop/EDAF/reports/coco/bundles/coco-bbob-publishable-v3/`
- Campaign HTML report:
  - `/Users/karloknezevic/Desktop/EDAF/reports/coco/coco-campaign-coco-bbob-publishable-v3.html`
- CSV exports:
  - `/Users/karloknezevic/Desktop/EDAF/results/coco/coco-bbob-publishable-v3-aggregates.csv`
  - `/Users/karloknezevic/Desktop/EDAF/results/coco/coco-bbob-publishable-v3-by-function.csv`
  - `/Users/karloknezevic/Desktop/EDAF/results/coco/coco-bbob-publishable-v3-campaign.csv`

### Regeneration Commands

```bash
cd /Users/karloknezevic/Desktop/EDAF
./scripts/coco/build_reference_from_ppdata.py \
  --functions 1,2,3,8,15 \
  --dimensions 2,5,10,20 \
  --target-label 1e-7 \
  --target-value 1e-7 \
  --out configs/coco/reference/coco-reference-bbob-ppdata-2009-2023-f1-2-3-8-15-d2-5-10-20-t1e-7.csv

sqlite3 edaf-v3.db "DELETE FROM coco_reference_results WHERE suite='bbob';"
./edaf coco import-reference \
  --csv configs/coco/reference/coco-reference-bbob-ppdata-2009-2023-f1-2-3-8-15-d2-5-10-20-t1e-7.csv \
  --suite bbob \
  --source-url https://numbbo.github.io/ppdata-archive/ \
  --db-url jdbc:sqlite:edaf-v3.db

./edaf coco run -c configs/coco/bbob-publishable-v3.yml
./edaf coco report --campaign-id coco-bbob-publishable-v3 --out reports/coco --db-url jdbc:sqlite:edaf-v3.db
```

## COCO BBOB CMA-ES Upgrade Comparison

### Reproducibility Inputs

- Campaign config: `/Users/karloknezevic/Desktop/EDAF/configs/coco/bbob-cma-compare-v3.yml`
- Optimizer templates:
  - `/Users/karloknezevic/Desktop/EDAF/configs/coco/optimizers/gaussian-baseline-v3.yml`
  - `/Users/karloknezevic/Desktop/EDAF/configs/coco/optimizers/gaussian-aggressive-v3.yml`
  - `/Users/karloknezevic/Desktop/EDAF/configs/coco/optimizers/cma-es-v3.yml`
- Reference CSV:
  - `/Users/karloknezevic/Desktop/EDAF/configs/coco/reference/coco-reference-bbob-ppdata-2009-2023-f1-2-3-8-15-d2-5-10-20-t1e-7.csv`
- Campaign id: `coco-bbob-cma-compare-v3`

### Scope

- Functions: `1,2,3,8,15`
- Dimensions: `2,5,10,20`
- Instances: `1,2`
- Repetitions: `3`
- Total trials: `360`
- Successful trials: `155`

### Aggregate ERT Ratio by Dimension

| Optimizer | D2 | D5 | D10 | D20 |
| --- | ---: | ---: | ---: | ---: |
| cma-es | 7.6241 | 3.5142 | 3.5066 | 5.5842 |
| gaussian-aggressive | 9.3047 | 3.8323 | 7.6693 | 17.9626 |
| gaussian-baseline | 24.8576 | 5.4593 | 4.4017 | 3.3679 |

Interpretation:

- lower is better (`EDAF ERT / reference ERT`)
- upgraded `cma-es` is strongest on `D2`, `D5`, and `D10`
- `gaussian-baseline` remains strongest on `D20` in this setup

### Overall Success Rates

| Optimizer | Trials | Successes | Success Rate |
| --- | ---: | ---: | ---: |
| cma-es | 120 | 65 | 0.541667 |
| gaussian-aggressive | 120 | 46 | 0.383333 |
| gaussian-baseline | 120 | 44 | 0.366667 |

### Generated Artifacts

- Campaign HTML report:
  - `/Users/karloknezevic/Desktop/EDAF/reports/coco/coco-campaign-coco-bbob-cma-compare-v3.html`
- CSV exports:
  - `/Users/karloknezevic/Desktop/EDAF/results/coco/coco-bbob-cma-compare-v3-aggregates.csv`
  - `/Users/karloknezevic/Desktop/EDAF/results/coco/coco-bbob-cma-compare-v3-overall.csv`
  - `/Users/karloknezevic/Desktop/EDAF/results/coco/coco-bbob-cma-compare-v3-by-function.csv`
- Publishable bundle:
  - `/Users/karloknezevic/Desktop/EDAF/reports/coco/bundles/coco-bbob-cma-compare-v3/`
  - `/Users/karloknezevic/Desktop/EDAF/reports/coco/bundles/coco-bbob-cma-compare-v3.tar.gz`

## COCO BBOB Publishable Comparison v4 (CMA + Restart + Larger Repetitions)

### Reproducibility Inputs

- Campaign config: `/Users/karloknezevic/Desktop/EDAF/configs/coco/bbob-publishable-v4.yml`
- Optimizer templates:
  - `/Users/karloknezevic/Desktop/EDAF/configs/coco/optimizers/gaussian-baseline-v3.yml`
  - `/Users/karloknezevic/Desktop/EDAF/configs/coco/optimizers/gaussian-aggressive-v3.yml`
  - `/Users/karloknezevic/Desktop/EDAF/configs/coco/optimizers/cma-es-v3.yml`
  - `/Users/karloknezevic/Desktop/EDAF/configs/coco/optimizers/cma-es-restart-v3.yml`
- Reference CSV:
  - `/Users/karloknezevic/Desktop/EDAF/configs/coco/reference/coco-reference-bbob-ppdata-2009-2023-f1-2-3-8-15-d2-5-10-20-t1e-7.csv`
- Campaign id: `coco-bbob-publishable-v4`

### Scope

- Functions: `1,2,3,8,15`
- Dimensions: `2,5,10,20`
- Instances: `1,2`
- Repetitions: `6`
- Total trials: `960`
- Successful trials: `459`

### Aggregate ERT Ratio by Dimension

| Optimizer | D2 | D5 | D10 | D20 |
| --- | ---: | ---: | ---: | ---: |
| cma-es | 5.3650 | 2.8518 | 2.1290 | 2.0031 |
| cma-es-restart | 7.6135 | 3.6076 | 2.7038 | 2.4021 |
| gaussian-aggressive | 11.0836 | 4.4594 | 6.3697 | 24.3039 |
| gaussian-baseline | 22.8558 | 4.7082 | 3.6400 | 3.6082 |

Interpretation:

- lower is better (`EDAF ERT / reference ERT`)
- `cma-es` is strongest across all tested dimensions in this campaign
- `cma-es-restart` improves evals-to-target on several slices but underperforms baseline `cma-es` in aggregate ERT ratio due reduced success rate

### Overall Success Rates

| Optimizer | Trials | Successes | Success Rate |
| --- | ---: | ---: | ---: |
| cma-es | 240 | 150 | 62.50% |
| cma-es-restart | 240 | 127 | 52.92% |
| gaussian-baseline | 240 | 97 | 40.42% |
| gaussian-aggressive | 240 | 85 | 35.42% |

### Generated Artifacts

- Campaign HTML report:
  - `/Users/karloknezevic/Desktop/EDAF/reports/coco/coco-campaign-coco-bbob-publishable-v4.html`
- CSV exports:
  - `/Users/karloknezevic/Desktop/EDAF/results/coco/coco-bbob-publishable-v4-aggregates.csv`
  - `/Users/karloknezevic/Desktop/EDAF/results/coco/coco-bbob-publishable-v4-overall.csv`
  - `/Users/karloknezevic/Desktop/EDAF/results/coco/coco-bbob-publishable-v4-by-function.csv`
- Publishable bundle:
  - `/Users/karloknezevic/Desktop/EDAF/reports/coco/bundles/coco-bbob-publishable-v4/`
  - `/Users/karloknezevic/Desktop/EDAF/reports/coco/bundles/coco-bbob-publishable-v4.tar.gz`

### Regeneration Commands

```bash
cd /Users/karloknezevic/Desktop/EDAF

./scripts/coco/build_reference_from_ppdata.py \
  --functions 1,2,3,8,15 \
  --dimensions 2,5,10,20 \
  --target-label 1e-7 \
  --target-value 1e-7 \
  --out configs/coco/reference/coco-reference-bbob-ppdata-2009-2023-f1-2-3-8-15-d2-5-10-20-t1e-7.csv

./edaf coco import-reference \
  --csv configs/coco/reference/coco-reference-bbob-ppdata-2009-2023-f1-2-3-8-15-d2-5-10-20-t1e-7.csv \
  --suite bbob \
  --source-url https://numbbo.github.io/ppdata-archive/bbob/ \
  --db-url jdbc:sqlite:edaf-v3.db

./edaf coco run -c configs/coco/bbob-publishable-v4.yml
./edaf coco report --campaign-id coco-bbob-publishable-v4 --out reports/coco --db-url jdbc:sqlite:edaf-v3.db
```
