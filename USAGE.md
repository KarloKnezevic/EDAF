# EDAF Usage (Quick Reference)

Primary runtime entrypoint:

```bash
./edaf --help
```

## Build

```bash
mvn -q clean test
```

## Run Commands

```bash
./edaf run -c configs/umda-onemax-v3.yml
./edaf batch -c configs/batch-v3.yml
./edaf resume --checkpoint results/checkpoints/<run-id>-iter-<k>.ckpt.yaml
./edaf report --run-id <run-id> --out reports --db-url jdbc:sqlite:edaf-v3.db
./edaf config validate configs/umda-onemax-v3.yml
./edaf list algorithms
./edaf list models
./edaf list problems
```

## Web Dashboard

```bash
EDAF_DB_URL=jdbc:sqlite:edaf-v3.db mvn -q -pl edaf-web -am spring-boot:run
```

Open [http://localhost:7070](http://localhost:7070)

## Docker Stack

```bash
docker compose up --build
docker compose down
```

## Full Documentation

- `README.md`
- `docs/index.md`
