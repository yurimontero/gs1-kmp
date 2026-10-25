package io.github.yurimontero.gs1.datamatrix

import io.github.yurimontero.gs1.Gs1ErrorCode
import io.github.yurimontero.gs1.Gs1Exception
import io.github.yurimontero.gs1.Gs1Message
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class Gs1DataMatrixTest {

    @Test
    fun reedSolomonMatchesGs1GuidelineExample() {
        // GS1 DataMatrix Guideline, encoding example: "123456" -> 142 164 186, 10x10 symbol
        assertContentEquals(intArrayOf(114, 25, 5, 88, 102), ReedSolomon.encodeBlock(intArrayOf(142, 164, 186), 5))
    }

    @Test
    fun asciiEncodationMatchesGuideline() {
        val message = Gs1Message.of("01" to "03453120000011")
        assertContentEquals(
            intArrayOf(232, 131, 133, 175, 161, 150, 130, 130, 141),
            Gs1DataMatrix.encodeAscii(message.tokens),
        )
    }

    @Test
    fun separatorIsCodeword232() {
        val message = Gs1Message.of("10" to "A", "21" to "1", order = io.github.yurimontero.gs1.Gs1Order.AS_GIVEN)
        // FNC1, "10", 'A', FNC1, "21", '1'
        assertContentEquals(intArrayOf(232, 140, 66, 232, 151, 50), Gs1DataMatrix.encodeAscii(message.tokens))
    }

    @Test
    fun choosesSmallestSquareSize() {
        // GTIN + 10-digit serial = 15 codewords -> 18x18 (GS1 DataMatrix Guideline, Q.3)
        val symbol = Gs1DataMatrix.encode(Gs1Message.of("01" to "09501101020917", "21" to "1234567890"))
        assertEquals(18, symbol.rows)
        assertEquals(18, symbol.columns)
        assertEquals(15, symbol.usedDataCodewords)
        assertEquals(18 + 14, symbol.codewords.size)
    }

    @Test
    fun choosesRectangularSizeWhenAsked() {
        val message = Gs1Message.of("01" to "09501101020917", "21" to "1234567890")
        val symbol = Gs1DataMatrix.encode(message, DataMatrixShape.RECTANGULAR)
        assertEquals(12, symbol.rows)
        assertEquals(26, symbol.columns)
    }

    @Test
    fun forcedSize() {
        val message = Gs1Message.of("01" to "09501101020917")
        val symbol = Gs1DataMatrix.encode(message, size = assertNotNull(DataMatrixSize.of(26, 26)))
        assertEquals(26, symbol.rows)
        val e = assertFailsWith<Gs1Exception> { Gs1DataMatrix.encode(message, size = DataMatrixSize.of(10, 10)) }
        assertEquals(Gs1ErrorCode.DATA_MATRIX_TOO_LONG, e.error.code)
    }

    @Test
    fun rejectsDataLargerThan144x144() {
        // 9 x (2 + 90) + 10 x (4 + 30) + 9 separators = 1177 codewords: fits in 132x132 (1304)
        val pairs = (91..99).map { it.toString() to "A".repeat(90) } + (7230..7239).map { it.toString() to "AA" + "B".repeat(28) }
        assertEquals(132, Gs1DataMatrix.encode(Gs1Message.of(pairs)).rows)
        // + 5 x (4 + 70 + 1) = 1552 codewords: fits in 144x144 (1558)
        val bigger = pairs + (4302..4306).map { it.toString() to "C".repeat(70) }
        assertEquals(144, Gs1DataMatrix.encode(Gs1Message.of(bigger)).rows)
        // + 1 more long field: does not fit
        val tooBig = bigger + ("4312" to "D".repeat(70))
        val e = assertFailsWith<Gs1Exception> { Gs1DataMatrix.encode(Gs1Message.of(tooBig)) }
        assertEquals(Gs1ErrorCode.DATA_MATRIX_TOO_LONG, e.error.code)
    }

    @Test
    fun padding() {
        // One plain pad (129), then pseudo-randomised pads: position 3 -> 129 + (149 * 3 % 253 + 1) - 254 = 70
        assertContentEquals(intArrayOf(232, 129, 70), Gs1DataMatrix.pad(intArrayOf(232), 3))
        assertContentEquals(intArrayOf(232, 66), Gs1DataMatrix.pad(intArrayOf(232, 66), 2))
    }

    @Test
    fun smallestSymbolMatchesReference() {
        // Checked bit for bit against an independent encoder and decoder
        assertSymbol(
            Gs1DataMatrix.encode(Gs1Message.of("90" to "A")),
            "#.#.#.#.#.",
            "###.#.##.#",
            "#.###.#.#.",
            "##..#.####",
            "##.....#..",
            "#........#",
            "###...###.",
            "#.#.##...#",
            "#.#...#.#.",
            "##########",
        )
    }

    @Test
    fun healthcareExampleMatchesReference() {
        val symbol = Gs1DataMatrix.encode(Gs1Message.of("01" to "09501101020917", "17" to "190508", "10" to "ABCD1234", "21" to "10"))
        assertSymbol(
            symbol,
            "#.#.#.#.#.#.#.#.#.#.#.",
            "##..####.#.#.....#..##",
            "#...#.#....#.#.#.#..#.",
            "#.##..##..#....#..##.#",
            "###.#.#....#.##...##..",
            "#....#.###.....#.##.##",
            "#..##.#..##..#.#.#..#.",
            "#..#.#.#...#.#.##..###",
            "#...#.#.#.#.#.#.#.##..",
            "##..##..#####.###.#..#",
            "#.#.#.##.#####..###.#.",
            "#.###...###......#...#",
            "#####..#.#....###..##.",
            "###.###...#.#..#.#####",
            "#.....#.....#.##...#..",
            "##.....#..#.##.#####.#",
            "######.#.#.#.#.##.#...",
            "##..##.#.###..###.#.##",
            "#.....#.##..####.##.#.",
            "#..#...#.#.#..##.#...#",
            "##..###..##..##..#.##.",
            "######################",
        )
        assertEquals("(01)09501101020917(17)190508(10)ABCD1234(21)10", symbol.humanReadable)
    }

    @Test
    fun finderPatternsInEveryRegion() {
        for (size in DataMatrixSize.all) {
            val symbol = Gs1DataMatrix.encode(Gs1Message.of("90" to "A"), size = size)
            val regionHeight = size.regionRows + 2
            val regionWidth = size.regionColumns + 2
            for (row in 0 until symbol.rows) for (column in 0 until symbol.columns) {
                val r = row % regionHeight
                val c = column % regionWidth
                val expected = when {
                    r == regionHeight - 1 || c == 0 -> true
                    r == 0 -> column % 2 == 0
                    c == regionWidth - 1 -> row % 2 == 1
                    else -> null
                }
                if (expected != null) assertEquals(expected, symbol[row, column], "$size finder at $row,$column")
            }
            assertFalse(symbol[0, symbol.columns - 1], "$size: top right module must be light")
        }
    }

    @Test
    fun interleavesErrorCorrectionFor144x144WithSkew() {
        val symbol = Gs1DataMatrix.encode(Gs1Message.of("90" to "A"), size = assertNotNull(DataMatrixSize.of(144, 144)))
        val data = symbol.codewords.take(1558)
        fun eccOf(block: Int): IntArray {
            val blockData = data.filterIndexed { index, _ -> index % 10 == block }.toIntArray()
            return ReedSolomon.encodeBlock(blockData, 62)
        }
        // First ECC slot holds block 9 (index 8), then block 10, then blocks 1-8
        assertEquals(eccOf(8)[0], symbol.codewords[1558])
        assertEquals(eccOf(9)[0], symbol.codewords[1559])
        assertEquals(eccOf(0)[0], symbol.codewords[1560])
        assertEquals(eccOf(7)[1], symbol.codewords[1558 + 10 + 9])
    }

    @Test
    fun sizeTable() {
        assertEquals(30, DataMatrixSize.all.size)
        assertEquals(24, DataMatrixSize.squares.size)
        assertEquals(6, DataMatrixSize.rectangles.size)
        assertEquals(1558, DataMatrixSize.of(144, 144)?.dataCodewords)
        assertEquals(DataMatrixSize.all.sortedBy { it.dataCodewords }, DataMatrixSize.all)
    }

    @Test
    fun modulesWithQuietZone() {
        val symbol = Gs1DataMatrix.encode(Gs1Message.of("90" to "A"))
        val modules = symbol.toModules(quietZone = 2)
        assertEquals(14, modules.size)
        assertEquals(14, modules[0].size)
        assertFalse(modules[1].any { it })
        assertTrue(modules[2][2])
        assertEquals(symbol.widthMm(0.5, quietZone = 2), 7.0)
    }

    private fun assertSymbol(symbol: DataMatrixSymbol, vararg rows: String) {
        assertEquals(rows.size, symbol.rows)
        val actual = (0 until symbol.rows).map { r -> (0 until symbol.columns).joinToString("") { if (symbol[r, it]) "#" else "." } }
        assertEquals(rows.toList(), actual)
    }
}
