# SVG and printing

## SVG

Every symbol renders to a standalone SVG document:

```kotlin
import io.github.yurimontero.gs1.render.toSvg

dataMatrix.toSvg()                                   // 10 px per module
qrCode.toSvg(moduleSize = 0.4, unit = "mm")          // exact physical size
gs1128.toSvg(moduleSize = 0.495, unit = "mm")        // bars and human readable text
```

Coordinates inside the SVG are in modules. `moduleSize` and `unit` only set the `width` and `height`
attributes, so the same SVG scales cleanly. Quiet zones are included.

| Option | | |
| --- | --- | --- |
| `quietZone` | Light modules around the symbol | Defaults to the minimum for the symbology |
| `darkColor` | Bar colour | `#000000` |
| `lightColor` | Background, or `null` for transparent | `#ffffff` |
| `barHeight` | GS1-128 bar height in modules | 60 |
| `humanReadable`, `fontSize`, `fontFamily` | GS1-128 text under the bars | Shown, 8 modules, monospace |

## Drawing it yourself

Every symbol exposes its modules, so you can draw it with any graphics API:

```kotlin
val modules: Array<BooleanArray> = dataMatrix.toModules()   // rows, quiet zone included
val bars: BooleanArray = gs1128.toModules()                 // one entry per module, left to right
```

## Printing tips

- **X-dimension**: the size of one module. Choose it from the symbol specification tables in the
  [GS1 General Specifications](https://www.gs1.org/standards/barcodes-epcrfid-id-keys/gs1-general-specifications)
  for your application. Logistics labels, for example, print GS1-128 with an X-dimension between 0.495 mm and
  1.016 mm.
- **Whole printer dots**: make each module a whole number of printer dots (at 300 dpi, a dot is 0.0847 mm).
  Rounded sizes print sharper than exact ones.
- **Quiet zones**: keep them free of text and graphics: 10 modules for GS1-128, 1 for DataMatrix, 4 for QR Code.
- **Contrast**: dark bars on a light background. Inverted colours are not reliably read.
- **Check the size**: `widthMm` (GS1-128), `widthMm`/`heightMm` (DataMatrix) and `sizeMm` (QR Code) give the
  printed size for an X-dimension, quiet zones included.
