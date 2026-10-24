package io.github.yurimontero.gs1.code128

import io.github.yurimontero.gs1.Gs1Error
import io.github.yurimontero.gs1.Gs1ErrorCode
import io.github.yurimontero.gs1.Gs1Exception
import io.github.yurimontero.gs1.Gs1Message
import io.github.yurimontero.gs1.Gs1Token

/**
 * GS1-128 encoder: Code 128 (ISO/IEC 15417) with FNC1 right after the start character.
 *
 * ```
 * val symbol = Gs1Code128.encode(Gs1Message.parse("(01)09501101020917(10)ABC123"))
 * symbol.widths          // bar/space widths in modules, starting with a bar
 * symbol.humanReadable   // (01)09501101020917(10)ABC123
 * ```
 */
public object Gs1Code128 {

    /** Maximum number of data characters in one GS1-128 symbol, separators included. */
    public const val MAX_DATA_CHARACTERS: Int = 48

    /** Maximum physical length of a GS1-128 symbol, quiet zones included, in millimetres. */
    public const val MAX_LENGTH_MM: Double = 165.10

    /** Minimum quiet zone on each side, in modules. */
    public const val QUIET_ZONE_MODULES: Int = 10

    /**
     * Encodes [message], choosing Code Sets C and B to produce the shortest symbol.
     *
     * @param enforceMaxDataCharacters reject messages with more than [MAX_DATA_CHARACTERS] data
     * characters, as the GS1 General Specifications require. Split such data over two symbols.
     * @throws Gs1Exception with [Gs1ErrorCode.GS1_128_TOO_LONG] if the message is too long.
     */
    public fun encode(message: Gs1Message, enforceMaxDataCharacters: Boolean = true): Code128Symbol {
        val items = toItems(message.tokens)
        val dataCharacters = items.size - 1 // the leading FNC1 is not a data character
        if (enforceMaxDataCharacters && dataCharacters > MAX_DATA_CHARACTERS) {
            throw Gs1Exception(Gs1Error(Gs1ErrorCode.GS1_128_TOO_LONG, null, null, 0, dataCharacters))
        }
        val values = encodeValues(items)
        return Code128Symbol(values, message.humanReadable, dataCharacters)
    }

    // ------------------------------------------------------------ code set selection

    private const val FNC1_ITEM = -1
    private const val SET_C = 0
    private const val SET_B = 1
    private const val INFINITY = Int.MAX_VALUE / 2

    internal const val FNC1 = 102
    internal const val CODE_C = 99
    internal const val CODE_B = 100
    internal const val START_B = 104
    internal const val START_C = 105
    internal const val STOP = 106

    private fun toItems(tokens: List<Gs1Token>): IntArray {
        val items = ArrayList<Int>()
        for (token in tokens) {
            when (token) {
                is Gs1Token.Fnc1 -> items += FNC1_ITEM
                is Gs1Token.Data -> token.text.forEach { c ->
                    require(c.code in 32..126) { "Character '$c' cannot be encoded in Code 128 set B" }
                    items += c.code
                }
            }
        }
        return items.toIntArray()
    }

    private fun isDigit(item: Int) = item in '0'.code..'9'.code

    /** Index after consuming one symbol character at [i] in set C, or -1 if impossible. */
    private fun nextInC(items: IntArray, i: Int): Int = when {
        items[i] == FNC1_ITEM -> i + 1
        i + 1 < items.size && isDigit(items[i]) && isDigit(items[i + 1]) -> i + 2
        else -> -1
    }

    /** Symbol character values: start, data (with code set switches), check, stop. */
    internal fun encodeValues(items: IntArray): List<Int> {
        val n = items.size
        // cost[i][s]: fewest symbol characters to encode items[i..] when currently in set s
        val cost = Array(n + 1) { IntArray(2) }
        val switchFirst = Array(n) { BooleanArray(2) }
        for (i in n - 1 downTo 0) {
            val nextC = nextInC(items, i)
            val directC = if (nextC >= 0) 1 + cost[nextC][SET_C] else INFINITY
            val directB = 1 + cost[i + 1][SET_B]
            if (directC <= 1 + directB) cost[i][SET_C] = directC else { cost[i][SET_C] = 1 + directB; switchFirst[i][SET_C] = true }
            if (directB <= 1 + directC) cost[i][SET_B] = directB else { cost[i][SET_B] = 1 + directC; switchFirst[i][SET_B] = true }
        }

        var set = if (cost[0][SET_C] <= cost[0][SET_B]) SET_C else SET_B
        val values = ArrayList<Int>(cost[0][set] + 3)
        values += if (set == SET_C) START_C else START_B
        var i = 0
        while (i < n) {
            if (switchFirst[i][set]) {
                values += if (set == SET_C) CODE_B else CODE_C
                set = 1 - set
            }
            if (set == SET_C) {
                if (items[i] == FNC1_ITEM) {
                    values += FNC1
                    i += 1
                } else {
                    values += (items[i] - '0'.code) * 10 + (items[i + 1] - '0'.code)
                    i += 2
                }
            } else {
                values += if (items[i] == FNC1_ITEM) FNC1 else items[i] - 32
                i += 1
            }
        }

        var checksum = values[0]
        for (position in 1 until values.size) checksum += values[position] * position
        values += checksum % 103
        values += STOP
        return values
    }
}

/**
 * An encoded GS1-128 symbol.
 *
 * Draw it from [widths] (or [toModules]) at a whole number of pixels or printer dots per module,
 * with at least [Gs1Code128.QUIET_ZONE_MODULES] light modules on each side.
 */
public class Code128Symbol internal constructor(
    /** Code 128 symbol character values: start, data, check character and stop (106). */
    public val values: List<Int>,
    /** Human readable interpretation to print near the bars, e.g. `(01)09501101020917(10)ABC123`. */
    public val humanReadable: String,
    /** GS1 data characters, separators included (the limit is [Gs1Code128.MAX_DATA_CHARACTERS]). */
    public val dataCharacterCount: Int,
) {
    /** Widths in modules of alternating bars and spaces, starting and ending with a bar. Quiet zones excluded. */
    public val widths: IntArray by lazy {
        val result = IntArray(values.size * 6 + 1)
        var index = 0
        for (value in values) {
            for (c in Code128Patterns.PATTERNS[value]) result[index++] = c - '0'
        }
        result
    }

    /** Width of the symbol in modules, quiet zones excluded. */
    public val moduleCount: Int get() = 11 * values.size + 2

    /** One entry per module, `true` for dark (bar), with [quietZone] light modules on each side. */
    public fun toModules(quietZone: Int = Gs1Code128.QUIET_ZONE_MODULES): BooleanArray {
        val modules = BooleanArray(moduleCount + 2 * quietZone)
        var x = quietZone
        widths.forEachIndexed { index, width ->
            if (index % 2 == 0) for (k in 0 until width) modules[x + k] = true
            x += width
        }
        return modules
    }

    /** Physical width for a module width of [xDimensionMm], including quiet zones. */
    public fun widthMm(xDimensionMm: Double, quietZone: Int = Gs1Code128.QUIET_ZONE_MODULES): Double =
        (moduleCount + 2 * quietZone) * xDimensionMm

    /** Largest module width (mm) that keeps the symbol within [Gs1Code128.MAX_LENGTH_MM]. */
    public fun maxXDimensionMm(quietZone: Int = Gs1Code128.QUIET_ZONE_MODULES): Double =
        Gs1Code128.MAX_LENGTH_MM / (moduleCount + 2 * quietZone)

    override fun toString(): String = "Code128Symbol($humanReadable, ${values.size} symbol characters)"
}

/** Code 128 bar/space patterns: six widths per symbol character, seven for the stop character. */
internal object Code128Patterns {
    val PATTERNS: Array<String> = arrayOf(
        "212222", "222122", "222221", "121223", "121322", "131222", "122213", "122312", "132212", "221213", // 0-9
        "221312", "231212", "112232", "122132", "122231", "113222", "123122", "123221", "223211", "221132", // 10-19
        "221231", "213212", "223112", "312131", "311222", "321122", "321221", "312212", "322112", "322211", // 20-29
        "212123", "212321", "232121", "111323", "131123", "131321", "112313", "132113", "132311", "211313", // 30-39
        "231113", "231311", "112133", "112331", "132131", "113123", "113321", "133121", "313121", "211331", // 40-49
        "231131", "213113", "213311", "213131", "311123", "311321", "331121", "312113", "312311", "332111", // 50-59
        "314111", "221411", "431111", "111224", "111422", "121124", "121421", "141122", "141221", "112214", // 60-69
        "112412", "122114", "122411", "142112", "142211", "241211", "221114", "413111", "241112", "134111", // 70-79
        "111242", "121142", "121241", "114212", "124112", "124211", "411212", "421112", "421211", "212141", // 80-89
        "214121", "412121", "111143", "111341", "131141", "114113", "114311", "411113", "411311", "113141", // 90-99
        "114131", "311141", "411131", "211412", "211214", "211232", "2331112", // 100-106
    )
}
