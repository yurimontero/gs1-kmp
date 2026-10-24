#!/usr/bin/env python3
"""
Extracts the unit test vectors of the GS1 Barcode Syntax Tests ("linters") from a checkout of
https://github.com/gs1/gs1-syntax-engine and writes them as a Kotlin test for gs1-core.

Usage (from the repository root):
    git clone --depth 1 https://github.com/gs1/gs1-syntax-engine /tmp/gs1-syntax-engine
    python3 tools/extract_lint_tests.py /tmp/gs1-syntax-engine
"""
import pathlib
import re
import sys

ROOT = pathlib.Path(__file__).resolve().parent.parent
TARGET = ROOT / "gs1-core/src/commonTest/kotlin/io/github/yurimontero/gs1/LinterReferenceTest.kt"
SKIPPED = {"gcppos1", "gcppos2", "couponcode", "couponposoffer"}
CHUNK = 250

C_STRING = r'"(?:[^"\\]|\\.)*"'
CALL = re.compile(
    r"UNIT_TEST_(PASS|FAIL)\(\s*gs1_lint_(\w+)\s*,\s*((?:" + C_STRING + r"\s*)+)"
    r"(?:,\s*GS1_LINTER_(\w+)\s*,\s*((?:" + C_STRING + r"\s*)+))?\)\s*;",
    re.S,
)


def c_unescape(literals: str) -> str:
    out = []
    for lit in re.findall(C_STRING, literals):
        body = lit[1:-1]
        i = 0
        while i < len(body):
            c = body[i]
            if c != "\\":
                out.append(c)
                i += 1
                continue
            n = body[i + 1]
            if n == "x":
                m = re.match(r"[0-9a-fA-F]+", body[i + 2:])
                out.append(chr(int(m.group(0), 16)))
                i += 2 + len(m.group(0))
            elif n in "01234567":
                m = re.match(r"[0-7]{1,3}", body[i + 1:])
                out.append(chr(int(m.group(0), 8)))
                i += 1 + len(m.group(0))
            else:
                out.append({"n": "\n", "t": "\t", "r": "\r", "\\": "\\", '"': '"', "'": "'", "?": "?"}[n])
                i += 2
    return "".join(out)


def highlight_span(data: str, marked: str):
    # Prefer the longest span: data containing "*" can make the highlight ambiguous
    for length in range(len(data), -1, -1):
        for pos in range(len(data) - length + 1):
            if data[:pos] + "*" + data[pos:pos + length] + "*" + data[pos + length:] == marked:
                return pos, length
    raise ValueError(f"Cannot locate highlight {marked!r} in {data!r}")


def kotlin_string(s: str) -> str:
    out = ['"']
    for ch in s:
        if ch == "\\":
            out.append("\\\\")
        elif ch == '"':
            out.append('\\"')
        elif ch == "$":
            out.append("\\$")
        elif 32 <= ord(ch) < 127:
            out.append(ch)
        else:
            out.append("\\u%04x" % ord(ch))
    out.append('"')
    return "".join(out)


def main() -> int:
    if len(sys.argv) != 2:
        print(__doc__)
        return 1
    syntax_dir = pathlib.Path(sys.argv[1]) / "src/c-lib/syntax"
    cases = {}
    for path in sorted(syntax_dir.glob("lint_*.c")):
        text = path.read_text(encoding="utf-8")
        for m in CALL.finditer(text):
            kind, linter, data, error, marked = m.groups()
            if linter in SKIPPED:
                continue
            value = c_unescape(data)
            if kind == "PASS":
                cases.setdefault(linter, []).append(f"pass({kotlin_string(value)})")
            else:
                pos, length = highlight_span(value, c_unescape(marked))
                cases.setdefault(linter, []).append(
                    f"fail({kotlin_string(value)}, Gs1ErrorCode.{error}, {pos}, {length})"
                )

    total = sum(len(v) for v in cases.values())
    out = [
        "// GENERATED FILE - DO NOT EDIT. Run `python3 tools/extract_lint_tests.py <gs1-syntax-engine>` instead.",
        "//",
        "// Test vectors from the GS1 Syntax Engine linter unit tests (https://github.com/gs1/gs1-syntax-engine).",
        "// Copyright (c) 2022-2025 GS1 AISBL. Licensed under the Apache License, Version 2.0.",
        "",
        "package io.github.yurimontero.gs1",
        "",
        "import kotlin.test.Test",
        "",
        f"/** {total} reference cases for {len(cases)} linters. */",
        "class LinterReferenceTest {",
        "",
    ]
    for linter in sorted(cases):
        lines = cases[linter]
        for index in range(0, len(lines), CHUNK):
            suffix = "" if len(lines) <= CHUNK else f"_{index // CHUNK + 1}"
            out.append("    @Test")
            out.append(f"    fun {linter}{suffix}() {{")
            out.append(f"        val cases = LinterCases({kotlin_string(linter)})")
            out.extend("        cases." + line for line in lines[index:index + CHUNK])
            out.append("        cases.verify()")
            out.append("    }")
            out.append("")
    out.append("}")
    out.append("")
    TARGET.write_text("\n".join(out), encoding="utf-8")
    print(f"Wrote {total} cases for {len(cases)} linters to {TARGET.relative_to(ROOT)}")
    return 0


if __name__ == "__main__":
    sys.exit(main())
