package io.github.yurimontero.gs1

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ApplicationIdentifierTest {

    @Test
    fun findsCommonAis() {
        val gtin = assertNotNull(ApplicationIdentifier.find("01"))
        assertEquals("GTIN", gtin.title)
        assertTrue(gtin.isPredefinedLength)
        assertEquals(14, gtin.minLength)
        assertEquals(14, gtin.maxLength)

        val batch = assertNotNull(ApplicationIdentifier.find("10"))
        assertFalse(batch.isPredefinedLength)
        assertEquals(1, batch.minLength)
        assertEquals(20, batch.maxLength)
    }

    @Test
    fun expandsRanges() {
        assertNotNull(ApplicationIdentifier.find("3100"))
        assertNotNull(ApplicationIdentifier.find("3105"))
        assertNull(ApplicationIdentifier.find("3106"))
        assertNotNull(ApplicationIdentifier.find("91"))
        assertNotNull(ApplicationIdentifier.find("99"))
    }

    @Test
    fun unknownAiIsNull() {
        assertNull(ApplicationIdentifier.find("04"))
        assertNull(ApplicationIdentifier.find("1"))
    }

    @Test
    fun predefinedLengthMatchesGs1Table() {
        // Every predefined-length AI starts with one of these prefixes (GS1 General Specifications, figure 7.8.5-2)
        val prefixes = setOf("00", "01", "02", "03", "04", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20",
            "31", "32", "33", "34", "35", "36", "41")
        ApplicationIdentifier.all.filter { it.isPredefinedLength }.forEach {
            assertTrue(it.code.take(2) in prefixes, "Unexpected predefined-length AI ${it.code}")
            assertEquals(it.minLength, it.maxLength, "Predefined-length AI ${it.code} must be fixed length")
        }
    }

    @Test
    fun aiTableIsPrefixFree() {
        val codes = ApplicationIdentifier.all.map { it.code }
        for (a in codes) for (b in codes) {
            if (a != b) assertFalse(b.startsWith(a), "AI $a is a prefix of $b")
        }
    }

    @Test
    fun validatesGtin() {
        assertNull(ApplicationIdentifier.find("01")!!.validate("09501101020917"))
        val error = assertNotNull(ApplicationIdentifier.find("01")!!.validate("09501101020918"))
        assertEquals(Gs1ErrorCode.INCORRECT_CHECK_DIGIT, error.code)
        assertEquals(13, error.position)
    }

    @Test
    fun validatesLengths() {
        assertEquals(Gs1ErrorCode.VALUE_TOO_SHORT, ApplicationIdentifier.find("01")!!.validate("123")?.code)
        assertEquals(Gs1ErrorCode.VALUE_TOO_LONG, ApplicationIdentifier.find("10")!!.validate("A".repeat(21))?.code)
        assertEquals(Gs1ErrorCode.EMPTY_VALUE, ApplicationIdentifier.find("10")!!.validate("")?.code)
    }

    @Test
    fun validatesDates() {
        val expiry = ApplicationIdentifier.find("17")!!
        assertNull(expiry.validate("261231"))
        assertNull(expiry.validate("261200")) // day 00 = end of month
        assertEquals(Gs1ErrorCode.ILLEGAL_MONTH, expiry.validate("261331")?.code)
        assertEquals(Gs1ErrorCode.ILLEGAL_DAY, expiry.validate("250229")?.code)
        assertNull(expiry.validate("240229"))
    }

    @Test
    fun validatesCharacterSet() {
        val error = assertNotNull(ApplicationIdentifier.find("10")!!.validate("AB CD"))
        assertEquals(Gs1ErrorCode.INVALID_CSET82_CHARACTER, error.code)
        assertEquals(2, error.position)
    }

    @Test
    fun validatesComponentsWithOffsets() {
        // AI 8003 GRAI: N1 zero, N13 with check digit, optional serial
        val grai = ApplicationIdentifier.find("8003")!!
        assertNull(grai.validate("00950110153007"))
        assertNull(grai.validate("00950110153007ABC123"))
        val error = assertNotNull(grai.validate("00950110153008"))
        assertEquals(Gs1ErrorCode.INCORRECT_CHECK_DIGIT, error.code)
        assertEquals(13, error.position)
        assertEquals(Gs1ErrorCode.NOT_ZERO, grai.validate("10950110153007")?.code)
    }

    @Test
    fun validatesCountryCodes() {
        val origin = ApplicationIdentifier.find("422")!!
        assertNull(origin.validate("724"))
        assertEquals(Gs1ErrorCode.NOT_ISO3166, origin.validate("999")?.code)
    }
}
