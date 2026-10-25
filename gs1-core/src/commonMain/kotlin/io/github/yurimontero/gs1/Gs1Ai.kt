// GENERATED FILE - DO NOT EDIT. Edit tools/gs1-ai-names.txt and run `python3 tools/generate_ai_dictionary.py`.

package io.github.yurimontero.gs1

/**
 * Codes of every GS1 Application Identifier known to the library.
 *
 * ```
 * Gs1Message.of(Gs1Ai.GTIN to "09501101020917", Gs1Ai.BATCH_LOT to "ABC123")
 * Gs1Ai.netWeightKg(3)   // "3103": AIs whose last digit is a parameter are functions
 * ```
 */
public object Gs1Ai {
    /** (00) Serial Shipping Container Code. Format: `N18,csum,gcppos2`. GS1 data title: SSCC. */
    public const val SSCC: String = "00"

    /** (01) Global Trade Item Number. Format: `N14,csum,gcppos2`. GS1 data title: GTIN. */
    public const val GTIN: String = "01"

    /** (02) GTIN of the trade items contained in a logistic unit. Format: `N14,csum,gcppos2`. GS1 data title: CONTENT. */
    public const val CONTENT: String = "02"

    /** (03) GTIN of a made-to-order trade item. Format: `N14,csum,gcppos2`. GS1 data title: MTO GTIN. */
    public const val MTO_GTIN: String = "03"

    /** (10) Batch or lot number. Format: `X..20`. GS1 data title: BATCH/LOT. */
    public const val BATCH_LOT: String = "10"

    /** (11) Production date (YYMMDD). Format: `N6,yymmd0`. GS1 data title: PROD DATE. */
    public const val PRODUCTION_DATE: String = "11"

    /** (12) Due date for amount on payment slip (YYMMDD). Format: `N6,yymmd0`. GS1 data title: DUE DATE. */
    public const val DUE_DATE: String = "12"

    /** (13) Packaging date (YYMMDD). Format: `N6,yymmd0`. GS1 data title: PACK DATE. */
    public const val PACKAGING_DATE: String = "13"

    /** (15) Best before date (YYMMDD). Format: `N6,yymmd0`. GS1 data title: BEST BEFORE or BEST BY. */
    public const val BEST_BEFORE_DATE: String = "15"

    /** (16) Sell by date (YYMMDD). Format: `N6,yymmd0`. GS1 data title: SELL BY. */
    public const val SELL_BY_DATE: String = "16"

    /** (17) Expiration date (YYMMDD). Format: `N6,yymmd0`. GS1 data title: USE BY or EXPIRY. */
    public const val EXPIRY_DATE: String = "17"

    /** (20) Internal product variant. Format: `N2`. GS1 data title: VARIANT. */
    public const val VARIANT: String = "20"

    /** (21) Serial number. Format: `X..20`. GS1 data title: SERIAL. */
    public const val SERIAL: String = "21"

    /** (22) Consumer product variant. Format: `X..20`. GS1 data title: CPV. */
    public const val CPV: String = "22"

    /** (235) Third party controlled, serialised extension of GTIN. Format: `X..28`. GS1 data title: TPX. */
    public const val TPX: String = "235"

    /** (240) Additional product identification assigned by the manufacturer. Format: `X..30`. GS1 data title: ADDITIONAL ID. */
    public const val ADDITIONAL_ID: String = "240"

    /** (241) Customer part number. Format: `X..30`. GS1 data title: CUST. PART No.. */
    public const val CUSTOMER_PART_NUMBER: String = "241"

    /** (242) Made-to-order variation number. Format: `N..6`. GS1 data title: MTO VARIANT. */
    public const val MTO_VARIANT: String = "242"

    /** (243) Packaging component number. Format: `X..20`. GS1 data title: PCN. */
    public const val PCN: String = "243"

    /** (250) Secondary serial number. Format: `X..30`. GS1 data title: SECONDARY SERIAL. */
    public const val SECONDARY_SERIAL: String = "250"

    /** (251) Reference to source entity. Format: `X..30`. GS1 data title: REF. TO SOURCE. */
    public const val REFERENCE_TO_SOURCE: String = "251"

    /** (253) Global Document Type Identifier. Format: `N13,csum,gcppos1 [X..17]`. GS1 data title: GDTI. */
    public const val GDTI: String = "253"

    /** (254) GLN extension component. Format: `X..20`. GS1 data title: GLN EXTENSION COMPONENT. */
    public const val GLN_EXTENSION: String = "254"

    /** (255) Global Coupon Number. Format: `N13,csum,gcppos1 [N..12]`. GS1 data title: GCN. */
    public const val GCN: String = "255"

    /** (30) Variable count of items. Format: `N..8`. GS1 data title: VAR. COUNT. */
    public const val VARIABLE_COUNT: String = "30"

    /**
     * (3100-3105) Net weight in kilograms. Format: `N6`. GS1 data title: NET WEIGHT (kg).
     *
     * @param decimals 0-5; netWeightKg(0) is AI 3100.
     */
    public fun netWeightKg(decimals: Int): String {
        require(decimals in 0..5) { "decimals must be 0-5, was $decimals" }
        return (3100 + decimals).toString().padStart(4, '0')
    }

    /**
     * (3110-3115) Length or first dimension in metres. Format: `N6`. GS1 data title: LENGTH (m).
     *
     * @param decimals 0-5; lengthM(0) is AI 3110.
     */
    public fun lengthM(decimals: Int): String {
        require(decimals in 0..5) { "decimals must be 0-5, was $decimals" }
        return (3110 + decimals).toString().padStart(4, '0')
    }

    /**
     * (3120-3125) Width, diameter or second dimension in metres. Format: `N6`. GS1 data title: WIDTH (m).
     *
     * @param decimals 0-5; widthM(0) is AI 3120.
     */
    public fun widthM(decimals: Int): String {
        require(decimals in 0..5) { "decimals must be 0-5, was $decimals" }
        return (3120 + decimals).toString().padStart(4, '0')
    }

    /**
     * (3130-3135) Depth, thickness, height or third dimension in metres. Format: `N6`. GS1 data title: HEIGHT (m).
     *
     * @param decimals 0-5; heightM(0) is AI 3130.
     */
    public fun heightM(decimals: Int): String {
        require(decimals in 0..5) { "decimals must be 0-5, was $decimals" }
        return (3130 + decimals).toString().padStart(4, '0')
    }

    /**
     * (3140-3145) Area in square metres. Format: `N6`. GS1 data title: AREA (m²).
     *
     * @param decimals 0-5; areaM2(0) is AI 3140.
     */
    public fun areaM2(decimals: Int): String {
        require(decimals in 0..5) { "decimals must be 0-5, was $decimals" }
        return (3140 + decimals).toString().padStart(4, '0')
    }

    /**
     * (3150-3155) Net volume in litres. Format: `N6`. GS1 data title: NET VOLUME (l).
     *
     * @param decimals 0-5; netVolumeL(0) is AI 3150.
     */
    public fun netVolumeL(decimals: Int): String {
        require(decimals in 0..5) { "decimals must be 0-5, was $decimals" }
        return (3150 + decimals).toString().padStart(4, '0')
    }

    /**
     * (3160-3165) Net volume in cubic metres. Format: `N6`. GS1 data title: NET VOLUME (m³).
     *
     * @param decimals 0-5; netVolumeM3(0) is AI 3160.
     */
    public fun netVolumeM3(decimals: Int): String {
        require(decimals in 0..5) { "decimals must be 0-5, was $decimals" }
        return (3160 + decimals).toString().padStart(4, '0')
    }

    /**
     * (3200-3205) Net weight in pounds. Format: `N6`. GS1 data title: NET WEIGHT (lb).
     *
     * @param decimals 0-5; netWeightLb(0) is AI 3200.
     */
    public fun netWeightLb(decimals: Int): String {
        require(decimals in 0..5) { "decimals must be 0-5, was $decimals" }
        return (3200 + decimals).toString().padStart(4, '0')
    }

    /**
     * (3210-3215) Length or first dimension in inches. Format: `N6`. GS1 data title: LENGTH (in).
     *
     * @param decimals 0-5; lengthIn(0) is AI 3210.
     */
    public fun lengthIn(decimals: Int): String {
        require(decimals in 0..5) { "decimals must be 0-5, was $decimals" }
        return (3210 + decimals).toString().padStart(4, '0')
    }

    /**
     * (3220-3225) Length or first dimension in feet. Format: `N6`. GS1 data title: LENGTH (ft).
     *
     * @param decimals 0-5; lengthFt(0) is AI 3220.
     */
    public fun lengthFt(decimals: Int): String {
        require(decimals in 0..5) { "decimals must be 0-5, was $decimals" }
        return (3220 + decimals).toString().padStart(4, '0')
    }

    /**
     * (3230-3235) Length or first dimension in yards. Format: `N6`. GS1 data title: LENGTH (yd).
     *
     * @param decimals 0-5; lengthYd(0) is AI 3230.
     */
    public fun lengthYd(decimals: Int): String {
        require(decimals in 0..5) { "decimals must be 0-5, was $decimals" }
        return (3230 + decimals).toString().padStart(4, '0')
    }

    /**
     * (3240-3245) Width, diameter or second dimension in inches. Format: `N6`. GS1 data title: WIDTH (in).
     *
     * @param decimals 0-5; widthIn(0) is AI 3240.
     */
    public fun widthIn(decimals: Int): String {
        require(decimals in 0..5) { "decimals must be 0-5, was $decimals" }
        return (3240 + decimals).toString().padStart(4, '0')
    }

    /**
     * (3250-3255) Width, diameter or second dimension in feet. Format: `N6`. GS1 data title: WIDTH (ft).
     *
     * @param decimals 0-5; widthFt(0) is AI 3250.
     */
    public fun widthFt(decimals: Int): String {
        require(decimals in 0..5) { "decimals must be 0-5, was $decimals" }
        return (3250 + decimals).toString().padStart(4, '0')
    }

    /**
     * (3260-3265) Width, diameter or second dimension in yards. Format: `N6`. GS1 data title: WIDTH (yd).
     *
     * @param decimals 0-5; widthYd(0) is AI 3260.
     */
    public fun widthYd(decimals: Int): String {
        require(decimals in 0..5) { "decimals must be 0-5, was $decimals" }
        return (3260 + decimals).toString().padStart(4, '0')
    }

    /**
     * (3270-3275) Depth, thickness, height or third dimension in inches. Format: `N6`. GS1 data title: HEIGHT (in).
     *
     * @param decimals 0-5; heightIn(0) is AI 3270.
     */
    public fun heightIn(decimals: Int): String {
        require(decimals in 0..5) { "decimals must be 0-5, was $decimals" }
        return (3270 + decimals).toString().padStart(4, '0')
    }

    /**
     * (3280-3285) Depth, thickness, height or third dimension in feet. Format: `N6`. GS1 data title: HEIGHT (ft).
     *
     * @param decimals 0-5; heightFt(0) is AI 3280.
     */
    public fun heightFt(decimals: Int): String {
        require(decimals in 0..5) { "decimals must be 0-5, was $decimals" }
        return (3280 + decimals).toString().padStart(4, '0')
    }

    /**
     * (3290-3295) Depth, thickness, height or third dimension in yards. Format: `N6`. GS1 data title: HEIGHT (yd).
     *
     * @param decimals 0-5; heightYd(0) is AI 3290.
     */
    public fun heightYd(decimals: Int): String {
        require(decimals in 0..5) { "decimals must be 0-5, was $decimals" }
        return (3290 + decimals).toString().padStart(4, '0')
    }

    /**
     * (3300-3305) Logistic (gross) weight in kilograms. Format: `N6`. GS1 data title: GROSS WEIGHT (kg).
     *
     * @param decimals 0-5; grossWeightKg(0) is AI 3300.
     */
    public fun grossWeightKg(decimals: Int): String {
        require(decimals in 0..5) { "decimals must be 0-5, was $decimals" }
        return (3300 + decimals).toString().padStart(4, '0')
    }

    /**
     * (3310-3315) Logistic length or first dimension in metres. Format: `N6`. GS1 data title: LENGTH (m), log.
     *
     * @param decimals 0-5; logisticLengthM(0) is AI 3310.
     */
    public fun logisticLengthM(decimals: Int): String {
        require(decimals in 0..5) { "decimals must be 0-5, was $decimals" }
        return (3310 + decimals).toString().padStart(4, '0')
    }

    /**
     * (3320-3325) Logistic width, diameter or second dimension in metres. Format: `N6`. GS1 data title: WIDTH (m), log.
     *
     * @param decimals 0-5; logisticWidthM(0) is AI 3320.
     */
    public fun logisticWidthM(decimals: Int): String {
        require(decimals in 0..5) { "decimals must be 0-5, was $decimals" }
        return (3320 + decimals).toString().padStart(4, '0')
    }

    /**
     * (3330-3335) Logistic depth, thickness, height or third dimension in metres. Format: `N6`. GS1 data title: HEIGHT (m), log.
     *
     * @param decimals 0-5; logisticHeightM(0) is AI 3330.
     */
    public fun logisticHeightM(decimals: Int): String {
        require(decimals in 0..5) { "decimals must be 0-5, was $decimals" }
        return (3330 + decimals).toString().padStart(4, '0')
    }

    /**
     * (3340-3345) Logistic area in square metres. Format: `N6`. GS1 data title: AREA (m²), log.
     *
     * @param decimals 0-5; logisticAreaM2(0) is AI 3340.
     */
    public fun logisticAreaM2(decimals: Int): String {
        require(decimals in 0..5) { "decimals must be 0-5, was $decimals" }
        return (3340 + decimals).toString().padStart(4, '0')
    }

    /**
     * (3350-3355) Logistic volume in litres. Format: `N6`. GS1 data title: VOLUME (l), log.
     *
     * @param decimals 0-5; logisticVolumeL(0) is AI 3350.
     */
    public fun logisticVolumeL(decimals: Int): String {
        require(decimals in 0..5) { "decimals must be 0-5, was $decimals" }
        return (3350 + decimals).toString().padStart(4, '0')
    }

    /**
     * (3360-3365) Logistic volume in cubic metres. Format: `N6`. GS1 data title: VOLUME (m³), log.
     *
     * @param decimals 0-5; logisticVolumeM3(0) is AI 3360.
     */
    public fun logisticVolumeM3(decimals: Int): String {
        require(decimals in 0..5) { "decimals must be 0-5, was $decimals" }
        return (3360 + decimals).toString().padStart(4, '0')
    }

    /**
     * (3370-3375) Kilograms per square metre. Format: `N6`. GS1 data title: KG PER m².
     *
     * @param decimals 0-5; kilogramsPerSquareMetre(0) is AI 3370.
     */
    public fun kilogramsPerSquareMetre(decimals: Int): String {
        require(decimals in 0..5) { "decimals must be 0-5, was $decimals" }
        return (3370 + decimals).toString().padStart(4, '0')
    }

    /**
     * (3400-3405) Logistic (gross) weight in pounds. Format: `N6`. GS1 data title: GROSS WEIGHT (lb).
     *
     * @param decimals 0-5; grossWeightLb(0) is AI 3400.
     */
    public fun grossWeightLb(decimals: Int): String {
        require(decimals in 0..5) { "decimals must be 0-5, was $decimals" }
        return (3400 + decimals).toString().padStart(4, '0')
    }

    /**
     * (3410-3415) Logistic length or first dimension in inches. Format: `N6`. GS1 data title: LENGTH (in), log.
     *
     * @param decimals 0-5; logisticLengthIn(0) is AI 3410.
     */
    public fun logisticLengthIn(decimals: Int): String {
        require(decimals in 0..5) { "decimals must be 0-5, was $decimals" }
        return (3410 + decimals).toString().padStart(4, '0')
    }

    /**
     * (3420-3425) Logistic length or first dimension in feet. Format: `N6`. GS1 data title: LENGTH (ft), log.
     *
     * @param decimals 0-5; logisticLengthFt(0) is AI 3420.
     */
    public fun logisticLengthFt(decimals: Int): String {
        require(decimals in 0..5) { "decimals must be 0-5, was $decimals" }
        return (3420 + decimals).toString().padStart(4, '0')
    }

    /**
     * (3430-3435) Logistic length or first dimension in yards. Format: `N6`. GS1 data title: LENGTH (yd), log.
     *
     * @param decimals 0-5; logisticLengthYd(0) is AI 3430.
     */
    public fun logisticLengthYd(decimals: Int): String {
        require(decimals in 0..5) { "decimals must be 0-5, was $decimals" }
        return (3430 + decimals).toString().padStart(4, '0')
    }

    /**
     * (3440-3445) Logistic width, diameter or second dimension in inches. Format: `N6`. GS1 data title: WIDTH (in), log.
     *
     * @param decimals 0-5; logisticWidthIn(0) is AI 3440.
     */
    public fun logisticWidthIn(decimals: Int): String {
        require(decimals in 0..5) { "decimals must be 0-5, was $decimals" }
        return (3440 + decimals).toString().padStart(4, '0')
    }

    /**
     * (3450-3455) Logistic width, diameter or second dimension in feet. Format: `N6`. GS1 data title: WIDTH (ft), log.
     *
     * @param decimals 0-5; logisticWidthFt(0) is AI 3450.
     */
    public fun logisticWidthFt(decimals: Int): String {
        require(decimals in 0..5) { "decimals must be 0-5, was $decimals" }
        return (3450 + decimals).toString().padStart(4, '0')
    }

    /**
     * (3460-3465) Logistic width, diameter or second dimension in yards. Format: `N6`. GS1 data title: WIDTH (yd), log.
     *
     * @param decimals 0-5; logisticWidthYd(0) is AI 3460.
     */
    public fun logisticWidthYd(decimals: Int): String {
        require(decimals in 0..5) { "decimals must be 0-5, was $decimals" }
        return (3460 + decimals).toString().padStart(4, '0')
    }

    /**
     * (3470-3475) Logistic depth, thickness, height or third dimension in inches. Format: `N6`. GS1 data title: HEIGHT (in), log.
     *
     * @param decimals 0-5; logisticHeightIn(0) is AI 3470.
     */
    public fun logisticHeightIn(decimals: Int): String {
        require(decimals in 0..5) { "decimals must be 0-5, was $decimals" }
        return (3470 + decimals).toString().padStart(4, '0')
    }

    /**
     * (3480-3485) Logistic depth, thickness, height or third dimension in feet. Format: `N6`. GS1 data title: HEIGHT (ft), log.
     *
     * @param decimals 0-5; logisticHeightFt(0) is AI 3480.
     */
    public fun logisticHeightFt(decimals: Int): String {
        require(decimals in 0..5) { "decimals must be 0-5, was $decimals" }
        return (3480 + decimals).toString().padStart(4, '0')
    }

    /**
     * (3490-3495) Logistic depth, thickness, height or third dimension in yards. Format: `N6`. GS1 data title: HEIGHT (yd), log.
     *
     * @param decimals 0-5; logisticHeightYd(0) is AI 3490.
     */
    public fun logisticHeightYd(decimals: Int): String {
        require(decimals in 0..5) { "decimals must be 0-5, was $decimals" }
        return (3490 + decimals).toString().padStart(4, '0')
    }

    /**
     * (3500-3505) Area in square inches. Format: `N6`. GS1 data title: AREA (in²).
     *
     * @param decimals 0-5; areaIn2(0) is AI 3500.
     */
    public fun areaIn2(decimals: Int): String {
        require(decimals in 0..5) { "decimals must be 0-5, was $decimals" }
        return (3500 + decimals).toString().padStart(4, '0')
    }

    /**
     * (3510-3515) Area in square feet. Format: `N6`. GS1 data title: AREA (ft²).
     *
     * @param decimals 0-5; areaFt2(0) is AI 3510.
     */
    public fun areaFt2(decimals: Int): String {
        require(decimals in 0..5) { "decimals must be 0-5, was $decimals" }
        return (3510 + decimals).toString().padStart(4, '0')
    }

    /**
     * (3520-3525) Area in square yards. Format: `N6`. GS1 data title: AREA (yd²).
     *
     * @param decimals 0-5; areaYd2(0) is AI 3520.
     */
    public fun areaYd2(decimals: Int): String {
        require(decimals in 0..5) { "decimals must be 0-5, was $decimals" }
        return (3520 + decimals).toString().padStart(4, '0')
    }

    /**
     * (3530-3535) Logistic area in square inches. Format: `N6`. GS1 data title: AREA (in²), log.
     *
     * @param decimals 0-5; logisticAreaIn2(0) is AI 3530.
     */
    public fun logisticAreaIn2(decimals: Int): String {
        require(decimals in 0..5) { "decimals must be 0-5, was $decimals" }
        return (3530 + decimals).toString().padStart(4, '0')
    }

    /**
     * (3540-3545) Logistic area in square feet. Format: `N6`. GS1 data title: AREA (ft²), log.
     *
     * @param decimals 0-5; logisticAreaFt2(0) is AI 3540.
     */
    public fun logisticAreaFt2(decimals: Int): String {
        require(decimals in 0..5) { "decimals must be 0-5, was $decimals" }
        return (3540 + decimals).toString().padStart(4, '0')
    }

    /**
     * (3550-3555) Logistic area in square yards. Format: `N6`. GS1 data title: AREA (yd²), log.
     *
     * @param decimals 0-5; logisticAreaYd2(0) is AI 3550.
     */
    public fun logisticAreaYd2(decimals: Int): String {
        require(decimals in 0..5) { "decimals must be 0-5, was $decimals" }
        return (3550 + decimals).toString().padStart(4, '0')
    }

    /**
     * (3560-3565) Net weight in troy ounces. Format: `N6`. GS1 data title: NET WEIGHT (tr oz).
     *
     * @param decimals 0-5; netWeightTroyOz(0) is AI 3560.
     */
    public fun netWeightTroyOz(decimals: Int): String {
        require(decimals in 0..5) { "decimals must be 0-5, was $decimals" }
        return (3560 + decimals).toString().padStart(4, '0')
    }

    /**
     * (3570-3575) Net weight or volume in ounces. Format: `N6`. GS1 data title: NET VOLUME (oz).
     *
     * @param decimals 0-5; netVolumeOz(0) is AI 3570.
     */
    public fun netVolumeOz(decimals: Int): String {
        require(decimals in 0..5) { "decimals must be 0-5, was $decimals" }
        return (3570 + decimals).toString().padStart(4, '0')
    }

    /**
     * (3600-3605) Net volume in US quarts. Format: `N6`. GS1 data title: NET VOLUME (qt (US)).
     *
     * @param decimals 0-5; netVolumeQt(0) is AI 3600.
     */
    public fun netVolumeQt(decimals: Int): String {
        require(decimals in 0..5) { "decimals must be 0-5, was $decimals" }
        return (3600 + decimals).toString().padStart(4, '0')
    }

    /**
     * (3610-3615) Net volume in US gallons. Format: `N6`. GS1 data title: NET VOLUME (gal.).
     *
     * @param decimals 0-5; netVolumeGal(0) is AI 3610.
     */
    public fun netVolumeGal(decimals: Int): String {
        require(decimals in 0..5) { "decimals must be 0-5, was $decimals" }
        return (3610 + decimals).toString().padStart(4, '0')
    }

    /**
     * (3620-3625) Logistic volume in US quarts. Format: `N6`. GS1 data title: VOLUME (qt (US)), log.
     *
     * @param decimals 0-5; logisticVolumeQt(0) is AI 3620.
     */
    public fun logisticVolumeQt(decimals: Int): String {
        require(decimals in 0..5) { "decimals must be 0-5, was $decimals" }
        return (3620 + decimals).toString().padStart(4, '0')
    }

    /**
     * (3630-3635) Logistic volume in US gallons. Format: `N6`. GS1 data title: VOLUME (gal (US)), log.
     *
     * @param decimals 0-5; logisticVolumeGal(0) is AI 3630.
     */
    public fun logisticVolumeGal(decimals: Int): String {
        require(decimals in 0..5) { "decimals must be 0-5, was $decimals" }
        return (3630 + decimals).toString().padStart(4, '0')
    }

    /**
     * (3640-3645) Net volume in cubic inches. Format: `N6`. GS1 data title: NET VOLUME (in³).
     *
     * @param decimals 0-5; netVolumeIn3(0) is AI 3640.
     */
    public fun netVolumeIn3(decimals: Int): String {
        require(decimals in 0..5) { "decimals must be 0-5, was $decimals" }
        return (3640 + decimals).toString().padStart(4, '0')
    }

    /**
     * (3650-3655) Net volume in cubic feet. Format: `N6`. GS1 data title: NET VOLUME (ft³).
     *
     * @param decimals 0-5; netVolumeFt3(0) is AI 3650.
     */
    public fun netVolumeFt3(decimals: Int): String {
        require(decimals in 0..5) { "decimals must be 0-5, was $decimals" }
        return (3650 + decimals).toString().padStart(4, '0')
    }

    /**
     * (3660-3665) Net volume in cubic yards. Format: `N6`. GS1 data title: NET VOLUME (yd³).
     *
     * @param decimals 0-5; netVolumeYd3(0) is AI 3660.
     */
    public fun netVolumeYd3(decimals: Int): String {
        require(decimals in 0..5) { "decimals must be 0-5, was $decimals" }
        return (3660 + decimals).toString().padStart(4, '0')
    }

    /**
     * (3670-3675) Logistic volume in cubic inches. Format: `N6`. GS1 data title: VOLUME (in³), log.
     *
     * @param decimals 0-5; logisticVolumeIn3(0) is AI 3670.
     */
    public fun logisticVolumeIn3(decimals: Int): String {
        require(decimals in 0..5) { "decimals must be 0-5, was $decimals" }
        return (3670 + decimals).toString().padStart(4, '0')
    }

    /**
     * (3680-3685) Logistic volume in cubic feet. Format: `N6`. GS1 data title: VOLUME (ft³), log.
     *
     * @param decimals 0-5; logisticVolumeFt3(0) is AI 3680.
     */
    public fun logisticVolumeFt3(decimals: Int): String {
        require(decimals in 0..5) { "decimals must be 0-5, was $decimals" }
        return (3680 + decimals).toString().padStart(4, '0')
    }

    /**
     * (3690-3695) Logistic volume in cubic yards. Format: `N6`. GS1 data title: VOLUME (yd³), log.
     *
     * @param decimals 0-5; logisticVolumeYd3(0) is AI 3690.
     */
    public fun logisticVolumeYd3(decimals: Int): String {
        require(decimals in 0..5) { "decimals must be 0-5, was $decimals" }
        return (3690 + decimals).toString().padStart(4, '0')
    }

    /** (37) Count of trade items or trade item pieces contained in a logistic unit. Format: `N..8`. GS1 data title: COUNT. */
    public const val COUNT: String = "37"

    /**
     * (3900-3909) Amount payable or coupon value in local currency. Format: `N..15`. GS1 data title: AMOUNT.
     *
     * @param decimals 0-9; amountPayable(0) is AI 3900.
     */
    public fun amountPayable(decimals: Int): String {
        require(decimals in 0..9) { "decimals must be 0-9, was $decimals" }
        return (3900 + decimals).toString().padStart(4, '0')
    }

    /**
     * (3910-3919) Amount payable or coupon value with ISO 4217 currency code. Format: `N3,iso4217 N..15`. GS1 data title: AMOUNT.
     *
     * @param decimals 0-9; amountPayableWithCurrency(0) is AI 3910.
     */
    public fun amountPayableWithCurrency(decimals: Int): String {
        require(decimals in 0..9) { "decimals must be 0-9, was $decimals" }
        return (3910 + decimals).toString().padStart(4, '0')
    }

    /**
     * (3920-3929) Amount payable for a variable measure trade item, local currency. Format: `N..15`. GS1 data title: PRICE.
     *
     * @param decimals 0-9; price(0) is AI 3920.
     */
    public fun price(decimals: Int): String {
        require(decimals in 0..9) { "decimals must be 0-9, was $decimals" }
        return (3920 + decimals).toString().padStart(4, '0')
    }

    /**
     * (3930-3939) Amount payable for a variable measure trade item, with ISO 4217 currency code. Format: `N3,iso4217 N..15`. GS1 data title: PRICE.
     *
     * @param decimals 0-9; priceWithCurrency(0) is AI 3930.
     */
    public fun priceWithCurrency(decimals: Int): String {
        require(decimals in 0..9) { "decimals must be 0-9, was $decimals" }
        return (3930 + decimals).toString().padStart(4, '0')
    }

    /**
     * (3940-3943) Percentage discount of a coupon. Format: `N4`. GS1 data title: PRCNT OFF.
     *
     * @param decimals 0-3; percentageDiscount(0) is AI 3940.
     */
    public fun percentageDiscount(decimals: Int): String {
        require(decimals in 0..3) { "decimals must be 0-3, was $decimals" }
        return (3940 + decimals).toString().padStart(4, '0')
    }

    /**
     * (3950-3955) Amount payable per unit of measure of a variable measure trade item. Format: `N6`. GS1 data title: PRICE/UoM.
     *
     * @param decimals 0-5; pricePerUnitOfMeasure(0) is AI 3950.
     */
    public fun pricePerUnitOfMeasure(decimals: Int): String {
        require(decimals in 0..5) { "decimals must be 0-5, was $decimals" }
        return (3950 + decimals).toString().padStart(4, '0')
    }

    /** (400) Customer's purchase order number. Format: `X..30`. GS1 data title: ORDER NUMBER. */
    public const val ORDER_NUMBER: String = "400"

    /** (401) Global Identification Number for Consignment. Format: `X..30,gcppos1`. GS1 data title: GINC. */
    public const val GINC: String = "401"

    /** (402) Global Shipment Identification Number. Format: `N17,csum,gcppos1`. GS1 data title: GSIN. */
    public const val GSIN: String = "402"

    /** (403) Routing code. Format: `X..30`. GS1 data title: ROUTE. */
    public const val ROUTE: String = "403"

    /** (410) Ship to / deliver to Global Location Number. Format: `N13,csum,gcppos1`. GS1 data title: SHIP TO LOC. */
    public const val SHIP_TO_GLN: String = "410"

    /** (411) Bill to / invoice to Global Location Number. Format: `N13,csum,gcppos1`. GS1 data title: BILL TO. */
    public const val BILL_TO_GLN: String = "411"

    /** (412) Purchased from Global Location Number. Format: `N13,csum,gcppos1`. GS1 data title: PURCHASE FROM. */
    public const val PURCHASED_FROM_GLN: String = "412"

    /** (413) Ship for / deliver for / forward to Global Location Number. Format: `N13,csum,gcppos1`. GS1 data title: SHIP FOR LOC. */
    public const val SHIP_FOR_GLN: String = "413"

    /** (414) Global Location Number of a physical location. Format: `N13,csum,gcppos1`. GS1 data title: LOC No.. */
    public const val LOCATION_GLN: String = "414"

    /** (415) Global Location Number of the invoicing party. Format: `N13,csum,gcppos1`. GS1 data title: PAY TO. */
    public const val PAY_TO_GLN: String = "415"

    /** (416) Global Location Number of the production or service location. Format: `N13,csum,gcppos1`. GS1 data title: PROD/SERV LOC. */
    public const val PRODUCTION_LOCATION_GLN: String = "416"

    /** (417) Party Global Location Number. Format: `N13,csum,gcppos1`. GS1 data title: PARTY. */
    public const val PARTY_GLN: String = "417"

    /** (420) Ship to / deliver to postal code within a single postal authority. Format: `X..20`. GS1 data title: SHIP TO POST. */
    public const val SHIP_TO_POSTAL_CODE: String = "420"

    /** (421) Ship to / deliver to postal code with ISO 3166 country code. Format: `N3,iso3166 X..9`. GS1 data title: SHIP TO POST. */
    public const val SHIP_TO_POSTAL_CODE_WITH_COUNTRY: String = "421"

    /** (422) Country of origin of a trade item (ISO 3166 numeric). Format: `N3,iso3166`. GS1 data title: ORIGIN. */
    public const val ORIGIN_COUNTRY: String = "422"

    /** (423) Country or countries of initial processing. Format: `N3,iso3166 [N3],iso3166 [N3],iso3166 [N3],iso3166 [N3],iso3166`. GS1 data title: COUNTRY - INITIAL PROCESS. */
    public const val INITIAL_PROCESSING_COUNTRY: String = "423"

    /** (424) Country of processing. Format: `N3,iso3166`. GS1 data title: COUNTRY - PROCESS. */
    public const val PROCESSING_COUNTRY: String = "424"

    /** (425) Country or countries of disassembly. Format: `N3,iso3166 [N3],iso3166 [N3],iso3166 [N3],iso3166 [N3],iso3166`. GS1 data title: COUNTRY - DISASSEMBLY. */
    public const val DISASSEMBLY_COUNTRY: String = "425"

    /** (426) Country covering the full process chain. Format: `N3,iso3166`. GS1 data title: COUNTRY - FULL PROCESS. */
    public const val FULL_PROCESS_COUNTRY: String = "426"

    /** (427) Country subdivision of origin (ISO 3166-2). Format: `X..3`. GS1 data title: ORIGIN SUBDIVISION. */
    public const val ORIGIN_SUBDIVISION: String = "427"

    /** (4300) Ship-to / deliver-to company name. Format: `X..35,pcenc`. GS1 data title: SHIP TO COMP. */
    public const val SHIP_TO_COMPANY: String = "4300"

    /** (4301) Ship-to / deliver-to contact. Format: `X..35,pcenc`. GS1 data title: SHIP TO NAME. */
    public const val SHIP_TO_CONTACT: String = "4301"

    /** (4302) Ship-to / deliver-to address line 1. Format: `X..70,pcenc`. GS1 data title: SHIP TO ADD1. */
    public const val SHIP_TO_ADDRESS_LINE_1: String = "4302"

    /** (4303) Ship-to / deliver-to address line 2. Format: `X..70,pcenc`. GS1 data title: SHIP TO ADD2. */
    public const val SHIP_TO_ADDRESS_LINE_2: String = "4303"

    /** (4304) Ship-to / deliver-to suburb. Format: `X..70,pcenc`. GS1 data title: SHIP TO SUB. */
    public const val SHIP_TO_SUBURB: String = "4304"

    /** (4305) Ship-to / deliver-to locality. Format: `X..70,pcenc`. GS1 data title: SHIP TO LOC. */
    public const val SHIP_TO_LOCALITY: String = "4305"

    /** (4306) Ship-to / deliver-to region. Format: `X..70,pcenc`. GS1 data title: SHIP TO REG. */
    public const val SHIP_TO_REGION: String = "4306"

    /** (4307) Ship-to / deliver-to country code (ISO 3166 alpha-2). Format: `X2,iso3166alpha2`. GS1 data title: SHIP TO COUNTRY. */
    public const val SHIP_TO_COUNTRY_CODE: String = "4307"

    /** (4308) Ship-to / deliver-to telephone number. Format: `X..30`. GS1 data title: SHIP TO PHONE. */
    public const val SHIP_TO_PHONE: String = "4308"

    /** (4309) Ship-to / deliver-to GEO location. Format: `N10,latitude N10,longitude`. GS1 data title: SHIP TO GEO. */
    public const val SHIP_TO_GEOCODE: String = "4309"

    /** (4310) Return-to company name. Format: `X..35,pcenc`. GS1 data title: RTN TO COMP. */
    public const val RETURN_TO_COMPANY: String = "4310"

    /** (4311) Return-to contact. Format: `X..35,pcenc`. GS1 data title: RTN TO NAME. */
    public const val RETURN_TO_CONTACT: String = "4311"

    /** (4312) Return-to address line 1. Format: `X..70,pcenc`. GS1 data title: RTN TO ADD1. */
    public const val RETURN_TO_ADDRESS_LINE_1: String = "4312"

    /** (4313) Return-to address line 2. Format: `X..70,pcenc`. GS1 data title: RTN TO ADD2. */
    public const val RETURN_TO_ADDRESS_LINE_2: String = "4313"

    /** (4314) Return-to suburb. Format: `X..70,pcenc`. GS1 data title: RTN TO SUB. */
    public const val RETURN_TO_SUBURB: String = "4314"

    /** (4315) Return-to locality. Format: `X..70,pcenc`. GS1 data title: RTN TO LOC. */
    public const val RETURN_TO_LOCALITY: String = "4315"

    /** (4316) Return-to region. Format: `X..70,pcenc`. GS1 data title: RTN TO REG. */
    public const val RETURN_TO_REGION: String = "4316"

    /** (4317) Return-to country code (ISO 3166 alpha-2). Format: `X2,iso3166alpha2`. GS1 data title: RTN TO COUNTRY. */
    public const val RETURN_TO_COUNTRY_CODE: String = "4317"

    /** (4318) Return-to postal code. Format: `X..20`. GS1 data title: RTN TO POST. */
    public const val RETURN_TO_POSTAL_CODE: String = "4318"

    /** (4319) Return-to telephone number. Format: `X..30`. GS1 data title: RTN TO PHONE. */
    public const val RETURN_TO_PHONE: String = "4319"

    /** (4320) Service code description. Format: `X..35,pcenc`. GS1 data title: SRV DESCRIPTION. */
    public const val SERVICE_CODE_DESCRIPTION: String = "4320"

    /** (4321) Dangerous goods flag (0 or 1). Format: `N1,yesno`. GS1 data title: DANGEROUS GOODS. */
    public const val DANGEROUS_GOODS: String = "4321"

    /** (4322) Authority to leave flag (0 or 1). Format: `N1,yesno`. GS1 data title: AUTH TO LEAVE. */
    public const val AUTHORITY_TO_LEAVE: String = "4322"

    /** (4323) Signature required flag (0 or 1). Format: `N1,yesno`. GS1 data title: SIG REQUIRED. */
    public const val SIGNATURE_REQUIRED: String = "4323"

    /** (4324) Not before delivery date and time (YYMMDDHHMM). Format: `N6,yymmd0 N4,hhmi`. GS1 data title: NOT BEF DEL DT. */
    public const val NOT_BEFORE_DELIVERY_DATE_TIME: String = "4324"

    /** (4325) Not after delivery date and time (YYMMDDHHMM). Format: `N6,yymmd0 N4,hhmi`. GS1 data title: NOT AFT DEL DT. */
    public const val NOT_AFTER_DELIVERY_DATE_TIME: String = "4325"

    /** (4326) Release date (YYMMDD). Format: `N6,yymmdd`. GS1 data title: REL DATE. */
    public const val RELEASE_DATE: String = "4326"

    /** (4330) Maximum temperature in Fahrenheit. Format: `N6 [X1],hyphen`. GS1 data title: MAX TEMP F.. */
    public const val MAX_TEMPERATURE_F: String = "4330"

    /** (4331) Maximum temperature in Celsius. Format: `N6 [X1],hyphen`. GS1 data title: MAX TEMP C.. */
    public const val MAX_TEMPERATURE_C: String = "4331"

    /** (4332) Minimum temperature in Fahrenheit. Format: `N6 [X1],hyphen`. GS1 data title: MIN TEMP F.. */
    public const val MIN_TEMPERATURE_F: String = "4332"

    /** (4333) Minimum temperature in Celsius. Format: `N6 [X1],hyphen`. GS1 data title: MIN TEMP C.. */
    public const val MIN_TEMPERATURE_C: String = "4333"

    /** (7001) NATO Stock Number. Format: `N13`. GS1 data title: NSN. */
    public const val NSN: String = "7001"

    /** (7002) UNECE meat carcasses and cuts classification. Format: `X..30`. GS1 data title: MEAT CUT. */
    public const val MEAT_CUT: String = "7002"

    /** (7003) Expiration date and time (YYMMDDHHMM). Format: `N6,yymmdd N4,hhmi`. GS1 data title: EXPIRY TIME. */
    public const val EXPIRY_DATE_TIME: String = "7003"

    /** (7004) Active potency. Format: `N..4`. GS1 data title: ACTIVE POTENCY. */
    public const val ACTIVE_POTENCY: String = "7004"

    /** (7005) Catch area. Format: `X..12`. GS1 data title: CATCH AREA. */
    public const val CATCH_AREA: String = "7005"

    /** (7006) First freeze date (YYMMDD). Format: `N6,yymmdd`. GS1 data title: FIRST FREEZE DATE. */
    public const val FIRST_FREEZE_DATE: String = "7006"

    /** (7007) Harvest date or date range (YYMMDD[YYMMDD]). Format: `N6,yymmdd [N6],yymmdd`. GS1 data title: HARVEST DATE. */
    public const val HARVEST_DATE: String = "7007"

    /** (7008) Species for fishery purposes. Format: `X..3`. GS1 data title: AQUATIC SPECIES. */
    public const val AQUATIC_SPECIES: String = "7008"

    /** (7009) Fishing gear type. Format: `X..10`. GS1 data title: FISHING GEAR TYPE. */
    public const val FISHING_GEAR_TYPE: String = "7009"

    /** (7010) Production method. Format: `X..2`. GS1 data title: PROD METHOD. */
    public const val PRODUCTION_METHOD: String = "7010"

    /** (7011) Test by date (YYMMDD[HHMM]). Format: `N6,yymmdd [N4],hhmi`. GS1 data title: TEST BY DATE. */
    public const val TEST_BY_DATE: String = "7011"

    /** (7020) Refurbishment lot ID. Format: `X..20`. GS1 data title: REFURB LOT. */
    public const val REFURBISHMENT_LOT: String = "7020"

    /** (7021) Functional status. Format: `X..20`. GS1 data title: FUNC STAT. */
    public const val FUNCTIONAL_STATUS: String = "7021"

    /** (7022) Revision status. Format: `X..20`. GS1 data title: REV STAT. */
    public const val REVISION_STATUS: String = "7022"

    /** (7023) Global Individual Asset Identifier of an assembly. Format: `X..30,gcppos1`. GS1 data title: GIAI - ASSEMBLY. */
    public const val GIAI_ASSEMBLY: String = "7023"

    /**
     * (7030-7039) Number of processor with three-digit ISO country code (index 0-9). Format: `N3,iso3166999 X..27`.
     *
     * @param index 0-9; processorApproval(0) is AI 7030.
     */
    public fun processorApproval(index: Int): String {
        require(index in 0..9) { "index must be 0-9, was $index" }
        return (7030 + index).toString().padStart(4, '0')
    }

    /** (7040) GS1 UIC with extension 1 and importer index. Format: `N1 X1 X1 X1,importeridx`. GS1 data title: UIC+EXT. */
    public const val UIC_EXTENSION: String = "7040"

    /** (7041) UN/CEFACT freight unit type. Format: `X..4,packagetype`. GS1 data title: UFRGT UNIT TYPE. */
    public const val FREIGHT_UNIT_TYPE: String = "7041"

    /** (710) National Healthcare Reimbursement Number, Germany (PZN). Format: `X..20`. GS1 data title: NHRN PZN. */
    public const val NHRN_PZN: String = "710"

    /** (711) National Healthcare Reimbursement Number, France (CIP). Format: `X..20`. GS1 data title: NHRN CIP. */
    public const val NHRN_CIP: String = "711"

    /** (712) National Healthcare Reimbursement Number, Spain (Código Nacional). Format: `X..20`. GS1 data title: NHRN CN. */
    public const val NHRN_CN: String = "712"

    /** (713) National Healthcare Reimbursement Number, Brazil (DRN). Format: `X..20`. GS1 data title: NHRN DRN. */
    public const val NHRN_DRN: String = "713"

    /** (714) National Healthcare Reimbursement Number, Portugal (AIM). Format: `X..20`. GS1 data title: NHRN AIM. */
    public const val NHRN_AIM: String = "714"

    /** (715) National Healthcare Reimbursement Number, United States (NDC). Format: `X..20`. GS1 data title: NHRN NDC. */
    public const val NHRN_NDC: String = "715"

    /** (716) National Healthcare Reimbursement Number, Italy (AIC). Format: `X..20`. GS1 data title: NHRN AIC. */
    public const val NHRN_AIC: String = "716"

    /** (717) National Healthcare Reimbursement Number (SRN). Format: `X..20`. GS1 data title: NHRN SRN. */
    public const val NHRN_SRN: String = "717"

    /**
     * (7230-7239) Certification reference (number 1-10). Format: `X2 X..28`.
     *
     * @param number 1-10; certification(1) is AI 7230.
     */
    public fun certification(number: Int): String {
        require(number in 1..10) { "number must be 1-10, was $number" }
        return (7230 + number - 1).toString().padStart(4, '0')
    }

    /** (7240) Protocol ID. Format: `X..20`. GS1 data title: PROTOCOL. */
    public const val PROTOCOL_ID: String = "7240"

    /** (7241) AIDC media type. Format: `N2,mediatype`. GS1 data title: AIDC MEDIA TYPE. */
    public const val AIDC_MEDIA_TYPE: String = "7241"

    /** (7242) Version Control Number. Format: `X..25`. GS1 data title: VCN. */
    public const val VCN: String = "7242"

    /** (7250) Date of birth (YYYYMMDD). Format: `N8,yyyymmdd`. GS1 data title: DOB. */
    public const val DATE_OF_BIRTH: String = "7250"

    /** (7251) Date and time of birth (YYYYMMDDHHMM). Format: `N8,yyyymmdd N4,hhmi`. GS1 data title: DOB TIME. */
    public const val DATE_AND_TIME_OF_BIRTH: String = "7251"

    /** (7252) Biological sex (ISO 5218). Format: `N1,iso5218`. GS1 data title: BIO SEX. */
    public const val BIOLOGICAL_SEX: String = "7252"

    /** (7253) Family name of a person. Format: `X..40,pcenc`. GS1 data title: FAMILY NAME. */
    public const val FAMILY_NAME: String = "7253"

    /** (7254) Given name of a person. Format: `X..40,pcenc`. GS1 data title: GIVEN NAME. */
    public const val GIVEN_NAME: String = "7254"

    /** (7255) Name suffix of a person. Format: `X..10`. GS1 data title: SUFFIX. */
    public const val NAME_SUFFIX: String = "7255"

    /** (7256) Full name of a person. Format: `X..90,pcenc`. GS1 data title: FULL NAME. */
    public const val FULL_NAME: String = "7256"

    /** (7257) Address of a person. Format: `X..70,pcenc`. GS1 data title: PERSON ADDR. */
    public const val PERSON_ADDRESS: String = "7257"

    /** (7258) Baby birth sequence indicator (e.g. 1/2). Format: `X3,posinseqslash`. GS1 data title: BIRTH SEQUENCE. */
    public const val BIRTH_SEQUENCE: String = "7258"

    /** (7259) Baby of family name. Format: `X..40,pcenc`. GS1 data title: BABY. */
    public const val BABY_OF_FAMILY_NAME: String = "7259"

    /** (8001) Roll products: width, length, core diameter, direction and splices. Format: `N4,nonzero N5,nonzero N3,nonzero N1,winding N1`. GS1 data title: DIMENSIONS. */
    public const val ROLL_DIMENSIONS: String = "8001"

    /** (8002) Cellular mobile telephone identifier. Format: `X..20`. GS1 data title: CMT No.. */
    public const val CMT_NUMBER: String = "8002"

    /** (8003) Global Returnable Asset Identifier. Format: `N1,zero N13,csum,gcppos1 [X..16]`. GS1 data title: GRAI. */
    public const val GRAI: String = "8003"

    /** (8004) Global Individual Asset Identifier. Format: `X..30,gcppos1`. GS1 data title: GIAI. */
    public const val GIAI: String = "8004"

    /** (8005) Price per unit of measure. Format: `N6`. GS1 data title: PRICE PER UNIT. */
    public const val PRICE_PER_UNIT: String = "8005"

    /** (8006) Identification of an individual trade item piece. Format: `N14,csum,gcppos2 N4,pieceoftotal`. GS1 data title: ITIP. */
    public const val ITIP: String = "8006"

    /** (8007) International Bank Account Number. Format: `X..34,iban`. GS1 data title: IBAN. */
    public const val IBAN: String = "8007"

    /** (8008) Date and time of production (YYMMDDHH[MM[SS]]). Format: `N6,yymmdd N2,hh [N2],mi [N2],ss`. GS1 data title: PROD TIME. */
    public const val PRODUCTION_DATE_TIME: String = "8008"

    /** (8009) Optically readable sensor indicator. Format: `X..50`. GS1 data title: OPTSEN. */
    public const val OPTSEN: String = "8009"

    /** (8010) Component / part identifier. Format: `Y..30,gcppos1`. GS1 data title: CPID. */
    public const val CPID: String = "8010"

    /** (8011) Component / part identifier serial number. Format: `N..12,nozeroprefix`. GS1 data title: CPID SERIAL. */
    public const val CPID_SERIAL: String = "8011"

    /** (8012) Software version. Format: `X..20`. GS1 data title: VERSION. */
    public const val SOFTWARE_VERSION: String = "8012"

    /** (8013) Global Model Number. Format: `X..25,csumalpha,gcppos1`. GS1 data title: GMN. */
    public const val GMN: String = "8013"

    /** (8014) Master UDI-DI. Format: `X..25,csumalpha,gcppos1,hasnondigit`. GS1 data title: MUDI. */
    public const val MUDI: String = "8014"

    /** (8017) Global Service Relation Number, provider. Format: `N18,csum,gcppos1`. GS1 data title: GSRN - PROVIDER. */
    public const val GSRN_PROVIDER: String = "8017"

    /** (8018) Global Service Relation Number, recipient. Format: `N18,csum,gcppos1`. GS1 data title: GSRN - RECIPIENT. */
    public const val GSRN_RECIPIENT: String = "8018"

    /** (8019) Service Relation Instance Number. Format: `N..10`. GS1 data title: SRIN. */
    public const val SRIN: String = "8019"

    /** (8020) Payment slip reference number. Format: `X..25`. GS1 data title: REF No.. */
    public const val PAYMENT_SLIP_REFERENCE: String = "8020"

    /** (8026) Identification of pieces of a trade item contained in a logistic unit. Format: `N14,csum,gcppos2 N4,pieceoftotal`. GS1 data title: ITIP CONTENT. */
    public const val ITIP_CONTENT: String = "8026"

    /** (8030) Digital signature. Format: `Z..90`. GS1 data title: DIGSIG. */
    public const val DIGITAL_SIGNATURE: String = "8030"

    /** (8040) International Mobile Equipment Identity. Format: `N15`. GS1 data title: IMEI. */
    public const val IMEI: String = "8040"

    /** (8041) Second International Mobile Equipment Identity. Format: `N15`. GS1 data title: IMEI2. */
    public const val IMEI_2: String = "8041"

    /** (8042) Embedded SIM identifier. Format: `N32`. GS1 data title: ESIM. */
    public const val ESIM: String = "8042"

    /** (8043) Physical SIM identifier. Format: `N18 [N..2]`. GS1 data title: PSIM. */
    public const val PSIM: String = "8043"

    /** (8110) Coupon code identification for use in North America. Format: `X..70,couponcode`. */
    public const val COUPON_CODE_NORTH_AMERICA: String = "8110"

    /** (8111) Loyalty points of a coupon. Format: `N4`. GS1 data title: POINTS. */
    public const val LOYALTY_POINTS: String = "8111"

    /** (8112) Paperless coupon code identification for use in North America. Format: `X..70,couponposoffer`. */
    public const val PAPERLESS_COUPON_CODE_NORTH_AMERICA: String = "8112"

    /** (8200) Extended packaging URL. Format: `X..70`. GS1 data title: PRODUCT URL. */
    public const val PRODUCT_URL: String = "8200"

    /** (90) Information mutually agreed between trading partners. Format: `X..30`. GS1 data title: INTERNAL. */
    public const val INTERNAL: String = "90"

    /**
     * (91-99) Company internal information (number 1-9 for AIs 91-99). Format: `X..90`. GS1 data title: INTERNAL.
     *
     * @param number 1-9; companyInternal(1) is AI 91.
     */
    public fun companyInternal(number: Int): String {
        require(number in 1..9) { "number must be 1-9, was $number" }
        return (91 + number - 1).toString().padStart(2, '0')
    }
}
