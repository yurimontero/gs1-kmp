package io.github.yurimontero.gs1

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class Gs1MessageBuilderTest {

    @Test
    fun constantsWorkWithOf() {
        val message = Gs1Message.of(Gs1Ai.GTIN to "09501101020917", Gs1Ai.BATCH_LOT to "ABC123")
        assertEquals("(01)09501101020917(10)ABC123", message.humanReadable)
    }

    @Test
    fun constantsMatchTheAiTable() {
        assertEquals("GTIN", ApplicationIdentifier.find(Gs1Ai.GTIN)?.title)
        assertEquals("BATCH/LOT", ApplicationIdentifier.find(Gs1Ai.BATCH_LOT)?.title)
        assertEquals("USE BY or EXPIRY", ApplicationIdentifier.find(Gs1Ai.EXPIRY_DATE)?.title)
        assertEquals("SERIAL", ApplicationIdentifier.find(Gs1Ai.SERIAL)?.title)
        assertEquals("3103", Gs1Ai.netWeightKg(3))
        assertEquals("NET WEIGHT (kg)", ApplicationIdentifier.find(Gs1Ai.netWeightKg(3))?.title)
        assertEquals("GROSS WEIGHT (kg)", ApplicationIdentifier.find(Gs1Ai.grossWeightKg(0))?.title)
        assertFailsWith<IllegalArgumentException> { Gs1Ai.netWeightKg(6) }
    }

    @Test
    fun everyConstantIsAKnownAi() {
        val codes = listOf(
            Gs1Ai.SSCC, Gs1Ai.GTIN, Gs1Ai.CONTENT, Gs1Ai.MTO_GTIN, Gs1Ai.GDTI, Gs1Ai.GCN, Gs1Ai.GINC, Gs1Ai.GSIN,
            Gs1Ai.LOCATION_GLN, Gs1Ai.PARTY_GLN, Gs1Ai.GRAI, Gs1Ai.GIAI, Gs1Ai.ITIP, Gs1Ai.GMN, Gs1Ai.GSRN_PROVIDER,
            Gs1Ai.GSRN_RECIPIENT, Gs1Ai.BATCH_LOT, Gs1Ai.SERIAL, Gs1Ai.CPV, Gs1Ai.TPX, Gs1Ai.VARIANT, Gs1Ai.ADDITIONAL_ID,
            Gs1Ai.CUSTOMER_PART_NUMBER, Gs1Ai.PCN, Gs1Ai.SECONDARY_SERIAL, Gs1Ai.REFERENCE_TO_SOURCE, Gs1Ai.SOFTWARE_VERSION,
            Gs1Ai.EXPIRY_DATE_TIME, Gs1Ai.PRODUCTION_DATE_TIME, Gs1Ai.PRODUCTION_DATE, Gs1Ai.DUE_DATE, Gs1Ai.PACKAGING_DATE,
            Gs1Ai.BEST_BEFORE_DATE, Gs1Ai.SELL_BY_DATE, Gs1Ai.EXPIRY_DATE, Gs1Ai.VARIABLE_COUNT, Gs1Ai.COUNT,
            Gs1Ai.ORDER_NUMBER, Gs1Ai.ROUTE, Gs1Ai.SHIP_TO_GLN, Gs1Ai.BILL_TO_GLN, Gs1Ai.PURCHASED_FROM_GLN,
            Gs1Ai.SHIP_FOR_GLN, Gs1Ai.PRODUCTION_LOCATION_GLN, Gs1Ai.SHIP_TO_POSTAL_CODE, Gs1Ai.ORIGIN_COUNTRY,
            Gs1Ai.PAYMENT_SLIP_REFERENCE, Gs1Ai.INTERNAL, Gs1Ai.NHRN_PZN, Gs1Ai.NHRN_CIP, Gs1Ai.NHRN_CN,
            Gs1Ai.NHRN_DRN, Gs1Ai.NHRN_AIM,
            Gs1Ai.netWeightKg(0), Gs1Ai.netWeightLb(5), Gs1Ai.lengthM(1), Gs1Ai.widthM(2), Gs1Ai.heightM(3),
            Gs1Ai.areaM2(4), Gs1Ai.netVolumeL(1), Gs1Ai.netVolumeM3(2), Gs1Ai.grossWeightKg(3),
            Gs1Ai.logisticLengthM(1), Gs1Ai.logisticWidthM(1), Gs1Ai.logisticHeightM(1), Gs1Ai.logisticVolumeL(1),
            Gs1Ai.logisticVolumeM3(1), Gs1Ai.amountPayable(2), Gs1Ai.amountPayableWithCurrency(2), Gs1Ai.price(2),
            Gs1Ai.priceWithCurrency(9),
        )
        codes.forEach { assertEquals(it, ApplicationIdentifier.find(it)?.code, "Unknown AI $it") }
    }

    @Test
    fun parameterisedAisMapToTheirRange() {
        assertEquals("7230", Gs1Ai.certification(1))
        assertEquals("7239", Gs1Ai.certification(10))
        assertEquals("7030", Gs1Ai.processorApproval(0))
        assertEquals("91", Gs1Ai.companyInternal(1))
        assertEquals("99", Gs1Ai.companyInternal(9))
        assertEquals("3943", Gs1Ai.percentageDiscount(3))
        assertEquals("NET VOLUME (gal.)", ApplicationIdentifier.find(Gs1Ai.netVolumeGal(2))?.title)
        assertFailsWith<IllegalArgumentException> { Gs1Ai.certification(0) }
        assertFailsWith<IllegalArgumentException> { Gs1Ai.percentageDiscount(4) }
    }

    @Test
    fun lessCommonAisHaveConstants() {
        assertEquals("IMEI", ApplicationIdentifier.find(Gs1Ai.IMEI)?.title)
        assertEquals("DOB", ApplicationIdentifier.find(Gs1Ai.DATE_OF_BIRTH)?.title)
        assertEquals("MAX TEMP C.", ApplicationIdentifier.find(Gs1Ai.MAX_TEMPERATURE_C)?.title)
        assertEquals("SHIP TO ADD1", ApplicationIdentifier.find(Gs1Ai.SHIP_TO_ADDRESS_LINE_1)?.title)
        assertEquals("NHRN NDC", ApplicationIdentifier.find(Gs1Ai.NHRN_NDC)?.title)
    }

    @Test
    fun buildsWithNamedFunctions() {
        val message = Gs1Message.build {
            gtin("09501101020917")
            batch("ABC123")
            expiryDate(2026, 12, 31)
            serial("SN1")
        }
        assertEquals("(01)09501101020917(17)261231(10)ABC123(21)SN1", message.humanReadable)
    }

    @Test
    fun padsShortGtins() {
        val message = Gs1Message.build { gtin("4006381333931") }
        assertEquals("04006381333931", message[Gs1Ai.GTIN])
    }

    @Test
    fun formatsDates() {
        val message = Gs1Message.build(Gs1Order.AS_GIVEN) {
            productionDate(2025, 3, 7)
            bestBeforeDate(2027, 2, 0)
            expiryDateTime(2026, 12, 31, 9, 5)
        }
        assertEquals("250307", message[Gs1Ai.PRODUCTION_DATE])
        assertEquals("270200", message[Gs1Ai.BEST_BEFORE_DATE])
        assertEquals("2612310905", message[Gs1Ai.EXPIRY_DATE_TIME])
        assertFailsWith<IllegalArgumentException> { Gs1Message.build { expiryDate(2026, 13, 1) } }
    }

    @Test
    fun invalidDayIsReportedByValidation() {
        val e = assertFailsWith<Gs1Exception> { Gs1Message.build { expiryDate(2026, 2, 30) } }
        assertEquals(Gs1ErrorCode.ILLEGAL_DAY, e.error.code)
    }

    @Test
    fun measuresChooseTheAiFromTheDecimals() {
        val message = Gs1Message.build(Gs1Order.AS_GIVEN) {
            netWeightKg("1.250")
            grossWeightKg("12.5")
            netVolumeL(0.75, decimals = 2)
            lengthM("2")
        }
        assertEquals(listOf("3103", "3301", "3152", "3110"), message.elements.map { it.ai.code })
        assertEquals("001250", message["3103"])
        assertEquals("000125", message["3301"])
        assertEquals("000075", message["3152"])
        assertEquals("000002", message["3110"])
    }

    @Test
    fun rejectsMeasuresThatDoNotFit() {
        assertEquals(
            Gs1ErrorCode.VALUE_TOO_LONG,
            assertFailsWith<Gs1Exception> { Gs1Message.build { netWeightKg("1234.567") } }.error.code,
        )
        assertEquals(
            Gs1ErrorCode.MALFORMED_INPUT,
            assertFailsWith<Gs1Exception> { Gs1Message.build { netWeightKg("1,5") } }.error.code,
        )
        assertFailsWith<IllegalArgumentException> { Gs1Message.build { netWeightKg("1.123456") } }
    }

    @Test
    fun pricesWithAndWithoutCurrency() {
        val local = Gs1Message.build { gtin("09501101020917"); price("4.95") }
        assertEquals("495", local["3922"])
        val euro = Gs1Message.build { gtin("09501101020917"); price("4.95", currency = "978") }
        assertEquals("978495", euro["3932"])
        val e = assertFailsWith<Gs1Exception> { Gs1Message.build { price("4.95", currency = "123") } }
        assertEquals(Gs1ErrorCode.NOT_ISO4217, e.error.code)
    }

    @Test
    fun logisticUnit() {
        val message = Gs1Message.build {
            sscc("106141411234567897")
            content("09501101020917")
            count(24)
            originCountry(724)
            orderNumber("PO-1001")
        }
        assertEquals("(00)106141411234567897(02)09501101020917(37)24(422)724(400)PO-1001", message.humanReadable)
    }

    @Test
    fun addAcceptsAnyAi() {
        val message = Gs1Message.build { gtin("09501101020917"); add("7003", "2612311430") }
        assertEquals("2612311430", message["7003"])
    }
}
