# GS1 DataMatrix

GS1 DataMatrix is Data Matrix ECC 200 starting with FNC1. It holds a lot of data in a small square, survives
damage thanks to Reed-Solomon error correction, and is the standard for healthcare products and direct part
marking.

```kotlin
val message = Gs1Message.parse("(01)09501101020917(17)261231(10)ABC123")
val symbol = Gs1DataMatrix.encode(message)

symbol.rows; symbol.columns   // 20 x 20
symbol[row, column]           // true for a dark module; row 0 is the top
symbol.humanReadable          // (01)09501101020917(17)261231(10)ABC123
```

## Size and shape

The encoder picks the smallest square symbol that fits, from 10×10 to 144×144 modules. For long, narrow print
areas, allow rectangular symbols (8×18 to 16×48):

```kotlin
Gs1DataMatrix.encode(message, shape = DataMatrixShape.RECTANGULAR)  // rectangles only
Gs1DataMatrix.encode(message, shape = DataMatrixShape.ANY)          // smallest of either shape
Gs1DataMatrix.encode(message, size = DataMatrixSize.of(24, 24))     // exactly 24 x 24
```

Data that does not fit throws `Gs1Exception` with `Gs1ErrorCode.DATA_MATRIX_TOO_LONG`.

`DataMatrixSize.all` lists every size with its data and error correction capacity.

## Quiet zone and human readable text

Leave at least **one light module** around the symbol (`Gs1DataMatrix.QUIET_ZONE_MODULES`). `toModules()`,
`toSvg()` and the composables add it for you.

GS1 asks for the human readable text near the symbol. The library gives you the text (`symbol.humanReadable`)
and leaves the layout to you, since it depends on the label.
