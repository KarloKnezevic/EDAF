#!/usr/bin/env python3
"""
Build an importer-compatible COCO reference CSV from official ppdata `pptables.html` pages.

The generated CSV matches EDAF's import format:
optimizer_name,function_id,dimension,target_value,ert,success_rate

Method:
- For each selected BBOB year, fetch `https://numbbo.github.io/ppdata-archive/bbob/<year>/pptables.html`.
- Parse each function/dimension table.
- Read the ratio value from `sorttable_customkey` in the target column (e.g. 1e-7).
- Multiply ratio by the header reference ERT shown in the same target column.
- Export absolute ERT values for each optimizer row.

This keeps the source of truth official (COCO ppdata) while producing a compact CSV
that can be imported directly via:
  ./edaf coco import-reference --csv <generated.csv> --suite bbob --db-url jdbc:sqlite:edaf-v3.db
"""

from __future__ import annotations

import argparse
import csv
import html
import math
import re
import sys
import urllib.error
import urllib.request
from dataclasses import dataclass
from pathlib import Path
from typing import Iterable

DEFAULT_YEARS = [
    "2009",
    "2010",
    "2012",
    "2013",
    "2014",
    "2015-CEC",
    "2015-GECCO",
    "2016",
    "2017",
    "2018",
    "2019",
    "2020",
    "2021",
    "2022",
    "2023",
]

PPDATA_URL_TEMPLATE = "https://numbbo.github.io/ppdata-archive/bbob/{year}/pptables.html"


@dataclass(frozen=True)
class ReferenceRow:
    optimizer_name: str
    function_id: int
    dimension: int
    target_value: float
    ert: float
    success_rate: float


def fetch_html(url: str) -> str:
    request = urllib.request.Request(url, headers={"User-Agent": "EDAF-COCO-Reference-Builder/1.0"})
    try:
        with urllib.request.urlopen(request, timeout=60) as response:
            charset = response.headers.get_content_charset() or "utf-8"
            return response.read().decode(charset, errors="replace")
    except urllib.error.URLError as exc:
        raise RuntimeError(f"Failed to fetch {url}: {exc}") from exc


def strip_tags(raw: str) -> str:
    text = re.sub(r"<[^>]+>", "", raw)
    return html.unescape(text).strip()


def normalize_target_label(label: str) -> str:
    cleaned = label.strip().lower().replace(" ", "")
    cleaned = cleaned.replace("−", "-").replace("+", "")
    return cleaned


def parse_number(value: str) -> float:
    token = value.strip()
    token = token.replace("−", "-")
    token = token.replace("&minus;", "-")
    token = token.replace("\u2212", "-")
    token = token.replace(",", "")
    token = token.replace("\u00a0", "")
    if token in {"", "-"}:
        return math.nan
    if token in {"inf", "infty", "∞"}:
        return math.inf
    if token.startswith("&infin"):
        return math.inf
    return float(token)


def parse_success_rate(cell_html: str) -> float:
    text = strip_tags(cell_html)
    match = re.search(r"(\d+)\s*/\s*(\d+)", text)
    if not match:
        return 0.0
    numerator = int(match.group(1))
    denominator = int(match.group(2))
    if denominator <= 0:
        return 0.0
    return numerator / float(denominator)


def parse_table_rows(
    table_html: str,
    year: str,
    wanted_functions: set[int],
    wanted_dimensions: set[int],
    target_label: str,
    target_value: float,
) -> list[ReferenceRow]:
    rows: list[ReferenceRow] = []

    func_match = re.search(r"<b>\s*f(\d+)\s*,\s*(\d+)\s*-D\s*</b>", table_html, flags=re.IGNORECASE)
    if not func_match:
        return rows

    function_id = int(func_match.group(1))
    dimension = int(func_match.group(2))

    if function_id not in wanted_functions or dimension not in wanted_dimensions:
        return rows

    thead_match = re.search(r"<thead>\s*<tr>(.*?)</tr>\s*</thead>", table_html, flags=re.IGNORECASE | re.DOTALL)
    if not thead_match:
        return rows

    header_cells = re.findall(r"<td[^>]*>(.*?)</td>", thead_match.group(1), flags=re.IGNORECASE | re.DOTALL)
    target_index = -1
    reference_ert = math.nan

    normalized_target = normalize_target_label(target_label)

    for idx, header in enumerate(header_cells):
        parts = re.split(r"<br\s*/?>", header, maxsplit=1, flags=re.IGNORECASE)
        if len(parts) < 2:
            continue
        label = normalize_target_label(strip_tags(parts[0]))
        if label != normalized_target:
            continue
        reference_ert = parse_number(strip_tags(parts[1]))
        target_index = idx
        break

    if target_index < 0 or not math.isfinite(reference_ert) or reference_ert <= 0:
        return rows

    after_head = table_html.split("</thead>", 1)
    if len(after_head) < 2:
        return rows

    tbody = after_head[1]
    if "<tbody>" in tbody.lower():
        tbody = re.split(r"<tbody>", tbody, maxsplit=1, flags=re.IGNORECASE)[1]
    tbody = re.sub(r"</tbody>\s*$", "", tbody, flags=re.IGNORECASE | re.DOTALL)
    row_chunks = re.findall(r"<tr>\s*(.*?)(?=<tr>|$)", tbody, flags=re.IGNORECASE | re.DOTALL)

    for row_html in row_chunks:
        algo_match = re.search(r"<th[^>]*>(.*?)</th>", row_html, flags=re.IGNORECASE | re.DOTALL)
        if not algo_match:
            continue
        algo_name = strip_tags(algo_match.group(1))
        if not algo_name:
            continue

        td_blocks = re.findall(r"(<td[^>]*>.*?</td>)", row_html, flags=re.IGNORECASE | re.DOTALL)
        if len(td_blocks) <= target_index:
            continue

        target_cell = td_blocks[target_index]
        key_match = re.search(r"sorttable_customkey=\"([^\"]+)\"", target_cell, flags=re.IGNORECASE)
        if not key_match:
            continue

        ratio = parse_number(key_match.group(1))
        if not math.isfinite(ratio) or ratio <= 0:
            continue

        ert = ratio * reference_ert
        if not math.isfinite(ert) or ert <= 0:
            continue

        success_rate = parse_success_rate(td_blocks[-1]) if td_blocks else 0.0

        optimizer_name = f"{algo_name} ({year})"
        rows.append(
            ReferenceRow(
                optimizer_name=optimizer_name,
                function_id=function_id,
                dimension=dimension,
                target_value=target_value,
                ert=ert,
                success_rate=success_rate,
            )
        )

    return rows


def extract_rows(
    years: Iterable[str],
    functions: set[int],
    dimensions: set[int],
    target_label: str,
    target_value: float,
) -> list[ReferenceRow]:
    extracted: list[ReferenceRow] = []

    for year in years:
        url = PPDATA_URL_TEMPLATE.format(year=year)
        print(f"[INFO] Fetching {url}", file=sys.stderr)
        try:
            page_html = fetch_html(url)
        except RuntimeError as exc:
            # Some archive buckets are listed but do not expose pptables.html.
            print(f"[WARN] {exc}", file=sys.stderr)
            continue
        tables = re.findall(
            r"<table\s+class=\"sortable\"\s*>(.*?)</table>",
            page_html,
            flags=re.IGNORECASE | re.DOTALL,
        )

        if not tables:
            print(f"[WARN] No sortable tables found for year {year}", file=sys.stderr)
            continue

        year_count = 0
        for table_html in tables:
            parsed = parse_table_rows(
                table_html=table_html,
                year=year,
                wanted_functions=functions,
                wanted_dimensions=dimensions,
                target_label=target_label,
                target_value=target_value,
            )
            extracted.extend(parsed)
            year_count += len(parsed)

        print(f"[INFO] Extracted {year_count} rows for year {year}", file=sys.stderr)

    return extracted


def write_csv(output: Path, rows: list[ReferenceRow]) -> None:
    output.parent.mkdir(parents=True, exist_ok=True)

    unique_rows = list({
        (r.optimizer_name, r.function_id, r.dimension, r.target_value, r.ert, r.success_rate): r
        for r in rows
    }.values())
    unique_rows.sort(key=lambda r: (r.optimizer_name.lower(), r.function_id, r.dimension))

    with output.open("w", newline="", encoding="utf-8") as file:
        writer = csv.writer(file)
        writer.writerow([
            "optimizer_name",
            "function_id",
            "dimension",
            "target_value",
            "ert",
            "success_rate",
        ])
        for row in unique_rows:
            writer.writerow([
                row.optimizer_name,
                row.function_id,
                row.dimension,
                f"{row.target_value:.10g}",
                f"{row.ert:.12g}",
                f"{row.success_rate:.6f}",
            ])

    print(f"[INFO] Wrote {len(unique_rows)} unique rows to {output}", file=sys.stderr)


def parse_int_list(raw: str) -> set[int]:
    values: set[int] = set()
    for token in raw.split(","):
        token = token.strip()
        if not token:
            continue
        values.add(int(token))
    return values


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(description="Build COCO reference CSV from ppdata pptables.")
    parser.add_argument(
        "--years",
        default=",".join(DEFAULT_YEARS),
        help="Comma-separated year buckets from ppdata archive (default: 2009..2023 official set)",
    )
    parser.add_argument(
        "--functions",
        default="1,2,3,8,15",
        help="Comma-separated BBOB function ids to include",
    )
    parser.add_argument(
        "--dimensions",
        default="2,5,10,20",
        help="Comma-separated dimensions to include",
    )
    parser.add_argument(
        "--target-label",
        default="1e-7",
        help="Target column label as written in pptables (default: 1e-7)",
    )
    parser.add_argument(
        "--target-value",
        type=float,
        default=1.0e-7,
        help="Numeric target value stored into CSV rows (default: 1e-7)",
    )
    parser.add_argument(
        "--out",
        type=Path,
        required=True,
        help="Output CSV path",
    )
    return parser.parse_args()


def main() -> int:
    args = parse_args()

    years = [segment.strip() for segment in args.years.split(",") if segment.strip()]
    functions = parse_int_list(args.functions)
    dimensions = parse_int_list(args.dimensions)

    rows = extract_rows(
        years=years,
        functions=functions,
        dimensions=dimensions,
        target_label=args.target_label,
        target_value=args.target_value,
    )

    if not rows:
        print("[ERROR] No rows extracted. Check selected years/functions/dimensions/target.", file=sys.stderr)
        return 1

    write_csv(args.out, rows)

    coverage = sorted({(row.function_id, row.dimension) for row in rows})
    print(f"[INFO] Covered slices: {coverage}", file=sys.stderr)
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
