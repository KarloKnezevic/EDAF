# Disjunct-Matrix Paper Suite (DM/RM/ADM)

This suite reproduces the exact instance grid requested from Table 1 of the paper for binary disjunct-matrix optimization.

## Scope

Variants:

- DM (`disjunct-matrix`, target `fit1 == 0`)
- RM (`resolvable-matrix`, target `fit2 == 0`, with `f = round(0.30 * (N - t))`)
- ADM (`almost-disjunct-matrix`, target `fit3 <= 1e-4`, with `epsilon = 1e-4`)

Mandatory algorithms:

- `umda`
- `chow-liu-eda`
- `dependency-tree-eda`
- `bmda`
- `ebna`
- `boa`
- `hboa`

Note on `tree-eda`:

- `tree-eda` in EDAF is a variable-length tree/token driver, not a bitstring dependency-tree baseline.
- For this binary matrix suite, the dependency-tree baseline is `dependency-tree-eda` (with `mimic-chow-liu` model), so `tree-eda` is intentionally not included.

Optional extras:

- `pbil`
- `cga`
- `mimic`

Continuous/permutation EDAs are intentionally excluded from this suite.

## Exact Instance Grid (Table 1)

`t = 2`:

- `(M,N) in {(8,8),(9,12),(10,13),(11,18),(12,20),(13,26),(14,28),(15,35)}`

`t = 3`:

- `(M,N) in {(13,13),(14,14),(15,15),(16,20),(17,21),(18,22),(19,28),(20,30),(21,31)}`

## Evaluation-Mode Policy

The problem plugins support `evaluationMode=exact|sampled|auto`.

Suite policy:

- small combination spaces: exact
- medium/large combination spaces: sampled

The generated suite uses per-instance mode resolution and stores the mode metadata in:

- `configs/adm_paper_suite/paper-suite-metadata.csv`

## Generated Config Layout

- `configs/adm_paper_suite/dm/t2/*.yml`
- `configs/adm_paper_suite/dm/t3/*.yml`
- `configs/adm_paper_suite/rm/t2/*.yml`
- `configs/adm_paper_suite/rm/t3/*.yml`
- `configs/adm_paper_suite/adm/t2/*.yml`
- `configs/adm_paper_suite/adm/t3/*.yml`

Batch manifests:

- `configs/adm_paper_suite/batch-paper-mandatory-30.yml`
- `configs/adm_paper_suite/batch-paper-optional-30.yml`
- `configs/adm_paper_suite/batch-paper-full-30.yml`
- `configs/adm_paper_suite/batch-paper-smoke.yml`
- `configs/adm_paper_suite/batch-paper-smoke-missing.yml` (auto-generated helper for filling interrupted smoke runs)
- `configs/adm_paper_suite/batch-paper-crossvariant-smoke.yml` (RM/ADM mandatory smoke)
- `configs/adm_paper_suite/batch-paper-t3-crossvariant-smoke.yml` (DM/RM/ADM, `t=3` mandatory smoke)

## Termination and Repetitions

Per run:

- stopping type: `budget-or-target`
- `maxEvaluations = 500000`
- plus target stop per variant

Batch repetition:

- mandatory/full manifests use `repetitions: 30` per config entry.

## Generate / Run / Report

Generate configs:

```bash
./scripts/adm_paper_suite/generate_configs.py --include-optional
```

Smoke batch:

```bash
./edaf batch -c configs/adm_paper_suite/batch-paper-smoke.yml
```

Mandatory 30x campaign:

```bash
./edaf batch -c configs/adm_paper_suite/batch-paper-mandatory-30.yml
```

Resume only missing canonical runs (no rerun of completed IDs):

```bash
./scripts/adm_paper_suite/run_missing_canonical.py \
  --batch configs/adm_paper_suite/batch-paper-mandatory-30.yml \
  --db edaf-v3.db \
  --verbosity quiet
```

Full campaign (mandatory + optional):

```bash
./edaf batch -c configs/adm_paper_suite/batch-paper-full-30.yml
```

Build comparative report bundle from DB:

```bash
./scripts/adm_paper_suite/build_comparison_report.py \
  --db edaf-v3.db \
  --metadata configs/adm_paper_suite/paper-suite-metadata.csv \
  --out reports/adm_paper_suite
```

Strict paper-metrics view (canonical run IDs only, excludes smoke/ad-hoc IDs):

```bash
./scripts/adm_paper_suite/build_comparison_report.py \
  --db edaf-v3.db \
  --metadata configs/adm_paper_suite/paper-suite-metadata.csv \
  --out reports/adm_paper_suite_canonical \
  --canonical-only
```

Generated report artifacts:

- `reports/adm_paper_suite/paper-suite-comparison.md`
- `reports/adm_paper_suite/paper-suite-comparison.html`
- `reports/adm_paper_suite/instance-algorithm-stats.csv`
- `reports/adm_paper_suite/instance-winners.csv`
- `reports/adm_paper_suite/aggregate-stats.csv`
- `reports/adm_paper_suite/campaign-coverage.csv`
- `reports/adm_paper_suite/best_matrices/...`
