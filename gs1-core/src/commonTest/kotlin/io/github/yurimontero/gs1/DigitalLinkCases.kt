package io.github.yurimontero.gs1

import kotlin.test.fail

/** Collects Digital Link reference cases and reports every failure at once. */
class DigitalLinkCases {
    private val failures = mutableListOf<String>()
    private var count = 0

    fun parses(uri: String, expected: String, zeroSuppressed: Boolean = false) {
        count++
        try {
            val actual = "^" + Gs1DigitalLink.parse(uri, zeroSuppressed).toElementString(separator = '^')
            if (actual != expected) failures += "parse $uri: expected $expected, got $actual"
        } catch (e: Gs1Exception) {
            failures += "parse $uri: expected $expected, got ${e.error.code} (${e.message})"
        }
    }

    /** [code] `null` accepts any validation error. */
    fun rejects(uri: String, code: Gs1ErrorCode?, zeroSuppressed: Boolean = false) {
        count++
        try {
            val message = Gs1DigitalLink.parse(uri, zeroSuppressed)
            failures += "parse $uri: expected ${code ?: "an error"}, got ${message.humanReadable}"
        } catch (e: Gs1Exception) {
            if (code != null && e.error.code != code) failures += "parse $uri: expected $code, got ${e.error.code}"
        }
    }

    fun generates(domain: String?, data: String, expected: String) {
        count++
        try {
            val message = Gs1Message.parse(data, Gs1Order.AS_GIVEN)
            val actual = if (domain == null) Gs1DigitalLink.toUri(message) else Gs1DigitalLink.toUri(message, domain)
            if (actual != expected) failures += "generate $data: expected $expected, got $actual"
        } catch (e: Gs1Exception) {
            failures += "generate $data: expected $expected, got ${e.error.code}"
        }
    }

    fun cannotGenerate(domain: String?, data: String, code: Gs1ErrorCode) {
        count++
        try {
            val message = Gs1Message.parse(data, Gs1Order.AS_GIVEN)
            val actual = if (domain == null) Gs1DigitalLink.toUri(message) else Gs1DigitalLink.toUri(message, domain)
            failures += "generate $data: expected $code, got $actual"
        } catch (e: Gs1Exception) {
            if (e.error.code != code) failures += "generate $data: expected $code, got ${e.error.code}"
        }
    }

    fun assertAllPassed() {
        if (failures.isNotEmpty()) fail("${failures.size} of $count cases failed:\n" + failures.joinToString("\n"))
    }
}
