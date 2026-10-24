// Kotlin port of the GS1 Barcode Syntax Tests ("linters") from the GS1 Syntax Engine
// (https://github.com/gs1/gs1-syntax-engine, src/c-lib/syntax).
// Copyright (c) 2022-2025 GS1 AISBL. Licensed under the Apache License, Version 2.0.
//
// Each linter returns null when the data is valid, or the failure with the position and
// length of the offending characters, exactly as the reference implementation does.

package io.github.yurimontero.gs1

internal class LintFailure(val code: Gs1ErrorCode, val position: Int, val length: Int)

internal typealias Linter = (String) -> LintFailure?

internal object Linters {

    /**
     * Linters named in the GS1 Syntax Dictionary that gs1-core does not implement:
     * - `gcppos1`, `gcppos2`: need the GS1 Company Prefix allocation table (an online data source).
     * - `couponcode`, `couponposoffer`: North American coupon formats (AIs 8110 and 8112).
     * Data for those components is still checked for length and character set.
     */
    val notImplemented: Set<String> = setOf("gcppos1", "gcppos2", "couponcode", "couponposoffer")

    /** Returns the linter for [name], or null when it is unknown or not implemented. */
    fun forName(name: String): Linter? = when (name) {
        "csetnumeric" -> ::csetNumeric
        "cset82" -> ::cset82
        "cset39" -> ::cset39
        "cset64" -> ::cset64
        "csum" -> ::csum
        "csumalpha" -> ::csumAlpha
        "yymmd0" -> ::yymmd0
        "yymmdd" -> ::yymmdd
        "yyyymmd0" -> ::yyyymmd0
        "yyyymmdd" -> ::yyyymmdd
        "hh" -> ::hh
        "mi" -> ::mi
        "ss" -> ::ss
        "hhmi" -> ::hhmi
        "nonzero" -> ::nonZero
        "zero" -> ::zero
        "yesno" -> ::yesNo
        "hyphen" -> ::hyphen
        "winding" -> ::winding
        "pieceoftotal" -> ::pieceOfTotal
        "nozeroprefix" -> ::noZeroPrefix
        "hasnondigit" -> ::hasNonDigit
        "iso5218" -> ::iso5218
        "latitude" -> ::latitude
        "longitude" -> ::longitude
        "posinseqslash" -> ::posInSeqSlash
        "pcenc" -> ::pcenc
        "iso3166" -> ::iso3166
        "iso3166999" -> ::iso3166999
        "iso3166alpha2" -> ::iso3166alpha2
        "iso4217" -> ::iso4217
        "iban" -> ::iban
        "mediatype" -> ::mediaType
        "importeridx" -> ::importerIdx
        "packagetype" -> ::packageType
        else -> null
    }

    // ---------------------------------------------------------------- character sets

    const val CSET82: String = "!\"%&'()*+,-./0123456789:;<=>?ABCDEFGHIJKLMNOPQRSTUVWXYZ_abcdefghijklmnopqrstuvwxyz"
    private const val CSET39 = "#-/0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ"
    private const val CSET64 = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-_"
    private const val CSET32 = "23456789ABCDEFGHJKLMNPQRSTUVWXYZ"

    fun csetNumeric(d: String): LintFailure? {
        for (i in d.indices) if (!d[i].isAsciiDigit()) return fail(Gs1ErrorCode.NON_DIGIT_CHARACTER, i, 1)
        return null
    }

    fun cset82(d: String): LintFailure? {
        for (i in d.indices) if (CSET82.indexOf(d[i]) < 0) return fail(Gs1ErrorCode.INVALID_CSET82_CHARACTER, i, 1)
        return null
    }

    fun cset39(d: String): LintFailure? {
        for (i in d.indices) if (CSET39.indexOf(d[i]) < 0) return fail(Gs1ErrorCode.INVALID_CSET39_CHARACTER, i, 1)
        return null
    }

    fun cset64(d: String): LintFailure? {
        var pads = 0
        while (pads < d.length && d[d.length - pads - 1] == '=') pads++
        val len = d.length - pads
        if (pads > 2 || (pads > 0 && d.length % 3 != 0)) return fail(Gs1ErrorCode.INVALID_CSET64_PADDING, len, pads)
        for (i in 0 until len) if (CSET64.indexOf(d[i]) < 0) return fail(Gs1ErrorCode.INVALID_CSET64_CHARACTER, i, 1)
        return null
    }

    // ---------------------------------------------------------------- check digits

    fun csum(d: String): LintFailure? {
        if (d.isEmpty()) return fail(Gs1ErrorCode.TOO_SHORT_FOR_CHECK_DIGIT, 0, 0)
        var weight = if (d.length % 2 == 0) 3 else 1
        var parity = 0
        for (i in 0 until d.length - 1) {
            if (!d[i].isAsciiDigit()) return fail(Gs1ErrorCode.NON_DIGIT_CHARACTER, i, 1)
            parity += weight * (d[i] - '0')
            weight = 4 - weight
        }
        val last = d.length - 1
        if (!d[last].isAsciiDigit()) return fail(Gs1ErrorCode.NON_DIGIT_CHARACTER, last, 1)
        parity = (10 - parity % 10) % 10
        if (d[last] - '0' != parity) return fail(Gs1ErrorCode.INCORRECT_CHECK_DIGIT, last, 1)
        return null
    }

    private val PRIMES = intArrayOf(
        2, 3, 5, 7, 11, 13, 17, 19, 23, 29, 31, 37, 41, 43, 47, 53, 59, 61, 67, 71, 73, 79, 83, 89,
        97, 101, 103, 107, 109, 113, 127, 131, 137, 139, 149, 151, 157, 163, 167, 173, 179, 181, 191, 193,
        197, 199, 211, 223, 227, 229, 233, 239, 241, 251, 257, 263, 269, 271, 277, 281, 283, 293, 307, 311,
        313, 317, 331, 337, 347, 349, 353, 359, 367, 373, 379, 383, 389, 397, 401, 409, 419, 421, 431, 433,
        439, 443, 449, 457, 461, 463, 467, 479, 487, 491, 499, 503, 509,
    )

    /** GS1 check character pair (e.g. GMN). */
    fun csumAlpha(d: String): LintFailure? {
        val n = d.length
        if (n > PRIMES.size + 2) return fail(Gs1ErrorCode.TOO_LONG_FOR_CHECK_PAIR_IMPLEMENTATION, 0, n)
        if (n < 2) return fail(Gs1ErrorCode.TOO_SHORT_FOR_CHECK_PAIR, 0, n)
        if (n == 2) {
            return if (d[0] != '2' || d[1] != '2') fail(Gs1ErrorCode.INCORRECT_CHECK_PAIR, 0, 2) else null
        }
        var sum = 0
        for (i in 0 until n - 2) {
            val value = CSET82.indexOf(d[i])
            if (value < 0) return fail(Gs1ErrorCode.INVALID_CSET82_CHARACTER, i, 1)
            sum += value * PRIMES[n - 3 - i]
        }
        sum %= 1021
        if (d[n - 2] != CSET32[sum shr 5] || d[n - 1] != CSET32[sum and 31]) {
            return fail(Gs1ErrorCode.INCORRECT_CHECK_PAIR, n - 2, 2)
        }
        return null
    }

    // ---------------------------------------------------------------- dates and times

    /**
     * Two-digit years are expanded to 19YY/20YY/21YY around this reference year (as 20YY),
     * which only matters for 29 February in years ending in 00. Same approach as GS1's reference code.
     */
    private const val REFERENCE_YEAR = 26

    private val DAYS_IN_MONTH = intArrayOf(31, 0, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31)

    private fun two(d: String, i: Int): Int = (d[i] - '0') * 10 + (d[i + 1] - '0')

    fun yyyymmd0(d: String): LintFailure? {
        if (d.length != 8) {
            return fail(if (d.length < 8) Gs1ErrorCode.DATE_TOO_SHORT else Gs1ErrorCode.DATE_TOO_LONG, 0, d.length)
        }
        for (i in 0 until 8) if (!d[i].isAsciiDigit()) return fail(Gs1ErrorCode.NON_DIGIT_CHARACTER, i, 1)
        val year = two(d, 0) * 100 + two(d, 2)
        val month = two(d, 4)
        val day = two(d, 6)
        if (month !in 1..12) return fail(Gs1ErrorCode.ILLEGAL_MONTH, 4, 2)
        val maxDay = if (month == 2) {
            if ((year % 4 == 0 && year % 100 != 0) || year % 400 == 0) 29 else 28
        } else {
            DAYS_IN_MONTH[month - 1]
        }
        if (day > maxDay) return fail(Gs1ErrorCode.ILLEGAL_DAY, 6, 2)
        return null
    }

    fun yyyymmdd(d: String): LintFailure? {
        yyyymmd0(d)?.let { return it }
        if (d[6] == '0' && d[7] == '0') return fail(Gs1ErrorCode.ILLEGAL_DAY, 6, 2)
        return null
    }

    fun yymmd0(d: String): LintFailure? {
        if (d.length != 6) {
            return fail(if (d.length < 6) Gs1ErrorCode.DATE_TOO_SHORT else Gs1ErrorCode.DATE_TOO_LONG, 0, d.length)
        }
        for (i in 0 until 6) if (!d[i].isAsciiDigit()) return fail(Gs1ErrorCode.NON_DIGIT_CHARACTER, i, 1)
        val yy = two(d, 0)
        val century = when {
            yy - REFERENCE_YEAR >= 51 -> "19"
            yy - REFERENCE_YEAR > -50 -> "20"
            else -> "21"
        }
        val failure = yyyymmd0(century + d) ?: return null
        return fail(failure.code, failure.position - 2, failure.length)
    }

    fun yymmdd(d: String): LintFailure? {
        yymmd0(d)?.let { return it }
        if (d[4] == '0' && d[5] == '0') return fail(Gs1ErrorCode.ILLEGAL_DAY, 4, 2)
        return null
    }

    private fun twoDigitsAtMost(
        d: String, max: Int, tooShort: Gs1ErrorCode, tooLong: Gs1ErrorCode, illegal: Gs1ErrorCode,
    ): LintFailure? {
        if (d.length != 2) return fail(if (d.length < 2) tooShort else tooLong, 0, d.length)
        for (i in 0 until 2) if (!d[i].isAsciiDigit()) return fail(Gs1ErrorCode.NON_DIGIT_CHARACTER, i, 1)
        if (two(d, 0) > max) return fail(illegal, 0, 2)
        return null
    }

    fun hh(d: String): LintFailure? =
        twoDigitsAtMost(d, 23, Gs1ErrorCode.HOUR_TOO_SHORT, Gs1ErrorCode.HOUR_TOO_LONG, Gs1ErrorCode.ILLEGAL_HOUR)

    fun mi(d: String): LintFailure? =
        twoDigitsAtMost(d, 59, Gs1ErrorCode.MINUTE_TOO_SHORT, Gs1ErrorCode.MINUTE_TOO_LONG, Gs1ErrorCode.ILLEGAL_MINUTE)

    fun ss(d: String): LintFailure? =
        twoDigitsAtMost(d, 59, Gs1ErrorCode.SECOND_TOO_SHORT, Gs1ErrorCode.SECOND_TOO_LONG, Gs1ErrorCode.ILLEGAL_SECOND)

    fun hhmi(d: String): LintFailure? {
        if (d.length != 4) {
            return fail(
                if (d.length < 4) Gs1ErrorCode.HOUR_WITH_MINUTE_TOO_SHORT else Gs1ErrorCode.HOUR_WITH_MINUTE_TOO_LONG,
                0, d.length,
            )
        }
        hh(d.substring(0, 2))?.let { return it }
        mi(d.substring(2, 4))?.let { return fail(it.code, it.position + 2, it.length) }
        return null
    }

    // ---------------------------------------------------------------- simple value rules

    fun nonZero(d: String): LintFailure? {
        var hasNonZero = false
        for (i in d.indices) {
            if (!d[i].isAsciiDigit()) return fail(Gs1ErrorCode.NON_DIGIT_CHARACTER, i, 1)
            if (d[i] != '0') hasNonZero = true
        }
        return if (!hasNonZero) fail(Gs1ErrorCode.ILLEGAL_ZERO_VALUE, 0, d.length) else null
    }

    fun zero(d: String): LintFailure? {
        if (d.isEmpty()) return fail(Gs1ErrorCode.NOT_ZERO, 0, 0)
        return if (d.any { it != '0' }) fail(Gs1ErrorCode.NOT_ZERO, 0, d.length) else null
    }

    fun yesNo(d: String): LintFailure? {
        if (d.length != 1) return fail(Gs1ErrorCode.NOT_ZERO_OR_ONE, 0, d.length)
        return if (d[0] != '0' && d[0] != '1') fail(Gs1ErrorCode.NOT_ZERO_OR_ONE, 0, 1) else null
    }

    fun hyphen(d: String): LintFailure? {
        if (d.isEmpty()) return fail(Gs1ErrorCode.NOT_HYPHEN, 0, 0)
        return if (d.any { it != '-' }) fail(Gs1ErrorCode.NOT_HYPHEN, 0, d.length) else null
    }

    fun winding(d: String): LintFailure? {
        if (d.length != 1) return fail(Gs1ErrorCode.INVALID_WINDING_DIRECTION, 0, d.length)
        return if (d[0] != '0' && d[0] != '1' && d[0] != '9') fail(Gs1ErrorCode.INVALID_WINDING_DIRECTION, 0, 1) else null
    }

    fun pieceOfTotal(d: String): LintFailure? {
        for (i in d.indices) if (!d[i].isAsciiDigit()) return fail(Gs1ErrorCode.NON_DIGIT_CHARACTER, i, 1)
        if (d.isEmpty() || d.length % 2 != 0) return fail(Gs1ErrorCode.INVALID_LENGTH_FOR_PIECE_OF_TOTAL, 0, d.length)
        val half = d.length / 2
        var pieceIsZero = true
        var totalIsZero = true
        var compare = 0
        for (i in 0 until half) {
            val p = d[i]
            val t = d[half + i]
            if (pieceIsZero && p != '0') pieceIsZero = false
            if (totalIsZero && t != '0') totalIsZero = false
            if (compare == 0 && p != t) compare = if (p < t) -1 else 1
        }
        if (pieceIsZero) return fail(Gs1ErrorCode.ZERO_PIECE_NUMBER, 0, half)
        if (totalIsZero) return fail(Gs1ErrorCode.ZERO_TOTAL_PIECES, half, half)
        if (compare == 1) return fail(Gs1ErrorCode.PIECE_NUMBER_EXCEEDS_TOTAL, 0, d.length)
        return null
    }

    fun noZeroPrefix(d: String): LintFailure? {
        if (d.isNotEmpty() && d[0] == '0') return fail(Gs1ErrorCode.ILLEGAL_ZERO_PREFIX, 0, 1)
        for (i in d.indices) if (!d[i].isAsciiDigit()) return fail(Gs1ErrorCode.NON_DIGIT_CHARACTER, i, 1)
        return null
    }

    fun hasNonDigit(d: String): LintFailure? =
        if (d.any { !it.isAsciiDigit() }) null else fail(Gs1ErrorCode.REQUIRES_NON_DIGIT_CHARACTER, 0, d.length)

    fun iso5218(d: String): LintFailure? {
        if (d.length != 1) return fail(Gs1ErrorCode.INVALID_BIOLOGICAL_SEX_CODE, 0, d.length)
        return if (d[0] !in "0129") fail(Gs1ErrorCode.INVALID_BIOLOGICAL_SEX_CODE, 0, 1) else null
    }

    private fun coordinate(d: String, max: Long, invalidLength: Gs1ErrorCode, invalid: Gs1ErrorCode): LintFailure? {
        if (d.length != 10) return fail(invalidLength, 0, d.length)
        var value = 0L
        for (i in 0 until 10) {
            if (!d[i].isAsciiDigit()) return fail(Gs1ErrorCode.NON_DIGIT_CHARACTER, i, 1)
            value = value * 10 + (d[i] - '0')
        }
        return if (value > max) fail(invalid, 0, 10) else null
    }

    fun latitude(d: String): LintFailure? =
        coordinate(d, 1_800_000_000L, Gs1ErrorCode.LATITUDE_INVALID_LENGTH, Gs1ErrorCode.INVALID_LATITUDE)

    fun longitude(d: String): LintFailure? =
        coordinate(d, 3_600_000_000L, Gs1ErrorCode.LONGITUDE_INVALID_LENGTH, Gs1ErrorCode.INVALID_LONGITUDE)

    /** "position/end", e.g. "1/3". */
    fun posInSeqSlash(d: String): LintFailure? {
        val n = d.length
        var pos = 0
        while (pos < n && d[pos].isAsciiDigit()) pos++
        if (pos == 0 || pos >= n || d[pos] != '/') return fail(Gs1ErrorCode.POSITION_IN_SEQUENCE_MALFORMED, 0, n)
        val slash = pos
        for (i in slash + 1 until n) {
            if (!d[i].isAsciiDigit()) return fail(Gs1ErrorCode.POSITION_IN_SEQUENCE_MALFORMED, 0, n)
        }
        if (slash >= n - 1) return fail(Gs1ErrorCode.POSITION_IN_SEQUENCE_MALFORMED, 0, n)
        if (d[0] == '0') return fail(Gs1ErrorCode.ILLEGAL_ZERO_PREFIX, 0, slash)
        if (d[slash + 1] == '0') return fail(Gs1ErrorCode.ILLEGAL_ZERO_PREFIX, slash + 1, n - slash - 1)
        val endLength = n - slash - 1
        if (slash == endLength) {
            for (i in 0 until slash) {
                val p = d[i]
                val e = d[slash + 1 + i]
                if (p != e) {
                    if (p > e) return fail(Gs1ErrorCode.POSITION_EXCEEDS_END, 0, n)
                    break
                }
            }
        } else if (slash > endLength) {
            return fail(Gs1ErrorCode.POSITION_EXCEEDS_END, 0, n)
        }
        return null
    }

    /** Percent-encoded text: every '%' must be followed by two hex digits. */
    fun pcenc(d: String): LintFailure? {
        var pos = 0
        while (pos < d.length) {
            if (d[pos] == '%') {
                if (pos + 2 >= d.length) return fail(Gs1ErrorCode.INVALID_PERCENT_SEQUENCE, pos, d.length - pos)
                if (!d[pos + 1].isHexDigit() || !d[pos + 2].isHexDigit()) {
                    return fail(Gs1ErrorCode.INVALID_PERCENT_SEQUENCE, pos, 3)
                }
                pos += 2
            }
            pos++
        }
        return null
    }

    // ---------------------------------------------------------------- code lists

    private val ISO3166 = longArrayOf(
        0x08a888898888b888uL.toLong(), 0x8aa80a2888888888uL.toLong(), 0x0888888a22232889uL.toLong(),
        0x88188a2221e322a2uL.toLong(), 0x2a2a180088888888uL.toLong(), 0x888a888888888888uL.toLong(),
        0x888288a2622a22a2uL.toLong(), 0x222222228808b888uL.toLong(), 0x8888970808222222uL.toLong(),
        0x2de102888888a222uL.toLong(), 0x320a1b222222a203uL.toLong(), 0xe20808c8088888a8uL.toLong(),
        0x8888889a89002021uL.toLong(), 0xe080222a00082102uL.toLong(), 0L, 0L,
    )

    private val ISO4217 = longArrayOf(
        0x008800008808b808uL.toLong(), 0x8880082080880808uL.toLong(), 0x0880808800220008uL.toLong(),
        0x8010820202822000uL.toLong(), 0x0202000008000000uL.toLong(), 0x8888088888888080uL.toLong(),
        0x088a88a262222002uL.toLong(), 0x022200008800a080uL.toLong(), 0x88080c0008220200uL.toLong(),
        0x2022028880000020uL.toLong(), 0x1202000000202002uL.toLong(), 0xa200008000088888uL.toLong(),
        0x0088880081002020uL.toLong(), 0x2080002800002200uL.toLong(), 0x0400000fe6adbfdfuL.toLong(),
        0xfdfdfce225000000uL.toLong(),
    )

    private val ISO3166_ALPHA2 = longArrayOf(
        0x1e9afb77f7bdbb7buL.toLong(), 0xe4fc21a8012b0070uL.toLong(), 0x003a900df9dfa800uL.toLong(),
        0xb160181ef00202c0uL.toLong(), 0x00b8d42f8281f2bfuL.toLong(), 0x3fffeba4d2100080uL.toLong(),
        0x023cf1ca80000002uL.toLong(), 0x008a8fbfe75cddf9uL.toLong(), 0x59820820eaa10200uL.toLong(),
        0x4002000000000800uL.toLong(), 0x1020020080000000uL.toLong(),
    )

    // Bits 0-99, most significant bit first
    private val MEDIA_TYPES = intArrayOf(0x7f, 0xe0, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0xff, 0xff, 0xf0)

    private fun bit(field: LongArray, index: Int): Boolean = (field[index / 64] ushr (63 - index % 64)) and 1L == 1L

    private fun threeDigitCode(d: String): Int =
        if (d.length == 3 && d.all { it.isAsciiDigit() }) (d[0] - '0') * 100 + (d[1] - '0') * 10 + (d[2] - '0') else -1

    fun iso3166(d: String): LintFailure? {
        val v = threeDigitCode(d)
        return if (v >= 0 && bit(ISO3166, v)) null else fail(Gs1ErrorCode.NOT_ISO3166, 0, d.length)
    }

    fun iso3166999(d: String): LintFailure? {
        if (d == "999") return null
        val failure = iso3166(d) ?: return null
        return fail(Gs1ErrorCode.NOT_ISO3166_OR_999, failure.position, failure.length)
    }

    fun iso4217(d: String): LintFailure? {
        val v = threeDigitCode(d)
        return if (v >= 0 && bit(ISO4217, v)) null else fail(Gs1ErrorCode.NOT_ISO4217, 0, d.length)
    }

    fun iso3166alpha2(d: String): LintFailure? {
        val valid = d.length == 2 && d[0] in 'A'..'Z' && d[1] in 'A'..'Z' &&
            bit(ISO3166_ALPHA2, (d[0] - 'A') * 26 + (d[1] - 'A'))
        return if (valid) null else fail(Gs1ErrorCode.NOT_ISO3166_ALPHA2, 0, d.length)
    }

    fun mediaType(d: String): LintFailure? {
        val valid = d.length == 2 && d[0].isAsciiDigit() && d[1].isAsciiDigit() && run {
            val v = (d[0] - '0') * 10 + (d[1] - '0')
            MEDIA_TYPES[v / 8] and (0x80 ushr (v % 8)) != 0
        }
        return if (valid) null else fail(Gs1ErrorCode.INVALID_MEDIA_TYPE, 0, d.length)
    }

    private const val IBAN_MIN_LENGTH = 10
    private const val IBAN_MAX_LENGTH = 34

    fun iban(d: String): LintFailure? {
        val n = d.length
        if (n < 4) return fail(Gs1ErrorCode.IBAN_TOO_SHORT, 0, n)
        if (iso3166alpha2(d.substring(0, 2)) != null) return fail(Gs1ErrorCode.ILLEGAL_IBAN_COUNTRY_CODE, 0, 2)
        if (n > IBAN_MAX_LENGTH) return fail(Gs1ErrorCode.IBAN_TOO_LONG, 0, n)
        if (n <= IBAN_MIN_LENGTH) return fail(Gs1ErrorCode.IBAN_TOO_SHORT, 0, n)
        var checksum = 0
        for (pos in 4 until n + 4) {
            val actual = if (pos < n) pos else pos - n
            val weight = when (val c = d[actual]) {
                in '0'..'9' -> c - '0' + 1
                in 'A'..'Z' -> c - 'A' + 11
                else -> return fail(Gs1ErrorCode.INVALID_IBAN_CHARACTER, actual, 1)
            }
            checksum = (checksum * (if (weight <= 10) 10 else 100) + (weight - 1)) % 97
        }
        return if (checksum != 1) fail(Gs1ErrorCode.INCORRECT_IBAN_CHECKSUM, 2, 2) else null
    }

    fun importerIdx(d: String): LintFailure? {
        if (d.length != 1) return fail(Gs1ErrorCode.IMPORTER_IDX_MUST_BE_ONE_CHARACTER, 0, d.length)
        return if (CSET64.indexOf(d[0]) < 0) fail(Gs1ErrorCode.INVALID_IMPORT_IDX_CHARACTER, 0, 1) else null
    }

    private val PACKAGE_TYPES: Set<String> by lazy {
        (
            "1A 1B 1D 1F 1G 1W 200 201 202 203 204 205 206 210 211 212 2C 3A 3H 43 44 4A 4B 4C 4D 4F 4G 4H " +
                "5H 5L 5M 6H 6P 7A 7B 8 8A 8B 8C 9 AA AB AC AD AF AG AH AI AJ AL AM AP APE AT AV B4 BB BC BD BE " +
                "BF BG BGE BH BI BJ BK BL BM BME BN BO BP BQ BR BRI BS BT BU BV BW BX BY BZ CA CB CBL CC CCE CD CE " +
                "CF CG CH CI CJ CK CL CM CN CO CP CQ CR CS CT CU CV CW CX CY CZ DA DB DC DG DH DI DJ DK DL DM DN DP " +
                "DPE DR DS DT DU DV DW DX DY E1 E2 E3 EC ED EE EF EG EH EI EN FB FC FD FE FI FL FO FOB FP FPE FR FT " +
                "FW FX GB GI GL GR GU GY GZ HA HB HC HG HN HR IA IB IC ID IE IF IG IH IK IL IN IZ JB JC JG JR JT JY " +
                "KG KI LAB LE LG LT LU LV LZ MA MB MC ME MPE MR MS MT MW MX NA NE NF NG NS NT NU NV OA OB OC OD OE OF " +
                "OK OPE OT OU P2 PA PAE PB PC PD PE PF PG PH PI PJ PK PL PLP PN PO POP PP PPE PR PT PU PUE PV PX PY PZ " +
                "QA QB QC QD QF QG QH QJ QK QL QM QN QP QQ QR QS RB1 RB2 RB3 RCB RD RG RJ RK RL RO RT RZ S1 SA SB SC SD " +
                "SE SEC SH SI SK SL SM SO SP SS ST STL SU SV SW SX SY SZ T1 TB TC TD TE TEV TG THE TI TK TL TN TO TR " +
                "TRE TS TT TTE TU TV TW TWE TY TZ UC UN UUE VA VG VI VK VL VN VO VP VQ VR VS VY WA WB WC WD WF WG WH WJ " +
                "WK WL WM WN WP WQ WR WRP WS WT WU WV WW WX WY WZ X11 X12 X15 X16 X17 X18 X19 X20 X3 XA XB XC XD XF XG " +
                "XH XJ XK YA YB YC YD YF YG YH YJ YK YL YM YN YP YQ YR YS YT YV YW YX YY YZ ZA ZB ZC ZD ZF ZG ZH ZJ ZK " +
                "ZL ZM ZN ZP ZQ ZR ZS ZT ZU ZV ZW ZX ZY ZZ"
            ).split(' ').toSet()
    }

    fun packageType(d: String): LintFailure? =
        if (d in PACKAGE_TYPES) null else fail(Gs1ErrorCode.INVALID_PACKAGE_TYPE, 0, d.length)

    // ---------------------------------------------------------------- helpers

    private fun fail(code: Gs1ErrorCode, position: Int, length: Int) = LintFailure(code, position, length)
}

internal fun Char.isAsciiDigit(): Boolean = this in '0'..'9'

private fun Char.isHexDigit(): Boolean = this in '0'..'9' || this in 'a'..'f' || this in 'A'..'F'
