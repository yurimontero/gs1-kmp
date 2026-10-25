# gs1-kmp

Generate **GS1-128**, **GS1 DataMatrix** and **GS1 Digital Link QR Code** barcodes on the device, from Kotlin
Multiplatform code. No server, no native libraries, no dependencies besides the Kotlin standard library.

```kotlin
val message = Gs1Message.build {
    gtin("09501101020917")
    expiryDate(2026, 12, 31)
    batch("ABC123")
}

val dataMatrix = Gs1DataMatrix.encode(message)   // GS1 DataMatrix
val gs1128 = Gs1Code128.encode(message)          // GS1-128 (Code 128)
val qr = Gs1QrCode.encode(message)               // GS1 Digital Link QR Code
qr.text  // https://id.gs1.org/01/09501101020917/10/ABC123?17=261231

val svg = dataMatrix.toSvg(moduleSize = 0.5, unit = "mm")
```

## Modules

| Module | What it does | Depends on |
| --- | --- | --- |
| `gs1-core` | GS1 data and validation, GS1 Digital Link URIs, GS1-128, Data Matrix and QR Code encoders, SVG output | Kotlin standard library |
| `gs1-compose` | Composables, painters and bitmaps for the symbols | `gs1-core`, Compose Multiplatform |

Both modules support **Android**, **iOS**, **desktop JVM** (Windows, macOS, Linux), **JavaScript** and
**WebAssembly**.

## What you get

- **Every GS1 Application Identifier**, generated from GS1's official
  [Barcode Syntax Dictionary](https://github.com/gs1/gs1-syntax-dictionary), with named constants
  (`Gs1Ai.GTIN`, `Gs1Ai.BATCH_LOT`…) and a readable builder.
- **Validation as strict as GS1's own tools**: check digits, dates, country and currency codes and the rest of the
  GS1 Barcode Syntax Tests, ported from the [GS1 Syntax Engine](https://github.com/gs1/gs1-syntax-engine) and
  checked against its reference test vectors. Errors say which AI failed and where.
- **Small symbols**: GS1-128 switches between Code 128 sets to use as few characters as possible, Data Matrix
  picks the smallest size that fits, and QR Code mixes numeric, alphanumeric and byte segments optimally.
- **Reading as well as writing**: parse what a scanner returns, or a GS1 Digital Link URI, back into GS1 data.
- **Output you can print**: SVG with exact module sizes in millimetres, or Compose drawing with whole-pixel
  modules.

[Get started](getting-started.md){ .md-button .md-button--primary }
[API reference](api/index.html){ .md-button }

## Status

Version 0.1.0 is on Maven Central. Until 1.0.0, minor versions may change the public API; see the
[changelog](https://github.com/yurimontero/gs1-kmp/blob/main/CHANGELOG.md).
