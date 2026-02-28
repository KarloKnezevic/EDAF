#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
cd "$ROOT_DIR"

SCAN_ROOT="${1:-edaf-core/src/main/java/com/knezevic/edaf/v3/core/api}"
export SCAN_ROOT

python3 <<'PY'
import pathlib
import re
import sys

ROOT = pathlib.Path(".")
scan_root = pathlib.Path(__import__("os").environ["SCAN_ROOT"])
if not scan_root.exists():
    print(f"Scan root not found: {scan_root}")
    sys.exit(2)
JAVA_FILES = sorted(scan_root.rglob("*.java"))

METHOD_PATTERN = re.compile(
    r"^\s*(public|protected)\s+(?!class\b|interface\b|enum\b|record\b)[^=;]*\([^;]*\)\s*(\{|;)\s*$"
)
CLASS_PATTERN = re.compile(r"^\s*(public\s+)?(final\s+)?(class|interface|record|enum)\s+\w+")


def previous_non_empty(lines, index):
    i = index - 1
    while i >= 0 and lines[i].strip() == "":
        i -= 1
    return i


def find_javadoc_start(lines, end_idx):
    i = end_idx
    while i >= 0:
        stripped = lines[i].strip()
        if stripped.startswith("/**"):
            return i
        if stripped.startswith("/*") or stripped.startswith("//"):
            return None
        i -= 1
    return None


violations = []

def split_parameters(raw):
    parts = []
    token = []
    angle = 0
    paren = 0
    bracket = 0
    for ch in raw:
        if ch == "<":
            angle += 1
        elif ch == ">":
            angle = max(0, angle - 1)
        elif ch == "(":
            paren += 1
        elif ch == ")":
            paren = max(0, paren - 1)
        elif ch == "[":
            bracket += 1
        elif ch == "]":
            bracket = max(0, bracket - 1)
        if ch == "," and angle == 0 and paren == 0 and bracket == 0:
            parts.append("".join(token).strip())
            token = []
            continue
        token.append(ch)
    final = "".join(token).strip()
    if final:
        parts.append(final)
    return parts


for path in JAVA_FILES:
    file_class_name = path.stem
    lines = path.read_text(encoding="utf-8").splitlines()
    for idx, line in enumerate(lines):
        stripped = line.strip()
        if not stripped:
            continue

        is_class = bool(CLASS_PATTERN.match(line))
        is_method = bool(METHOD_PATTERN.match(line))
        if not (is_class or is_method):
            continue

        prev_idx = previous_non_empty(lines, idx)
        if prev_idx < 0:
            violations.append((path, idx + 1, "Missing JavaDoc block"))
            continue

        while prev_idx >= 0 and lines[prev_idx].strip().startswith("@"):
            prev_idx = previous_non_empty(lines, prev_idx)
        if prev_idx < 0 or not lines[prev_idx].strip().endswith("*/"):
            violations.append((path, idx + 1, "Missing JavaDoc block"))
            continue

        doc_start = find_javadoc_start(lines, prev_idx)
        if doc_start is None:
            violations.append((path, idx + 1, "Malformed JavaDoc block"))
            continue

        block = "\n".join(lines[doc_start:prev_idx + 1])
        summary_line = None
        for doc_line in lines[doc_start + 1:prev_idx + 1]:
            content = doc_line.strip().lstrip("*").strip()
            if content and not content.startswith("@"):
                summary_line = content
                break
        if summary_line and not summary_line.endswith("."):
            violations.append((path, idx + 1, "JavaDoc summary must end with a period"))

        if is_class:
            if "@author " not in block:
                violations.append((path, idx + 1, "Missing @author tag"))
            if "@version " not in block:
                violations.append((path, idx + 1, "Missing @version tag"))
            continue

        signature = line.strip()
        method_name = signature.split("(")[0].split()[-1]
        is_constructor = method_name == file_class_name
        params_match = re.search(r"\((.*)\)", signature)
        params = params_match.group(1).strip() if params_match else ""
        non_void = (
            not is_constructor
            and " void " not in f" {signature} "
            and not signature.startswith("public void ")
            and not signature.startswith("protected void ")
        )
        if params and params != "":
            param_names = []
            for raw in split_parameters(params):
                fragment = raw.strip()
                if not fragment or fragment == "...":
                    continue
                name = fragment.split()[-1].replace("...", "")
                param_names.append(name)
            for name in param_names:
                if f"@param {name} " not in block:
                    violations.append((path, idx + 1, f"Missing @param tag for '{name}'"))
        if non_void and "@return " not in block:
            violations.append((path, idx + 1, "Missing @return tag"))

if violations:
    for file_path, line_no, message in violations:
        print(f"{file_path}:{line_no}: {message}")
    print(f"\nJavaDoc audit failed: {len(violations)} issue(s) found.")
    sys.exit(1)

print("JavaDoc audit passed.")
PY
