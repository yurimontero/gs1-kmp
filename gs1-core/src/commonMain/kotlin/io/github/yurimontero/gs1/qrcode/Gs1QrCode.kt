package io.github.yurimontero.gs1.qrcode

import io.github.yurimontero.gs1.Gs1DigitalLink
import io.github.yurimontero.gs1.Gs1ErrorCode
import io.github.yurimontero.gs1.Gs1Exception
import io.github.yurimontero.gs1.Gs1Message

/**
 * QR Code with a GS1 Digital Link URI, e.g. `https://example.com/01/09501101020917/10/ABC123`.
 *
 * Phones open the URI as a web address and GS1-aware scanners read the GS1 data from it.
 *
 * ```
 * val message = Gs1Message.parse("(01)09501101020917(10)ABC123(17)261231")
 * val symbol = Gs1QrCode.encode(message, domain = "https://example.com")
 * symbol.text      // https://example.com/01/09501101020917/10/ABC123?17=261231
 * symbol[row, column]
 * ```
 */
public object Gs1QrCode {

    /**
     * Encodes the GS1 Digital Link URI of [message] (see [Gs1DigitalLink.toUri]).
     *
     * @param domain scheme and domain, optionally with a path prefix; GS1's `https://id.gs1.org` by default.
     * @throws Gs1Exception if the message has no Digital Link primary key, contains AIs not allowed in a
     * Digital Link URI, or is too long ([Gs1ErrorCode.QR_CODE_TOO_LONG]).
     */
    public fun encode(
        message: Gs1Message,
        domain: String = Gs1DigitalLink.CANONICAL_DOMAIN,
        errorCorrection: QrErrorCorrection = QrErrorCorrection.M,
        version: Int? = null,
    ): QrCodeSymbol =
        QrCode.encode(Gs1DigitalLink.toUri(message, domain), errorCorrection, version, null, message.humanReadable)

    /**
     * Encodes an existing GS1 Digital Link URI exactly as given, after checking that it is valid.
     *
     * @throws Gs1Exception if [uri] is not a valid GS1 Digital Link URI or is too long.
     */
    public fun encodeUri(
        uri: String,
        errorCorrection: QrErrorCorrection = QrErrorCorrection.M,
        version: Int? = null,
    ): QrCodeSymbol {
        val message = Gs1DigitalLink.parse(uri)
        return QrCode.encode(uri, errorCorrection, version, null, message.humanReadable)
    }
}
