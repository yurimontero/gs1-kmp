package io.github.yurimontero.gs1.render

import io.github.yurimontero.gs1.Gs1Message
import io.github.yurimontero.gs1.code128.Gs1Code128
import io.github.yurimontero.gs1.datamatrix.Gs1DataMatrix
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SvgTest {

    private val message = Gs1Message.parse("(01)09501101020917(17)261231(10)A&B<1>")

    @Test
    fun dataMatrixSvgReproducesEveryModule() {
        val symbol = Gs1DataMatrix.encode(message)
        val svg = symbol.toSvg(moduleSize = 0.5, unit = "mm")
        val size = symbol.columns + 2
        assertTrue(svg.startsWith("<svg xmlns=\"http://www.w3.org/2000/svg\""))
        assertContains(svg, "width=\"${format(size * 0.5)}mm\"")
        assertContains(svg, "viewBox=\"0 0 $size $size\"")
        assertTrue(svg.endsWith("</svg>"))

        val grid = Array(size) { BooleanArray(size) }
        Regex("M(\\d+) (\\d+)h(\\d+)v1h-\\d+z").findAll(svg).forEach { m ->
            val (x, y, w) = m.destructured
            for (k in 0 until w.toInt()) grid[y.toInt()][x.toInt() + k] = true
        }
        assertEquals(symbol.toModules().map { it.toList() }, grid.map { it.toList() })
    }

    @Test
    fun code128SvgReproducesEveryBar() {
        val symbol = Gs1Code128.encode(message)
        val svg = symbol.toSvg(barHeight = 40, humanReadable = false)
        val width = symbol.moduleCount + 20
        assertContains(svg, "viewBox=\"0 0 $width 40\"")
        assertFalse(svg.contains("<text"))

        val modules = BooleanArray(width)
        Regex("M(\\d+) 0h(\\d+)v40h-\\d+z").findAll(svg).forEach { m ->
            val (x, w) = m.destructured
            for (k in 0 until w.toInt()) modules[x.toInt() + k] = true
        }
        assertEquals(symbol.toModules().toList(), modules.toList())
    }

    @Test
    fun code128SvgIncludesEscapedHumanReadableText() {
        val svg = Gs1Code128.encode(message).toSvg()
        assertContains(svg, ">(01)09501101020917(17)261231(10)A&amp;B&lt;1&gt;</text>")
        assertContains(svg, "font-size=\"8\"")
    }

    @Test
    fun transparentBackground() {
        val svg = Gs1DataMatrix.encode(message).toSvg(lightColor = null, darkColor = "#123456")
        assertFalse(svg.contains("<rect"))
        assertContains(svg, "fill=\"#123456\"")
    }

    @Test
    fun formatsNumbersTheSameEverywhere() {
        assertEquals("10", format(10.0))
        assertEquals("0.495", format(0.495))
        assertEquals("12.5", format(12.5))
        assertEquals("1.333", format(4.0 / 3.0))
        assertEquals("-2.25", format(-2.25))
        assertEquals("0", format(0.0001))
    }
}
