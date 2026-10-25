package io.github.yurimontero.gs1.qrcode

import io.github.yurimontero.gs1.Gs1ErrorCode
import io.github.yurimontero.gs1.Gs1Exception
import io.github.yurimontero.gs1.Gs1Message
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class QrCodeTest {

    /** "01234567", level M: version 1, mask 0. Checked with an independent decoder. */
    private val golden = listOf(
        "#######...###.#######",
        "#.....#.###...#.....#",
        "#.###.#..##...#.###.#",
        "#.###.#..#.##.#.###.#",
        "#.###.#.##.##.#.###.#",
        "#.....#....#..#.....#",
        "#######.#.#.#.#######",
        ".....................",
        "#.#.#.#...#.#...#..#.",
        "##.#....#.##.#.#...#.",
        "...##.###.##.###.###.",
        "##..##.#.#.###.##..#.",
        "..#..###.###.###....#",
        "........#.#...#....#.",
        "#######.....#...#...#",
        "#.....#...#...#..#.##",
        "#.###.#.###.#.#.###.#",
        "#.###.#..#.#.#.#.###.",
        "#.###.#.##.#.###..#.#",
        "#.....#....###.###...",
        "#######.#..#.###..#.#",
    )

    private fun QrCodeSymbol.rows() = (0 until size).map { r -> (0 until size).joinToString("") { c -> if (this[r, c]) "#" else "." } }

    @Test
    fun matchesReferenceSymbol() {
        val symbol = QrCode.encode("01234567", QrErrorCorrection.M)
        assertEquals(1, symbol.version)
        assertEquals(0, symbol.mask)
        assertEquals(golden, symbol.rows())
    }

    @Test
    fun choosesSmallestVersion() {
        assertEquals(1, QrCode.encode("1".repeat(41), QrErrorCorrection.L).version)
        assertEquals(2, QrCode.encode("1".repeat(42), QrErrorCorrection.L).version)
        assertEquals(40, QrCode.encode("1".repeat(7089), QrErrorCorrection.L).version)
        assertEquals(40, QrCode.encode("A".repeat(4296), QrErrorCorrection.L).version)
        assertEquals(40, QrCode.encode("a".repeat(2953), QrErrorCorrection.L).version)
        assertEquals(40, QrCode.encode("a".repeat(1273), QrErrorCorrection.H).version)
    }

    @Test
    fun reportsTooLong() {
        val e = assertFailsWith<Gs1Exception> { QrCode.encode("1".repeat(7090), QrErrorCorrection.L) }
        assertEquals(Gs1ErrorCode.QR_CODE_TOO_LONG, e.error.code)
        assertFailsWith<Gs1Exception> { QrCode.encode("1".repeat(42), QrErrorCorrection.L, version = 1) }
    }

    @Test
    fun honoursVersionAndMask() {
        val symbol = QrCode.encode("123", QrErrorCorrection.H, version = 7, mask = 5)
        assertEquals(7, symbol.version)
        assertEquals(45, symbol.size)
        assertEquals(5, symbol.mask)
        assertFailsWith<IllegalArgumentException> { QrCode.encode("1", version = 41) }
        assertFailsWith<IllegalArgumentException> { QrCode.encode("1", mask = 8) }
    }

    @Test
    fun mixedSegmentsAreSmallerThanByteMode() {
        val segments = Segmentation.optimal("https://example.com/01/09501101020917".encodeToByteArray(), 1)
        assertEquals(listOf(QrMode.BYTE, QrMode.NUMERIC), segments.map { it.mode })
        val upper = Segmentation.optimal("HTTPS://EXAMPLE.COM/01/09501101020917".encodeToByteArray(), 1)
        assertEquals(listOf(QrMode.ALPHANUMERIC, QrMode.NUMERIC), upper.map { it.mode })
    }

    @Test
    fun finderPatternsAndQuietZone() {
        val symbol = QrCode.encode("HELLO")
        val modules = symbol.toModules()
        assertEquals(symbol.size + 8, modules.size)
        assertTrue(modules[0].none { it })
        for ((row, column) in listOf(0 to 0, 0 to symbol.size - 7, symbol.size - 7 to 0)) {
            assertTrue(symbol[row, column] && symbol[row + 6, column + 6] && symbol[row + 3, column + 3])
            assertTrue(!symbol[row + 1, column + 1])
        }
        assertEquals((symbol.size + 8) * 0.5, symbol.sizeMm(0.5))
    }

    @Test
    fun gs1DigitalLinkQrCode() {
        val message = Gs1Message.parse("(01)35789835863696(10)1234")
        val symbol = Gs1QrCode.encode(message, domain = "https://subdomain.domain")
        assertEquals("https://subdomain.domain/01/35789835863696/10/1234", symbol.text)
        assertEquals("(01)35789835863696(10)1234", symbol.humanReadable)
        assertEquals(4, symbol.version)
        assertEquals(QrErrorCorrection.M, symbol.errorCorrection)
        // An uppercase domain lets the whole URI use alphanumeric mode
        assertEquals(3, Gs1QrCode.encode(message, domain = "HTTPS://SUBDOMAIN.DOMAIN").version)
    }

    @Test
    fun encodesExistingUriAsGiven() {
        val uri = "https://example.com/01/09501101020917?17=261231"
        val symbol = Gs1QrCode.encodeUri(uri, QrErrorCorrection.Q)
        assertEquals(uri, symbol.text)
        assertEquals("(01)09501101020917(17)261231", symbol.humanReadable)
        assertFailsWith<Gs1Exception> { Gs1QrCode.encodeUri("https://example.com/products/42") }
    }
}
