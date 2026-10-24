package io.github.yurimontero.gs1

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class Gs1CheckDigitTest {

    @Test
    fun computesCheckDigitForGtin14() {
        // Example from the GS1 DataMatrix Guideline
        assertEquals(1, Gs1CheckDigit.compute("0345312000001"))
        assertEquals(7, Gs1CheckDigit.compute("0950110102091"))
    }

    @Test
    fun validatesKeysOfEveryLength() {
        listOf(
            "96385074",           // GTIN-8
            "036000291452",       // GTIN-12
            "4006381333931",      // GTIN-13
            "03453120000011",     // GTIN-14
            "106141411234567897", // SSCC
        ).forEach { assertTrue(Gs1CheckDigit.isValid(it), "Expected valid: $it") }
    }

    @Test
    fun rejectsWrongCheckDigit() {
        assertFalse(Gs1CheckDigit.isValid("03453120000012"))
    }

    @Test
    fun rejectsNonDigitsAndTooShortKeys() {
        assertFalse(Gs1CheckDigit.isValid("0345312000001A"))
        assertFalse(Gs1CheckDigit.isValid("1"))
        assertFalse(Gs1CheckDigit.isValid(""))
    }

    @Test
    fun appendsCheckDigit() {
        assertEquals("03453120000011", Gs1CheckDigit.append("0345312000001"))
    }

    @Test
    fun computeRejectsInvalidBody() {
        assertFailsWith<IllegalArgumentException> { Gs1CheckDigit.compute("") }
        assertFailsWith<IllegalArgumentException> { Gs1CheckDigit.compute("12A4") }
    }
}
