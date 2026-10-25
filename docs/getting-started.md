# Getting started

## Add the dependency

!!! note "Not published yet"
    The library is not on Maven Central yet. Until it is, clone the repository next to your project and include
    it as a composite build; Gradle then uses it for the coordinates below:

    ```kotlin title="settings.gradle.kts"
    includeBuild("../gs1-kmp")
    ```

In a Kotlin Multiplatform project, add the modules to `commonMain`:

```kotlin title="build.gradle.kts"
kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation("io.github.yurimontero:gs1-core:<version>")
            // Optional: composables for Compose Multiplatform (includes gs1-core)
            implementation("io.github.yurimontero:gs1-compose:<version>")
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
