# GS1 Digital Link QR Code

A GS1 Digital Link QR Code holds a web address that also carries the GS1 data:

```
https://example.com/01/09501101020917/10/ABC123?17=261231
```

A phone camera opens it as a link, to whatever page the brand chooses, while GS1-aware scanners read the GTIN,
batch and expiry date from it.

```kotlin
val message = Gs1Message.parse("(01)09501101020917(10)ABC123(17)261231")
val symbol = Gs1QrCode.encode(message, domain = "https://example.com")

symbol.text           // https://example.com/01/09501101020917/10/ABC123?17=261231
symbol.version        // QR Code version (size): 1-40
symbol[row, column]   // true for a dark module
```

## How the URI is built

GS1 Digital Link puts the AIs in two places:

- **The path** identifies the item: a *primary key* such as the GTIN `(01)` or SSCC `(00)`, followed by its
  *key qualifiers* in a fixed order. For a GTIN those are the variant `(22)`, batch `(10)` and serial `(21)`.
- **The query string** carries everything else, the *attributes*: expiry date `(17)`, weight, price…

So `(17)` always appears as `?17=261231`, never as `/17/261231`: the standard does not allow attributes in the
path, and readers reject such URIs.

```kotlin
message.toDigitalLinkUri("https://example.com")   // just the URI, without a QR Code
```

The message needs a primary key, and every other AI must be one GS1 allows in a Digital Link URI. Otherwise
`Gs1Exception` reports `DIGITAL_LINK_NO_PRIMARY_KEY` or `DIGITAL_LINK_INVALID_ATTRIBUTE`.

### The domain

Use your own domain, set up to redirect to product pages, or GS1's resolver `https://id.gs1.org`, which is the
default. A path prefix is allowed: `https://example.com/products`.

!!! tip "Smaller symbols with an uppercase domain"
    `HTTPS://EXAMPLE.COM` is as valid as `https://example.com`, and in uppercase the whole address fits QR Code's
    compact alphanumeric mode. The symbol can be one or two versions smaller.

## Error correction

QR Code can recover from damage to part of the symbol. Higher levels make bigger symbols.

| Level | Recovers about | |
| --- | --- | --- |
| `QrErrorCorrection.L` | 7% | Smallest |
| `QrErrorCorrection.M` | 15% | Default |
| `QrErrorCorrection.Q` | 25% | |
| `QrErrorCorrection.H` | 30% | Harsh environments, or a logo over the symbol |

```kotlin
Gs1QrCode.encode(message, errorCorrection = QrErrorCorrection.Q)
Gs1QrCode.encode(message, version = 5)    // exactly version 5 (37 x 37 modules)
```

## Encoding an existing URI

If you already have the URI, `encodeUri` checks it and encodes it exactly as written:

```kotlin
Gs1QrCode.encodeUri("https://example.com/01/09501101020917?17=261231")
```

## Reading a Digital Link URI

```kotlin
val message = Gs1DigitalLink.parse("https://example.com/01/09501101020917/10/ABC123?17=261231")
message.humanReadable          // (01)09501101020917(10)ABC123(17)261231
message[Gs1Ai.EXPIRY_DATE]     // "261231"
```

`parse` also accepts the `]Q1` prefix some scanners add, ignores query parameters that are not AIs (such as
`utm_source`) and validates every value. For old URIs with a GTIN-8, -12 or -13, pass
`permitZeroSuppressedGtin = true`.

## Plain QR Codes

`QrCode.encode(text)` encodes any text, such as a URL that is not a GS1 Digital Link URI. Characters outside ASCII
are encoded as UTF-8.

Leave a quiet zone of at least **four light modules** around every QR Code (`QrCode.QUIET_ZONE_MODULES`).
