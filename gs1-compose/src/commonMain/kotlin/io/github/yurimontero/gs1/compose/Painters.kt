package io.github.yurimontero.gs1.compose

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.isSpecified
import androidx.compose.ui.graphics.painter.Painter
import io.github.yurimontero.gs1.code128.Code128Symbol
import io.github.yurimontero.gs1.code128.Gs1Code128
import io.github.yurimontero.gs1.datamatrix.DataMatrixSymbol
import io.github.yurimontero.gs1.datamatrix.Gs1DataMatrix

/**
 * Draws a GS1 DataMatrix symbol, centred and as large as fits, with whole-pixel modules.
 *
 * The painter has no intrinsic size: give the `Image` (or whatever uses it) a size.
 */
public class DataMatrixPainter(
    private val symbol: DataMatrixSymbol,
    private val quietZone: Int = Gs1DataMatrix.QUIET_ZONE_MODULES,
    private val darkColor: Color = Color.Black,
    private val lightColor: Color = Color.White,
) : Painter() {
    override val intrinsicSize: Size get() = Size.Unspecified

    override fun DrawScope.onDraw() {
        drawDataMatrix(symbol, quietZone, darkColor, lightColor)
    }
}

/**
 * Draws the bars of a GS1-128 symbol over the full height, centred horizontally,
 * with whole-pixel modules. The human readable text is not drawn (see [Gs1Code128Barcode]).
 */
public class Code128Painter(
    private val symbol: Code128Symbol,
    private val quietZone: Int = Gs1Code128.QUIET_ZONE_MODULES,
    private val darkColor: Color = Color.Black,
    private val lightColor: Color = Color.White,
) : Painter() {
    override val intrinsicSize: Size get() = Size.Unspecified

    override fun DrawScope.onDraw() {
        drawCode128Bars(symbol, quietZone, size.height, darkColor, lightColor)
    }
}

internal fun DrawScope.drawDataMatrix(symbol: DataMatrixSymbol, quietZone: Int, darkColor: Color, lightColor: Color) {
    val columns = symbol.columns + 2 * quietZone
    val rows = symbol.rows + 2 * quietZone
    val module = minOf(moduleSize(size.width, columns), moduleSize(size.height, rows))
    if (module <= 0f) return
    val width = module * columns
    val height = module * rows
    val left = centeredOffset(size.width, width)
    val top = centeredOffset(size.height, height)

    if (lightColor.isSpecified) drawRect(lightColor, Offset(left, top), Size(width, height))
    for (row in 0 until symbol.rows) {
        var column = 0
        while (column < symbol.columns) {
            if (!symbol[row, column]) {
                column++
                continue
            }
            val start = column
            while (column < symbol.columns && symbol[row, column]) column++
            drawRect(
                color = darkColor,
                topLeft = Offset(left + (start + quietZone) * module, top + (row + quietZone) * module),
                size = Size((column - start) * module, module),
            )
        }
    }
}

internal fun DrawScope.drawCode128Bars(
    symbol: Code128Symbol,
    quietZone: Int,
    barHeight: Float,
    darkColor: Color,
    lightColor: Color,
) {
    val modules = symbol.moduleCount + 2 * quietZone
    val module = moduleSize(size.width, modules)
    if (module <= 0f || barHeight <= 0f) return
    val width = module * modules
    val left = centeredOffset(size.width, width)

    if (lightColor.isSpecified) drawRect(lightColor, Offset(left, 0f), Size(width, barHeight))
    var x = quietZone
    symbol.widths.forEachIndexed { index, w ->
        if (index % 2 == 0) {
            drawRect(darkColor, Offset(left + x * module, 0f), Size(w * module, barHeight))
        }
        x += w
    }
}
