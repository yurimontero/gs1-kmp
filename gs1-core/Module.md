# Module gs1-core

GS1 data and barcode encoders for Kotlin Multiplatform, with no dependencies besides the Kotlin standard library.

Build a [io.github.yurimontero.gs1.Gs1Message], then encode it with
[io.github.yurimontero.gs1.code128.Gs1Code128], [io.github.yurimontero.gs1.datamatrix.Gs1DataMatrix] or
[io.github.yurimontero.gs1.qrcode.Gs1QrCode], and draw the result yourself or as SVG.

# Package io.github.yurimontero.gs1

GS1 element strings: Application Identifiers, validation, parsing and building messages, check digits and
GS1 Digital Link URIs.

# Package io.github.yurimontero.gs1.code128

GS1-128: Code 128 with FNC1, for logistics labels.

# Package io.github.yurimontero.gs1.datamatrix

GS1 DataMatrix: Data Matrix ECC 200 with FNC1, for small items, healthcare and direct part marking.

# Package io.github.yurimontero.gs1.qrcode

QR Code encoder and GS1 Digital Link QR Codes, which phones open as web addresses.

# Package io.github.yurimontero.gs1.render

SVG output for every symbol.
