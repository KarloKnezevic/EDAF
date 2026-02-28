<p align="right"><img src="./assets/branding/edaf_logo2.png" alt="EDAF logo" width="180" /></p>

# API JavaDoc

This page describes how EDAF Java API documentation is generated and how to navigate it efficiently.

## Scope

EDAF v3 JavaDoc now includes:

- package-level docs (`package-info.java`) for core, algorithms, models, problems, persistence, CLI, web, reporting, representations and COCO modules
- expanded class-level docs for key algorithm drivers and probabilistic model implementations
- mathematical descriptions for core update equations in discrete/continuous/permutation model families

## Documentation Standard Used

EDAF JavaDoc uses the following conventions across classes and public methods:

- Class JavaDoc:
  - complete sentence description ending with a period
  - `@author Karlo Knezevic`
  - `@version EDAF 3.0.0`
- Method JavaDoc:
  - one-sentence (or longer) behavior description ending with a period
  - one `@param` entry per parameter
  - `@return` phrase for non-`void` methods
- `@param` / `@return` descriptions are concise phrases without trailing punctuation
- avoid type names and variable names inside `@return` description
- avoid dash-based parameter format (for example, no `@param x - ...`)

## Generate Local API Docs

From repository root:

```bash
./scripts/docs/build-javadocs.sh
```

Equivalent Maven command:

```bash
mvn -q -P apidocs -DskipTests verify
```

Output entrypoint:

- `target/site/apidocs/index.html`

## Where JavaDoc Configuration Lives

- Root Maven profile: `apidocs` in `pom.xml`
- Helper script: `scripts/docs/build-javadocs.sh`

`apidocs` profile runs Maven Javadoc aggregate goal over the whole reactor, so cross-module links are preserved.

## Recommended Navigation

1. Start at package summary pages to understand module boundaries and responsibilities.
2. Open model classes under:
   - `com.knezevic.edaf.v3.models.discrete`
   - `com.knezevic.edaf.v3.models.continuous`
   - `com.knezevic.edaf.v3.models.permutation`
3. Review algorithm drivers under `com.knezevic.edaf.v3.algorithms*`.
4. Cross-check runtime orchestration in `com.knezevic.edaf.v3.experiments.runner`.

## CI/Release Usage

For release validation, include API doc generation in your local pre-release checklist:

```bash
mvn -q -P apidocs -DskipTests verify
```

If this succeeds, JavaDoc structure and symbol resolution are healthy across modules.
