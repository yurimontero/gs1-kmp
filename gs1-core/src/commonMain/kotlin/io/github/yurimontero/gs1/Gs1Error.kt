package io.github.yurimontero.gs1

/**
 * Why GS1 data was rejected.
 *
 * Codes from [NON_DIGIT_CHARACTER] onwards mirror the error codes of the GS1 Barcode Syntax Tests
 * ("linters") used by the GS1 Syntax Engine, so results can be compared with GS1's own tools.
 */
public enum class Gs1ErrorCode(public val description: String) {
    // Message structure
    EMPTY_MESSAGE("no element strings"),
    MALFORMED_INPUT("malformed input"),
    NOT_GS1_SYMBOLOGY("symbology identifier does not denote GS1 data"),
    UNKNOWN_AI("unknown Application Identifier"),
    EMPTY_VALUE("value is empty"),
    VALUE_TOO_SHORT("value is too short"),
    VALUE_TOO_LONG("value is too long"),
    INVALID_COMPONENT_LENGTH("a component of the value has an incorrect length"),
    DUPLICATE_AI("AI appears more than once with different values"),

    // Symbologies
    GS1_128_TOO_LONG("too many data characters for one GS1-128 symbol (maximum 48)"),
    DATA_MATRIX_TOO_LONG("too much data for the requested Data Matrix size"),

    // GS1 Barcode Syntax Tests
    NON_DIGIT_CHARACTER("non-digit character"),
    INVALID_CSET82_CHARACTER("character not allowed in GS1 AI data (CSET 82)"),
    INVALID_CSET39_CHARACTER("character not allowed (CSET 39)"),
    INVALID_CSET64_CHARACTER("character not allowed (CSET 64)"),
    INVALID_CSET64_PADDING("invalid CSET 64 padding"),
    INCORRECT_CHECK_DIGIT("incorrect check digit"),
    TOO_SHORT_FOR_CHECK_DIGIT("too short to contain a check digit"),
    INCORRECT_CHECK_PAIR("incorrect check character pair"),
    TOO_SHORT_FOR_CHECK_PAIR("too short to contain a check character pair"),
    TOO_LONG_FOR_CHECK_PAIR_IMPLEMENTATION("too long for check character pair calculation"),
    IMPORTER_IDX_MUST_BE_ONE_CHARACTER("importer index must be one character"),
    INVALID_IMPORT_IDX_CHARACTER("invalid importer index character"),
    ILLEGAL_ZERO_VALUE("zero value not allowed"),
    NOT_ZERO("must be zero"),
    ILLEGAL_ZERO_PREFIX("leading zero not allowed"),
    NOT_ZERO_OR_ONE("must be 0 or 1"),
    INVALID_WINDING_DIRECTION("invalid winding direction"),
    NOT_ISO3166("not an ISO 3166 numeric country code"),
    NOT_ISO3166_OR_999("not an ISO 3166 numeric country code or 999"),
    NOT_ISO3166_ALPHA2("not an ISO 3166 alpha-2 country code"),
    NOT_ISO4217("not an ISO 4217 numeric currency code"),
    IBAN_TOO_SHORT("IBAN too short"),
    IBAN_TOO_LONG("IBAN too long"),
    INVALID_IBAN_CHARACTER("invalid IBAN character"),
    ILLEGAL_IBAN_COUNTRY_CODE("invalid IBAN country code"),
    INCORRECT_IBAN_CHECKSUM("incorrect IBAN checksum"),
    DATE_TOO_SHORT("date too short"),
    DATE_TOO_LONG("date too long"),
    HOUR_WITH_MINUTE_TOO_SHORT("time too short"),
    HOUR_WITH_MINUTE_TOO_LONG("time too long"),
    ILLEGAL_MONTH("invalid month"),
    ILLEGAL_DAY("invalid day"),
    ILLEGAL_HOUR("invalid hour"),
    ILLEGAL_MINUTE("invalid minute"),
    ILLEGAL_SECOND("invalid second"),
    HOUR_TOO_SHORT("hour too short"),
    HOUR_TOO_LONG("hour too long"),
    MINUTE_TOO_SHORT("minute too short"),
    MINUTE_TOO_LONG("minute too long"),
    SECOND_TOO_SHORT("second too short"),
    SECOND_TOO_LONG("second too long"),
    INVALID_LENGTH_FOR_PIECE_OF_TOTAL("invalid length for piece of total"),
    ZERO_PIECE_NUMBER("piece number is zero"),
    ZERO_TOTAL_PIECES("total pieces is zero"),
    PIECE_NUMBER_EXCEEDS_TOTAL("piece number exceeds total"),
    INVALID_PERCENT_SEQUENCE("invalid percent-encoded sequence"),
    INVALID_LATITUDE("invalid latitude"),
    INVALID_LONGITUDE("invalid longitude"),
    LATITUDE_INVALID_LENGTH("latitude has an invalid length"),
    LONGITUDE_INVALID_LENGTH("longitude has an invalid length"),
    INVALID_MEDIA_TYPE("invalid AIDC media type"),
    NOT_HYPHEN("must be a hyphen"),
    INVALID_BIOLOGICAL_SEX_CODE("invalid biological sex code"),
    POSITION_IN_SEQUENCE_MALFORMED("malformed position in sequence"),
    POSITION_EXCEEDS_END("position exceeds end of sequence"),
    REQUIRES_NON_DIGIT_CHARACTER("must contain a non-digit character"),
    INVALID_PACKAGE_TYPE("invalid package type"),
}

/**
 * A validation failure.
 *
 * @property ai the Application Identifier concerned, or `null` for message-level errors.
 * @property position offset of the offending characters: within [value] when [ai] is set,
 * otherwise within the input that was parsed.
 * @property length number of offending characters (may be 0).
 */
public class Gs1Error internal constructor(
    public val code: Gs1ErrorCode,
    public val ai: String?,
    public val value: String?,
    public val position: Int,
    public val length: Int,
) {
    public val message: String
        get() {
            val start = position
            val end = position + length
            return buildString {
                if (ai != null) append("AI (").append(ai).append("): ")
                append(code.description)
                if (code == Gs1ErrorCode.GS1_128_TOO_LONG) {
                    append(": ").append(length).append(" data characters")
                } else if (code == Gs1ErrorCode.DATA_MATRIX_TOO_LONG) {
                    append(": ").append(length).append(" data codewords")
                } else if (value != null && end > start && end <= value.length) {
                    append(" at \"").appendRange(value, start, end).append("\" (position ").append(start).append(')')
                } else if (ai == null && end > start) {
                    append(" (position ").append(start).append(')')
                }
            }
        }

    override fun toString(): String = message
}

/** Thrown when GS1 data cannot be built or parsed. */
public class Gs1Exception(public val error: Gs1Error) : IllegalArgumentException(error.message)
