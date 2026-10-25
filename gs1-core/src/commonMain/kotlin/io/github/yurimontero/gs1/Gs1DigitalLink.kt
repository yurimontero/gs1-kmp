// Generation and parsing follow the GS1 Syntax Engine (https://github.com/gs1/gs1-syntax-engine, dl.c).
// Copyright (c) 2022-2025 GS1 AISBL. Licensed under the Apache License, Version 2.0.

package io.github.yurimontero.gs1

/**
 * GS1 Digital Link URIs: GS1 data as a web address, e.g.
 * `https://id.gs1.org/01/09501101020917/10/ABC123?17=261231`.
 *
 * The primary key (e.g. GTIN) and its qualifiers (e.g. batch, serial) go in the path; other AIs that
 * GS1 allows as attributes go in the query string.
 */
public object Gs1DigitalLink {

    /** GS1's canonical domain. Brands normally use their own, e.g. `https://example.com`. */
    public const val CANONICAL_DOMAIN: String = "https://id.gs1.org"

    private const val URI_CHARACTERS =
        "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-._~:/?#[]@!$&'()*+,;=%"
    private const val BAD_DOMAIN_CHARACTERS = "_~?#@!$&'()*+,;=%"
    private val ZERO_SUPPRESSED_GTIN_LENGTHS = setOf(8, 12, 13)
    private val SCHEMES = listOf("https://", "http://", "HTTPS://", "HTTP://")

    /**
     * Builds the GS1 Digital Link URI for [message].
     *
     * The first primary key AI of the message (in element order) goes first in the path, followed by
     * the longest allowed sequence of its qualifiers present in the message. The remaining AIs become
     * query parameters, predefined-length AIs first.
     *
     * @param domain scheme, domain and optional path prefix, e.g. `https://example.com` or `https://example.com/gs1`.
     * An uppercase domain such as `HTTPS://EXAMPLE.COM` gives a smaller QR Code.
     * @throws Gs1Exception if there is no primary key or an AI cannot be a query parameter.
     */
    public fun toUri(message: Gs1Message, domain: String = CANONICAL_DOMAIN): String {
        require(SCHEMES.any { domain.startsWith(it) }) {
            "Domain must start with https://, http://, HTTPS:// or HTTP://, was \"$domain\""
        }
        val elements = message.elements
        val key = elements.firstOrNull { it.ai.isDigitalLinkPrimaryKey }
            ?: throw Gs1Exception(Gs1Error(Gs1ErrorCode.DIGITAL_LINK_NO_PRIMARY_KEY, null, null, 0, 0))

        val byCode = elements.associateBy { it.ai.code }
        var qualifiers = emptyList<Gs1Element>()
        for (sequence in key.ai.digitalLinkQualifiers) {
            val present = sequence.mapNotNull { byCode[it] }
            if (present.size > qualifiers.size) qualifiers = present
        }
        val path = listOf(key) + qualifiers
        val inPath = path.map { it.ai.code }.toSet()
        val attributes = elements.filter { it.ai.code !in inPath }
        attributes.firstOrNull { !it.ai.isDigitalLinkAttribute }?.let {
            throw Gs1Exception(Gs1Error(Gs1ErrorCode.DIGITAL_LINK_INVALID_ATTRIBUTE, it.ai.code, it.value, 0, 0))
        }
        val query = attributes.filter { it.ai.isPredefinedLength } + attributes.filter { !it.ai.isPredefinedLength }

        return buildString {
            append(domain.removeSuffix("/"))
            for (element in path) append('/').append(element.ai.code).append('/').append(escape(element.value, query = false))
            query.forEachIndexed { index, element ->
                append(if (index == 0) '?' else '&')
                append(element.ai.code).append('=').append(escape(element.value, query = true))
            }
        }
    }

    /**
     * Parses a GS1 Digital Link URI into a [Gs1Message] (path elements first, then query parameters).
     *
     * Accepts an optional `]Q1` symbology identifier prefix as sent by barcode readers. Query parameters
     * that are not AIs (e.g. `?utm_source=x`) are ignored; the fragment is ignored.
     *
     * @param permitZeroSuppressedGtin accept a GTIN-8, GTIN-12 or GTIN-13 for AI (01) and pad it
     * to 14 digits (legacy URIs; the standard requires 14 digits).
     * @throws Gs1Exception if the URI is not a valid GS1 Digital Link URI or the data is invalid.
     */
    public fun parse(uri: String, permitZeroSuppressedGtin: Boolean = false): Gs1Message {
        val text = uri.removePrefix("]Q1")
        if (text.any { it !in URI_CHARACTERS }) throw malformed()
        val afterScheme = when {
            text.startsWith("https://") || text.startsWith("HTTPS://") -> text.substring(8)
            text.startsWith("http://") || text.startsWith("HTTP://") -> text.substring(7)
            else -> throw malformed()
        }
        val slash = afterScheme.indexOf('/')
        if (slash < 1) throw malformed()
        if (afterScheme.substring(0, slash).any { it in BAD_DOMAIN_CHARACTERS }) throw malformed()

        val rest = afterScheme.substring(slash).substringBefore('#')
        val pathInfo = rest.substringBefore('?')
        val queryInfo = if ('?' in rest) rest.substringAfter('?') else ""

        // Search backwards for "/AI/value" pairs until the primary key is found
        val segments = pathInfo.substring(1).split('/')
        var start = -1
        var i = segments.size - 2
        while (i >= 0) {
            val ai = findAi(segments[i]) ?: break
            if (ai.isDigitalLinkPrimaryKey) {
                start = i
                break
            }
            i -= 2
        }
        if (start < 0) throw Gs1Exception(Gs1Error(Gs1ErrorCode.DIGITAL_LINK_NO_PRIMARY_KEY, null, null, 0, 0))

        val pairs = mutableListOf<Pair<ApplicationIdentifier, String>>()
        for (j in start until segments.size step 2) {
            val ai = findAi(segments[j])!!
            pairs += ai to unescape(segments[j + 1], query = false).padGtin(ai, permitZeroSuppressedGtin)
        }
        val pathCodes = pairs.map { it.first.code }

        for (parameter in queryInfo.split('&')) {
            val name = parameter.substringBefore('=', missingDelimiterValue = "")
            if (name.isEmpty() || !name.all { it.isAsciiDigit() }) continue // singletons and non-AI parameters are ignored
            val ai = ApplicationIdentifier.find(name)
                ?: throw Gs1Exception(Gs1Error(Gs1ErrorCode.UNKNOWN_AI, name, null, 0, 0))
            pairs += ai to unescape(parameter.substringAfter('='), query = true).padGtin(ai, permitZeroSuppressedGtin)
        }

        pairs.firstNotNullOfOrNull { (ai, value) -> ai.validate(value) }?.let { throw Gs1Exception(it) }

        if (!isValidPath(pathCodes)) {
            throw Gs1Exception(Gs1Error(Gs1ErrorCode.DIGITAL_LINK_INVALID_QUALIFIERS, pathCodes.drop(1).joinToString(","), null, 0, 0))
        }
        pairs.groupBy { it.first.code }.values.firstOrNull { it.size > 1 }?.let { duplicates ->
            val (ai, value) = duplicates[1]
            throw Gs1Exception(Gs1Error(Gs1ErrorCode.DUPLICATE_AI, ai.code, value, 0, 0))
        }
        for ((ai, value) in pairs.drop(pathCodes.size)) {
            if (!ai.isDigitalLinkAttribute) {
                throw Gs1Exception(Gs1Error(Gs1ErrorCode.DIGITAL_LINK_INVALID_ATTRIBUTE, ai.code, value, 0, 0))
            }
            for (position in 1..pathCodes.size) {
                val trial = pathCodes.subList(0, position) + ai.code + pathCodes.subList(position, pathCodes.size)
                if (isValidPath(trial)) {
                    throw Gs1Exception(Gs1Error(Gs1ErrorCode.DIGITAL_LINK_AI_SHOULD_BE_IN_PATH, ai.code, value, 0, 0))
                }
            }
        }
        return Gs1Message.of(pairs.map { it.first.code to it.second }, Gs1Order.AS_GIVEN)
    }

    private fun String.padGtin(ai: ApplicationIdentifier, permit: Boolean): String =
        if (permit && ai.code == Gs1Ai.GTIN && length in ZERO_SUPPRESSED_GTIN_LENGTHS) padStart(14, '0') else this

    private fun findAi(segment: String): ApplicationIdentifier? =
        if (segment.length in 2..4 && segment.all { it.isAsciiDigit() }) ApplicationIdentifier.find(segment) else null

    /** Primary key followed by qualifiers that keep the order of one of its allowed sequences. */
    private fun isValidPath(codes: List<String>): Boolean {
        val key = ApplicationIdentifier.find(codes.first()) ?: return false
        if (!key.isDigitalLinkPrimaryKey) return false
        val qualifiers = codes.drop(1)
        if (qualifiers.isEmpty()) return true
        return key.digitalLinkQualifiers.any { sequence ->
            var next = 0
            qualifiers.all { code ->
                val index = sequence.indexOf(code)
                (index >= next).also { if (it) next = index + 1 }
            }
        }
    }

    private const val HEX = "0123456789ABCDEF"

    private fun Char.isUnreserved() = this in 'A'..'Z' || this in 'a'..'z' || this in '0'..'9' || this in "-._~"

    internal fun escape(value: String, query: Boolean): String = buildString {
        for (c in value) {
            when {
                c.isUnreserved() -> append(c)
                c == ' ' && query -> append('+')
                else -> {
                    val code = c.code and 0xFF
                    append('%').append(HEX[code shr 4]).append(HEX[code and 0x0F])
                }
            }
        }
    }

    internal fun unescape(value: String, query: Boolean): String = buildString {
        var i = 0
        while (i < value.length) {
            val c = value[i]
            if (c == '%' && i + 2 < value.length) {
                val hi = HEX.indexOf(value[i + 1].uppercaseChar())
                val lo = HEX.indexOf(value[i + 2].uppercaseChar())
                if (hi >= 0 && lo >= 0) {
                    val decoded = (hi shl 4) or lo
                    if (decoded == 0) throw malformed()
                    append(decoded.toChar())
                    i += 3
                    continue
                }
                append('%')
            } else if (c == '+' && query) {
                append(' ')
            } else {
                append(c)
            }
            i++
        }
    }

    private fun malformed() = Gs1Exception(Gs1Error(Gs1ErrorCode.DIGITAL_LINK_MALFORMED_URI, null, null, 0, 0))
}

/** The GS1 Digital Link URI of this message. See [Gs1DigitalLink.toUri]. */
public fun Gs1Message.toDigitalLinkUri(domain: String = Gs1DigitalLink.CANONICAL_DOMAIN): String =
    Gs1DigitalLink.toUri(this, domain)
