package io.github.yurimontero.gs1

/**
 * GS1 mod-10 check digit, shared by GTIN-8/12/13/14, SSCC, GLN and other numeric GS1 keys.
 *
 * Starting from the rightmost digit of the key body, digits are weighted 3, 1, 3, 1…;
 * the check digit is the amount needed to reach the next multiple of ten.
 */
public object Gs1CheckDigit {

    /**
     * Computes the check digit for [body], the key digits without the check digit.
     *
     * @throws IllegalArgumentException if [body] is empty or contains non-digit characters.
     */
    public fun compute(body: String): Int {
        require(body.isNotEmpty()) { "GS1 key body must not be empty" }
        require(body.all { it in '0'..'9' }) { "GS1 key body must contain only digits: \"$body\"" }

        var sum = 0
        for (i in body.indices) {
            val digit = body[body.length - 1 - i] - '0'
            sum += if (i % 2 == 0) digit * 3 else digit
        }
        return (10 - sum % 10) % 10
    }

    /** Returns `true` if [key] is all digits and its last digit is the correct check digit. */
    public fun isValid(key: String): Boolean {
        if (key.length < 2 || !key.all { it in '0'..'9' }) return false
        return compute(key.dropLast(1)) == key.last() - '0'
    }

    /** Returns [body] with its check digit appended. */
    public fun append(body: String): String = body + compute(body)
}
