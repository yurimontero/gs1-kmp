# GS1 data

Every barcode starts from a `Gs1Message`: a list of **element strings**, each an
**Application Identifier** (AI) and its value. `(01)` is the GTIN, `(10)` the batch or lot, `(17)` the expiry
date, and so on.

A `Gs1Message` is always valid: building one checks every value, so an encoder never receives bad data.

## Building a message

There are three ways, and they give the same result.

=== "Builder"

    ```kotlin
    val message = Gs1Message.build {
        gtin("09501101020917")
        expiryDate(2026, 12, 31)    // (17)261231
        batch("ABC123")
        netWeightKg("1.250")        // (3103)001250: the AI encodes the decimals
    }
    ```

=== "AI constants"

    ```kotlin
    val message = Gs1Message.of(
        Gs1Ai.GTIN to "09501101020917",
        Gs1Ai.EXPIRY_DATE to "261231",
        Gs1Ai.BATCH_LOT to "ABC123",
        Gs1Ai.netWeightKg(3) to "001250",
    )
    ```

=== "Bracketed text"

    ```kotlin
    val message = Gs1Message.parse("(01)09501101020917(17)261231(10)ABC123(3103)001250")
    ```

The builder knows the common AIs: `gtin`, `sscc`, `content`, `batch`, `serial`, dates (`productionDate`,
`bestBeforeDate`, `expiryDate`…), counts, weights and dimensions, `price`, `amountPayable`, `orderNumber`, GLNs
and `originCountry`. For anything else use `add(ai, value)`:

```kotlin
Gs1Message.build {
    gtin("4006381333931")            // GTIN-13: padded to 14 digits for you
    add(Gs1Ai.companyInternal(1), "BIN-42")   // (91)BIN-42
}
```

### AI constants

`Gs1Ai` has a constant for every AI, named after its meaning. AIs whose last digit is a parameter (decimal
places, a sequence number) are functions:

```kotlin
Gs1Ai.GTIN                   // "01"
Gs1Ai.BATCH_LOT              // "10"
Gs1Ai.EXPIRY_DATE            // "17"
Gs1Ai.netWeightKg(3)         // "3103": net weight in kg with 3 decimals
Gs1Ai.companyInternal(1)     // "91"
```

To look up an AI's format and title, use `ApplicationIdentifier`:

```kotlin
val ai = ApplicationIdentifier.find("17")!!
ai.title                     // "USE BY or EXPIRY"
ai.isPredefinedLength        // true
ApplicationIdentifier.all    // every AI
```

## Validation

Building a message throws `Gs1Exception` with a `Gs1Error` that says what failed and where:

```kotlin
try {
    Gs1Message.parse("(01)09501101020910")
} catch (e: Gs1Exception) {
    e.error.code      // Gs1ErrorCode.INCORRECT_CHECK_DIGIT
    e.error.ai        // "01"
    e.error.position  // 13: offset of the wrong digit within the value
    e.message         // AI (01): incorrect check digit at "0" (position 13)
}
```

Values are checked against the GS1 Barcode Syntax Dictionary and the GS1 Barcode Syntax Tests: lengths,
character sets, check digits, dates and times, ISO country and currency codes, IBANs and more. To check a single
value without building a message, call `ApplicationIdentifier.validate(value)`, which returns the error or
`null`.

Not checked yet: GS1 Company Prefix positions, coupon formats (AIs 8110 and 8112) and the rules about which AIs
must or must not appear together.

### Check digits

```kotlin
Gs1CheckDigit.compute("950110102091")    // 7
Gs1CheckDigit.append("0950110102091")    // "09501101020917"
Gs1CheckDigit.isValid("09501101020917")  // true
```

## Element order

By default, `of`, `parse` and `build` put the predefined-length AIs (GTIN, dates…) first. Those need no
separator after them, so the symbol is smaller. Pass `Gs1Order.AS_GIVEN` to keep your order:

```kotlin
Gs1Message.parse("(10)ABC123(01)09501101020917").humanReadable
// (01)09501101020917(10)ABC123

Gs1Message.parse("(10)ABC123(01)09501101020917", Gs1Order.AS_GIVEN).humanReadable
// (10)ABC123(01)09501101020917
```

## Reading scanned data

Barcode readers return GS1 data as an element string: a symbology identifier such as `]d2` (DataMatrix),
`]C1` (GS1-128) or `]Q3` (QR Code with FNC1), the AIs and values, and the ASCII group separator (GS, character
29) after variable-length values. Parse it back into a message:

```kotlin
val scanned = Gs1Message.parseElementString("]d2010950110102091717261231" + "10ABC123")
scanned[Gs1Ai.BATCH_LOT]       // "ABC123"
scanned.humanReadable           // (01)09501101020917(17)261231(10)ABC123
```

A GS1 Digital Link QR Code gives a URI instead; see
[parsing Digital Link URIs](barcodes/digital-link-qr.md#reading-a-digital-link-uri).

The reverse, `message.toElementString()`, produces the same string a reader would return, which is useful in
tests.
