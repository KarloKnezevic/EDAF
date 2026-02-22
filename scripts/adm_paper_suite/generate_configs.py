#!/usr/bin/env python3
"""
Generate ADM paper benchmark suite configs for DM/RM/ADM on exact Table-1 instances.

Output structure:
  configs/adm_paper_suite/<variant>/{t2,t3}/<instance>-<algorithm>.yml
and batch manifests:
  configs/adm_paper_suite/batch-paper-mandatory-30.yml
  configs/adm_paper_suite/batch-paper-optional-30.yml
  configs/adm_paper_suite/batch-paper-full-30.yml
  configs/adm_paper_suite/batch-paper-smoke.yml
"""

from __future__ import annotations

import argparse
import hashlib
import math
from dataclasses import dataclass
from pathlib import Path
from typing import Any


T2_INSTANCES = [(8, 8), (9, 12), (10, 13), (11, 18), (12, 20), (13, 26), (14, 28), (15, 35)]
T3_INSTANCES = [(13, 13), (14, 14), (15, 15), (16, 20), (17, 21), (18, 22), (19, 28), (20, 30), (21, 31)]


@dataclass(frozen=True)
class AlgorithmSpec:
    name: str
    model: str
    algorithm_params: dict[str, Any]
    model_params: dict[str, Any]
    mandatory: bool


ALGORITHMS = [
    AlgorithmSpec(
        name="umda",
        model="umda-bernoulli",
        algorithm_params={"populationSize": 220, "elitism": 2, "selectionRatio": 0.45},
        model_params={"smoothing": 0.01},
        mandatory=True,
    ),
    AlgorithmSpec(
        name="chow-liu-eda",
        model="mimic-chow-liu",
        algorithm_params={"populationSize": 220, "elitism": 2, "selectionRatio": 0.45},
        model_params={"smoothing": 0.35},
        mandatory=True,
    ),
    AlgorithmSpec(
        name="dependency-tree-eda",
        model="mimic-chow-liu",
        algorithm_params={"populationSize": 220, "elitism": 2, "selectionRatio": 0.45},
        model_params={"smoothing": 0.35},
        mandatory=True,
    ),
    AlgorithmSpec(
        name="bmda",
        model="bmda",
        algorithm_params={"populationSize": 240, "elitism": 2, "selectionRatio": 0.42},
        model_params={"smoothing": 0.25},
        mandatory=True,
    ),
    AlgorithmSpec(
        name="ebna",
        model="boa-ebna",
        algorithm_params={"populationSize": 260, "elitism": 2, "selectionRatio": 0.40},
        model_params={"maxParents": 3, "smoothing": 0.35},
        mandatory=True,
    ),
    AlgorithmSpec(
        name="boa",
        model="boa-ebna",
        algorithm_params={"populationSize": 260, "elitism": 2, "selectionRatio": 0.40},
        model_params={"maxParents": 3, "smoothing": 0.35},
        mandatory=True,
    ),
    AlgorithmSpec(
        name="hboa",
        model="hboa-network",
        algorithm_params={"populationSize": 280, "elitism": 2, "selectionRatio": 0.40},
        model_params={"smoothing": 0.35, "minMutualInformation": 0.0001, "learningRate": 0.85},
        mandatory=True,
    ),
    AlgorithmSpec(
        name="pbil",
        model="pbil-frequency",
        algorithm_params={"populationSize": 220, "elitism": 2, "selectionRatio": 0.45},
        model_params={"learningRate": 0.2},
        mandatory=False,
    ),
    AlgorithmSpec(
        name="cga",
        model="cga-frequency",
        algorithm_params={"populationSize": 220, "elitism": 2, "selectionRatio": 0.45},
        model_params={"step": 0.02},
        mandatory=False,
    ),
    AlgorithmSpec(
        name="mimic",
        model="mimic-chow-liu",
        algorithm_params={"populationSize": 220, "elitism": 2, "selectionRatio": 0.45},
        model_params={"smoothing": 0.35},
        mandatory=False,
    ),
]


def yaml_scalar(value: Any) -> str:
    if isinstance(value, bool):
        return "true" if value else "false"
    if isinstance(value, str):
        if value == "" or any(ch in value for ch in [":", "#", "{", "}", "[", "]", ",", " "]):
            return f"\"{value}\""
        return value
    if isinstance(value, float):
        if math.isfinite(value):
            if value == int(value):
                return f"{value:.1f}"
            return f"{value:.10g}"
        raise ValueError(f"unsupported float: {value}")
    return str(value)


def dump_section(lines: list[str], key: str, mapping: dict[str, Any], indent: int = 0) -> None:
    pad = " " * indent
    lines.append(f"{pad}{key}:")
    for section_key, section_value in mapping.items():
        if isinstance(section_value, dict):
            dump_section(lines, section_key, section_value, indent + 2)
        elif isinstance(section_value, list):
            rendered = ", ".join(yaml_scalar(item) for item in section_value)
            lines.append(f"{pad}  {section_key}: [{rendered}]")
        else:
            lines.append(f"{pad}  {section_key}: {yaml_scalar(section_value)}")


def algorithm_list(include_optional: bool) -> list[AlgorithmSpec]:
    if include_optional:
        return ALGORITHMS
    return [spec for spec in ALGORITHMS if spec.mandatory]


def instance_tier(n: int, t: int, max_exact_subsets: int) -> tuple[str, int]:
    subsets = math.comb(n, t)
    if subsets <= max_exact_subsets:
        return "exact", subsets
    return "sampled", subsets


def build_problem_section(variant: str,
                          m: int,
                          n: int,
                          t: int,
                          max_exact_subsets: int,
                          sample_size: int,
                          sample_seed: int) -> dict[str, Any]:
    if variant == "dm":
        problem = {"type": "disjunct-matrix", "m": m, "n": n, "t": t}
    elif variant == "rm":
        f = round(0.30 * (n - t))
        problem = {"type": "resolvable-matrix", "m": m, "n": n, "t": t, "f": f}
    elif variant == "adm":
        problem = {"type": "almost-disjunct-matrix", "m": m, "n": n, "t": t, "epsilon": 1.0e-4}
    else:
        raise ValueError(f"unknown variant: {variant}")

    mode, _ = instance_tier(n, t, max_exact_subsets)
    problem["evaluationMode"] = "exact" if mode == "exact" else "sampled"
    problem["maxExactSubsets"] = max_exact_subsets
    problem["sampleSize"] = sample_size
    problem["samplingSeed"] = sample_seed
    return problem


def target_for_variant(variant: str) -> float:
    if variant in ("dm", "rm"):
        return 0.0
    if variant == "adm":
        return 1.0e-4
    raise ValueError(f"unknown variant: {variant}")


def config_text(run_id: str,
                run_name: str,
                length: int,
                problem: dict[str, Any],
                algorithm: AlgorithmSpec,
                output_dir: str,
                log_file: str) -> str:
    top: dict[str, Any] = {
        "schema": "3.0",
        "run": {
            "id": run_id,
            "name": run_name,
            "masterSeed": 91000000,
            "deterministicStreams": True,
            "checkpointEveryIterations": 0,
        },
        "representation": {
            "type": "bitstring",
            "length": length,
        },
        "problem": problem,
        "algorithm": {
            "type": algorithm.name,
            **algorithm.algorithm_params,
        },
        "model": {
            "type": algorithm.model,
            **algorithm.model_params,
        },
        "selection": {"type": "truncation"},
        "replacement": {"type": "elitist"},
        "stopping": {
            "type": "budget-or-target",
            "maxIterations": 10000,
            "maxEvaluations": 500000,
            "targetFitness": target_for_variant("adm" if problem["type"] == "almost-disjunct-matrix"
                                                else "rm" if problem["type"] == "resolvable-matrix"
                                                else "dm"),
        },
        "constraints": {"type": "identity"},
        "localSearch": {"type": "none"},
        "restart": {"type": "none"},
        "niching": {"type": "none"},
        "observability": {
            "metricsEveryIterations": 10,
            "emitModelDiagnostics": True,
        },
        "persistence": {
            "enabled": True,
            "bundleArtifacts": False,
            "sinks": ["db"],
            "outputDirectory": output_dir,
            "database": {
                "enabled": True,
                "url": "jdbc:sqlite:edaf-v3.db",
                "user": "",
                "password": "",
            },
        },
        "reporting": {
            "enabled": False,
            "formats": ["html"],
            "outputDirectory": "./reports/adm_paper_suite",
        },
        "web": {"enabled": False, "port": 7070, "pollSeconds": 3},
        "logging": {
            "modes": ["db", "file"],
            "verbosity": "quiet",
            "jsonlFile": f"{output_dir}/{run_id}-events.jsonl",
            "logFile": log_file,
        },
    }

    lines: list[str] = []
    for key, value in top.items():
        if isinstance(value, dict):
            dump_section(lines, key, value)
        else:
            lines.append(f"{key}: {yaml_scalar(value)}")
        lines.append("")
    return "\n".join(lines).strip() + "\n"


def main() -> None:
    parser = argparse.ArgumentParser()
    parser.add_argument("--root", type=Path, default=Path(__file__).resolve().parents[2])
    parser.add_argument("--include-optional", action="store_true")
    parser.add_argument("--repetitions", type=int, default=30)
    parser.add_argument("--sample-size", type=int, default=512)
    parser.add_argument("--max-exact-subsets", type=int, default=1500)
    parser.add_argument("--sample-seed", type=int, default=20260222)
    args = parser.parse_args()

    root: Path = args.root
    config_root = root / "configs" / "adm_paper_suite"
    config_root.mkdir(parents=True, exist_ok=True)

    selected_algorithms = algorithm_list(include_optional=args.include_optional)
    batch_mandatory: list[dict[str, Any]] = []
    batch_optional: list[dict[str, Any]] = []
    smoke_entries: list[dict[str, Any]] = []
    metadata_rows: list[str] = [
        "variant,t,m,n,instance_key,algorithm,mandatory,problem_type,model_type,evaluation_mode,subset_count,max_exact_subsets,sample_size,target"
    ]

    for variant in ("dm", "rm", "adm"):
        for label, t, instances in (("t2", 2, T2_INSTANCES), ("t3", 3, T3_INSTANCES)):
            target_dir = config_root / variant / label
            target_dir.mkdir(parents=True, exist_ok=True)

            for m, n in instances:
                length = m * n
                problem = build_problem_section(
                    variant=variant,
                    m=m,
                    n=n,
                    t=t,
                    max_exact_subsets=args.max_exact_subsets,
                    sample_size=args.sample_size,
                    sample_seed=args.sample_seed,
                )
                evaluation_mode, subset_count = instance_tier(n, t, args.max_exact_subsets)
                instance_key = f"{variant}-t{t}-m{m:02d}-n{n:02d}"
                target = target_for_variant(variant)

                for algo in selected_algorithms:
                    run_id = f"adm-paper-{instance_key}-{algo.name}"
                    run_name = f"{variant.upper()} t={t} M={m} N={n} {algo.name}"
                    relative_path = Path(variant) / label / f"{instance_key}-{algo.name}.yml"
                    output_dir = f"./results/adm_paper_suite/{variant}/{label}"
                    log_file = f"./results/adm_paper_suite/adm-paper-{variant}.log"

                    text = config_text(
                        run_id=run_id,
                        run_name=run_name,
                        length=length,
                        problem=problem,
                        algorithm=algo,
                        output_dir=output_dir,
                        log_file=log_file,
                    )
                    (config_root / relative_path).write_text(text, encoding="utf-8")

                    entry = {
                        "config": str(relative_path).replace("\\", "/"),
                        "repetitions": args.repetitions,
                        "seedStart": 91000000 + (int(hashlib.sha256(run_id.encode("utf-8")).hexdigest()[:8], 16) % 1_000_000),
                        "runIdPrefix": run_id,
                    }
                    (batch_mandatory if algo.mandatory else batch_optional).append(entry)

                    if len(smoke_entries) < 36:
                        smoke_entries.append({
                            "config": entry["config"],
                            "repetitions": 1,
                            "seedStart": entry["seedStart"],
                            "runIdPrefix": run_id + "-smoke",
                        })

                    metadata_rows.append(",".join([
                        variant,
                        str(t),
                        str(m),
                        str(n),
                        instance_key,
                        algo.name,
                        "true" if algo.mandatory else "false",
                        problem["type"],
                        algo.model,
                        evaluation_mode,
                        str(subset_count),
                        str(args.max_exact_subsets),
                        str(args.sample_size),
                        f"{target:.10g}",
                    ]))

    def write_batch(path: Path, entries: list[dict[str, Any]]) -> None:
        lines = ["defaultRepetitions: 1", "", "experiments:"]
        for entry in entries:
            lines.append(f"  - config: {entry['config']}")
            lines.append(f"    repetitions: {entry['repetitions']}")
            lines.append(f"    seedStart: {entry['seedStart']}")
            lines.append(f"    runIdPrefix: {entry['runIdPrefix']}")
        path.write_text("\n".join(lines) + "\n", encoding="utf-8")

    write_batch(config_root / "batch-paper-mandatory-30.yml", batch_mandatory)
    write_batch(config_root / "batch-paper-optional-30.yml", batch_optional)
    write_batch(config_root / "batch-paper-full-30.yml", batch_mandatory + batch_optional)
    write_batch(config_root / "batch-paper-smoke.yml", smoke_entries)

    metadata = config_root / "paper-suite-metadata.csv"
    metadata.write_text("\n".join(metadata_rows) + "\n", encoding="utf-8")

    print(f"Generated {len(batch_mandatory) + len(batch_optional)} experiment configs under {config_root}")
    print(f"Mandatory entries: {len(batch_mandatory)}")
    print(f"Optional entries: {len(batch_optional)}")


if __name__ == "__main__":
    main()
