# Changelog

All notable changes to this project are documented here. The format follows
[Keep a Changelog](https://keepachangelog.com/en/1.1.0/) and the project uses [Semantic Versioning](https://semver.org/).
Before 1.0.0, minor versions may change the public API.

## [Unreleased]

## [0.1.0] - 2026-10-25

First release.

### gs1-core

- GS1 messages: `Gs1Message.parse` (bracketed AI syntax), `Gs1Message.of`, `Gs1Message.build { }` and
  `Gs1Message.parseElementString` for scanner output, with element order optimised for size.
- Every GS1 Application Identifier from the GS1 Barcode Syntax Dictionary, with named constants in `Gs1Ai`.
- Validation with the GS1 Barcode Syntax Tests (check digits, dates, ISO codes and more), reporting the AI
  and position of each error.
- GS1-128 encoder with optimal Code 128 set selection and GS1 length limits.
- GS1 DataMatrix encoder (ECC 200): all square and rectangular sizes.
- GS1 Digital Link URI generation and parsing.
- QR Code encoder (versions 1-40, levels L/M/Q/H) with optimal segmentation, and GS1 Digital Link QR Codes.
- SVG output for every symbol.
- Targets: Android, iOS (arm64, simulator arm64), JVM, JS and Wasm.

### gs1-compose

- Composables `Gs1DataMatrixBarcode`, `Gs1Code128Barcode` and `Gs1QrCodeBarcode` with whole-pixel modules.
- Painters (`DataMatrixPainter`, `Code128Painter`, `QrCodePainter`) and `toImageBitmap` for every symbol.

[Unreleased]: https://github.com/yurimontero/gs1-kmp/compare/v0.1.0...HEAD
[0.1.0]: https://github.com/yurimontero/gs1-kmp/releases/tag/v0.1.0
