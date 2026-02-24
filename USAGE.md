<p align="right"><img src="docs/assets/branding/edaf_logo2.png" alt="EDAF logo" width="180" /></p>

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
# Build once, then run the packaged web app
mvn -q -pl edaf-web -am package -DskipTests
EDAF_DB_URL=jdbc:sqlite:edaf-v3.db java -jar edaf-web/target/edaf-web-*.jar
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
