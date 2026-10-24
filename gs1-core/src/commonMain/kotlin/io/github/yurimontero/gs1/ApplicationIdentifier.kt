package io.github.yurimontero.gs1

/** Character sets used by GS1 AI data. */
public enum class Gs1CharacterSet {
    /** Digits 0-9 ("N"). */
    NUMERIC,

    /** GS1 AI encodable character set 82 ("X"). */
    CSET82,

    /** GS1 AI encodable character set 39 ("Y"). */
    CSET39,

    /** File-safe base64 ("Z"). */
    CSET64,
}

/** One part of an AI's data field, e.g. the 14 digits of a GTIN. */
public class AiComponent internal constructor(
    public val characterSet: Gs1CharacterSet,
    public val minLength: Int,
    public val maxLength: Int,
    public val isOptional: Boolean,
    /** Names of the GS1 Barcode Syntax Tests applied to this component, e.g. `csum` or `yymmd0`. */
    public val linters: List<String>,
) {
    public val isFixedLength: Boolean get() = minLength == maxLength
}

/**
 * A GS1 Application Identifier, as defined by the GS1 Barcode Syntax Dictionary.
 *
 * Look one up with [find]; [all] lists every AI known to this library.
 */
public class ApplicationIdentifier internal constructor(
    /** The AI digits, e.g. `"01"` or `"3103"`. */
    public val code: String,
    /** GS1 data title, e.g. `"GTIN"` or `"BATCH/LOT"`. */
    public val title: String,
    /**
     * `true` when the AI belongs to GS1's table of predefined-length AIs. Only these may be
     * followed by another element string without an FNC1 separator.
     */
    public val isPredefinedLength: Boolean,
    public val components: List<AiComponent>,
) {
    /** Minimum length of the data field. */
    public val minLength: Int = components.filter { !it.isOptional }.sumOf { it.minLength }

    /** Maximum length of the data field. */
    public val maxLength: Int = components.sumOf { it.maxLength }

    /** `true` when an FNC1 separator must follow this element string if another one comes after it. */
    public val requiresSeparator: Boolean get() = !isPredefinedLength

    /** Validates [value] as the data field of this AI. Returns `null` when it is valid. */
    public fun validate(value: String): Gs1Error? {
        fun error(code: Gs1ErrorCode, position: Int, length: Int) = Gs1Error(code, this.code, value, position, length)

        if (value.isEmpty()) return error(Gs1ErrorCode.EMPTY_VALUE, 0, 0)
        if (value.length < minLength) return error(Gs1ErrorCode.VALUE_TOO_SHORT, 0, value.length)
        if (value.length > maxLength) return error(Gs1ErrorCode.VALUE_TOO_LONG, 0, value.length)

        var start = 0
        for (component in components) {
            val length = minOf(value.length - start, component.maxLength)
            if (component.isOptional && length == 0) continue
            if (length < component.minLength) return error(Gs1ErrorCode.INVALID_COMPONENT_LENGTH, start, length)

            val part = value.substring(start, start + length)
            val charsetLinter = when (component.characterSet) {
                Gs1CharacterSet.NUMERIC -> Linters::csetNumeric
                Gs1CharacterSet.CSET82 -> Linters::cset82
                Gs1CharacterSet.CSET39 -> Linters::cset39
                Gs1CharacterSet.CSET64 -> Linters::cset64
            }
            charsetLinter(part)?.let { return error(it.code, start + it.position, it.length) }
            for (name in component.linters) {
                val linter = Linters.forName(name) ?: continue
                linter(part)?.let { return error(it.code, start + it.position, it.length) }
            }
            start += length
        }
        if (start < value.length) return error(Gs1ErrorCode.VALUE_TOO_LONG, start, value.length - start)
        return null
    }

    override fun equals(other: Any?): Boolean = other is ApplicationIdentifier && other.code == code
    override fun hashCode(): Int = code.hashCode()
    override fun toString(): String = "($code) $title"

    public companion object {
        private val byCode: Map<String, ApplicationIdentifier> by lazy {
            buildMap { AI_DICTIONARY.forEach { line -> parseDictionaryLine(line).forEach { put(it.code, it) } } }
        }

        /** Every AI known to this library, sorted by code. */
        public val all: List<ApplicationIdentifier> by lazy { byCode.values.sortedBy { it.code } }

        /** Returns the AI with this [code] (e.g. `"01"`), or `null` if it does not exist. */
        public fun find(code: String): ApplicationIdentifier? = byCode[code]

        /** Finds the AI that starts at [start] in [data]. GS1 AIs are prefix-free, so the match is unique. */
        internal fun matchPrefix(data: String, start: Int): ApplicationIdentifier? {
            for (length in 2..4) {
                if (start + length > data.length) return null
                byCode[data.substring(start, start + length)]?.let { return it }
            }
            return null
        }

        private fun parseDictionaryLine(line: String): List<ApplicationIdentifier> {
            val (codes, flags, spec, title) = line.split('|')
            val components = spec.split(' ').map(::parseComponent)
            val predefined = flags.contains('*')
            val range = codes.split('-')
            if (range.size == 1) return listOf(ApplicationIdentifier(codes, title, predefined, components))
            val first = range[0]
            val last = range[1]
            return (first.toInt()..last.toInt()).map { number ->
                ApplicationIdentifier(number.toString().padStart(first.length, '0'), title, predefined, components)
            }
        }

        private fun parseComponent(token: String): AiComponent {
            val parts = token.split(',')
            var type = parts[0]
            val optional = type.startsWith('[')
            if (optional) type = type.removePrefix("[").removeSuffix("]")
            val charset = when (type[0]) {
                'N' -> Gs1CharacterSet.NUMERIC
                'X' -> Gs1CharacterSet.CSET82
                'Y' -> Gs1CharacterSet.CSET39
                'Z' -> Gs1CharacterSet.CSET64
                else -> error("Unknown character set in $token")
            }
            val length = type.substring(1)
            val (min, max) = if (length.startsWith("..")) {
                1 to length.removePrefix("..").toInt()
            } else {
                length.toInt() to length.toInt()
            }
            return AiComponent(charset, min, max, optional, parts.drop(1))
        }
    }
}
