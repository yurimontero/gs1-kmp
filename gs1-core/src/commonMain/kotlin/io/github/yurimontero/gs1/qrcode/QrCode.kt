package io.github.yurimontero.gs1.qrcode

import io.github.yurimontero.gs1.Gs1Error
import io.github.yurimontero.gs1.Gs1ErrorCode
import io.github.yurimontero.gs1.Gs1Exception

/** QR Code error correction level: the share of the symbol that can be damaged and still be read. */
public enum class QrErrorCorrection(internal val formatBits: Int, internal val tableIndex: Int) {
    /** About 7% recovery. Smallest symbols. */
    L(1, 0),

    /** About 15% recovery. The usual choice. */
    M(0, 1),

    /** About 25% recovery. */
    Q(3, 2),

    /** About 30% recovery, for harsh environments. */
    H(2, 3),
}

/**
 * QR Code encoder (ISO/IEC 18004, Model 2, versions 1 to 40).
 *
 * The text is split into numeric, alphanumeric and byte segments so that the symbol is as small as
 * possible. Characters outside ASCII are encoded as UTF-8 bytes. For GS1 data use [Gs1QrCode].
 */
public object QrCode {

    /** Minimum quiet zone on every side, in modules. */
    public const val QUIET_ZONE_MODULES: Int = 4

    /**
     * Encodes [text] in the smallest version that fits, or in exactly [version] when given.
     *
     * @param mask mask pattern 0-7, or `null` to choose the one with the lowest penalty (recommended).
     * @throws Gs1Exception with [Gs1ErrorCode.QR_CODE_TOO_LONG] if the text does not fit.
     */
    public fun encode(
        text: String,
        errorCorrection: QrErrorCorrection = QrErrorCorrection.M,
        version: Int? = null,
        mask: Int? = null,
    ): QrCodeSymbol = encode(text, errorCorrection, version, mask, humanReadable = "")

    internal fun encode(
        text: String,
        errorCorrection: QrErrorCorrection,
        version: Int?,
        mask: Int?,
        humanReadable: String,
    ): QrCodeSymbol {
        require(version == null || version in 1..40) { "version must be 1-40, was $version" }
        require(mask == null || mask in 0..7) { "mask must be 0-7, was $mask" }
        val bytes = text.encodeToByteArray()
        val candidates = if (version != null) version..version else 1..40
        var needed = 0
        for (v in candidates) {
            val info = QrVersion.of(v)
            val capacity = info.dataCodewords(errorCorrection) * 8
            val segments = Segmentation.optimal(bytes, v)
            val bits = segments.sumOf { it.bitLength(v) }
            if (bits <= capacity) {
                val data = QrBits.dataCodewords(segments, v, capacity / 8)
                val codewords = QrErrorCorrectionCoding.interleave(data, info, errorCorrection)
                val matrix = QrMatrix(info)
                matrix.placeCodewords(codewords)
                val chosenMask = mask ?: (0..7).minBy { matrix.penaltyWithMask(it, errorCorrection) }
                matrix.applyMask(chosenMask, errorCorrection)
                return QrCodeSymbol(v, errorCorrection, chosenMask, matrix.size, matrix.modules, text, humanReadable)
            }
            needed = (bits + 7) / 8
        }
        throw Gs1Exception(Gs1Error(Gs1ErrorCode.QR_CODE_TOO_LONG, null, null, 0, needed))
    }
}

/**
 * An encoded QR Code symbol.
 *
 * Draw each module as a square of a whole number of pixels or printer dots, leaving at least
 * [QrCode.QUIET_ZONE_MODULES] light modules around the symbol.
 */
public class QrCodeSymbol internal constructor(
    /** QR Code version, 1-40. */
    public val version: Int,
    public val errorCorrection: QrErrorCorrection,
    /** Mask pattern used, 0-7. */
    public val mask: Int,
    /** Width and height in modules (`17 + 4 * version`), quiet zone excluded. */
    public val size: Int,
    private val modules: BooleanArray,
    /** The encoded text, e.g. a GS1 Digital Link URI. */
    public val text: String,
    /** Human readable interpretation of the GS1 data, e.g. `(01)09501101020917(10)ABC123`; empty for plain text. */
    public val humanReadable: String,
) {
    /** `true` if the module at [row], [column] is dark. Row 0 is the top. */
    public operator fun get(row: Int, column: Int): Boolean = modules[row * size + column]

    /** Rows of modules (`true` = dark) with [quietZone] light modules on every side. */
    public fun toModules(quietZone: Int = QrCode.QUIET_ZONE_MODULES): Array<BooleanArray> =
        Array(size + 2 * quietZone) { y ->
            BooleanArray(size + 2 * quietZone) { x ->
                val row = y - quietZone
                val column = x - quietZone
                row in 0 until size && column in 0 until size && get(row, column)
            }
        }

    /** Physical width (and height) for a module size of [xDimensionMm], including quiet zones. */
    public fun sizeMm(xDimensionMm: Double, quietZone: Int = QrCode.QUIET_ZONE_MODULES): Double =
        (size + 2 * quietZone) * xDimensionMm

    override fun toString(): String = "QrCodeSymbol($text, version $version-$errorCorrection)"
}

/** Version parameters: alignment pattern centres and error correction blocks per level. */
internal class QrVersion private constructor(
    val number: Int,
    val alignmentPositions: IntArray,
    /** Per level (L, M, Q, H): error correction codewords per block, then data codewords of each block. */
    private val blocks: Array<Pair<Int, IntArray>>,
) {
    val size: Int get() = 17 + 4 * number

    fun errorCodewordsPerBlock(level: QrErrorCorrection): Int = blocks[level.tableIndex].first
    fun blockDataCodewords(level: QrErrorCorrection): IntArray = blocks[level.tableIndex].second
    fun dataCodewords(level: QrErrorCorrection): Int = blockDataCodewords(level).sum()

    companion object {
        // version | alignment centres | per level "ecPerBlock:blocks x dataCodewords[+blocks x dataCodewords]"
        private val TABLE = arrayOf(
            "1||7:1x19|10:1x16|13:1x13|17:1x9",
            "2|6,18|10:1x34|16:1x28|22:1x22|28:1x16",
            "3|6,22|15:1x55|26:1x44|18:2x17|22:2x13",
            "4|6,26|20:1x80|18:2x32|26:2x24|16:4x9",
            "5|6,30|26:1x108|24:2x43|18:2x15+2x16|22:2x11+2x12",
            "6|6,34|18:2x68|16:4x27|24:4x19|28:4x15",
            "7|6,22,38|20:2x78|18:4x31|18:2x14+4x15|26:4x13+1x14",
            "8|6,24,42|24:2x97|22:2x38+2x39|22:4x18+2x19|26:4x14+2x15",
            "9|6,26,46|30:2x116|22:3x36+2x37|20:4x16+4x17|24:4x12+4x13",
            "10|6,28,50|18:2x68+2x69|26:4x43+1x44|24:6x19+2x20|28:6x15+2x16",
            "11|6,30,54|20:4x81|30:1x50+4x51|28:4x22+4x23|24:3x12+8x13",
            "12|6,32,58|24:2x92+2x93|22:6x36+2x37|26:4x20+6x21|28:7x14+4x15",
            "13|6,34,62|26:4x107|22:8x37+1x38|24:8x20+4x21|22:12x11+4x12",
            "14|6,26,46,66|30:3x115+1x116|24:4x40+5x41|20:11x16+5x17|24:11x12+5x13",
            "15|6,26,48,70|22:5x87+1x88|24:5x41+5x42|30:5x24+7x25|24:11x12+7x13",
            "16|6,26,50,74|24:5x98+1x99|28:7x45+3x46|24:15x19+2x20|30:3x15+13x16",
            "17|6,30,54,78|28:1x107+5x108|28:10x46+1x47|28:1x22+15x23|28:2x14+17x15",
            "18|6,30,56,82|30:5x120+1x121|26:9x43+4x44|28:17x22+1x23|28:2x14+19x15",
            "19|6,30,58,86|28:3x113+4x114|26:3x44+11x45|26:17x21+4x22|26:9x13+16x14",
            "20|6,34,62,90|28:3x107+5x108|26:3x41+13x42|30:15x24+5x25|28:15x15+10x16",
            "21|6,28,50,72,94|28:4x116+4x117|26:17x42|28:17x22+6x23|30:19x16+6x17",
            "22|6,26,50,74,98|28:2x111+7x112|28:17x46|30:7x24+16x25|24:34x13",
            "23|6,30,54,78,102|30:4x121+5x122|28:4x47+14x48|30:11x24+14x25|30:16x15+14x16",
            "24|6,28,54,80,106|30:6x117+4x118|28:6x45+14x46|30:11x24+16x25|30:30x16+2x17",
            "25|6,32,58,84,110|26:8x106+4x107|28:8x47+13x48|30:7x24+22x25|30:22x15+13x16",
            "26|6,30,58,86,114|28:10x114+2x115|28:19x46+4x47|28:28x22+6x23|30:33x16+4x17",
            "27|6,34,62,90,118|30:8x122+4x123|28:22x45+3x46|30:8x23+26x24|30:12x15+28x16",
            "28|6,26,50,74,98,122|30:3x117+10x118|28:3x45+23x46|30:4x24+31x25|30:11x15+31x16",
            "29|6,30,54,78,102,126|30:7x116+7x117|28:21x45+7x46|30:1x23+37x24|30:19x15+26x16",
            "30|6,26,52,78,104,130|30:5x115+10x116|28:19x47+10x48|30:15x24+25x25|30:23x15+25x16",
            "31|6,30,56,82,108,134|30:13x115+3x116|28:2x46+29x47|30:42x24+1x25|30:23x15+28x16",
            "32|6,34,60,86,112,138|30:17x115|28:10x46+23x47|30:10x24+35x25|30:19x15+35x16",
            "33|6,30,58,86,114,142|30:17x115+1x116|28:14x46+21x47|30:29x24+19x25|30:11x15+46x16",
            "34|6,34,62,90,118,146|30:13x115+6x116|28:14x46+23x47|30:44x24+7x25|30:59x16+1x17",
            "35|6,30,54,78,102,126,150|30:12x121+7x122|28:12x47+26x48|30:39x24+14x25|30:22x15+41x16",
            "36|6,24,50,76,102,128,154|30:6x121+14x122|28:6x47+34x48|30:46x24+10x25|30:2x15+64x16",
            "37|6,28,54,80,106,132,158|30:17x122+4x123|28:29x46+14x47|30:49x24+10x25|30:24x15+46x16",
            "38|6,32,58,84,110,136,162|30:4x122+18x123|28:13x46+32x47|30:48x24+14x25|30:42x15+32x16",
            "39|6,26,54,82,110,138,166|30:20x117+4x118|28:40x47+7x48|30:43x24+22x25|30:10x15+67x16",
            "40|6,30,58,86,114,142,170|30:19x118+6x119|28:18x47+31x48|30:34x24+34x25|30:20x15+61x16",
        )

        private val versions: Array<QrVersion> by lazy {
            Array(40) { index ->
                val fields = TABLE[index].split('|')
                val alignment = if (fields[1].isEmpty()) IntArray(0) else fields[1].split(',').map { it.toInt() }.toIntArray()
                val levels = Array(4) { level ->
                    val (perBlock, groups) = fields[2 + level].split(':')
                    val data = groups.split('+').flatMap { group ->
                        val (count, length) = group.split('x').map { it.toInt() }
                        List(count) { length }
                    }
                    perBlock.toInt() to data.toIntArray()
                }
                QrVersion(fields[0].toInt(), alignment, levels)
            }
        }

        fun of(version: Int): QrVersion = versions[version - 1]
    }
}

internal enum class QrMode(val indicator: Int, private val countBits: IntArray) {
    NUMERIC(0b0001, intArrayOf(10, 12, 14)),
    ALPHANUMERIC(0b0010, intArrayOf(9, 11, 13)),
    BYTE(0b0100, intArrayOf(8, 16, 16)),
    ;

    fun characterCountBits(version: Int): Int = countBits[if (version <= 9) 0 else if (version <= 26) 1 else 2]
}

internal const val ALPHANUMERIC_CHARSET = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ $%*+-./:"

internal class QrSegment(val mode: QrMode, val data: ByteArray) {
    fun bitLength(version: Int): Int {
        val n = data.size
        val payload = when (mode) {
            QrMode.NUMERIC -> n / 3 * 10 + intArrayOf(0, 4, 7)[n % 3]
            QrMode.ALPHANUMERIC -> n / 2 * 11 + (n % 2) * 6
            QrMode.BYTE -> n * 8
        }
        return 4 + mode.characterCountBits(version) + payload
    }
}

/**
 * Splits bytes into segments with the fewest total bits.
 *
 * Dynamic programming over modes: `cost[m]` is the cheapest encoding (in sixths of a bit) of the
 * characters so far with the last one in mode `m`. Per character, numeric costs 10/3 bits,
 * alphanumeric 11/2 and byte 8; changing mode rounds the finished segment up to whole bits and
 * pays the new segment header. The result is exact, since segment lengths round the same way.
 */
internal object Segmentation {
    private const val INFINITY = Int.MAX_VALUE / 2
    private val MODES = QrMode.entries
    private val CHAR_COST = intArrayOf(20, 33, 48)

    private fun QrMode.accepts(b: Byte): Boolean = when (this) {
        QrMode.NUMERIC -> b in '0'.code.toByte()..'9'.code.toByte()
        QrMode.ALPHANUMERIC -> b >= 0 && ALPHANUMERIC_CHARSET.indexOf(b.toInt().toChar()) >= 0
        QrMode.BYTE -> true
    }

    private fun roundUp(sixths: Int) = (sixths + 5) / 6 * 6

    fun optimal(bytes: ByteArray, version: Int): List<QrSegment> {
        if (bytes.isEmpty()) return emptyList()
        val count = MODES.size
        val headCost = IntArray(count) { (4 + MODES[it].characterCountBits(version)) * 6 }
        // previousMode[i][m]: mode of character i - 1 on the cheapest path encoding character i in mode m
        val previousMode = Array(bytes.size) { IntArray(count) { -1 } }
        var cost = IntArray(count) { m -> if (MODES[m].accepts(bytes[0])) headCost[m] + CHAR_COST[m] else INFINITY }
        for (i in 1 until bytes.size) {
            val next = IntArray(count) { INFINITY }
            for (m in 0 until count) {
                if (!MODES[m].accepts(bytes[i])) continue
                for (k in 0 until count) {
                    if (cost[k] >= INFINITY) continue
                    val before = if (k == m) cost[k] else roundUp(cost[k]) + headCost[m]
                    val total = before + CHAR_COST[m]
                    if (total < next[m] || (total == next[m] && k == m)) {
                        next[m] = total
                        previousMode[i][m] = k
                    }
                }
            }
            cost = next
        }
        var mode = (0 until count).minBy { if (cost[it] >= INFINITY) INFINITY else roundUp(cost[it]) }
        val modes = IntArray(bytes.size)
        for (i in bytes.indices.reversed()) {
            modes[i] = mode
            if (i > 0) mode = previousMode[i][mode]
        }
        val segments = mutableListOf<QrSegment>()
        var start = 0
        for (i in 1..bytes.size) {
            if (i == bytes.size || modes[i] != modes[start]) {
                segments += QrSegment(MODES[modes[start]], bytes.copyOfRange(start, i))
                start = i
            }
        }
        return segments
    }
}

/** Builds the data codewords: segments, terminator, padding to a byte, then pad codewords 0xEC 0x11. */
internal object QrBits {
    fun dataCodewords(segments: List<QrSegment>, version: Int, capacity: Int): IntArray {
        val bits = BitBuffer()
        for (segment in segments) {
            bits.append(segment.mode.indicator, 4)
            bits.append(segment.data.size, segment.mode.characterCountBits(version))
            val data = segment.data
            when (segment.mode) {
                QrMode.NUMERIC -> {
                    var i = 0
                    while (i < data.size) {
                        val n = minOf(3, data.size - i)
                        var value = 0
                        for (j in 0 until n) value = value * 10 + (data[i + j] - '0'.code.toByte())
                        bits.append(value, intArrayOf(0, 4, 7, 10)[n])
                        i += n
                    }
                }
                QrMode.ALPHANUMERIC -> {
                    var i = 0
                    while (i < data.size) {
                        val first = ALPHANUMERIC_CHARSET.indexOf(data[i].toInt().toChar())
                        if (i + 1 < data.size) {
                            bits.append(first * 45 + ALPHANUMERIC_CHARSET.indexOf(data[i + 1].toInt().toChar()), 11)
                        } else {
                            bits.append(first, 6)
                        }
                        i += 2
                    }
                }
                QrMode.BYTE -> for (b in data) bits.append(b.toInt() and 0xFF, 8)
            }
        }
        val capacityBits = capacity * 8
        bits.append(0, minOf(4, capacityBits - bits.size))
        bits.append(0, (8 - bits.size % 8) % 8)
        val result = IntArray(capacity)
        val used = bits.size / 8
        for (i in 0 until used) result[i] = bits.byteAt(i)
        for (i in used until capacity) result[i] = if ((i - used) % 2 == 0) 0xEC else 0x11
        return result
    }

    private class BitBuffer {
        private val bits = ArrayList<Boolean>()
        val size: Int get() = bits.size

        fun append(value: Int, length: Int) {
            for (i in length - 1 downTo 0) bits += (value ushr i) and 1 == 1
        }

        fun byteAt(index: Int): Int {
            var value = 0
            for (i in 0 until 8) value = (value shl 1) or (if (bits[index * 8 + i]) 1 else 0)
            return value
        }
    }
}

/** Reed-Solomon error correction over GF(256) with the QR polynomial x^8 + x^4 + x^3 + x^2 + 1. */
internal object QrErrorCorrectionCoding {
    private const val PRIMITIVE = 0x11D
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

    /** Coefficients of (x - 2^0)(x - 2^1)...(x - 2^(n-1)), highest degree first, leading 1 omitted. */
    private fun generator(n: Int): IntArray = generators.getOrPut(n) {
        var poly = intArrayOf(1)
        for (i in 0 until n) {
            val root = EXP[i]
            val next = IntArray(poly.size + 1)
            for (j in poly.indices) {
                next[j] = next[j] xor poly[j]
                next[j + 1] = next[j + 1] xor multiply(poly[j], root)
            }
            poly = next
        }
        poly.copyOfRange(1, poly.size)
    }

    fun encodeBlock(data: IntArray, errorCodewords: Int): IntArray {
        val gen = generator(errorCodewords)
        val remainder = IntArray(errorCodewords)
        for (d in data) {
            val factor = d xor remainder[0]
            for (j in 0 until errorCodewords - 1) remainder[j] = remainder[j + 1] xor multiply(factor, gen[j])
            remainder[errorCodewords - 1] = multiply(factor, gen[errorCodewords - 1])
        }
        return remainder
    }

    /** Splits [data] into blocks, adds error correction and interleaves data, then error correction. */
    fun interleave(data: IntArray, version: QrVersion, level: QrErrorCorrection): IntArray {
        val lengths = version.blockDataCodewords(level)
        val ecLength = version.errorCodewordsPerBlock(level)
        val blocks = ArrayList<IntArray>(lengths.size)
        var offset = 0
        for (length in lengths) {
            blocks += data.copyOfRange(offset, offset + length)
            offset += length
        }
        val ecBlocks = blocks.map { encodeBlock(it, ecLength) }
        val result = IntArray(data.size + ecLength * blocks.size)
        var index = 0
        for (i in 0 until lengths.max()) {
            for (block in blocks) if (i < block.size) result[index++] = block[i]
        }
        for (i in 0 until ecLength) {
            for (block in ecBlocks) result[index++] = block[i]
        }
        return result
    }
}

/** The module matrix: function patterns, codeword placement, masking and format information. */
internal class QrMatrix(private val version: QrVersion) {
    val size: Int = version.size
    val modules = BooleanArray(size * size)
    private val isFunction = BooleanArray(size * size)

    init {
        for (i in 0 until size) {
            setFunction(6, i, i % 2 == 0)
            setFunction(i, 6, i % 2 == 0)
        }
        drawFinder(3, 3)
        drawFinder(size - 4, 3)
        drawFinder(3, size - 4)
        val positions = version.alignmentPositions
        val last = positions.size - 1
        for (i in positions.indices) {
            for (j in positions.indices) {
                if ((i == 0 && j == 0) || (i == 0 && j == last) || (i == last && j == 0)) continue
                drawAlignment(positions[i], positions[j])
            }
        }
        drawFormat(0, QrErrorCorrection.M) // reserves the area; rewritten when the mask is applied
        drawVersion()
    }

    private fun setFunction(x: Int, y: Int, dark: Boolean) {
        modules[y * size + x] = dark
        isFunction[y * size + x] = true
    }

    private fun drawFinder(cx: Int, cy: Int) {
        for (dy in -4..4) {
            for (dx in -4..4) {
                val x = cx + dx
                val y = cy + dy
                if (x !in 0 until size || y !in 0 until size) continue
                val distance = maxOf(kotlin.math.abs(dx), kotlin.math.abs(dy))
                setFunction(x, y, distance != 2 && distance != 4)
            }
        }
    }

    private fun drawAlignment(cx: Int, cy: Int) {
        for (dy in -2..2) {
            for (dx in -2..2) setFunction(cx + dx, cy + dy, maxOf(kotlin.math.abs(dx), kotlin.math.abs(dy)) != 1)
        }
    }

    private fun drawFormat(mask: Int, level: QrErrorCorrection) {
        val data = level.formatBits shl 3 or mask
        var remainder = data
        repeat(10) { remainder = (remainder shl 1) xor ((remainder ushr 9) * 0x537) }
        val bits = (data shl 10 or remainder) xor 0x5412
        fun bit(i: Int) = (bits ushr i) and 1 == 1
        for (i in 0..5) setFunction(8, i, bit(i))
        setFunction(8, 7, bit(6))
        setFunction(8, 8, bit(7))
        setFunction(7, 8, bit(8))
        for (i in 9 until 15) setFunction(14 - i, 8, bit(i))
        for (i in 0 until 8) setFunction(size - 1 - i, 8, bit(i))
        for (i in 8 until 15) setFunction(8, size - 15 + i, bit(i))
        setFunction(8, size - 8, true) // dark module
    }

    private fun drawVersion() {
        if (version.number < 7) return
        var remainder = version.number
        repeat(12) { remainder = (remainder shl 1) xor ((remainder ushr 11) * 0x1F25) }
        val bits = version.number shl 12 or remainder
        for (i in 0 until 18) {
            val dark = (bits ushr i) and 1 == 1
            val a = size - 11 + i % 3
            val b = i / 3
            setFunction(a, b, dark)
            setFunction(b, a, dark)
        }
    }

    /** Places the codeword bits in the two-column zigzag, from the bottom right corner. */
    fun placeCodewords(codewords: IntArray) {
        var index = 0
        val total = codewords.size * 8
        var right = size - 1
        while (right >= 1) {
            if (right == 6) right = 5
            val upward = ((right + 1) and 2) == 0
            for (vertical in 0 until size) {
                val y = if (upward) size - 1 - vertical else vertical
                for (j in 0..1) {
                    val x = right - j
                    if (!isFunction[y * size + x] && index < total) {
                        modules[y * size + x] = (codewords[index ushr 3] ushr (7 - (index and 7))) and 1 == 1
                        index++
                    }
                }
            }
            right -= 2
        }
    }

    private fun maskBit(mask: Int, x: Int, y: Int): Boolean = when (mask) {
        0 -> (x + y) % 2 == 0
        1 -> y % 2 == 0
        2 -> x % 3 == 0
        3 -> (x + y) % 3 == 0
        4 -> (x / 3 + y / 2) % 2 == 0
        5 -> x * y % 2 + x * y % 3 == 0
        6 -> (x * y % 2 + x * y % 3) % 2 == 0
        else -> ((x + y) % 2 + x * y % 3) % 2 == 0
    }

    /** XORs the mask over the data modules (applying it twice removes it) and writes the format information. */
    fun applyMask(mask: Int, level: QrErrorCorrection) {
        for (y in 0 until size) {
            for (x in 0 until size) {
                val i = y * size + x
                if (!isFunction[i] && maskBit(mask, x, y)) modules[i] = !modules[i]
            }
        }
        drawFormat(mask, level)
    }

    fun penaltyWithMask(mask: Int, level: QrErrorCorrection): Int {
        applyMask(mask, level)
        val penalty = QrPenalty.of(modules, size)
        applyMask(mask, level)
        return penalty
    }
}

/**
 * Mask penalty rules N1-N4 (ISO/IEC 18004 section 7.8.3).
 *
 * N3 counts a finder-like pattern only when its four light modules lie inside the symbol.
 */
internal object QrPenalty {
    fun of(m: BooleanArray, size: Int): Int = rule1(m, size, true) + rule1(m, size, false) + rule2(m, size) + rule3(m, size) + rule4(m)

    private fun rule1(m: BooleanArray, size: Int, horizontal: Boolean): Int {
        var penalty = 0
        for (i in 0 until size) {
            var run = 0
            var previous = false
            for (j in 0 until size) {
                val bit = if (horizontal) m[i * size + j] else m[j * size + i]
                if (j > 0 && bit == previous) {
                    run++
                } else {
                    if (run >= 5) penalty += 3 + (run - 5)
                    run = 1
                    previous = bit
                }
            }
            if (run >= 5) penalty += 3 + (run - 5)
        }
        return penalty
    }

    private fun rule2(m: BooleanArray, size: Int): Int {
        var penalty = 0
        for (y in 0 until size - 1) {
            for (x in 0 until size - 1) {
                val v = m[y * size + x]
                if (v == m[y * size + x + 1] && v == m[(y + 1) * size + x] && v == m[(y + 1) * size + x + 1]) penalty += 3
            }
        }
        return penalty
    }

    private fun rule3(m: BooleanArray, size: Int): Int {
        var count = 0
        fun h(y: Int, x: Int) = m[y * size + x]
        fun v(x: Int, y: Int) = m[y * size + x]
        // Four light modules on one side, inside the symbol
        fun lightRun(get: (Int) -> Boolean, from: Int, to: Int): Boolean {
            if (from < 0 || to > size) return false
            for (k in from until to) if (get(k)) return false
            return true
        }
        for (a in 0 until size) {
            for (b in 0 until size) {
                if (b + 6 < size &&
                    h(a, b) && !h(a, b + 1) && h(a, b + 2) && h(a, b + 3) && h(a, b + 4) && !h(a, b + 5) && h(a, b + 6) &&
                    (lightRun({ h(a, it) }, b - 4, b) || lightRun({ h(a, it) }, b + 7, b + 11))
                ) count++
                if (b + 6 < size &&
                    v(a, b) && !v(a, b + 1) && v(a, b + 2) && v(a, b + 3) && v(a, b + 4) && !v(a, b + 5) && v(a, b + 6) &&
                    (lightRun({ v(a, it) }, b - 4, b) || lightRun({ v(a, it) }, b + 7, b + 11))
                ) count++
            }
        }
        return count * 40
    }

    private fun rule4(m: BooleanArray): Int {
        val dark = m.count { it }
        return kotlin.math.abs(dark * 2 - m.size) * 10 / m.size * 10
    }
}
