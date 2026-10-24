# gs1-kmp

Kotlin Multiplatform library to generate **GS1-128** (Code 128) and **GS1 DataMatrix** barcodes on-device, with no third-party dependencies.

| Module | Description | Targets |
| --- | --- | --- |
| `gs1-core` | GS1 element strings, validation, Code 128 and Data Matrix encoders, SVG output. Depends only on the Kotlin standard library. | Android, iOS, JVM (desktop), JS, Wasm |
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
// or: Gs1Message.of("01" to "09501101020917", "17" to "261231", "10" to "ABC123")

// GS1 DataMatrix
val dataMatrix = Gs1DataMatrix.encode(message)          // smallest square symbol
val svg = dataMatrix.toSvg(moduleSize = 0.5, unit = "mm")

// GS1-128
val gs1128 = Gs1Code128.encode(message)
val barsSvg = gs1128.toSvg()                            // bars + human readable text

// Reading scanner output
val scanned = Gs1Message.parseElementString("]d2010950110102091717261231" + "10ABC123")
scanned["10"]                                           // "ABC123"
```

With Compose Multiplatform (`gs1-compose`):

```kotlin
Gs1DataMatrixBarcode(message, modifier = Modifier.size(160.dp))
Gs1Code128Barcode(message)                              // bars + human readable text
val bitmap = Gs1DataMatrix.encode(message).toImageBitmap(modulePixels = 8)
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
```

Not checked yet: GS1 Company Prefix positions (`gcppos1`, `gcppos2`), coupon formats (AIs 8110, 8112) and AI pairing rules (`req`/`ex`).
