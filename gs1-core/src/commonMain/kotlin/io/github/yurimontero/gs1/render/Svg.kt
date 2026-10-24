package io.github.yurimontero.gs1.render

import io.github.yurimontero.gs1.code128.Code128Symbol
import io.github.yurimontero.gs1.code128.Gs1Code128
import io.github.yurimontero.gs1.datamatrix.DataMatrixSymbol
import io.github.yurimontero.gs1.datamatrix.Gs1DataMatrix
import kotlin.math.roundToLong

/**
 * Renders the symbol as an SVG document.
 *
 * Coordinates are in modules; [moduleSize] only sets the `width`/`height` attributes, in [unit].
 * For print, pass the X-dimension in millimetres with `unit = "mm"` (e.g. `moduleSize = 0.495`).
 *
 * @param barHeight bar height in modules.
 * @param humanReadable print the human readable interpretation under the bars.
 * @param fontSize text height in modules (8 modules is 3.96 mm at X = 0.495 mm; GS1 asks for at least 3 mm).
 * @param lightColor background colour, or `null` for a transparent background.
 */
public fun Code128Symbol.toSvg(
    moduleSize: Double = 2.0,
    unit: String = "",
    barHeight: Int = 60,
    quietZone: Int = Gs1Code128.QUIET_ZONE_MODULES,
    humanReadable: Boolean = true,
    fontSize: Int = 8,
    fontFamily: String = "monospace",
    darkColor: String = "#000000",
    lightColor: String? = "#ffffff",
): String {
    val width = moduleCount + 2 * quietZone
    val textGap = if (humanReadable) fontSize / 4 + 1 else 0
    val height = barHeight + if (humanReadable) textGap + fontSize else 0

    val path = StringBuilder()
    var x = quietZone
    widths.forEachIndexed { index, w ->
        if (index % 2 == 0) path.append('M').append(x).append(' ').append(0).append('h').append(w).append('v').append(barHeight).append('h').append(-w).append('z')
        x += w
    }

    val text = this.humanReadable
    return buildString {
        svgHeader(width, height, moduleSize, unit, lightColor)
        append("<path fill=\"").append(escapeXml(darkColor)).append("\" d=\"").append(path).append("\"/>")
        if (humanReadable) {
            // Baseline slightly above the bottom so descenders stay inside the image
            append("<text x=\"").append(format(width / 2.0))
            append("\" y=\"").append(format(height - fontSize * 0.15))
            append("\" font-family=\"").append(escapeXml(fontFamily))
            append("\" font-size=\"").append(fontSize)
            append("\" text-anchor=\"middle\" fill=\"").append(escapeXml(darkColor)).append("\">")
            append(escapeXml(text))
            append("</text>")
        }
        append("</svg>")
    }
}

/**
 * Renders the symbol as an SVG document.
 *
 * Coordinates are in modules; [moduleSize] only sets the `width`/`height` attributes, in [unit].
 * For print, pass the X-dimension in millimetres with `unit = "mm"`.
 * The human readable interpretation is not included: place [DataMatrixSymbol.humanReadable] near the symbol.
 *
 * @param lightColor background colour, or `null` for a transparent background.
 */
public fun DataMatrixSymbol.toSvg(
    moduleSize: Double = 10.0,
    unit: String = "",
    quietZone: Int = Gs1DataMatrix.QUIET_ZONE_MODULES,
    darkColor: String = "#000000",
    lightColor: String? = "#ffffff",
): String {
    val width = columns + 2 * quietZone
    val height = rows + 2 * quietZone
    val path = StringBuilder()
    for (row in 0 until rows) {
        var column = 0
        while (column < columns) {
            if (!this[row, column]) {
                column++
                continue
            }
            val start = column
            while (column < columns && this[row, column]) column++
            val run = column - start
            path.append('M').append(start + quietZone).append(' ').append(row + quietZone)
                .append('h').append(run).append("v1h").append(-run).append('z')
        }
    }
    return buildString {
        svgHeader(width, height, moduleSize, unit, lightColor)
        append("<path fill=\"").append(escapeXml(darkColor)).append("\" d=\"").append(path).append("\"/>")
        append("</svg>")
    }
}

private fun StringBuilder.svgHeader(width: Int, height: Int, moduleSize: Double, unit: String, lightColor: String?) {
    append("<svg xmlns=\"http://www.w3.org/2000/svg\" version=\"1.1\"")
    append(" width=\"").append(format(width * moduleSize)).append(escapeXml(unit)).append('"')
    append(" height=\"").append(format(height * moduleSize)).append(escapeXml(unit)).append('"')
    append(" viewBox=\"0 0 ").append(width).append(' ').append(height).append('"')
    append(" shape-rendering=\"crispEdges\">")
    if (lightColor != null) {
        append("<rect width=\"").append(width).append("\" height=\"").append(height)
            .append("\" fill=\"").append(escapeXml(lightColor)).append("\"/>")
    }
}

/** Formats with up to 3 decimals and no trailing zeros, identically on every platform. */
internal fun format(value: Double): String {
    val thousandths = (value * 1000).roundToLong()
    val negative = thousandths < 0
    val abs = if (negative) -thousandths else thousandths
    val integer = abs / 1000
    val fraction = (abs % 1000).toString().padStart(3, '0').trimEnd('0')
    return buildString {
        if (negative) append('-')
        append(integer)
        if (fraction.isNotEmpty()) append('.').append(fraction)
    }
}

internal fun escapeXml(text: String): String = buildString {
    for (c in text) {
        when (c) {
            '&' -> append("&amp;")
            '<' -> append("&lt;")
            '>' -> append("&gt;")
            '"' -> append("&quot;")
            '\'' -> append("&apos;")
            else -> append(c)
        }
    }
}
