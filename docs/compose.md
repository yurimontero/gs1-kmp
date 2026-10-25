# Compose

`gs1-compose` draws the symbols in Compose Multiplatform, on every platform the library supports. It includes
`gs1-core`, so one dependency is enough.

```kotlin
implementation("io.github.yurimontero:gs1-compose:0.1.0")
```

## Composables

```kotlin
val message = Gs1Message.parse("(01)09501101020917(17)261231(10)ABC123")

Gs1DataMatrixBarcode(message)
Gs1Code128Barcode(message)                                   // bars and human readable text
Gs1QrCodeBarcode(message, domain = "https://example.com")
```

Sizing works in two ways:

- **Module size**: without a size from the modifier, each module is `moduleSize` (`moduleWidth` for GS1-128),
  rounded to whole pixels: `Gs1DataMatrixBarcode(message, moduleSize = 6.dp)`.
- **Fixed size**: give the modifier a size and the symbol is drawn as large as fits, centred:
  `Gs1QrCodeBarcode(message, modifier = Modifier.size(200.dp))`.

Either way, every module is a whole number of pixels, so edges stay sharp and scanners read the symbol
reliably.

The composables that take a `Gs1Message` encode it inside `remember`. Encoding throws `Gs1Exception` for data that
does not fit or (for QR Codes) cannot form a Digital Link URI. To handle errors yourself, encode first and pass the
symbol:

```kotlin
val symbol = remember(message) {
    runCatching { Gs1DataMatrix.encode(message) }.getOrNull()
}
if (symbol != null) Gs1DataMatrixBarcode(symbol) else Text("Data too long")
```

### Options

| Parameter | Default | |
| --- | --- | --- |
| `quietZone` | 1 (DataMatrix), 10 (GS1-128), 4 (QR Code) | Light modules around the symbol |
| `darkColor`, `lightColor` | Black, white | `Color.Unspecified` as `lightColor` gives a transparent background |
| `barHeight` | `64.dp` | GS1-128 bar height when the modifier does not fix the height |
| `textStyle` | Monospace, 12 sp | GS1-128 human readable text; `null` hides it |

## Painters

To use a symbol with `Image` or anywhere a `Painter` fits:

```kotlin
Image(
    painter = DataMatrixPainter(symbol),
    contentDescription = "GS1 DataMatrix",
    modifier = Modifier.size(160.dp),
)
```

`QrCodePainter` and `Code128Painter` work the same way. `Code128Painter` draws only the bars.

## Bitmaps

To share or print an image, render it with an exact number of pixels per module:

```kotlin
val bitmap: ImageBitmap = symbol.toImageBitmap(modulePixels = 8)
```

For printers, use a whole number of printer dots per module: at 203 dpi, 4 dots make a 0.5 mm module.
