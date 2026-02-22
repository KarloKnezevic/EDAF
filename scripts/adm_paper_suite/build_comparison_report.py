#!/usr/bin/env python3
"""
Build comparative DM/RM/ADM paper-suite report from SQLite persisted runs.

Outputs:
  reports/adm_paper_suite/paper-suite-comparison.md
  reports/adm_paper_suite/paper-suite-comparison.html
  reports/adm_paper_suite/instance-algorithm-stats.csv
  reports/adm_paper_suite/instance-winners.csv
  reports/adm_paper_suite/aggregate-stats.csv
  reports/adm_paper_suite/best_matrices/<variant>/<instance>/<algorithm>.txt
"""

from __future__ import annotations

import argparse
import csv
import json
import math
import sqlite3
from collections import defaultdict
from dataclasses import dataclass
from pathlib import Path
from statistics import mean, median
from typing import Any


def to_float(value: Any) -> float | None:
    if value is None:
        return None
    try:
        return float(value)
    except (TypeError, ValueError):
        return None


@dataclass(frozen=True)
class InstanceKey:
    variant: str
    t: int
    m: int
    n: int

    @property
    def key(self) -> str:
        return f"{self.variant}-t{self.t}-m{self.m:02d}-n{self.n:02d}"


@dataclass
class RunRow:
    run_id: str
    experiment_id: str
    algorithm: str
    problem_type: str
    status: str
    best_fitness: float | None
    evaluations: int | None
    runtime_millis: int | None
    config: dict[str, Any]
    instance: InstanceKey


def variant_from_problem(problem_type: str) -> str:
    mapping = {
        "disjunct-matrix": "dm",
        "resolvable-matrix": "rm",
        "almost-disjunct-matrix": "adm",
    }
    if problem_type not in mapping:
        raise ValueError(f"Unsupported problem type in paper suite: {problem_type}")
    return mapping[problem_type]


def target_for_variant(variant: str) -> float:
    if variant in {"dm", "rm"}:
        return 0.0
    if variant == "adm":
        return 1.0e-4
    raise ValueError(f"Unknown variant: {variant}")


def is_success(row: RunRow) -> bool:
    if row.status.upper() != "COMPLETED":
        return False
    target = target_for_variant(row.instance.variant)
    if row.best_fitness is None:
        return False
    return row.best_fitness <= target


def load_runs(connection: sqlite3.Connection) -> list[RunRow]:
    sql = """
    SELECT
      r.run_id,
      r.experiment_id,
      r.status,
      r.best_fitness,
      r.evaluations,
      r.runtime_millis,
      e.algorithm_type,
      e.problem_type,
      e.config_json
    FROM runs r
    JOIN experiments e ON e.experiment_id = r.experiment_id
    WHERE r.run_id LIKE 'adm-paper-%'
    """
    rows: list[RunRow] = []
    for rec in connection.execute(sql):
        run_id, exp_id, status, best, evals, runtime, algo, problem_type, config_json = rec
        config = json.loads(config_json)
        problem = config.get("problem", {})
        params = problem.get("params", {})
        m = int(problem.get("m", params.get("m", problem.get("rows", params.get("rows", -1)))))
        n = int(problem.get("n", params.get("n", problem.get("columns", params.get("columns", -1)))))
        t = int(problem.get("t", params.get("t", -1)))
        variant = variant_from_problem(problem_type)
        rows.append(
            RunRow(
                run_id=run_id,
                experiment_id=exp_id,
                algorithm=algo,
                problem_type=problem_type,
                status=status,
                best_fitness=to_float(best),
                evaluations=int(evals) if evals is not None else None,
                runtime_millis=int(runtime) if runtime is not None else None,
                config=config,
                instance=InstanceKey(variant=variant, t=t, m=m, n=n),
            )
        )
    return rows


def safe_mean(values: list[float | int]) -> float | None:
    data = [float(v) for v in values]
    return mean(data) if data else None


def safe_median(values: list[float | int]) -> float | None:
    data = [float(v) for v in values]
    return median(data) if data else None


def format_num(value: float | None, digits: int = 6) -> str:
    if value is None:
        return "-"
    return f"{value:.{digits}g}"


def load_completed_best_genotype(connection: sqlite3.Connection, run_id: str) -> str | None:
    sql = """
    SELECT payload_json
    FROM events
    WHERE run_id = ? AND event_type = 'run_completed'
    ORDER BY id DESC
    LIMIT 1
    """
    rec = connection.execute(sql, (run_id,)).fetchone()
    if not rec:
        return None
    try:
        payload = json.loads(rec[0])
        genotype = payload.get("bestGenotype")
        if isinstance(genotype, str) and genotype and set(genotype).issubset({"0", "1"}):
            return genotype
    except Exception:
        return None
    return None


def write_matrix(path: Path, genotype: str, m: int, n: int) -> None:
    if len(genotype) != m * n:
        return
    lines = [f"# rows={m} cols={n}"]
    for row in range(m):
        line = " ".join(genotype[col * m + row] for col in range(n))
        lines.append(line)
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text("\n".join(lines) + "\n", encoding="utf-8")


def main() -> None:
    parser = argparse.ArgumentParser()
    parser.add_argument("--db", default="edaf-v3.db")
    parser.add_argument("--metadata", default="configs/adm_paper_suite/paper-suite-metadata.csv")
    parser.add_argument("--out", default="reports/adm_paper_suite")
    parser.add_argument("--include-optional", action="store_true")
    parser.add_argument("--canonical-only", action="store_true")
    parser.add_argument("--expected-repetitions", type=int, default=30)
    args = parser.parse_args()

    db_path = Path(args.db)
    metadata_path = Path(args.metadata)
    out_dir = Path(args.out)
    out_dir.mkdir(parents=True, exist_ok=True)

    metadata: dict[tuple[str, str], dict[str, str]] = {}
    with metadata_path.open(newline="", encoding="utf-8") as handle:
        reader = csv.DictReader(handle)
        for row in reader:
            if not args.include_optional and row["mandatory"] != "true":
                continue
            metadata[(row["instance_key"], row["algorithm"])] = row

    with sqlite3.connect(db_path) as conn:
        runs = load_runs(conn)

        if not args.include_optional:
            runs = [r for r in runs if metadata.get((r.instance.key, r.algorithm), {}).get("mandatory") == "true"]

        if args.canonical_only:
            filtered: list[RunRow] = []
            for run in runs:
                prefix = f"adm-paper-{run.instance.key}-{run.algorithm}"
                if not run.run_id.startswith(prefix):
                    continue
                suffix = run.run_id[len(prefix):]
                if suffix.startswith("-r") and suffix[2:].isdigit():
                    filtered.append(run)
            runs = filtered

        grouped: dict[tuple[str, str], list[RunRow]] = defaultdict(list)
        for run in runs:
            grouped[(run.instance.key, run.algorithm)].append(run)

        # Coverage of strict paper campaign run IDs:
        # canonical pattern generated by batch-paper-*-30 manifests:
        #   adm-paper-<instance_key>-<algorithm>-rXX
        # This intentionally excludes smoke/xsmoke/poptest ad-hoc runs.
        coverage_counts: dict[tuple[str, str], dict[str, int]] = defaultdict(lambda: {
            "completed": 0,
            "failed": 0,
            "total_canonical": 0,
        })
        for run in runs:
            key = (run.instance.key, run.algorithm)
            prefix = f"adm-paper-{run.instance.key}-{run.algorithm}"
            if not run.run_id.startswith(prefix):
                continue
            suffix = run.run_id[len(prefix):]
            if not (suffix.startswith("-r") and suffix[2:].isdigit()):
                continue
            coverage_counts[key]["total_canonical"] += 1
            status = (run.status or "").upper()
            if status == "COMPLETED":
                coverage_counts[key]["completed"] += 1
            elif status == "FAILED":
                coverage_counts[key]["failed"] += 1

        stat_rows: list[dict[str, Any]] = []
        winner_rows: list[dict[str, Any]] = []
        coverage_rows: list[dict[str, Any]] = []
        aggregate_groups: dict[tuple[str, int, str], list[RunRow]] = defaultdict(list)

        for (instance_key, algorithm), rows in sorted(grouped.items()):
            ref = rows[0]
            success_rows = [r for r in rows if is_success(r)]
            success_rate = len(success_rows) / len(rows) if rows else 0.0
            evals_success = [r.evaluations for r in success_rows if r.evaluations is not None]
            best_values = [r.best_fitness for r in rows if r.best_fitness is not None]
            runtimes = [r.runtime_millis for r in rows if r.runtime_millis is not None]

            item = {
                "variant": ref.instance.variant,
                "t": ref.instance.t,
                "m": ref.instance.m,
                "n": ref.instance.n,
                "instance_key": instance_key,
                "algorithm": algorithm,
                "runs": len(rows),
                "success_rate": success_rate,
                "mean_evals_to_target": safe_mean(evals_success),
                "median_evals_to_target": safe_median(evals_success),
                "mean_final_best_fit": safe_mean(best_values),
                "median_final_best_fit": safe_median(best_values),
                "mean_runtime_ms": safe_mean(runtimes),
                "evaluation_mode": metadata.get((instance_key, algorithm), {}).get("evaluation_mode", "unknown"),
                "subset_count": metadata.get((instance_key, algorithm), {}).get("subset_count", ""),
                "max_exact_subsets": metadata.get((instance_key, algorithm), {}).get("max_exact_subsets", ""),
                "sample_size": metadata.get((instance_key, algorithm), {}).get("sample_size", ""),
            }
            stat_rows.append(item)
            aggregate_groups[(ref.instance.variant, ref.instance.t, algorithm)].extend(rows)

        # Coverage rows are driven by metadata so report can show missing combinations too.
        for (instance_key, algorithm), meta in sorted(metadata.items()):
            variant = meta["variant"]
            t = int(meta["t"])
            m = int(meta["m"])
            n = int(meta["n"])
            counts = coverage_counts.get((instance_key, algorithm), {})
            completed = int(counts.get("completed", 0))
            failed = int(counts.get("failed", 0))
            total_canonical = int(counts.get("total_canonical", 0))
            expected = args.expected_repetitions
            coverage_rows.append({
                "instance_key": instance_key,
                "variant": variant,
                "t": t,
                "m": m,
                "n": n,
                "algorithm": algorithm,
                "mandatory": meta["mandatory"],
                "expected_repetitions": expected,
                "completed_runs": completed,
                "failed_runs": failed,
                "canonical_runs_present": total_canonical,
                "completion_ratio": (completed / expected) if expected > 0 else 0.0,
            })

        per_instance: dict[str, list[dict[str, Any]]] = defaultdict(list)
        for row in stat_rows:
            per_instance[row["instance_key"]].append(row)

        for instance_key, rows in sorted(per_instance.items()):
            rows_sorted = sorted(
                rows,
                key=lambda r: (
                    -float(r["success_rate"]),
                    float(r["mean_evals_to_target"]) if r["mean_evals_to_target"] is not None else math.inf,
                    float(r["mean_final_best_fit"]) if r["mean_final_best_fit"] is not None else math.inf,
                    r["algorithm"],
                ),
            )
            winner = rows_sorted[0]
            winner_rows.append({
                "instance_key": instance_key,
                "variant": winner["variant"],
                "t": winner["t"],
                "m": winner["m"],
                "n": winner["n"],
                "winner_algorithm": winner["algorithm"],
                "winner_success_rate": winner["success_rate"],
                "winner_mean_evals_to_target": winner["mean_evals_to_target"],
                "winner_mean_final_best_fit": winner["mean_final_best_fit"],
            })

        aggregate_rows: list[dict[str, Any]] = []
        for (variant, t, algorithm), rows in sorted(aggregate_groups.items()):
            success_rows = [r for r in rows if is_success(r)]
            success_rate = len(success_rows) / len(rows) if rows else 0.0
            evals_success = [r.evaluations for r in success_rows if r.evaluations is not None]
            best_values = [r.best_fitness for r in rows if r.best_fitness is not None]
            runtimes = [r.runtime_millis for r in rows if r.runtime_millis is not None]
            aggregate_rows.append({
                "variant": variant,
                "t": t,
                "algorithm": algorithm,
                "runs": len(rows),
                "success_rate": success_rate,
                "mean_evals_to_target": safe_mean(evals_success),
                "median_evals_to_target": safe_median(evals_success),
                "mean_final_best_fit": safe_mean(best_values),
                "median_final_best_fit": safe_median(best_values),
                "mean_runtime_ms": safe_mean(runtimes),
            })

        # Best matrix artifact per (instance, algorithm)
        for row in stat_rows:
            instance_runs = grouped[(row["instance_key"], row["algorithm"])]
            ranked = sorted(
                [r for r in instance_runs if r.best_fitness is not None],
                key=lambda r: (float(r.best_fitness), r.evaluations if r.evaluations is not None else math.inf),
            )
            if not ranked:
                continue
            best_run = ranked[0]
            genotype = load_completed_best_genotype(conn, best_run.run_id)
            if genotype is None:
                continue
            matrix_path = out_dir / "best_matrices" / row["variant"] / row["instance_key"] / f"{row['algorithm']}.txt"
            write_matrix(matrix_path, genotype, int(row["m"]), int(row["n"]))

    # CSV outputs
    def write_csv(path: Path, rows: list[dict[str, Any]], columns: list[str]) -> None:
        with path.open("w", newline="", encoding="utf-8") as handle:
            writer = csv.DictWriter(handle, fieldnames=columns)
            writer.writeheader()
            for row in rows:
                writer.writerow(row)

    write_csv(
        out_dir / "instance-algorithm-stats.csv",
        stat_rows,
        [
            "variant", "t", "m", "n", "instance_key", "algorithm", "runs",
            "success_rate", "mean_evals_to_target", "median_evals_to_target",
            "mean_final_best_fit", "median_final_best_fit", "mean_runtime_ms",
            "evaluation_mode", "subset_count", "max_exact_subsets", "sample_size"
        ],
    )
    write_csv(
        out_dir / "instance-winners.csv",
        winner_rows,
        [
            "instance_key", "variant", "t", "m", "n", "winner_algorithm",
            "winner_success_rate", "winner_mean_evals_to_target", "winner_mean_final_best_fit"
        ],
    )
    write_csv(
        out_dir / "aggregate-stats.csv",
        aggregate_rows,
        [
            "variant", "t", "algorithm", "runs", "success_rate",
            "mean_evals_to_target", "median_evals_to_target",
            "mean_final_best_fit", "median_final_best_fit", "mean_runtime_ms"
        ],
    )
    write_csv(
        out_dir / "campaign-coverage.csv",
        coverage_rows,
        [
            "instance_key", "variant", "t", "m", "n", "algorithm", "mandatory",
            "expected_repetitions", "completed_runs", "failed_runs",
            "canonical_runs_present", "completion_ratio"
        ],
    )

    # Markdown report
    md_lines: list[str] = []
    md_lines.append("# ADM/DM/RM Paper-Suite Comparative Report")
    md_lines.append("")
    mandatory_coverage = [row for row in coverage_rows if row["mandatory"] == "true"]
    total_expected = sum(int(row["expected_repetitions"]) for row in mandatory_coverage)
    total_completed = sum(int(row["completed_runs"]) for row in mandatory_coverage)
    total_failed = sum(int(row["failed_runs"]) for row in mandatory_coverage)
    coverage_pct = (100.0 * total_completed / total_expected) if total_expected > 0 else 0.0
    md_lines.append("## Campaign Coverage")
    md_lines.append("")
    md_lines.append(
        f"- Mandatory completion: `{total_completed}/{total_expected}` ({coverage_pct:.2f}%)"
    )
    md_lines.append(f"- Mandatory failed canonical runs: `{total_failed}`")
    md_lines.append(
        f"- Coverage target per (instance,algorithm): `{args.expected_repetitions}` canonical repetitions"
    )
    md_lines.append(
        "- Canonical run IDs counted for coverage use pattern: "
        "`adm-paper-<instance>-<algorithm>-rXX`."
    )
    md_lines.append(
        "- Smoke/ad-hoc IDs (`-smoke`, `-xsmoke`, `-poptest`) are excluded from coverage accounting."
    )
    md_lines.append(
        "- Metric rows use: "
        + ("canonical-only run IDs." if args.canonical_only else "all `adm-paper-*` run IDs (including smoke/ad-hoc).")
    )
    md_lines.append("")

    md_lines.append("## Evaluation-Mode Policy")
    md_lines.append("")
    md_lines.append("| Instance | Variant | t | M | N | C(N,t) | Mode | Why |")
    md_lines.append("|---|---:|---:|---:|---:|---:|---|---|")
    seen_instance = set()
    for row in sorted(stat_rows, key=lambda r: (r["variant"], int(r["t"]), int(r["m"]), int(r["n"]), r["algorithm"])):
        key = row["instance_key"]
        if key in seen_instance:
            continue
        seen_instance.add(key)
        subset_count = int(row["subset_count"]) if str(row["subset_count"]).isdigit() else 0
        max_exact = int(row["max_exact_subsets"]) if str(row["max_exact_subsets"]).isdigit() else 0
        mode = row["evaluation_mode"]
        reason = (
            f"C(N,t)={subset_count} <= {max_exact}" if mode == "exact"
            else f"C(N,t)={subset_count} > {max_exact}, sampled with budget={row['sample_size']}"
        )
        md_lines.append(
            f"| {key} | {row['variant']} | {row['t']} | {row['m']} | {row['n']} | {subset_count} | {mode} | {reason} |"
        )

    md_lines.append("")
    md_lines.append("## Per-Instance Winners")
    md_lines.append("")
    md_lines.append("| Instance | Winner | Success rate | Mean evals-to-target | Mean final best fit |")
    md_lines.append("|---|---|---:|---:|---:|")
    for row in sorted(winner_rows, key=lambda r: (r["variant"], int(r["t"]), int(r["m"]), int(r["n"]))):
        md_lines.append(
            "| {instance_key} | {winner_algorithm} | {success:.3f} | {evals} | {best} |".format(
                instance_key=row["instance_key"],
                winner_algorithm=row["winner_algorithm"],
                success=float(row["winner_success_rate"]),
                evals=format_num(to_float(row["winner_mean_evals_to_target"])),
                best=format_num(to_float(row["winner_mean_final_best_fit"])),
            )
        )

    md_lines.append("")
    md_lines.append("## Aggregate by (variant, t, algorithm)")
    md_lines.append("")
    md_lines.append("| Variant | t | Algorithm | Runs | Success rate | Mean evals-to-target | Median evals-to-target | Mean final best fit | Mean runtime (ms) |")
    md_lines.append("|---|---:|---|---:|---:|---:|---:|---:|---:|")
    for row in sorted(aggregate_rows, key=lambda r: (r["variant"], int(r["t"]), -float(r["success_rate"]), r["algorithm"])):
        md_lines.append(
            "| {variant} | {t} | {algorithm} | {runs} | {success:.3f} | {mean_eval} | {median_eval} | {mean_best} | {mean_runtime} |".format(
                variant=row["variant"],
                t=row["t"],
                algorithm=row["algorithm"],
                runs=row["runs"],
                success=float(row["success_rate"]),
                mean_eval=format_num(to_float(row["mean_evals_to_target"])),
                median_eval=format_num(to_float(row["median_evals_to_target"])),
                mean_best=format_num(to_float(row["mean_final_best_fit"])),
                mean_runtime=format_num(to_float(row["mean_runtime_ms"])),
            )
        )

    md_lines.append("")
    md_lines.append("## Notes")
    md_lines.append("")
    md_lines.append("- Success criterion used per variant:")
    md_lines.append("  - DM: `fit1 == 0`")
    md_lines.append("  - RM: `fit2 == 0`")
    md_lines.append("  - ADM: `fit3 <= 1e-4`")
    md_lines.append("- Winner selection: success rate desc, mean evals-to-target asc, mean final best fitness asc.")
    md_lines.append("- Best matrix artifacts are under `best_matrices/`.")
    md_lines.append("- Coverage details are exported to `campaign-coverage.csv`.")

    report_md = out_dir / "paper-suite-comparison.md"
    report_md.write_text("\n".join(md_lines) + "\n", encoding="utf-8")

    # Minimal HTML wrapper
    html = "<html><head><meta charset='utf-8'><title>ADM paper suite report</title>" \
           "<style>body{font-family:system-ui,Segoe UI,Roboto,Arial,sans-serif;max-width:1200px;margin:2rem auto;padding:0 1rem;}" \
           "table{border-collapse:collapse;width:100%;margin:1rem 0;}th,td{border:1px solid #d6d6d6;padding:0.4rem;text-align:left;}th{background:#f3f5f7;}" \
           "code{background:#f1f1f1;padding:0.1rem 0.25rem;}</style></head><body><pre>" \
           + report_md.read_text(encoding="utf-8").replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;") \
           + "</pre></body></html>"
    (out_dir / "paper-suite-comparison.html").write_text(html, encoding="utf-8")

    print(f"Wrote report to {report_md}")


if __name__ == "__main__":
    main()
