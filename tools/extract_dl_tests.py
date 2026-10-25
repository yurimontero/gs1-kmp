#!/usr/bin/env python3
"""
Extracts the GS1 Digital Link unit test vectors (URI parsing and generation) from a checkout of
https://github.com/gs1/gs1-syntax-engine and writes them as a Kotlin test for gs1-core.

Usage (from the repository root):
    git clone --depth 1 https://github.com/gs1/gs1-syntax-engine /tmp/gs1-syntax-engine
    python3 tools/extract_dl_tests.py /tmp/gs1-syntax-engine

Cases that need options gs1-core does not offer (convenience alphas such as /gtin/, unknown AIs)
are skipped.
"""
import pathlib
import re
import sys

ROOT = pathlib.Path(__file__).resolve().parent.parent
TARGET = ROOT / "gs1-core/src/commonTest/kotlin/io/github/yurimontero/gs1/DigitalLinkReferenceTest.kt"

C_STRING = r'"(?:[^"\\]|\\.)*"'

# Syntax Engine error -> Gs1ErrorCode; None means "any validation error" (linter failures)
ERRORS = {
    "URI_CONTAINS_ILLEGAL_SCHEME": "DIGITAL_LINK_MALFORMED_URI",
    "URI_MISSING_DOMAIN_AND_PATH_INFO": "DIGITAL_LINK_MALFORMED_URI",
    "DOMAIN_CONTAINS_ILLEGAL_CHARACTERS": "DIGITAL_LINK_MALFORMED_URI",
    "URI_CONTAINS_ILLEGAL_CHARACTERS": "DIGITAL_LINK_MALFORMED_URI",
    "DECODED_AI_FROM_DL_PATH_INFO_CONTAINS_ILLEGAL_NULL": "DIGITAL_LINK_MALFORMED_URI",
    "DECODED_AI_VALUE_FROM_QUERY_PARAMS_CONTAINS_ILLEGAL_NULL": "DIGITAL_LINK_MALFORMED_URI",
    "NO_GS1_DL_KEYS_FOUND_IN_PATH_INFO": "DIGITAL_LINK_NO_PRIMARY_KEY",
    "CANNOT_CREATE_DL_URI_WITHOUT_PRIMARY_KEY_AI": "DIGITAL_LINK_NO_PRIMARY_KEY",
    "INVALID_KEY_QUALIFIER_SEQUENCE": "DIGITAL_LINK_INVALID_QUALIFIERS",
    "AI_IS_NOT_VALID_DATA_ATTRIBUTE": "DIGITAL_LINK_INVALID_ATTRIBUTE",
    "AI_SHOULD_BE_IN_PATH_INFO": "DIGITAL_LINK_AI_SHOULD_BE_IN_PATH",
    "UNKNOWN_AI_IN_QUERY_PARAMS": "UNKNOWN_AI",
    "DUPLICATE_AI": "DUPLICATE_AI",
    "AI_VALUE_IS_TOO_SHORT": "VALUE_TOO_SHORT",
    "AI_VALUE_IS_TOO_LONG": "VALUE_TOO_LONG",
    "AI_VALUE_QUERY_ELEMENT_IN_EMPTY": "EMPTY_VALUE",
    "AI_VALUE_PATH_ELEMENT_IS_EMPTY": "EMPTY_VALUE",
    "AI_LINTER_ERROR": None,
}


def strip_comments(source: str) -> str:
    """Removes // and /* */ comments, leaving string literals intact."""
    return re.sub(r'(' + C_STRING + r')|//[^\n]*|/\*.*?\*/', lambda m: m.group(1) or "", source, flags=re.S)


def c_unescape(literals: str) -> str:
    out = []
    for lit in re.findall(C_STRING, literals):
        out.append(bytes(lit[1:-1], "latin1").decode("unicode_escape"))
    return "".join(out)


def kotlin_string(s: str) -> str:
    out = ['"']
    for c in s:
        if c in '\\"$':
            out.append("\\" + c)
        elif ord(c) < 32 or ord(c) > 126:
            out.append("\\u%04x" % ord(c))
        else:
            out.append(c)
    out.append('"')
    return "".join(out)


def arg(name: str) -> str:
    return r"(?P<" + name + r">(?:" + C_STRING + r"\s*)+|NULL)"
STATEMENT = re.compile(
    r"(?P<flag>setPermitZeroSuppressedGTINinDLuris\(ctx, (?P<zero>true|false)\)"
    r"|permitConvenienceAlphas = (?P<alpha>true|false)"
    r"|setPermitUnknownAIs\(ctx, (?P<unknown>true|false)\))"
    r"|(?P<parse>test_parseDLuri\((?P<perr>\w+),\s*" + arg("uri") + r"\s*,\s*" + arg("out") + r"\s*\);)"
    r"|(?P<gen>test_testGenerateDLuri\((?P<gerr>\w+),\s*" + arg("stem") + r"\s*,\s*" + arg("data") + r"\s*,\s*" + arg("uri2") + r"\s*\);)",
    re.S,
)


def main() -> int:
    if len(sys.argv) != 2:
        raise SystemExit(__doc__)
    source = strip_comments((pathlib.Path(sys.argv[1]) / "src/c-lib/dl.c").read_text(encoding="utf-8"))
    zero = alpha = unknown = False
    parse_cases, generate_cases = [], []
    for m in STATEMENT.finditer(source):
        if m.group("zero"):
            zero = m.group("zero") == "true"
        elif m.group("alpha"):
            alpha = m.group("alpha") == "true"
        elif m.group("unknown"):
            unknown = m.group("unknown") == "true"
        elif m.group("parse"):
            if alpha or unknown:
                continue
            err = m.group("perr")
            uri, expected = c_unescape(m.group("uri")), c_unescape(m.group("out"))
            parse_cases.append((err, uri, expected, zero))
        elif m.group("gen"):
            if unknown:
                continue
            err = m.group("gerr")
            stem = None if m.group("stem") == "NULL" else c_unescape(m.group("stem"))
            data, expected = c_unescape(m.group("data")), c_unescape(m.group("uri2"))
            generate_cases.append((err, stem, data, expected))

    unknown_errors = {c[0] for c in parse_cases + generate_cases if c[0] != "OK" and c[0] not in ERRORS}
    if unknown_errors:
        raise SystemExit(f"Map these errors to Gs1ErrorCode: {sorted(unknown_errors)}")

    def error(err: str) -> str:
        code = ERRORS[err]
        return "null" if code is None else f"Gs1ErrorCode.{code}"

    out = [
        "// GENERATED FILE - DO NOT EDIT. Run `python3 tools/extract_dl_tests.py <gs1-syntax-engine>` instead.",
        "//",
        "// Test vectors from the GS1 Syntax Engine Digital Link unit tests (https://github.com/gs1/gs1-syntax-engine).",
        "// Copyright (c) 2022-2025 GS1 AISBL. Licensed under the Apache License, Version 2.0.",
        "",
        "package io.github.yurimontero.gs1",
        "",
        "import kotlin.test.Test",
        "",
        f"/** {len(parse_cases)} URI parsing and {len(generate_cases)} URI generation reference cases. */",
        "class DigitalLinkReferenceTest {",
        "",
        "    @Test",
        "    fun parse() {",
        "        val cases = DigitalLinkCases()",
    ]
    for err, uri, expected, zero in parse_cases:
        flag = ", zeroSuppressed = true" if zero else ""
        if err == "OK":
            out.append(f"        cases.parses({kotlin_string(uri)}, {kotlin_string(expected)}{flag})")
        else:
            out.append(f"        cases.rejects({kotlin_string(uri)}, {error(err)}{flag})")
    out += ["        cases.assertAllPassed()", "    }", "", "    @Test", "    fun generate() {", "        val cases = DigitalLinkCases()"]
    for err, stem, data, expected in generate_cases:
        domain = "null" if stem is None else kotlin_string(stem)
        if err == "OK":
            out.append(f"        cases.generates({domain}, {kotlin_string(data)}, {kotlin_string(expected)})")
        else:
            out.append(f"        cases.cannotGenerate({domain}, {kotlin_string(data)}, {error(err)})")
    out += ["        cases.assertAllPassed()", "    }", "}", ""]
    TARGET.write_text("\n".join(out), encoding="utf-8")
    print(f"Wrote {len(parse_cases)} parse and {len(generate_cases)} generation cases to {TARGET.relative_to(ROOT)}")
    return 0


if __name__ == "__main__":
    sys.exit(main())
