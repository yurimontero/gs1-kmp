package io.github.yurimontero.gs1.code128

import io.github.yurimontero.gs1.Gs1ErrorCode
import io.github.yurimontero.gs1.Gs1Exception
import io.github.yurimontero.gs1.Gs1Message
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class Gs1Code128Test {

    private val gs = Gs1Message.GROUP_SEPARATOR

    @Test
    fun encodesGtinWithKnownCheckCharacter() {
        val symbol = Gs1Code128.encode(Gs1Message.of("01" to "09501101020917"))
        // Start C, FNC1, 01 09 50 11 01 02 09 17, check 15, stop
        assertEquals(listOf(105, 102, 1, 9, 50, 11, 1, 2, 9, 17, 15, 106), symbol.values)
        assertEquals("(01)09501101020917", symbol.humanReadable)
        assertEquals(16, symbol.dataCharacterCount)
    }

    @Test
    fun switchesToSetBForOddTrailingDigit() {
        val symbol = Gs1Code128.encode(Gs1Message.of("10" to "1234567"))
        // Start C, FNC1, 10 12 34 56, Code B, '7'
        assertEquals(listOf(105, 102, 10, 12, 34, 56, 100, '7'.code - 32), symbol.values.dropLast(2))
    }

    @Test
    fun usesSetBForAlphanumericData() {
        val symbol = Gs1Code128.encode(Gs1Message.of("01" to "09501101020917", "10" to "AB12cd", "21" to "1234"))
        // Digits stay in set C until the batch letters, then back to C for the serial number
        val expected = listOf(105, 102, 1, 9, 50, 11, 1, 2, 9, 17, 10, 100, 33, 34, 17, 18, 67, 68, 102, 99, 21, 12, 34)
        assertEquals(expected, symbol.values.dropLast(2))
        assertDecodes(symbol, "]C1010950110102091710AB12cd${gs}211234")
    }

    @Test
    fun roundTripsVariousMessages() {
        listOf(
            Gs1Message.parse("(00)106141411234567897"),
            Gs1Message.parse("(01)09501101020917(17)261231(10)ABC123"),
            Gs1Message.parse("(01)09501101020917(10)A(21)1"),
            Gs1Message.parse("(02)09501101020917(37)12(10)lot-7/x"),
            Gs1Message.parse("(3103)001250(15)261231(10)9"),
            Gs1Message.parse("(400)PO 1".replace(" ", "_")),
            Gs1Message.parse("(90)!\"%&'*+,-./:;<=>?_z"),
        ).forEach { message ->
            assertDecodes(Gs1Code128.encode(message), message.toElementString(symbologyIdentifier = "]C1"))
        }
    }

    @Test
    fun choosesShortestEncoding() {
        // 12 digits after a letter: switching back to set C is cheaper than staying in set B
        val symbol = Gs1Code128.encode(Gs1Message.of("10" to "A123456789012"))
        assertEquals(listOf(105, 102, 10, 100, 33, 99, 12, 34, 56, 78, 90, 12), symbol.values.dropLast(2))
    }

    @Test
    fun prefersSetCOnTies() {
        // Start C + "90" + Code B costs the same as Start B + "9" + "0": either way 9 data symbols
        val symbol = Gs1Code128.encode(Gs1Message.of("90" to "ABCDEF"))
        assertEquals(listOf(105, 102, 90, 100, 33, 34, 35, 36, 37, 38), symbol.values.dropLast(2))
        assertDecodes(symbol, "]C190ABCDEF")
    }

    @Test
    fun enforcesMaximumDataCharacters() {
        val longMessage = Gs1Message.of("01" to "09501101020917", "400" to "A".repeat(30), "21" to "12345")
        val e = assertFailsWith<Gs1Exception> { Gs1Code128.encode(longMessage) }
        assertEquals(Gs1ErrorCode.GS1_128_TOO_LONG, e.error.code)
        assertTrue(Gs1Code128.encode(longMessage, enforceMaxDataCharacters = false).dataCharacterCount > 48)
    }

    @Test
    fun widthsAndModulesAreConsistent() {
        val symbol = Gs1Code128.encode(Gs1Message.parse("(01)09501101020917(10)ABC"))
        assertEquals(symbol.moduleCount, symbol.widths.sum())
        assertEquals(symbol.values.size * 6 + 1, symbol.widths.size)
        val modules = symbol.toModules()
        assertEquals(symbol.moduleCount + 20, modules.size)
        assertFalse(modules[9])
        assertTrue(modules[10])
        assertTrue(modules[modules.size - 11])
        assertFalse(modules[modules.size - 10])
        assertEquals(symbol.widths.filterIndexed { i, _ -> i % 2 == 0 }.sum(), modules.count { it })
    }

    @Test
    fun physicalSize() {
        val symbol = Gs1Code128.encode(Gs1Message.of("00" to "106141411234567897"))
        // SSCC: Start C, FNC1, 00 + 18 digits = 10 pairs, check, stop -> 14 characters -> 156 modules
        assertEquals(156, symbol.moduleCount)
        assertTrue(abs(symbol.widthMm(0.495) - 176 * 0.495) < 1e-9)
        assertTrue(abs(symbol.maxXDimensionMm() - 165.10 / 176) < 1e-9)
    }

    private fun assertDecodes(symbol: Code128Symbol, expected: String) {
        assertEquals(expected, Code128TestDecoder.decode(symbol.widths))
    }
}

/** Minimal Code 128 decoder used to check the encoder: returns `]C1` + data with GS separators. */
internal object Code128TestDecoder {
    private val byPattern = Code128Patterns.PATTERNS.withIndex().associate { (value, pattern) -> pattern to value }

    fun decode(widths: IntArray): String {
        require(widths.size % 6 == 1) { "Unexpected number of elements: ${widths.size}" }
        val values = (0 until widths.size / 6).map { k ->
            val pattern = (0 until if (k == widths.size / 6 - 1) 7 else 6).joinToString("") { widths[k * 6 + it].toString() }
            byPattern[pattern] ?: error("Unknown pattern $pattern")
        }
        require(values.last() == 106) { "Missing stop" }
        val data = values.dropLast(2)
        var checksum = data[0]
        for (i in 1 until data.size) checksum += data[i] * i
        require(checksum % 103 == values[values.size - 2]) { "Bad check character" }

        var set = when (data[0]) {
            104 -> 'B'
            105 -> 'C'
            else -> error("Unsupported start ${data[0]}")
        }
        require(data[1] == 102) { "Not GS1-128: no FNC1 after start" }
        val out = StringBuilder("]C1")
        for (v in data.drop(2)) {
            when {
                v == 102 -> out.append('\u001D')
                v == 99 -> set = 'C'
                v == 100 -> set = 'B'
                set == 'C' -> out.append(v.toString().padStart(2, '0'))
                else -> out.append((v + 32).toChar())
            }
        }
        return out.toString()
    }
}
