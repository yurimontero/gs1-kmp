#!/usr/bin/env python3
"""
Generates gs1-core's AI table (AiDictionary.kt) from the GS1 Barcode Syntax Dictionary.

Usage (from the repository root):
    python3 tools/generate_ai_dictionary.py

Update tools/gs1-syntax-dictionary.txt from
https://github.com/gs1/gs1-syntax-dictionary when GS1 publishes new AIs, then rerun.
The script fails if the dictionary uses a validation rule ("linter") that gs1-core
does not know about, so new rules are never silently ignored.
"""
import pathlib
import re
import sys

ROOT = pathlib.Path(__file__).resolve().parent.parent
SOURCE = ROOT / "tools" / "gs1-syntax-dictionary.txt"
TARGET = ROOT / "gs1-core/src/commonMain/kotlin/io/github/yurimontero/gs1/AiDictionary.kt"

# Linters implemented in Linters.kt
IMPLEMENTED = {
    "csum", "csumalpha", "yymmd0", "yymmdd", "yyyymmd0", "yyyymmdd", "hh", "mi", "ss", "hhmi",
    "nonzero", "zero", "yesno", "hyphen", "winding", "pieceoftotal", "nozeroprefix", "hasnondigit",
    "iso5218", "latitude", "longitude", "posinseqslash", "pcenc", "iso3166", "iso3166999",
    "iso3166alpha2", "iso4217", "iban", "mediatype", "importeridx", "packagetype",
}
# Linters deliberately not implemented (documented in Linters.kt)
SKIPPED = {"gcppos1", "gcppos2", "couponcode", "couponposoffer"}

LINE = re.compile(r"^(?P<ais>\d+(?:-\d+)?)\s+(?P<flags>[*?!\"$%&'()+,\-./:;<=>@\[\\\]^_`{|}~]*)\s+(?P<rest>.*)$")
COMPONENT = re.compile(r"^\[?[NXYZ](?:\d+|\.\.\d+)\]?(?:,[a-z0-9]+)*$")


def kotlin_string(s: str) -> str:
    return '"' + s.replace("\\", "\\\\").replace('"', '\\"').replace("$", "\\$") + '"'


def main() -> int:
    entries = []
    unknown = set()
    for raw in SOURCE.read_text(encoding="utf-8").splitlines():
        line = raw.strip()
        if not line or line.startswith("#"):
            continue
        m = LINE.match(line)
        if not m:
            raise SystemExit(f"Cannot parse line: {raw}")
        ais, flags, rest = m.group("ais"), m.group("flags"), m.group("rest")
        title = ""
        if "#" in rest:
            rest, title = rest.split("#", 1)
            title = title.strip()
        components = []
        for token in rest.split():
            if COMPONENT.match(token):
                components.append(token)
                for linter in token.split(",")[1:]:
                    if linter not in IMPLEMENTED and linter not in SKIPPED:
                        unknown.add(linter)
            else:
                break  # attributes (req=, ex=, dlpkey…) start here
        if not components:
            raise SystemExit(f"No components in line: {raw}")
        predefined = "*" in flags
        entries.append((ais, predefined, " ".join(components), title))

    if unknown:
        raise SystemExit(f"Unknown linters (implement them or add to SKIPPED): {sorted(unknown)}")

    out = []
    out.append("// GENERATED FILE - DO NOT EDIT. Run `python3 tools/generate_ai_dictionary.py` instead.")
    out.append("//")
    out.append("// Source: GS1 Barcode Syntax Dictionary (https://github.com/gs1/gs1-syntax-dictionary)")
    out.append("// Copyright (c) 2020-2021 BWIPP project, (c) 2020-2021 Zint Project, (c) 2021-2025 GS1 AISBL.")
    out.append("// Licensed under the Apache License, Version 2.0.")
    out.append("")
    out.append("package io.github.yurimontero.gs1")
    out.append("")
    out.append("/**")
    out.append(" * One entry per line of the GS1 Syntax Dictionary: `AI or range | * if predefined length | components | title`.")
    out.append(" */")
    out.append("internal val AI_DICTIONARY: Array<String> = arrayOf(")
    for ais, predefined, spec, title in entries:
        out.append("    " + kotlin_string(f"{ais}|{'*' if predefined else ''}|{spec}|{title}") + ",")
    out.append(")")
    out.append("")
    TARGET.write_text("\n".join(out), encoding="utf-8")
    print(f"Wrote {len(entries)} entries to {TARGET.relative_to(ROOT)}")
    return 0


if __name__ == "__main__":
    sys.exit(main())
