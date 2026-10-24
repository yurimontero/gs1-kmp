package io.github.yurimontero.gs1

import kotlin.test.assertNotNull
import kotlin.test.fail

/** Collects linter expectations and reports every mismatch at once. */
class LinterCases(private val name: String) {
    private val linter: Linter = assertNotNull(Linters.forName(name), "Linter '$name' is not implemented")
    private val mismatches = mutableListOf<String>()

    fun pass(data: String) {
        val result = linter(data)
        if (result != null) {
            mismatches += "${show(data)}: expected to pass, got ${result.code} at ${result.position}+${result.length}"
        }
    }

    fun fail(data: String, code: Gs1ErrorCode, position: Int, length: Int) {
        val result = linter(data)
        when {
            result == null -> mismatches += "${show(data)}: expected $code at $position+$length, but passed"
            result.code != code || result.position != position || result.length != length ->
                mismatches += "${show(data)}: expected $code at $position+$length, " +
                    "got ${result.code} at ${result.position}+${result.length}"
        }
    }

    fun verify() {
        if (mismatches.isNotEmpty()) {
            fail("Linter '$name': ${mismatches.size} mismatches\n" + mismatches.take(20).joinToString("\n"))
        }
    }

    private fun show(data: String) = "\"" + data.map { if (it.code in 32..126) "$it" else "\\u" + it.code.toString(16).padStart(4, '0') }.joinToString("") + "\""
}
