package io.github.yurimontero.gs1.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.isSpecified
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.constrainHeight
import androidx.compose.ui.unit.constrainWidth
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.yurimontero.gs1.Gs1Message
import io.github.yurimontero.gs1.code128.Code128Symbol
import io.github.yurimontero.gs1.code128.Gs1Code128
import io.github.yurimontero.gs1.datamatrix.DataMatrixShape
import io.github.yurimontero.gs1.datamatrix.DataMatrixSymbol
import io.github.yurimontero.gs1.datamatrix.Gs1DataMatrix
import kotlin.math.ceil

/**
 * Shows a GS1 DataMatrix symbol.
 *
 * Without a size from [modifier], each module is [moduleSize] (rounded to whole pixels).
 * With a fixed size, the symbol is drawn as large as fits, centred, with whole-pixel modules.
 */
@Composable
public fun Gs1DataMatrixBarcode(
    symbol: DataMatrixSymbol,
    modifier: Modifier = Modifier,
    moduleSize: Dp = 4.dp,
    quietZone: Int = Gs1DataMatrix.QUIET_ZONE_MODULES,
    darkColor: Color = Color.Black,
    lightColor: Color = Color.White,
) {
    Layout(
        content = {},
        modifier = modifier.drawBehind { drawDataMatrix(symbol, quietZone, darkColor, lightColor) },
    ) { _, constraints ->
        val module = moduleSize.roundToPx().coerceAtLeast(1)
        val width = constraints.constrainWidth((symbol.columns + 2 * quietZone) * module)
        val height = constraints.constrainHeight((symbol.rows + 2 * quietZone) * module)
        layout(width, height) {}
    }
}

/**
 * Encodes [message] and shows it as a GS1 DataMatrix symbol.
 *
 * @throws io.github.yurimontero.gs1.Gs1Exception if the message does not fit; validate data beforehand.
 */
@Composable
public fun Gs1DataMatrixBarcode(
    message: Gs1Message,
    modifier: Modifier = Modifier,
    shape: DataMatrixShape = DataMatrixShape.SQUARE,
    moduleSize: Dp = 4.dp,
    quietZone: Int = Gs1DataMatrix.QUIET_ZONE_MODULES,
    darkColor: Color = Color.Black,
    lightColor: Color = Color.White,
) {
    val symbol = remember(message, shape) { Gs1DataMatrix.encode(message, shape) }
    Gs1DataMatrixBarcode(symbol, modifier, moduleSize, quietZone, darkColor, lightColor)
}

/** Default style of the human readable text under GS1-128 symbols. */
public val Gs1HumanReadableTextStyle: TextStyle = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 12.sp)

/**
 * Shows a GS1-128 symbol with its human readable text underneath.
 *
 * Without a width from [modifier], each module is [moduleWidth] (rounded to whole pixels).
 * With a fixed width, the bars are drawn as wide as fits, centred, with whole-pixel modules.
 *
 * @param barHeight height of the bars when the height is not fixed by [modifier].
 * @param textStyle style of the human readable text, or `null` to hide it.
 */
@Composable
public fun Gs1Code128Barcode(
    symbol: Code128Symbol,
    modifier: Modifier = Modifier,
    moduleWidth: Dp = 2.dp,
    barHeight: Dp = 64.dp,
    quietZone: Int = Gs1Code128.QUIET_ZONE_MODULES,
    darkColor: Color = Color.Black,
    lightColor: Color = Color.White,
    textStyle: TextStyle? = Gs1HumanReadableTextStyle,
) {
    val textMeasurer = rememberTextMeasurer()
    val textLayout = remember(symbol, textStyle, textMeasurer) {
        textStyle?.let { textMeasurer.measure(AnnotatedString(symbol.humanReadable), it) }
    }
    val textColor = textStyle?.color?.takeIf { it.isSpecified } ?: darkColor

    Layout(
        content = {},
        modifier = modifier.drawBehind {
            val textHeight = textLayout?.size?.height?.toFloat() ?: 0f
            val gap = if (textLayout != null) textHeight / 4f else 0f
            val bars = size.height - textHeight - gap
            if (lightColor.isSpecified) drawRect(lightColor)
            drawCode128Bars(symbol, quietZone, bars, darkColor, lightColor)
            if (textLayout != null && bars > 0f) {
                drawText(
                    textLayoutResult = textLayout,
                    color = textColor,
                    topLeft = Offset((size.width - textLayout.size.width) / 2f, bars + gap),
                )
            }
        },
    ) { _, constraints ->
        val module = moduleWidth.roundToPx().coerceAtLeast(1)
        val textHeight = textLayout?.size?.height ?: 0
        val gap = if (textLayout != null) ceil(textHeight / 4f).toInt() else 0
        val width = constraints.constrainWidth((symbol.moduleCount + 2 * quietZone) * module)
        val height = constraints.constrainHeight(barHeight.roundToPx() + gap + textHeight)
        layout(width, height) {}
    }
}

/**
 * Encodes [message] and shows it as a GS1-128 symbol.
 *
 * @throws io.github.yurimontero.gs1.Gs1Exception if the message is too long; validate data beforehand.
 */
@Composable
public fun Gs1Code128Barcode(
    message: Gs1Message,
    modifier: Modifier = Modifier,
    moduleWidth: Dp = 2.dp,
    barHeight: Dp = 64.dp,
    quietZone: Int = Gs1Code128.QUIET_ZONE_MODULES,
    darkColor: Color = Color.Black,
    lightColor: Color = Color.White,
    textStyle: TextStyle? = Gs1HumanReadableTextStyle,
) {
    val symbol = remember(message) { Gs1Code128.encode(message) }
    Gs1Code128Barcode(symbol, modifier, moduleWidth, barHeight, quietZone, darkColor, lightColor, textStyle)
}
