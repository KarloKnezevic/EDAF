# CLI Reference

EDAF CLI root:

```bash
./edaf --help
```

## 1) Command Overview

```text
edaf [COMMAND]
```

Top-level commands:

- `run`
- `batch`
- `resume`
- `report`
- `config`
- `list`
- `coco`

## 2) `run`

Run one experiment YAML.

```bash
./edaf run -c configs/umda-onemax-v3.yml
./edaf run -c configs/gaussian-sphere-v3.yml --verbosity verbose
```

Options:

- `-c`, `--config` (required): path to experiment YAML
- `--verbosity` (optional): `quiet|normal|verbose|debug`

## 3) `batch`

Run multiple experiments from one batch file.

```bash
./edaf batch -c configs/batch-v3.yml
./edaf batch -c configs/batch-benchmark-core-v3.yml
./edaf batch -c configs/batch-stat-sample-v3.yml
```

Options:

- `-c`, `--config` (required): path to batch YAML
- `--verbosity` (optional): `quiet|normal|verbose|debug`

Batch config supports both:

- simple list (`experiments: [file1.yml, file2.yml]`)
- repetition objects (`config`, `repetitions`, `seedStart`, `runIdPrefix`) for 30-run statistical campaigns

## 4) `resume`

Resume from checkpoint YAML.

```bash
./edaf resume --checkpoint results/checkpoints/gaussian-sphere-v3-iter-50.ckpt.yaml
```

Options:

- `--checkpoint` (required): checkpoint file path
- `--verbosity` (optional): `quiet|normal|verbose|debug`

## 5) `report`

Generate run-level report artifacts from persisted run history.

```bash
./edaf report --run-id umda-onemax-v3 --out reports --db-url jdbc:sqlite:edaf-v3.db
./edaf report --run-id umda-onemax-v3 --out reports --formats html,latex
```

Options:

- `--run-id` (required)
- `--out` (required)
- `--db-url` (optional, default `jdbc:sqlite:edaf-v3.db`)
- `--db-user` (optional)
- `--db-password` (optional)
- `--formats` (optional, default `html`, supports `html,latex`)
- `--verbosity` (optional)

## 6) `config`

Subcommands:

- `validate`

### `config validate`

```bash
./edaf config validate configs/umda-onemax-v3.yml
./edaf config validate configs/batch-v3.yml
./edaf config validate configs/batch-benchmark-core-v3.yml
```

## 7) `list`

Subcommands:

- `algorithms`
- `models`
- `problems`

Examples:

```bash
./edaf list algorithms
./edaf list models
./edaf list problems
```

## 8) `coco`

COCO/BBOB campaign orchestration commands.

Subcommands:

- `run`
- `import-reference`
- `report`

### `coco run`

Run one campaign YAML and persist campaign-level rows + report.

```bash
./edaf coco run -c configs/coco/bbob-campaign-v3.yml
./edaf coco run -c configs/coco/bbob-smoke-v3.yml
```

Options:

- `-c`, `--config` (required): campaign YAML path
- `--verbosity` (optional): `quiet|normal|verbose|debug`

### `coco import-reference`

Import external reference ERT rows used for ERT ratio comparison.

```bash
./edaf coco import-reference \
  --csv configs/coco/reference/coco-reference-template.csv \
  --suite bbob \
  --source-url https://numbbo.github.io/coco/ \
  --db-url jdbc:sqlite:edaf-v3.db
```

Options:

- `--csv` (required): CSV path
- `--suite` (optional, default `bbob`)
- `--source-url` (optional)
- `--db-url` (optional, default `jdbc:sqlite:edaf-v3.db`)
- `--db-user` (optional)
- `--db-password` (optional)

### `coco report`

Rebuild one campaign report from DB state.

```bash
./edaf coco report \
  --campaign-id coco-bbob-publishable-v4 \
  --out reports/coco \
  --db-url jdbc:sqlite:edaf-v3.db
```

Options:

- `--campaign-id` (required)
- `--out` (required)
- `--db-url` (optional)
- `--db-user` (optional)
- `--db-password` (optional)

## 9) Web Startup Command Notes

From repository root, recommended web startup command is:

```bash
EDAF_DB_URL="jdbc:sqlite:$(pwd)/edaf-v3.db" mvn -q -pl edaf-web -am spring-boot:run
```

Alternative direct module command:

```bash
EDAF_DB_URL="jdbc:sqlite:$(pwd)/edaf-v3.db" mvn -q -f edaf-web/pom.xml spring-boot:run
```

## 10) Exit Behavior

- successful execution returns `0`
- validation/runtime failures return non-zero with actionable messages
