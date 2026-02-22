#!/usr/bin/env python3
"""
Run only missing canonical batch runs without rerunning completed IDs.

This script materializes per-run temporary configs and invokes:
  ./edaf run -c <temp-config>
with exact canonical run IDs and deterministic seeds.

Canonical ID convention:
  <runIdPrefix>-rXX
"""

from __future__ import annotations

import argparse
import json
import os
import sqlite3
import subprocess
import tempfile
from dataclasses import dataclass
from pathlib import Path
from typing import Any

import yaml


@dataclass(frozen=True)
class PlannedRun:
    config_path: Path
    run_id: str
    seed: int


def load_batch(path: Path) -> dict[str, Any]:
    with path.open("r", encoding="utf-8") as handle:
        return yaml.safe_load(handle) or {}


def fetch_run_statuses(db_path: Path) -> dict[str, str]:
    conn = sqlite3.connect(db_path)
    try:
        rows = conn.execute("SELECT run_id, status FROM runs").fetchall()
        return {run_id: (status or "").upper() for run_id, status in rows}
    finally:
        conn.close()


def plan_missing_runs(batch_path: Path,
                      db_status: dict[str, str],
                      rerun_failed: bool,
                      only_prefix: str | None) -> list[PlannedRun]:
    doc = load_batch(batch_path)
    experiments = doc.get("experiments", [])
    plans: list[PlannedRun] = []

    base_dir = batch_path.parent

    for entry in experiments:
        config_rel = entry["config"]
        config_path = (base_dir / config_rel).resolve()
        repetitions = max(1, int(entry.get("repetitions", doc.get("defaultRepetitions", 1))))
        run_prefix = str(entry.get("runIdPrefix", "")).strip()
        if not run_prefix:
            raise ValueError(f"batch entry missing runIdPrefix: {entry}")
        if only_prefix and not run_prefix.startswith(only_prefix):
            continue

        seed_start = entry.get("seedStart", doc.get("defaultSeedStart", None))
        if seed_start is None:
            seed_start = 12345
        seed_start = int(seed_start)

        for rep in range(repetitions):
            ordinal = rep + 1
            run_id = f"{run_prefix}-r{ordinal:02d}"
            status = db_status.get(run_id)
            if status is None:
                plans.append(PlannedRun(config_path=config_path, run_id=run_id, seed=seed_start + rep))
                continue
            if status == "FAILED" and rerun_failed:
                plans.append(PlannedRun(config_path=config_path, run_id=run_id, seed=seed_start + rep))

    return plans


def write_temp_config(base_config: dict[str, Any], run_id: str, seed: int) -> Path:
    cfg = json.loads(json.dumps(base_config))
    run = cfg.setdefault("run", {})
    run["id"] = run_id
    run["masterSeed"] = seed

    fd, temp_path = tempfile.mkstemp(prefix="edaf-missing-", suffix=".yml")
    os.close(fd)
    path = Path(temp_path)
    path.write_text(yaml.safe_dump(cfg, sort_keys=False), encoding="utf-8")
    return path


def execute_runs(root: Path,
                 plans: list[PlannedRun],
                 verbosity: str,
                 dry_run: bool,
                 max_runs: int | None) -> int:
    if max_runs is not None:
        plans = plans[:max_runs]

    if dry_run:
        for index, plan in enumerate(plans, start=1):
            print(f"[DRY] {index}/{len(plans)} {plan.run_id} seed={plan.seed} config={plan.config_path}")
        return 0

    config_cache: dict[Path, dict[str, Any]] = {}
    failures = 0

    for index, plan in enumerate(plans, start=1):
        base = config_cache.get(plan.config_path)
        if base is None:
            base = yaml.safe_load(plan.config_path.read_text(encoding="utf-8")) or {}
            config_cache[plan.config_path] = base

        temp_cfg = write_temp_config(base, plan.run_id, plan.seed)
        try:
            cmd = ["./edaf", "run", "-c", str(temp_cfg), "--verbosity", verbosity]
            print(f"[{index}/{len(plans)}] running {plan.run_id} (seed={plan.seed})")
            proc = subprocess.run(cmd, cwd=root, check=False)
            if proc.returncode != 0:
                failures += 1
                print(f"  -> FAILED return code {proc.returncode}: {plan.run_id}")
        finally:
            try:
                temp_cfg.unlink(missing_ok=True)
            except Exception:
                pass

    return failures


def main() -> None:
    parser = argparse.ArgumentParser()
    parser.add_argument("--batch", default="configs/adm_paper_suite/batch-paper-mandatory-30.yml")
    parser.add_argument("--db", default="edaf-v3.db")
    parser.add_argument("--verbosity", default="quiet", choices=["quiet", "normal", "verbose", "debug"])
    parser.add_argument("--rerun-failed", action="store_true")
    parser.add_argument("--dry-run", action="store_true")
    parser.add_argument("--max-runs", type=int)
    parser.add_argument("--only-prefix")
    args = parser.parse_args()

    root = Path(__file__).resolve().parents[2]
    batch_path = (root / args.batch).resolve()
    db_path = (root / args.db).resolve()

    statuses = fetch_run_statuses(db_path)
    plans = plan_missing_runs(
        batch_path=batch_path,
        db_status=statuses,
        rerun_failed=args.rerun_failed,
        only_prefix=args.only_prefix,
    )

    print(f"planned_missing_runs={len(plans)}")
    failures = execute_runs(
        root=root,
        plans=plans,
        verbosity=args.verbosity,
        dry_run=args.dry_run,
        max_runs=args.max_runs,
    )
    if failures > 0:
        raise SystemExit(1)


if __name__ == "__main__":
    main()
