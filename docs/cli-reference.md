# CLI Reference

EDAF CLI command root:

```bash
./edaf --help
```

## 1) Global Command

```text
edaf [COMMAND]
```

Commands:

- `run`
- `batch`
- `resume`
- `report`
- `config`
- `list`

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
```

Options:

- `-c`, `--config` (required): path to batch YAML
- `--verbosity` (optional): `quiet|normal|verbose|debug`

## 4) `resume`

Resume from checkpoint YAML.

```bash
./edaf resume --checkpoint results/checkpoints/gaussian-sphere-v3-iter-50.ckpt.yaml
```

Options:

- `--checkpoint` (required): checkpoint file path
- `--verbosity` (optional): `quiet|normal|verbose|debug`

## 5) `report`

Generate report artifacts from persisted run history.

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

## 8) Exit Behavior

- command returns `0` on successful execution
- validation/runtime errors return non-zero and include actionable message paths where available

## 9) Typical Workflows

### Single run + report

```bash
./edaf run -c configs/umda-onemax-v3.yml
./edaf report --run-id umda-onemax-v3 --out reports --db-url jdbc:sqlite:edaf-v3.db
```

### Batch experiment pack

```bash
./edaf batch -c configs/batch-v3.yml
```

### Resume interrupted run

```bash
./edaf resume --checkpoint results/checkpoints/<run-id>-iter-<k>.ckpt.yaml
```

### Discover available components

```bash
./edaf list algorithms
./edaf list models
./edaf list problems
```
