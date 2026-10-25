package io.github.yurimontero.gs1.compose

import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.CanvasDrawScope
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import io.github.yurimontero.gs1.code128.Code128Symbol
import io.github.yurimontero.gs1.code128.Gs1Code128
import io.github.yurimontero.gs1.datamatrix.DataMatrixSymbol
import io.github.yurimontero.gs1.datamatrix.Gs1DataMatrix
import io.github.yurimontero.gs1.qrcode.QrCode
import io.github.yurimontero.gs1.qrcode.QrCodeSymbol

/**
 * Renders the symbol to a bitmap with exactly [modulePixels] pixels per module, e.g. for sharing
 * or sending to a printer (use the printer's dots per module for sharp edges).
 */
public fun DataMatrixSymbol.toImageBitmap(
    modulePixels: Int = 10,
    quietZone: Int = Gs1DataMatrix.QUIET_ZONE_MODULES,
    darkColor: Color = Color.Black,
    lightColor: Color = Color.White,
): ImageBitmap {
    require(modulePixels > 0) { "modulePixels must be positive" }
    val width = (columns + 2 * quietZone) * modulePixels
    val height = (rows + 2 * quietZone) * modulePixels
    return render(width, height) { drawDataMatrix(this@toImageBitmap, quietZone, darkColor, lightColor) }
}

/**
 * Renders the symbol to a bitmap with exactly [modulePixels] pixels per module, e.g. for sharing
 * or sending to a printer (use the printer's dots per module for sharp edges).
 */
public fun QrCodeSymbol.toImageBitmap(
    modulePixels: Int = 10,
    quietZone: Int = QrCode.QUIET_ZONE_MODULES,
    darkColor: Color = Color.Black,
    lightColor: Color = Color.White,
): ImageBitmap {
    require(modulePixels > 0) { "modulePixels must be positive" }
    val side = (size + 2 * quietZone) * modulePixels
    return render(side, side) { drawQrCode(this@toImageBitmap, quietZone, darkColor, lightColor) }
}

/**
 * Renders the bars to a bitmap with exactly [modulePixels] pixels per module and [barHeightPixels] high.
 * The human readable text is not included.
 */
public fun Code128Symbol.toImageBitmap(
    modulePixels: Int = 2,
    barHeightPixels: Int = 128,
    quietZone: Int = Gs1Code128.QUIET_ZONE_MODULES,
    darkColor: Color = Color.Black,
    lightColor: Color = Color.White,
): ImageBitmap {
    require(modulePixels > 0 && barHeightPixels > 0) { "modulePixels and barHeightPixels must be positive" }
    val width = (moduleCount + 2 * quietZone) * modulePixels
    return render(width, barHeightPixels) {
        drawCode128Bars(this@toImageBitmap, quietZone, barHeightPixels.toFloat(), darkColor, lightColor)
    }
}

private fun render(width: Int, height: Int, block: androidx.compose.ui.graphics.drawscope.DrawScope.() -> Unit): ImageBitmap {
    val bitmap = ImageBitmap(width, height)
    CanvasDrawScope().draw(
        density = Density(1f),
        layoutDirection = LayoutDirection.Ltr,
        canvas = Canvas(bitmap),
        size = Size(width.toFloat(), height.toFloat()),
        block = block,
    )
    return bitmap
}
