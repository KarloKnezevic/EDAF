# API JavaDoc

This page explains how to access and regenerate the HTML API documentation for EDAF.

## Open the Generated HTML Docs

The repository contains generated aggregate JavaDoc under:

- [docs/api/javadocs/index.html](./javadocs/index.html)

Open it directly in a browser from the repository checkout.

## What Is Documented

The generated API docs cover all framework modules, including:

- core contracts and runtime policies
- representation and model implementations
- algorithm drivers and alias families
- experiment runner orchestration
- persistence query/write layers
- reporting and web adapters

Algorithm and model classes include mathematical update descriptions and reference-oriented class JavaDoc where applicable.

## Regenerate JavaDoc

From repository root:

```bash
./scripts/docs/build-javadocs.sh
```

This executes the aggregate JavaDoc build profile and refreshes module docs.

If you also want to refresh the docs-hosted HTML bundle:

```bash
mkdir -p docs/api/javadocs
rsync -a --delete target/reports/apidocs/ docs/api/javadocs/
```

## JavaDoc Authoring Standard Used

EDAF Java sources follow a strict JavaDoc style:

- Class JavaDoc includes complete sentence descriptions, author, and version tags
- Public methods include `@param` and `@return` tags where required
- Formula-heavy classes include concise mathematical intent and literature references
- Non-trivial implementation logic includes inline comments explaining intent

## Navigation Tips

1. Start from package summaries to understand module boundaries.
2. Continue with algorithm drivers in `com.knezevic.edaf.v3.algorithms.*`.
3. Inspect probabilistic models in `com.knezevic.edaf.v3.models.*`.
4. Cross-check runtime and persistence entry points in experiments and persistence modules.

---
Estimation of Distribution Algorithms Framework  
Copyright (c) 2026 Dr. Karlo Knezevic  
Licensed under the Apache License, Version 2.0.
