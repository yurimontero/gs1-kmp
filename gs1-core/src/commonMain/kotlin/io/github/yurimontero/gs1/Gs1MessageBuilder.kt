package io.github.yurimontero.gs1

/**
 * Builds a [Gs1Message] with named functions:
 *
 * ```
 * val message = Gs1Message.build {
 *     gtin("09501101020917")
 *     batch("ABC123")
 *     expiryDate(2026, 12, 31)
 *     netWeightKg("1.250")      // (3103)001250
 * }
 * ```
 *
 * Values are validated when the message is built, as with [Gs1Message.of].
 *
 * @throws Gs1Exception from [Gs1Message.Companion.build] if a value is invalid.
 */
public fun Gs1Message.Companion.build(
    order: Gs1Order = Gs1Order.OPTIMIZED,
    block: Gs1MessageBuilder.() -> Unit,
): Gs1Message = of(Gs1MessageBuilder().apply(block).elements, order)

/** Receiver of [Gs1Message.Companion.build]. */
public class Gs1MessageBuilder internal constructor() {
    internal val elements: MutableList<Pair<String, String>> = mutableListOf()

    /** Adds any AI with its value, e.g. `add(Gs1Ai.GTIN, "09501101020917")` or `add("7003", "2612311430")`. */
    public fun add(ai: String, value: String): Gs1MessageBuilder = apply { elements += ai to value }

    // ------------------------------------------------------------ identification

    /** (00) SSCC, 18 digits including the check digit. */
    public fun sscc(value: String): Gs1MessageBuilder = add(Gs1Ai.SSCC, value)

    /**
     * (01) GTIN. GTIN-8, GTIN-12 and GTIN-13 are padded to 14 digits with leading zeros.
     * The check digit must be included (see [Gs1CheckDigit.append]).
     */
    public fun gtin(value: String): Gs1MessageBuilder = add(Gs1Ai.GTIN, padGtin(value))

    /** (02) GTIN of the contained trade items (use with [count]). */
    public fun content(value: String): Gs1MessageBuilder = add(Gs1Ai.CONTENT, padGtin(value))

    /** (10) Batch or lot number. */
    public fun batch(value: String): Gs1MessageBuilder = add(Gs1Ai.BATCH_LOT, value)

    /** (21) Serial number. */
    public fun serial(value: String): Gs1MessageBuilder = add(Gs1Ai.SERIAL, value)

    // ------------------------------------------------------------ dates

    /** (11) Production date. [day] 0 means the last day of the month. */
    public fun productionDate(year: Int, month: Int, day: Int): Gs1MessageBuilder =
        add(Gs1Ai.PRODUCTION_DATE, yymmdd(year, month, day))

    /** (13) Packaging date. */
    public fun packagingDate(year: Int, month: Int, day: Int): Gs1MessageBuilder =
        add(Gs1Ai.PACKAGING_DATE, yymmdd(year, month, day))

    /** (15) Best before date. [day] 0 means the last day of the month. */
    public fun bestBeforeDate(year: Int, month: Int, day: Int): Gs1MessageBuilder =
        add(Gs1Ai.BEST_BEFORE_DATE, yymmdd(year, month, day))

    /** (16) Sell by date. */
    public fun sellByDate(year: Int, month: Int, day: Int): Gs1MessageBuilder =
        add(Gs1Ai.SELL_BY_DATE, yymmdd(year, month, day))

    /** (17) Expiration date. [day] 0 means the last day of the month. */
    public fun expiryDate(year: Int, month: Int, day: Int): Gs1MessageBuilder =
        add(Gs1Ai.EXPIRY_DATE, yymmdd(year, month, day))

    /** (7003) Expiration date and time. */
    public fun expiryDateTime(year: Int, month: Int, day: Int, hour: Int, minute: Int): Gs1MessageBuilder =
        add(Gs1Ai.EXPIRY_DATE_TIME, yymmdd(year, month, day) + two(hour) + two(minute))

    // ------------------------------------------------------------ quantities

    /** (30) Variable count. */
    public fun variableCount(count: Int): Gs1MessageBuilder = add(Gs1Ai.VARIABLE_COUNT, nonNegative(count))

    /** (37) Count of trade items contained in a logistic unit. */
    public fun count(count: Int): Gs1MessageBuilder = add(Gs1Ai.COUNT, nonNegative(count))

    /**
     * (310n) Net weight in kilograms, as a decimal string: the decimals written choose the AI,
     * so `"1.250"` gives `(3103)001250` and `"12.5"` gives `(3101)000125`.
     */
    public fun netWeightKg(kilograms: String): Gs1MessageBuilder = measure("310", kilograms)

    /** (310n) Net weight in kilograms rounded to [decimals] decimal places. */
    public fun netWeightKg(kilograms: Double, decimals: Int): Gs1MessageBuilder = measure("310", kilograms, decimals)

    /** (330n) Logistic gross weight in kilograms, as a decimal string (see [netWeightKg]). */
    public fun grossWeightKg(kilograms: String): Gs1MessageBuilder = measure("330", kilograms)

    /** (330n) Logistic gross weight in kilograms rounded to [decimals] decimal places. */
    public fun grossWeightKg(kilograms: Double, decimals: Int): Gs1MessageBuilder = measure("330", kilograms, decimals)

    /** (315n) Net volume in litres, as a decimal string (see [netWeightKg]). */
    public fun netVolumeL(litres: String): Gs1MessageBuilder = measure("315", litres)

    /** (315n) Net volume in litres rounded to [decimals] decimal places. */
    public fun netVolumeL(litres: Double, decimals: Int): Gs1MessageBuilder = measure("315", litres, decimals)

    /** (311n) Length in metres, as a decimal string (see [netWeightKg]). */
    public fun lengthM(metres: String): Gs1MessageBuilder = measure("311", metres)

    /** (312n) Width in metres, as a decimal string. */
    public fun widthM(metres: String): Gs1MessageBuilder = measure("312", metres)

    /** (313n) Height in metres, as a decimal string. */
    public fun heightM(metres: String): Gs1MessageBuilder = measure("313", metres)

    /**
     * (392n / 393n) Price of a variable measure trade item, as a decimal string, e.g. `"4.95"`.
     * With an ISO 4217 numeric [currency] (e.g. `"978"` for euro) AI 393n is used.
     */
    public fun price(amount: String, currency: String? = null): Gs1MessageBuilder =
        money(if (currency == null) "392" else "393", amount, currency)

    /** (390n / 391n) Amount payable, as a decimal string, optionally with an ISO 4217 numeric [currency]. */
    public fun amountPayable(amount: String, currency: String? = null): Gs1MessageBuilder =
        money(if (currency == null) "390" else "391", amount, currency)

    // ------------------------------------------------------------ logistics

    /** (400) Customer's purchase order number. */
    public fun orderNumber(value: String): Gs1MessageBuilder = add(Gs1Ai.ORDER_NUMBER, value)

    /** (410) Ship to GLN, 13 digits. */
    public fun shipToGln(gln: String): Gs1MessageBuilder = add(Gs1Ai.SHIP_TO_GLN, gln)

    /** (414) Location GLN, 13 digits. */
    public fun locationGln(gln: String): Gs1MessageBuilder = add(Gs1Ai.LOCATION_GLN, gln)

    /** (422) Country of origin, ISO 3166 numeric code (e.g. 724 for Spain). */
    public fun originCountry(isoNumeric: Int): Gs1MessageBuilder =
        add(Gs1Ai.ORIGIN_COUNTRY, isoNumeric.toString().padStart(3, '0'))

    // ------------------------------------------------------------ helpers

    private fun padGtin(value: String): String = if (value.length in 8..13 && value.all { it.isAsciiDigit() }) value.padStart(14, '0') else value

    private fun two(value: Int): String = value.toString().padStart(2, '0')

    private fun yymmdd(year: Int, month: Int, day: Int): String {
        require(month in 1..12) { "Month must be 1-12, was $month" }
        require(day in 0..31) { "Day must be 0-31, was $day" }
        return two(((year % 100) + 100) % 100) + two(month) + two(day)
    }

    private fun nonNegative(count: Int): String {
        require(count >= 0) { "Count must not be negative, was $count" }
        return count.toString()
    }

    private fun measure(prefix: String, decimal: String): Gs1MessageBuilder {
        val (digits, decimals) = splitDecimal(prefix, decimal, maxDecimals = 5)
        if (digits.length > 6) {
            throw Gs1Exception(Gs1Error(Gs1ErrorCode.VALUE_TOO_LONG, prefix + decimals, decimal, 0, decimal.length))
        }
        return add(prefix + decimals, digits.padStart(6, '0'))
    }

    private fun measure(prefix: String, value: Double, decimals: Int): Gs1MessageBuilder =
        measure(prefix, formatDecimal(value, decimals))

    private fun money(prefix: String, decimal: String, currency: String?): Gs1MessageBuilder {
        val (digits, decimals) = splitDecimal(prefix, decimal, maxDecimals = 9)
        return add(prefix + decimals, (currency ?: "") + digits)
    }

    /** "1.250" -> ("1250", 3); "12" -> ("12", 0). */
    private fun splitDecimal(prefix: String, decimal: String, maxDecimals: Int): Pair<String, Int> {
        val parts = decimal.split('.')
        val valid = parts.size in 1..2 && parts.all { part -> part.isNotEmpty() && part.all { it.isAsciiDigit() } }
        if (!valid) throw Gs1Exception(Gs1Error(Gs1ErrorCode.MALFORMED_INPUT, prefix + "n", decimal, 0, decimal.length))
        val decimals = if (parts.size == 2) parts[1].length else 0
        require(decimals <= maxDecimals) { "At most $maxDecimals decimals allowed for AI ${prefix}n, got \"$decimal\"" }
        return parts.joinToString("") to decimals
    }

    private fun formatDecimal(value: Double, decimals: Int): String {
        require(decimals in 0..5) { "Decimals must be 0-5, was $decimals" }
        require(value >= 0 && !value.isNaN() && !value.isInfinite()) { "Value must be a non-negative number, was $value" }
        var scale = 1L
        repeat(decimals) { scale *= 10 }
        val scaled = kotlin.math.round(value * scale).toLong()
        val integer = (scaled / scale).toString()
        if (decimals == 0) return integer
        return integer + "." + (scaled % scale).toString().padStart(decimals, '0')
    }
}
