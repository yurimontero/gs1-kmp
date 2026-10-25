# Getting started

## Add the dependency

The library is on [Maven Central](https://central.sonatype.com/namespace/io.github.yurimontero), which Gradle
projects use by default. The latest version is listed in the
[changelog](https://github.com/yurimontero/gs1-kmp/blob/main/CHANGELOG.md).

In a Kotlin Multiplatform project, add the modules to `commonMain`:

```kotlin title="build.gradle.kts"
kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation("io.github.yurimontero:gs1-core:0.1.0")
            // Optional: composables for Compose Multiplatform (includes gs1-core)
            implementation("io.github.yurimontero:gs1-compose:0.1.0")
        }
    }
}
```

On a single platform (an Android app, a desktop app), add them to `dependencies { }` the same way.

## Your first barcode

```kotlin
import io.github.yurimontero.gs1.Gs1Message
import io.github.yurimontero.gs1.datamatrix.Gs1DataMatrix
import io.github.yurimontero.gs1.render.toSvg

// 1. Describe the data. Invalid data throws Gs1Exception explaining what is wrong.
val message = Gs1Message.parse("(01)09501101020917(17)261231(10)ABC123")

// 2. Encode it.
val symbol = Gs1DataMatrix.encode(message)
println("${symbol.rows} x ${symbol.columns}")    // 20 x 20

// 3. Draw it: an SVG, your own canvas (symbol[row, column] is true for dark modules),
//    or a composable from gs1-compose.
val svg = symbol.toSvg(moduleSize = 0.5, unit = "mm")
```

With Compose:

```kotlin
@Composable
fun ProductLabel(message: Gs1Message) {
    Gs1DataMatrixBarcode(message, moduleSize = 4.dp)
}
```

## Choosing a barcode

| Barcode | Use it for | Notes |
| --- | --- | --- |
| [GS1 DataMatrix](barcodes/datamatrix.md) | Healthcare, small items, direct part marking | Compact, square or rectangular |
| [GS1-128](barcodes/gs1-128.md) | Logistics labels (SSCC, cartons, pallets) | Linear, up to 48 data characters |
| [GS1 Digital Link QR Code](barcodes/digital-link-qr.md) | Consumer products, anything a phone should open | Carries a web address with the GS1 data |

Next: learn how to [build and validate GS1 data](gs1-data.md).
