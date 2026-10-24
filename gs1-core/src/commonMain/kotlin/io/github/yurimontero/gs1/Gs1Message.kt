package io.github.yurimontero.gs1

/** An AI and its validated data field. */
public class Gs1Element internal constructor(
    public val ai: ApplicationIdentifier,
    public val value: String,
) {
    /** Human readable form, e.g. `(01)09501101020917`. */
    public val humanReadable: String get() = "(${ai.code})$value"

    override fun equals(other: Any?): Boolean = other is Gs1Element && other.ai == ai && other.value == value
    override fun hashCode(): Int = 31 * ai.hashCode() + value.hashCode()
    override fun toString(): String = humanReadable
}

/** What a GS1 symbol carries: FNC1 function characters and runs of data characters. */
public sealed interface Gs1Token {
    /** Function 1 Symbol Character: first in every GS1 symbol, and the separator after variable-length fields. */
    public data object Fnc1 : Gs1Token

    /** Data characters (AI digits and values), never containing FNC1. */
    public class Data(public val text: String) : Gs1Token {
        override fun equals(other: Any?): Boolean = other is Data && other.text == text
        override fun hashCode(): Int = text.hashCode()
        override fun toString(): String = text
    }
}

/** Order in which element strings are encoded. */
public enum class Gs1Order {
    /** Keep the order given. */
    AS_GIVEN,

    /**
     * Predefined-length AIs first, then the rest, each group in the order given.
     * This needs the fewest FNC1 separators, which keeps symbols small.
     */
    OPTIMIZED,
}

/**
 * Validated GS1 data ready to be encoded in a GS1-128 or GS1 DataMatrix symbol.
 *
 * ```
 * val message = Gs1Message.of("01" to "09501101020917", "17" to "190508", "10" to "ABCD1234", "21" to "10")
 * message.humanReadable   // (01)09501101020917(17)190508(10)ABCD1234(21)10
 * message.tokens          // [Fnc1, 01095011010209171719050810ABCD1234, Fnc1, 2110]
 * ```
 */
public class Gs1Message private constructor(
    /** Element strings in encoding order. */
    public val elements: List<Gs1Element>,
) {
    /**
     * The symbol content: a leading [Gs1Token.Fnc1], the data, and an FNC1 separator after every
     * element string that is not predefined-length, except the last one.
     */
    public val tokens: List<Gs1Token> by lazy {
        val result = mutableListOf<Gs1Token>(Gs1Token.Fnc1)
        val run = StringBuilder()
        elements.forEachIndexed { index, element ->
            run.append(element.ai.code).append(element.value)
            if (index < elements.lastIndex && element.ai.requiresSeparator) {
                result += Gs1Token.Data(run.toString())
                result += Gs1Token.Fnc1
                run.clear()
            }
        }
        if (run.isNotEmpty()) result += Gs1Token.Data(run.toString())
        result
    }

    /** Human readable interpretation, e.g. `(01)09501101020917(10)ABCD1234`. */
    public val humanReadable: String get() = elements.joinToString("") { it.humanReadable }

    /**
     * The data as a barcode reader transmits it: separators as [separator] (ASCII GS by default),
     * no leading FNC1, optionally prefixed with a [symbologyIdentifier] such as `]d2` or `]C1`.
     */
    public fun toElementString(separator: Char = GROUP_SEPARATOR, symbologyIdentifier: String = ""): String =
        buildString {
            append(symbologyIdentifier)
            tokens.forEachIndexed { index, token ->
                when (token) {
                    is Gs1Token.Fnc1 -> if (index > 0) append(separator)
                    is Gs1Token.Data -> append(token.text)
                }
            }
        }

    /** Returns the value of the first element with this AI [code], or `null`. */
    public operator fun get(code: String): String? = elements.firstOrNull { it.ai.code == code }?.value

    override fun equals(other: Any?): Boolean = other is Gs1Message && other.elements == elements
    override fun hashCode(): Int = elements.hashCode()
    override fun toString(): String = humanReadable

    public companion object {
        /** ASCII Group Separator, the transmitted value of FNC1 used as a separator. */
        public const val GROUP_SEPARATOR: Char = '\u001D'

        /** Symbology identifiers that denote GS1 AI data. */
        private val GS1_SYMBOLOGY_IDENTIFIERS = setOf("]C1", "]d2", "]Q3", "]e0", "]J1")

        /**
         * Builds a message from AI/value pairs, e.g. `"01" to "09501101020917"`.
         *
         * @throws Gs1Exception if an AI is unknown, a value is invalid, or an AI is repeated with different values.
         */
        public fun of(vararg elements: Pair<String, String>, order: Gs1Order = Gs1Order.OPTIMIZED): Gs1Message =
            of(elements.asList(), order)

        /** List variant of [of]. */
        public fun of(elements: List<Pair<String, String>>, order: Gs1Order = Gs1Order.OPTIMIZED): Gs1Message {
            if (elements.isEmpty()) throw Gs1Exception(Gs1Error(Gs1ErrorCode.EMPTY_MESSAGE, null, null, 0, 0))
            val validated = mutableListOf<Gs1Element>()
            for ((code, value) in elements) {
                val ai = ApplicationIdentifier.find(code)
                    ?: throw Gs1Exception(Gs1Error(Gs1ErrorCode.UNKNOWN_AI, code, null, 0, 0))
                ai.validate(value)?.let { throw Gs1Exception(it) }
                val previous = validated.firstOrNull { it.ai == ai }
                if (previous == null) {
                    validated += Gs1Element(ai, value)
                } else if (previous.value != value) {
                    throw Gs1Exception(Gs1Error(Gs1ErrorCode.DUPLICATE_AI, code, value, 0, 0))
                }
            }
            val ordered = when (order) {
                Gs1Order.AS_GIVEN -> validated
                Gs1Order.OPTIMIZED -> validated.filter { it.ai.isPredefinedLength } + validated.filter { !it.ai.isPredefinedLength }
            }
            return Gs1Message(ordered)
        }

        /**
         * Parses bracketed AI syntax, e.g. `(01)09501101020917(17)190508(10)ABCD1234`.
         *
         * A literal `(` inside a value must be written as `\(`.
         *
         * @throws Gs1Exception if the text is malformed or the data is invalid.
         */
        public fun parse(text: String, order: Gs1Order = Gs1Order.OPTIMIZED): Gs1Message {
            if (text.isEmpty()) throw Gs1Exception(Gs1Error(Gs1ErrorCode.EMPTY_MESSAGE, null, null, 0, 0))
            val pairs = mutableListOf<Pair<String, String>>()
            var i = 0
            while (i < text.length) {
                if (text[i] != '(') throw malformed(i)
                val close = text.indexOf(')', i + 1)
                if (close < 0) throw malformed(i)
                val code = text.substring(i + 1, close)
                if (code.length !in 2..4 || !code.all { it.isAsciiDigit() } || ApplicationIdentifier.find(code) == null) {
                    throw Gs1Exception(Gs1Error(Gs1ErrorCode.UNKNOWN_AI, code, null, 0, 0))
                }
                val value = StringBuilder()
                var j = close + 1
                while (j < text.length && text[j] != '(') {
                    if (text[j] == '\\' && j + 1 < text.length && text[j + 1] == '(') {
                        value.append('(')
                        j += 2
                    } else {
                        value.append(text[j])
                        j++
                    }
                }
                pairs += code to value.toString()
                i = j
            }
            return of(pairs, order)
        }

        /**
         * Parses data as transmitted by a barcode reader, e.g. `]d20109501101020917\u001D2110`.
         *
         * Accepts an optional GS1 symbology identifier (`]C1`, `]d2`, `]Q3`, `]e0`, `]J1`) and an
         * optional leading separator. The element order is kept.
         *
         * @throws Gs1Exception if the data is not GS1 data or is invalid.
         */
        public fun parseElementString(data: String, separator: Char = GROUP_SEPARATOR): Gs1Message {
            var start = 0
            if (data.startsWith(']')) {
                if (data.length < 3 || data.substring(0, 3) !in GS1_SYMBOLOGY_IDENTIFIERS) {
                    throw Gs1Exception(Gs1Error(Gs1ErrorCode.NOT_GS1_SYMBOLOGY, null, null, 0, minOf(3, data.length)))
                }
                start = 3
            }
            if (start < data.length && data[start] == separator) start++
            if (start >= data.length) throw Gs1Exception(Gs1Error(Gs1ErrorCode.EMPTY_MESSAGE, null, null, 0, 0))

            val pairs = mutableListOf<Pair<String, String>>()
            var i = start
            while (i < data.length) {
                val ai = ApplicationIdentifier.matchPrefix(data, i)
                    ?: throw Gs1Exception(Gs1Error(Gs1ErrorCode.UNKNOWN_AI, null, null, i, minOf(4, data.length - i)))
                val valueStart = i + ai.code.length
                val value: String
                if (ai.isPredefinedLength) {
                    val end = valueStart + ai.maxLength
                    if (end > data.length) {
                        throw Gs1Exception(
                            Gs1Error(Gs1ErrorCode.VALUE_TOO_SHORT, ai.code, data.substring(valueStart), 0, data.length - valueStart),
                        )
                    }
                    value = data.substring(valueStart, end)
                    i = end
                    if (i < data.length && data[i] == separator) i++
                } else {
                    val separatorIndex = data.indexOf(separator, valueStart)
                    val end = if (separatorIndex < 0) data.length else separatorIndex
                    value = data.substring(valueStart, end)
                    i = if (separatorIndex < 0) data.length else separatorIndex + 1
                }
                pairs += ai.code to value
            }
            return of(pairs, Gs1Order.AS_GIVEN)
        }

        private fun malformed(position: Int) =
            Gs1Exception(Gs1Error(Gs1ErrorCode.MALFORMED_INPUT, null, null, position, 1))
    }
}
