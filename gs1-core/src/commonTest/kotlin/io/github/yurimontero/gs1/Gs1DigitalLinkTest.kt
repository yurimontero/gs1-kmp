package io.github.yurimontero.gs1

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class Gs1DigitalLinkTest {

    @Test
    fun buildsPathFromKeyAndQualifiers() {
        val message = Gs1Message.of(Gs1Ai.GTIN to "35789835863696", Gs1Ai.BATCH_LOT to "1234")
        assertEquals("https://subdomain.domain/01/35789835863696/10/1234", message.toDigitalLinkUri("https://subdomain.domain"))
    }

    @Test
    fun usesCanonicalDomainByDefault() {
        val message = Gs1Message.of(Gs1Ai.GTIN to "09501101020917")
        assertEquals("https://id.gs1.org/01/09501101020917", Gs1DigitalLink.toUri(message))
    }

    @Test
    fun putsAttributesInTheQueryString() {
        val message = Gs1Message.build {
            gtin("09501101020917")
            batch("AB/1%")
            expiryDate(2026, 12, 31)
            serial("SN1")
            add("99", "Q&R=")
        }
        assertEquals(
            "https://example.com/01/09501101020917/10/AB%2F1%25/21/SN1?17=261231&99=Q%26R%3D",
            message.toDigitalLinkUri("https://example.com/"),
        )
    }

    @Test
    fun keepsDomainPathPrefix() {
        val message = Gs1Message.of(Gs1Ai.SSCC to "106141411234567897")
        assertEquals("https://example.com/gs1/00/106141411234567897", message.toDigitalLinkUri("https://example.com/gs1"))
    }

    @Test
    fun rejectsDomainWithoutScheme() {
        val message = Gs1Message.of(Gs1Ai.GTIN to "09501101020917")
        assertFailsWith<IllegalArgumentException> { message.toDigitalLinkUri("example.com") }
    }

    @Test
    fun rejectsMessagesWithoutPrimaryKey() {
        val e = assertFailsWith<Gs1Exception> { Gs1Message.of(Gs1Ai.BATCH_LOT to "ABC").toDigitalLinkUri() }
        assertEquals(Gs1ErrorCode.DIGITAL_LINK_NO_PRIMARY_KEY, e.error.code)
    }

    @Test
    fun parsesUserExample() {
        val message = Gs1DigitalLink.parse("https://subdomain.domain/01/35789835863696/10/1234")
        assertEquals("(01)35789835863696(10)1234", message.humanReadable)
    }

    @Test
    fun parsesScannerOutputWithSymbologyIdentifier() {
        val message = Gs1DigitalLink.parse("]Q1https://id.gs1.org/01/09501101020917?17=261231")
        assertEquals("261231", message[Gs1Ai.EXPIRY_DATE])
    }

    @Test
    fun parseReportsInvalidData() {
        val e = assertFailsWith<Gs1Exception> { Gs1DigitalLink.parse("https://id.gs1.org/01/09501101020910") }
        assertEquals(Gs1ErrorCode.INCORRECT_CHECK_DIGIT, e.error.code)
    }

    @Test
    fun roundTrips() {
        val message = Gs1Message.parse("(01)09501101020917(22)2A(10)A+B/C(21)12%(3103)000195(17)261231(99)Q?R", Gs1Order.AS_GIVEN)
        val uri = message.toDigitalLinkUri("https://example.com")
        assertEquals("https://example.com/01/09501101020917/22/2A/10/A%2BB%2FC/21/12%25?3103=000195&17=261231&99=Q%3FR", uri)
        val parsed = Gs1DigitalLink.parse(uri)
        assertEquals(message.elements.toSet(), parsed.elements.toSet())
    }

    @Test
    fun plusDecodesToSpaceOnlyInTheQueryString() {
        assertEquals("A+B", Gs1DigitalLink.parse("https://a/01/09501101020917/10/A+B")[Gs1Ai.BATCH_LOT])
        assertFailsWith<Gs1Exception> { Gs1DigitalLink.parse("https://a/01/09501101020917?99=A+B") }
    }

    @Test
    fun ignoresNonAiQueryParametersAndFragment() {
        val message = Gs1DigitalLink.parse("https://a/01/09501101020917?utm_source=x&17=261231&flag#top")
        assertEquals("(01)09501101020917(17)261231", message.humanReadable)
    }

    @Test
    fun aiFlagsComeFromTheDictionary() {
        val gtin = ApplicationIdentifier.find(Gs1Ai.GTIN)!!
        assertEquals(true, gtin.isDigitalLinkPrimaryKey)
        assertEquals(listOf(listOf("22", "10", "21"), listOf("235")), gtin.digitalLinkQualifiers)
        assertEquals(true, ApplicationIdentifier.find(Gs1Ai.EXPIRY_DATE)!!.isDigitalLinkAttribute)
        assertEquals(false, ApplicationIdentifier.find("8200")!!.isDigitalLinkAttribute)
        assertEquals(false, ApplicationIdentifier.find(Gs1Ai.BATCH_LOT)!!.isDigitalLinkPrimaryKey)
    }
}
