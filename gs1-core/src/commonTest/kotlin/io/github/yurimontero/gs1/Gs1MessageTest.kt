package io.github.yurimontero.gs1

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class Gs1MessageTest {

    private val gs = Gs1Message.GROUP_SEPARATOR

    @Test
    fun buildsHealthcareExample() {
        // Example 2 of the GS1 DataMatrix Guideline
        val message = Gs1Message.of("01" to "09501101020917", "17" to "190508", "10" to "ABCD1234", "21" to "10")
        assertEquals("(01)09501101020917(17)190508(10)ABCD1234(21)10", message.humanReadable)
        assertEquals(
            listOf(Gs1Token.Fnc1, Gs1Token.Data("01095011010209171719050810ABCD1234"), Gs1Token.Fnc1, Gs1Token.Data("2110")),
            message.tokens,
        )
        assertEquals("]d201095011010209171719050810ABCD1234${gs}2110", message.toElementString(symbologyIdentifier = "]d2"))
    }

    @Test
    fun noSeparatorWhenVariableFieldIsLast() {
        val message = Gs1Message.of("01" to "03453120000011", "17" to "191125", "10" to "ABCD1234")
        assertEquals(listOf(Gs1Token.Fnc1, Gs1Token.Data("0103453120000011171911251" + "0ABCD1234")), message.tokens)
    }

    @Test
    fun optimizedOrderPutsPredefinedLengthFirst() {
        val message = Gs1Message.of("10" to "LOT1", "21" to "SN1", "01" to "09501101020917", "17" to "261231")
        assertEquals(listOf("01", "17", "10", "21"), message.elements.map { it.ai.code })
        assertEquals(
            listOf(Gs1Token.Fnc1, Gs1Token.Data("01095011010209171726123110LOT1"), Gs1Token.Fnc1, Gs1Token.Data("21SN1")),
            message.tokens,
        )
    }

    @Test
    fun asGivenOrderKeepsInput() {
        val message = Gs1Message.of("10" to "LOT1", "01" to "09501101020917", order = Gs1Order.AS_GIVEN)
        assertEquals(listOf("10", "01"), message.elements.map { it.ai.code })
        assertEquals(
            listOf(Gs1Token.Fnc1, Gs1Token.Data("10LOT1"), Gs1Token.Fnc1, Gs1Token.Data("0109501101020917")),
            message.tokens,
        )
    }

    @Test
    fun fixedLengthAiOutsidePredefinedTableNeedsSeparator() {
        // AI 422 is always 3 digits but is not in the predefined-length table
        val message = Gs1Message.of("422" to "724", "10" to "LOT1", order = Gs1Order.AS_GIVEN)
        assertEquals(listOf(Gs1Token.Fnc1, Gs1Token.Data("422724"), Gs1Token.Fnc1, Gs1Token.Data("10LOT1")), message.tokens)
    }

    @Test
    fun parsesBracketedText() {
        val message = Gs1Message.parse("(01)09501101020917(17)190508(10)ABCD1234(21)10")
        assertEquals("ABCD1234", message["10"])
        assertEquals("10", message["21"])
        assertEquals("(01)09501101020917(17)190508(10)ABCD1234(21)10", message.humanReadable)
    }

    @Test
    fun parsesEscapedParenthesis() {
        val message = Gs1Message.parse("(10)AB\\(1)(21)X")
        assertEquals("AB(1)", message["10"])
    }

    @Test
    fun parsesScannerOutput() {
        val message = Gs1Message.parseElementString("]d201034531200000111719112510ABCD1234${gs}2110")
        assertEquals("(01)03453120000011(17)191125(10)ABCD1234(21)10", message.humanReadable)
    }

    @Test
    fun parsesScannerOutputWithCustomSeparatorAndNoIdentifier() {
        val message = Gs1Message.parseElementString("0103453120000011~10LOT~2110", separator = '~')
        assertEquals("LOT", message["10"])
        assertEquals("10", message["21"])
    }

    @Test
    fun roundTripsThroughElementString() {
        val original = Gs1Message.of("00" to "106141411234567897", "02" to "09501101020917", "37" to "24", "10" to "LOT")
        val parsed = Gs1Message.parseElementString(original.toElementString(symbologyIdentifier = "]C1"))
        assertEquals(original, parsed)
    }

    @Test
    fun rejectsNonGs1Symbology() {
        val e = assertFailsWith<Gs1Exception> { Gs1Message.parseElementString("]d10109501101020917") }
        assertEquals(Gs1ErrorCode.NOT_GS1_SYMBOLOGY, e.error.code)
    }

    @Test
    fun rejectsUnknownAi() {
        val e = assertFailsWith<Gs1Exception> { Gs1Message.of("04" to "123") }
        assertEquals(Gs1ErrorCode.UNKNOWN_AI, e.error.code)
    }

    @Test
    fun rejectsInvalidValueWithDetails() {
        val e = assertFailsWith<Gs1Exception> { Gs1Message.of("01" to "09501101020918") }
        assertEquals(Gs1ErrorCode.INCORRECT_CHECK_DIGIT, e.error.code)
        assertEquals("01", e.error.ai)
        assertEquals(13, e.error.position)
        assertEquals("AI (01): incorrect check digit at \"8\" (position 13)", e.message)
    }

    @Test
    fun rejectsDuplicateAiWithDifferentValues() {
        val e = assertFailsWith<Gs1Exception> { Gs1Message.of("10" to "A", "10" to "B") }
        assertEquals(Gs1ErrorCode.DUPLICATE_AI, e.error.code)
    }

    @Test
    fun dropsIdenticalDuplicates() {
        assertEquals(1, Gs1Message.of("10" to "A", "10" to "A").elements.size)
    }

    @Test
    fun rejectsEmptyAndMalformedInput() {
        assertEquals(Gs1ErrorCode.EMPTY_MESSAGE, assertFailsWith<Gs1Exception> { Gs1Message.of() }.error.code)
        assertEquals(Gs1ErrorCode.MALFORMED_INPUT, assertFailsWith<Gs1Exception> { Gs1Message.parse("01123") }.error.code)
        assertEquals(Gs1ErrorCode.MALFORMED_INPUT, assertFailsWith<Gs1Exception> { Gs1Message.parse("(01") }.error.code)
    }

    @Test
    fun rejectsTruncatedScannerData() {
        val e = assertFailsWith<Gs1Exception> { Gs1Message.parseElementString("010950110102") }
        assertEquals(Gs1ErrorCode.VALUE_TOO_SHORT, e.error.code)
    }
}
