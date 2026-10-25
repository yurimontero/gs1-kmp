#!/usr/bin/env python3
"""
Generates gs1-core's AI table (AiDictionary.kt) and AI names (Gs1Ai.kt, from tools/gs1-ai-names.txt)
from the GS1 Barcode Syntax Dictionary.

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
NAMES = ROOT / "tools" / "gs1-ai-names.txt"
NAMES_TARGET = ROOT / "gs1-core/src/commonMain/kotlin/io/github/yurimontero/gs1/Gs1Ai.kt"

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
        tokens = rest.split()
        while tokens and COMPONENT.match(tokens[0]):
            token = tokens.pop(0)
            components.append(token)
            for linter in token.split(",")[1:]:
                if linter not in IMPLEMENTED and linter not in SKIPPED:
                    unknown.add(linter)
        if not components:
            raise SystemExit(f"No components in line: {raw}")
        # Remaining tokens are attributes (req=, ex=, dlpkey…). Keep the Digital Link primary key ones:
        # "" = not a key, "-" = key without qualifiers, "22,10,21|235" = allowed qualifier sequences
        dlpkey = ""
        for token in tokens:
            if token == "dlpkey":
                dlpkey = "-"
            elif token.startswith("dlpkey="):
                dlpkey = token[len("dlpkey="):]
        # Flags kept: "*" predefined length, "?" permitted as a Digital Link query parameter
        kept_flags = "".join(f for f in "*?" if f in flags)
        entries.append((ais, kept_flags, " ".join(components), title, dlpkey))

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
    out.append(" * One entry per line of the GS1 Syntax Dictionary:")
    out.append(" * `AI or range | flags | components | title | Digital Link key qualifiers`.")
    out.append(" *")
    out.append(" * Flags: `*` predefined length (no FNC1 needed), `?` allowed as a GS1 Digital Link query parameter.")
    out.append(" * Qualifiers: empty if not a Digital Link primary key, `-` if a key without qualifiers, otherwise")
    out.append(" * the allowed qualifier sequences, e.g. `22,10,21|235`.")
    out.append(" */")
    out.append("internal val AI_DICTIONARY: Array<String> = arrayOf(")
    for ais, flags, spec, title, dlpkey in entries:
        out.append("    " + kotlin_string(f"{ais}|{flags}|{spec}|{title}|{dlpkey}") + ",")
    out.append(")")
    out.append("")
    TARGET.write_text("\n".join(out), encoding="utf-8")
    print(f"Wrote {len(entries)} entries to {TARGET.relative_to(ROOT)}")
    write_names(entries)
    return 0


NAME_LINE = re.compile(r"^(?P<ais>\d+(?:-\d+)?)\s*\|\s*(?P<name>[^|]+?)\s*\|\s*(?P<description>.+?)\s*$")
CONSTANT = re.compile(r"^[A-Z][A-Z0-9_]*$")
FUNCTION = re.compile(r"^(?P<fn>[a-z][A-Za-z0-9]*)\((?P<param>[a-z][A-Za-z0-9]*)(?:=(?P<first>\d+))?\)$")


def kdoc(text: str) -> str:
    return text.replace("*/", "*&#47;")


def write_names(entries) -> None:
    names = {}
    for raw in NAMES.read_text(encoding="utf-8").splitlines():
        line = raw.strip()
        if not line or line.startswith("#"):
            continue
        m = NAME_LINE.match(line)
        if not m:
            raise SystemExit(f"Cannot parse name line: {raw}")
        names[m.group("ais")] = (m.group("name"), m.group("description"))

    def expand(ais):
        if "-" not in ais:
            return [ais]
        start, end = ais.split("-")
        return [str(n).zfill(len(start)) for n in range(int(start), int(end) + 1)]

    # A names range may group several consecutive dictionary entries (e.g. 7030 ... 7039)
    owner = {}
    for key in names:
        for code in expand(key):
            if code in owner:
                raise SystemExit(f"AI {code} named twice in tools/gs1-ai-names.txt")
            owner[code] = key
    dictionary_codes = [code for e in entries for code in expand(e[0])]
    missing = [code for code in dictionary_codes if code not in owner]
    extra = [code for code in owner if code not in dictionary_codes]
    if missing or extra:
        raise SystemExit(f"tools/gs1-ai-names.txt out of date. Missing: {missing}. Not in dictionary: {extra}")
    used = set()
    emitted = set()

    out = [
        "// GENERATED FILE - DO NOT EDIT. Edit tools/gs1-ai-names.txt and run `python3 tools/generate_ai_dictionary.py`.",
        "",
        "package io.github.yurimontero.gs1",
        "",
        "/**",
        " * Codes of every GS1 Application Identifier known to the library.",
        " *",
        " * ```",
        " * Gs1Message.of(Gs1Ai.GTIN to \"09501101020917\", Gs1Ai.BATCH_LOT to \"ABC123\")",
        " * Gs1Ai.netWeightKg(3)   // \"3103\": AIs whose last digit is a parameter are functions",
        " * ```",
        " */",
        "public object Gs1Ai {",
    ]
    for dictionary_ais, _flags, spec, title, _dlpkey in entries:
        ais = owner[expand(dictionary_ais)[0]]
        if ais in emitted:
            continue
        emitted.add(ais)
        name, description = names[ais]
        fmt = spec
        grouped = ais != dictionary_ais
        doc_title = f" GS1 data title: {title}." if title and not grouped else ""
        if "-" not in ais:
            if not CONSTANT.match(name):
                raise SystemExit(f"Constant name expected for AI {ais}: {name}")
            if name in used:
                raise SystemExit(f"Duplicate name {name}")
            used.add(name)
            out.append(f"    /** ({ais}) {kdoc(description)}. Format: `{fmt}`.{kdoc(doc_title)} */")
            out.append(f"    public const val {name}: String = \"{ais}\"")
            out.append("")
        else:
            m = FUNCTION.match(name)
            if not m:
                raise SystemExit(f"Function name expected for range {ais}: {name}")
            fn, param = m.group("fn"), m.group("param")
            if fn in used:
                raise SystemExit(f"Duplicate name {fn}")
            used.add(fn)
            first_value = int(m.group("first") or 0)
            start, end = ais.split("-")
            last_value = first_value + int(end) - int(start)
            width = len(start)
            out.append(f"    /**")
            out.append(f"     * ({start}-{end}) {kdoc(description)}. Format: `{fmt}`.{kdoc(doc_title)}")
            out.append(f"     *")
            out.append(f"     * @param {param} {first_value}-{last_value}; {fn}({first_value}) is AI {start}.")
            out.append(f"     */")
            out.append(f"    public fun {fn}({param}: Int): String {{")
            out.append(f"        require({param} in {first_value}..{last_value}) {{ \"{param} must be {first_value}-{last_value}, was ${param}\" }}")
            offset = f" - {first_value}" if first_value else ""
            out.append(f"        return ({int(start)} + {param}{offset}).toString().padStart({width}, '0')")
            out.append(f"    }}")
            out.append("")
    if out[-1] == "":
        out.pop()
    out.append("}")
    out.append("")
    NAMES_TARGET.write_text("\n".join(out), encoding="utf-8")
    print(f"Wrote {len(entries)} AI names to {NAMES_TARGET.relative_to(ROOT)}")


if __name__ == "__main__":
    sys.exit(main())
