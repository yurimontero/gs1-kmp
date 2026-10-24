package io.github.yurimontero.gs1.datamatrix

import io.github.yurimontero.gs1.Gs1Error
import io.github.yurimontero.gs1.Gs1ErrorCode
import io.github.yurimontero.gs1.Gs1Exception
import io.github.yurimontero.gs1.Gs1Message
import io.github.yurimontero.gs1.Gs1Token

/** Which Data Matrix shapes may be chosen. */
public enum class DataMatrixShape {
    /** Square symbols only (10x10 to 144x144). The usual choice for GS1 DataMatrix. */
    SQUARE,

    /** Rectangular symbols only (8x18 to 16x48), for long and narrow print areas. */
    RECTANGULAR,

    /** The smallest symbol of either shape. */
    ANY,
}

/**
 * GS1 DataMatrix encoder: Data Matrix ECC 200 (ISO/IEC 16022) with FNC1 (codeword 232) first.
 *
 * Data is encoded with ASCII encodation, digit pairs taking one codeword each.
 *
 * ```
 * val symbol = Gs1DataMatrix.encode(Gs1Message.parse("(01)09501101020917(17)261231(10)ABC123"))
 * symbol.rows; symbol.columns   // 18 x 18
 * symbol[row, column]           // true = dark module
 * ```
 */
public object Gs1DataMatrix {

    /** Minimum quiet zone on every side, in modules. */
    public const val QUIET_ZONE_MODULES: Int = 1

    /**
     * Encodes [message] in the smallest symbol of the allowed [shape], or in exactly [size] when given.
     *
     * @throws Gs1Exception with [Gs1ErrorCode.DATA_MATRIX_TOO_LONG] if the data does not fit.
     */
    public fun encode(
        message: Gs1Message,
        shape: DataMatrixShape = DataMatrixShape.SQUARE,
        size: DataMatrixSize? = null,
    ): DataMatrixSymbol {
        val data = encodeAscii(message.tokens)
        val chosen = size ?: DataMatrixSize.all.firstOrNull { it.matches(shape) && it.dataCodewords >= data.size }
        if (chosen == null || chosen.dataCodewords < data.size) {
            throw Gs1Exception(Gs1Error(Gs1ErrorCode.DATA_MATRIX_TOO_LONG, null, null, 0, data.size))
        }
        val codewords = ReedSolomon.addErrorCorrection(pad(data, chosen.dataCodewords), chosen)
        return DataMatrixSymbol(chosen, Placement.build(codewords, chosen), codewords, data.size, message.humanReadable)
    }

    private fun DataMatrixSize.matches(shape: DataMatrixShape) = when (shape) {
        DataMatrixShape.SQUARE -> isSquare
        DataMatrixShape.RECTANGULAR -> !isSquare
        DataMatrixShape.ANY -> true
    }

    internal const val FNC1 = 232
    private const val PAD = 129

    /** ASCII encodation: FNC1 -> 232, two digits -> 130 + value, other characters -> ASCII + 1. */
    internal fun encodeAscii(tokens: List<Gs1Token>): IntArray {
        val out = ArrayList<Int>()
        for (token in tokens) {
            when (token) {
                is Gs1Token.Fnc1 -> out += FNC1
                is Gs1Token.Data -> {
                    val text = token.text
                    var i = 0
                    while (i < text.length) {
                        val c = text[i]
                        if (c in '0'..'9' && i + 1 < text.length && text[i + 1] in '0'..'9') {
                            out += 130 + (c - '0') * 10 + (text[i + 1] - '0')
                            i += 2
                        } else {
                            require(c.code in 0..127) { "Character '$c' cannot be encoded in ASCII encodation" }
                            out += c.code + 1
                            i++
                        }
                    }
                }
            }
        }
        return out.toIntArray()
    }

    /** Fills the remaining data capacity: one plain pad codeword, then pseudo-randomised ones. */
    internal fun pad(data: IntArray, capacity: Int): IntArray {
        val out = data.copyOf(capacity)
        for (index in data.size until capacity) {
            out[index] = if (index == data.size) {
                PAD
            } else {
                val position = index + 1
                val value = PAD + (149 * position) % 253 + 1
                if (value <= 254) value else value - 254
            }
        }
        return out
    }
}

/**
 * A Data Matrix ECC 200 symbol size.
 *
 * @property rows symbol height in modules, finder pattern included, quiet zone excluded.
 * @property columns symbol width in modules.
 */
public class DataMatrixSize private constructor(
    public val rows: Int,
    public val columns: Int,
    /** Number of data codewords (FNC1 and each character or digit pair use one). */
    public val dataCodewords: Int,
    /** Number of error correction codewords. */
    public val errorCodewords: Int,
    internal val regionRows: Int,
    internal val regionColumns: Int,
    internal val verticalRegions: Int,
    internal val horizontalRegions: Int,
    internal val blocks: Int,
) {
    public val isSquare: Boolean get() = rows == columns

    /** Data codewords of Reed-Solomon block [index] (0-based). Data is interleaved over [blocks] blocks. */
    internal fun blockDataCodewords(index: Int): Int =
        dataCodewords / blocks + if (index < dataCodewords % blocks) 1 else 0

    internal val blockErrorCodewords: Int get() = errorCodewords / blocks

    override fun toString(): String = "${rows}x$columns"

    public companion object {
        private fun square(size: Int, data: Int, error: Int, regions: Int, blocks: Int = 1): DataMatrixSize {
            val regionSize = size / regions - 2
            return DataMatrixSize(size, size, data, error, regionSize, regionSize, regions, regions, blocks)
        }

        private fun rectangle(rows: Int, columns: Int, data: Int, error: Int, horizontalRegions: Int): DataMatrixSize =
            DataMatrixSize(rows, columns, data, error, rows - 2, columns / horizontalRegions - 2, 1, horizontalRegions, 1)

        /** Every ECC 200 size, ordered by data capacity (ISO/IEC 16022, table 7). */
        public val all: List<DataMatrixSize> = listOf(
            square(10, 3, 5, 1),
            square(12, 5, 7, 1),
            rectangle(8, 18, 5, 7, 1),
            square(14, 8, 10, 1),
            rectangle(8, 32, 10, 11, 2),
            square(16, 12, 12, 1),
            rectangle(12, 26, 16, 14, 1),
            square(18, 18, 14, 1),
            square(20, 22, 18, 1),
            rectangle(12, 36, 22, 18, 2),
            square(22, 30, 20, 1),
            rectangle(16, 36, 32, 24, 2),
            square(24, 36, 24, 1),
            square(26, 44, 28, 1),
            rectangle(16, 48, 49, 28, 2),
            square(32, 62, 36, 2),
            square(36, 86, 42, 2),
            square(40, 114, 48, 2),
            square(44, 144, 56, 2),
            square(48, 174, 68, 2),
            square(52, 204, 84, 2, blocks = 2),
            square(64, 280, 112, 4, blocks = 2),
            square(72, 368, 144, 4, blocks = 4),
            square(80, 456, 192, 4, blocks = 4),
            square(88, 576, 224, 4, blocks = 4),
            square(96, 696, 272, 4, blocks = 4),
            square(104, 816, 336, 4, blocks = 6),
            square(120, 1050, 408, 6, blocks = 6),
            square(132, 1304, 496, 6, blocks = 8),
            square(144, 1558, 620, 6, blocks = 10),
        )

        /** Square sizes, smallest first. */
        public val squares: List<DataMatrixSize> get() = all.filter { it.isSquare }

        /** Rectangular sizes, smallest first. */
        public val rectangles: List<DataMatrixSize> get() = all.filter { !it.isSquare }

        /** Returns the size with these dimensions, or `null` if ECC 200 has no such size. */
        public fun of(rows: Int, columns: Int): DataMatrixSize? = all.firstOrNull { it.rows == rows && it.columns == columns }
    }
}

/**
 * An encoded GS1 DataMatrix symbol.
 *
 * Draw each module as a square of a whole number of pixels or printer dots, leaving at least
 * [Gs1DataMatrix.QUIET_ZONE_MODULES] light module around the symbol.
 */
public class DataMatrixSymbol internal constructor(
    public val size: DataMatrixSize,
    private val modules: BooleanArray,
    /** All codewords placed in the symbol: data (including padding) followed by error correction. */
    public val codewords: List<Int>,
    /** Data codewords used by the message, before padding. */
    public val usedDataCodewords: Int,
    /** Human readable interpretation to print near the symbol, e.g. `(01)09501101020917(10)ABC123`. */
    public val humanReadable: String,
) {
    public val rows: Int get() = size.rows
    public val columns: Int get() = size.columns

    /** `true` if the module at [row], [column] is dark. Row 0 is the top. */
    public operator fun get(row: Int, column: Int): Boolean = modules[row * columns + column]

    /** Rows of modules (`true` = dark) with [quietZone] light modules on every side. */
    public fun toModules(quietZone: Int = Gs1DataMatrix.QUIET_ZONE_MODULES): Array<BooleanArray> =
        Array(rows + 2 * quietZone) { y ->
            BooleanArray(columns + 2 * quietZone) { x ->
                val row = y - quietZone
                val column = x - quietZone
                row in 0 until rows && column in 0 until columns && get(row, column)
            }
        }

    /** Physical width for a module size of [xDimensionMm], including quiet zones. */
    public fun widthMm(xDimensionMm: Double, quietZone: Int = Gs1DataMatrix.QUIET_ZONE_MODULES): Double =
        (columns + 2 * quietZone) * xDimensionMm

    /** Physical height for a module size of [xDimensionMm], including quiet zones. */
    public fun heightMm(xDimensionMm: Double, quietZone: Int = Gs1DataMatrix.QUIET_ZONE_MODULES): Double =
        (rows + 2 * quietZone) * xDimensionMm

    override fun toString(): String = "DataMatrixSymbol($humanReadable, $size)"
}

/** Reed-Solomon error correction over GF(256) with the Data Matrix polynomial x^8 + x^5 + x^3 + x^2 + 1. */
internal object ReedSolomon {
    private const val PRIMITIVE = 0x12D
    private val EXP = IntArray(255)
    private val LOG = IntArray(256)
    private val generators = HashMap<Int, IntArray>()

    init {
        var x = 1
        for (i in 0 until 255) {
            EXP[i] = x
            LOG[x] = i
            x = x shl 1
            if (x >= 256) x = x xor PRIMITIVE
        }
    }

    private fun multiply(a: Int, b: Int): Int = if (a == 0 || b == 0) 0 else EXP[(LOG[a] + LOG[b]) % 255]

    /** Coefficients of (x - 2^1)(x - 2^2)...(x - 2^n), highest degree first, leading 1 omitted. */
    internal fun generator(n: Int): IntArray = generators.getOrPut(n) {
        var poly = intArrayOf(1)
        for (i in 1..n) {
            val root = EXP[i % 255]
            val next = IntArray(poly.size + 1)
            for (j in poly.indices) {
                next[j] = next[j] xor poly[j]
                next[j + 1] = next[j + 1] xor multiply(poly[j], root)
            }
            poly = next
        }
        poly.copyOfRange(1, poly.size)
    }

    /** Error correction codewords for [data]. */
    internal fun encodeBlock(data: IntArray, errorCodewords: Int): IntArray {
        val gen = generator(errorCodewords)
        val remainder = IntArray(errorCodewords)
        for (d in data) {
            val factor = d xor remainder[0]
            for (j in 0 until errorCodewords - 1) remainder[j] = remainder[j + 1] xor multiply(factor, gen[j])
            remainder[errorCodewords - 1] = multiply(factor, gen[errorCodewords - 1])
        }
        return remainder
    }

    /** Appends interleaved error correction to the padded [data]. */
    internal fun addErrorCorrection(data: IntArray, size: DataMatrixSize): List<Int> {
        val result = IntArray(size.dataCodewords + size.errorCodewords)
        data.copyInto(result)
        val blocks = size.blocks
        // In 144x144 symbols the error correction is interleaved starting with block 9, as readers
        // expect (same as Zint and the ZXing decoder; see https://github.com/zxing-cpp/zxing-cpp/issues/259).
        val skew = size.rows == 144
        for (block in 0 until blocks) {
            val blockData = IntArray(size.blockDataCodewords(block)) { data[block + it * blocks] }
            val ecc = encodeBlock(blockData, size.blockErrorCodewords)
            val slot = if (!skew) block else if (block < 8) block + 2 else block - 8
            ecc.forEachIndexed { k, value -> result[size.dataCodewords + slot + k * blocks] = value }
        }
        return result.asList()
    }
}

/** Module placement (ISO/IEC 16022, annex F) and finder patterns. */
internal object Placement {

    fun build(codewords: List<Int>, size: DataMatrixSize): BooleanArray {
        val mappingRows = size.regionRows * size.verticalRegions
        val mappingColumns = size.regionColumns * size.horizontalRegions
        val mapping = place(codewords, mappingRows, mappingColumns)

        val modules = BooleanArray(size.rows * size.columns)
        for (row in 0 until size.rows) {
            val regionRow = row % (size.regionRows + 2)
            for (column in 0 until size.columns) {
                val regionColumn = column % (size.regionColumns + 2)
                val dark = when {
                    regionRow == size.regionRows + 1 -> true // bottom: solid
                    regionColumn == 0 -> true // left: solid
                    regionRow == 0 -> column % 2 == 0 // top: clock track
                    regionColumn == size.regionColumns + 1 -> row % 2 == 1 // right: clock track
                    else -> {
                        val y = (row / (size.regionRows + 2)) * size.regionRows + regionRow - 1
                        val x = (column / (size.regionColumns + 2)) * size.regionColumns + regionColumn - 1
                        mapping[y * mappingColumns + x].toInt() == 1
                    }
                }
                modules[row * size.columns + column] = dark
            }
        }
        return modules
    }

    /** Places codeword bits in the mapping matrix (data regions without finder patterns). 1 = dark. */
    internal fun place(codewords: List<Int>, numRows: Int, numCols: Int): ByteArray {
        val bits = ByteArray(numRows * numCols) { -1 }

        fun module(rowIn: Int, colIn: Int, pos: Int, bit: Int) {
            var row = rowIn
            var col = colIn
            if (row < 0) {
                row += numRows
                col += 4 - ((numRows + 4) % 8)
            }
            if (col < 0) {
                col += numCols
                row += 4 - ((numCols + 4) % 8)
            }
            val value = codewords[pos] and (1 shl (8 - bit))
            bits[row * numCols + col] = if (value != 0) 1 else 0
        }

        fun utah(row: Int, col: Int, pos: Int) {
            module(row - 2, col - 2, pos, 1)
            module(row - 2, col - 1, pos, 2)
            module(row - 1, col - 2, pos, 3)
            module(row - 1, col - 1, pos, 4)
            module(row - 1, col, pos, 5)
            module(row, col - 2, pos, 6)
            module(row, col - 1, pos, 7)
            module(row, col, pos, 8)
        }

        fun corner1(pos: Int) {
            module(numRows - 1, 0, pos, 1); module(numRows - 1, 1, pos, 2); module(numRows - 1, 2, pos, 3)
            module(0, numCols - 2, pos, 4); module(0, numCols - 1, pos, 5); module(1, numCols - 1, pos, 6)
            module(2, numCols - 1, pos, 7); module(3, numCols - 1, pos, 8)
        }

        fun corner2(pos: Int) {
            module(numRows - 3, 0, pos, 1); module(numRows - 2, 0, pos, 2); module(numRows - 1, 0, pos, 3)
            module(0, numCols - 4, pos, 4); module(0, numCols - 3, pos, 5); module(0, numCols - 2, pos, 6)
            module(0, numCols - 1, pos, 7); module(1, numCols - 1, pos, 8)
        }

        fun corner3(pos: Int) {
            module(numRows - 3, 0, pos, 1); module(numRows - 2, 0, pos, 2); module(numRows - 1, 0, pos, 3)
            module(0, numCols - 2, pos, 4); module(0, numCols - 1, pos, 5); module(1, numCols - 1, pos, 6)
            module(2, numCols - 1, pos, 7); module(3, numCols - 1, pos, 8)
        }

        fun corner4(pos: Int) {
            module(numRows - 1, 0, pos, 1); module(numRows - 1, numCols - 1, pos, 2); module(0, numCols - 3, pos, 3)
            module(0, numCols - 2, pos, 4); module(0, numCols - 1, pos, 5); module(1, numCols - 3, pos, 6)
            module(1, numCols - 2, pos, 7); module(1, numCols - 1, pos, 8)
        }

        fun empty(row: Int, col: Int) = bits[row * numCols + col] < 0

        var pos = 0
        var row = 4
        var col = 0
        do {
            if (row == numRows && col == 0) corner1(pos++)
            if (row == numRows - 2 && col == 0 && numCols % 4 != 0) corner2(pos++)
            if (row == numRows - 2 && col == 0 && numCols % 8 == 4) corner3(pos++)
            if (row == numRows + 4 && col == 2 && numCols % 8 == 0) corner4(pos++)
            do {
                if (row < numRows && col >= 0 && empty(row, col)) utah(row, col, pos++)
                row -= 2
                col += 2
            } while (row >= 0 && col < numCols)
            row++
            col += 3
            do {
                if (row >= 0 && col < numCols && empty(row, col)) utah(row, col, pos++)
                row += 2
                col -= 2
            } while (row < numRows && col >= 0)
            row += 3
            col++
        } while (row < numRows || col < numCols)

        // Unused corner modules (some sizes): fixed pattern
        if (empty(numRows - 1, numCols - 1)) {
            bits[(numRows - 1) * numCols + numCols - 1] = 1
            bits[(numRows - 2) * numCols + numCols - 2] = 1
        }
        return bits
    }
}
