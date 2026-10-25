# gs1-kmp

Kotlin Multiplatform library to generate **GS1-128** (Code 128), **GS1 DataMatrix** and **GS1 Digital Link QR Code** barcodes on-device, with no third-party dependencies.

| Module | Description | Targets |
| --- | --- | --- |
| `gs1-core` | GS1 element strings, validation, GS1 Digital Link URIs, Code 128, Data Matrix and QR Code encoders, SVG output. Depends only on the Kotlin standard library. | Android, iOS, JVM (desktop), JS, Wasm |
| `gs1-compose` | Compose Multiplatform drawing layer on top of `gs1-core`. | Android, iOS, JVM (desktop), JS, Wasm |

> Work in progress — not published yet.

## Coordinates

```kotlin
implementation("io.github.yurimontero:gs1-core:<version>")
implementation("io.github.yurimontero:gs1-compose:<version>")
```

## Usage

```kotlin
// Build and validate GS1 data (throws Gs1Exception with the exact problem)
val message = Gs1Message.parse("(01)09501101020917(17)261231(10)ABC123")
// or: Gs1Message.of(Gs1Ai.GTIN to "09501101020917", Gs1Ai.EXPIRY_DATE to "261231", Gs1Ai.BATCH_LOT to "ABC123")
// or:
val built = Gs1Message.build {
    gtin("09501101020917")
    expiryDate(2026, 12, 31)
    batch("ABC123")
    netWeightKg("1.250")                                // (3103)001250
}

// GS1 DataMatrix
val dataMatrix = Gs1DataMatrix.encode(message)          // smallest square symbol
val svg = dataMatrix.toSvg(moduleSize = 0.5, unit = "mm")

// GS1-128
val gs1128 = Gs1Code128.encode(message)
val barsSvg = gs1128.toSvg()                            // bars + human readable text

// GS1 Digital Link QR Code: https://example.com/01/09501101020917/10/ABC123?17=261231
val qr = Gs1QrCode.encode(message, domain = "https://example.com")
qr.text                                                 // the URI
val qrSvg = qr.toSvg(moduleSize = 0.4, unit = "mm")
val uri = message.toDigitalLinkUri("https://example.com") // just the URI
val fromUri = Gs1DigitalLink.parse(uri)                 // and back

// Reading scanner output
val scanned = Gs1Message.parseElementString("]d2010950110102091717261231" + "10ABC123")
scanned["10"]                                           // "ABC123"
```

With Compose Multiplatform (`gs1-compose`):

```kotlin
Gs1DataMatrixBarcode(message, modifier = Modifier.size(160.dp))
Gs1Code128Barcode(message)                              // bars + human readable text
Gs1QrCodeBarcode(message, domain = "https://example.com")
val bitmap = Gs1DataMatrix.encode(message).toImageBitmap(modulePixels = 8)
```

## Documentation

Guides and API reference: https://yurimontero.github.io/gs1-kmp/ (published by `.github/workflows/docs.yml`).

To preview locally:

```bash
pip install zensical==0.0.72
zensical serve                               # guides at http://localhost:8000/gs1-kmp/
./gradlew :dokkaGeneratePublicationHtml      # API reference in build/dokka/html
```

## Building

Requirements: JDK 17+ and the Android SDK (open the project once in Android Studio or IntelliJ IDEA, or create `local.properties` with `sdk.dir=/path/to/Android/Sdk`).

```bash
./gradlew build          # compile every target and run the tests
./gradlew jvmTest        # fast feedback while developing
./gradlew allTests       # tests on JVM, JS (Node.js), Wasm (Node.js) and, on macOS, the iOS simulator
```

iOS klibs can be built on Linux; running the iOS tests requires macOS.

## GS1 data

The AI table is generated from the official [GS1 Barcode Syntax Dictionary](https://github.com/gs1/gs1-syntax-dictionary), and value validation is a port of the GS1 Barcode Syntax Tests from the [GS1 Syntax Engine](https://github.com/gs1/gs1-syntax-engine), checked against its reference test vectors.

```bash
python3 tools/generate_ai_dictionary.py                       # regenerate AiDictionary.kt
python3 tools/extract_lint_tests.py /path/to/gs1-syntax-engine # regenerate LinterReferenceTest.kt
python3 tools/extract_dl_tests.py /path/to/gs1-syntax-engine   # regenerate DigitalLinkReferenceTest.kt
```

GS1 Digital Link URI generation and parsing follow the Syntax Engine and pass its reference test vectors. The QR Code encoder (ISO/IEC 18004, versions 1-40, levels L/M/Q/H) chooses the smallest mix of numeric, alphanumeric and byte segments; an uppercase domain such as `HTTPS://EXAMPLE.COM` gives a smaller symbol.

Not checked yet: GS1 Company Prefix positions (`gcppos1`, `gcppos2`), coupon formats (AIs 8110, 8112) and AI pairing rules (`req`/`ex`).

## License

```
Copyright 2026 Yuri Montero

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    https://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```

See [NOTICE](NOTICE) for the GS1 material this library is derived from.
