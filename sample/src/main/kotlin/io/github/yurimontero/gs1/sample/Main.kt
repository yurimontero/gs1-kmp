package io.github.yurimontero.gs1.sample

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import io.github.yurimontero.gs1.Gs1DigitalLink
import io.github.yurimontero.gs1.Gs1Exception
import io.github.yurimontero.gs1.Gs1Message
import io.github.yurimontero.gs1.code128.Code128Symbol
import io.github.yurimontero.gs1.code128.Gs1Code128
import io.github.yurimontero.gs1.compose.Gs1Code128Barcode
import io.github.yurimontero.gs1.compose.Gs1DataMatrixBarcode
import io.github.yurimontero.gs1.compose.Gs1QrCodeBarcode
import io.github.yurimontero.gs1.datamatrix.DataMatrixShape
import io.github.yurimontero.gs1.datamatrix.DataMatrixSymbol
import io.github.yurimontero.gs1.datamatrix.Gs1DataMatrix
import io.github.yurimontero.gs1.qrcode.Gs1QrCode
import io.github.yurimontero.gs1.qrcode.QrCodeSymbol
import io.github.yurimontero.gs1.render.toSvg
import java.io.File

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "gs1-kmp sample",
        state = rememberWindowState(width = 900.dp, height = 800.dp),
    ) {
        App()
    }
}

private val Ink = Color(0xFF1B1F24)
private val Muted = Color(0xFF5F6B7A)
private val ErrorRed = Color(0xFFC62828)
private val Surface = Color(0xFFF4F6F8)
private val Accent = Color(0xFF1565C0)

private val body = TextStyle(color = Ink, fontSize = 14.sp)
private val label = TextStyle(color = Muted, fontSize = 12.sp, fontWeight = FontWeight.Medium)
private val mono = TextStyle(color = Ink, fontSize = 14.sp, fontFamily = FontFamily.Monospace)

private const val EXAMPLE = "(01)09501101020917(17)261231(10)ABC123(21)SN-001"
private const val DOMAIN = "https://example.com"

private class Result(
    val message: Gs1Message? = null,
    val dataMatrix: DataMatrixSymbol? = null,
    val code128: Code128Symbol? = null,
    val code128Error: String? = null,
    val qrCode: QrCodeSymbol? = null,
    val qrCodeError: String? = null,
    val error: String? = null,
)

/** Accepts bracketed AI syntax or a GS1 Digital Link URI. */
private fun encode(text: String, shape: DataMatrixShape, domain: String): Result {
    val input = text.trim()
    val message = try {
        if (input.startsWith("http", ignoreCase = true)) Gs1DigitalLink.parse(input) else Gs1Message.parse(input)
    } catch (e: Gs1Exception) {
        return Result(error = e.message)
    }
    val dataMatrix = try {
        Gs1DataMatrix.encode(message, shape)
    } catch (e: Gs1Exception) {
        return Result(message = message, error = e.message)
    }
    var code128: Code128Symbol? = null
    var code128Error: String? = null
    try {
        code128 = Gs1Code128.encode(message)
    } catch (e: Gs1Exception) {
        code128Error = e.message
    }
    var qrCode: QrCodeSymbol? = null
    var qrCodeError: String? = null
    try {
        qrCode = Gs1QrCode.encode(message, domain.trim())
    } catch (e: Gs1Exception) {
        qrCodeError = e.message
    } catch (e: IllegalArgumentException) {
        qrCodeError = e.message
    }
    return Result(message, dataMatrix, code128, code128Error, qrCode, qrCodeError)
}

@Composable
private fun App() {
    var input by remember { mutableStateOf(EXAMPLE) }
    var shape by remember { mutableStateOf(DataMatrixShape.SQUARE) }
    var domain by remember { mutableStateOf(DOMAIN) }
    var status by remember { mutableStateOf<String?>(null) }
    val result = remember(input, shape, domain) { encode(input, shape, domain) }

    Column(
        modifier = Modifier.fillMaxSize().background(Color.White).verticalScroll(rememberScrollState()).padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        BasicText("GS1 barcode generator", style = TextStyle(color = Ink, fontSize = 22.sp, fontWeight = FontWeight.SemiBold))

        Section("GS1 data (bracketed AI syntax or GS1 Digital Link URI)") {
            BasicTextField(
                value = input,
                onValueChange = { input = it; status = null },
                textStyle = mono,
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
                    .border(1.dp, if (result.error != null) ErrorRed else Muted, RoundedCornerShape(6.dp))
                    .padding(12.dp),
            )
            result.error?.let { BasicText(it, style = body.copy(color = ErrorRed)) }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            DataMatrixShape.entries.forEach { option ->
                Chip(option.name.lowercase(), selected = option == shape) { shape = option }
            }
            Spacer(Modifier.width(16.dp))
            Chip("Save SVG files", selected = false) {
                status = saveSvgs(result)
            }
        }
        status?.let { BasicText(it, style = body.copy(color = Muted)) }

        result.dataMatrix?.let { symbol ->
            Section("GS1 DataMatrix · ${symbol.size} · ${symbol.usedDataCodewords}/${symbol.size.dataCodewords} codewords") {
                Gs1DataMatrixBarcode(symbol, moduleSize = 8.dp)
                BasicText(symbol.humanReadable, style = mono)
            }
        }

        if (result.message != null) {
            Section("GS1 Digital Link QR Code") {
                BasicTextField(
                    value = domain,
                    onValueChange = { domain = it; status = null },
                    textStyle = mono,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                        .border(1.dp, if (result.qrCodeError != null) ErrorRed else Muted, RoundedCornerShape(6.dp))
                        .padding(12.dp),
                )
                val qrCode = result.qrCode
                if (qrCode != null) {
                    Gs1QrCodeBarcode(qrCode, moduleSize = 6.dp)
                    BasicText(qrCode.text, style = mono)
                    BasicText("Version ${qrCode.version}-${qrCode.errorCorrection} · ${qrCode.size}x${qrCode.size} modules", style = label)
                } else {
                    BasicText(result.qrCodeError ?: "", style = body.copy(color = ErrorRed))
                }
            }

            Section("GS1-128") {
                val code128 = result.code128
                if (code128 != null) {
                    Gs1Code128Barcode(code128, moduleWidth = 2.dp, barHeight = 96.dp)
                    BasicText("${code128.dataCharacterCount}/${Gs1Code128.MAX_DATA_CHARACTERS} data characters", style = label)
                } else {
                    BasicText(result.code128Error ?: "", style = body.copy(color = ErrorRed))
                }
            }
        }

        result.message?.let { message ->
            Section("Element strings") {
                message.elements.forEach { element ->
                    Row {
                        BasicText("(${element.ai.code})", style = mono.copy(color = Accent), modifier = Modifier.width(72.dp))
                        BasicText(element.ai.title, style = label, modifier = Modifier.width(180.dp))
                        BasicText(element.value, style = mono)
                    }
                }
                Spacer(Modifier.height(4.dp))
                BasicText("Scanner output: " + message.toElementString(symbologyIdentifier = "]d2").replace("\u001D", "<GS>"), style = mono.copy(color = Muted))
            }
        }
    }
}

@Composable
private fun Section(title: String, content: @Composable () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth().background(Surface, RoundedCornerShape(8.dp)).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        BasicText(title, style = label)
        content()
    }
}

@Composable
private fun Chip(text: String, selected: Boolean, onClick: () -> Unit) {
    BasicText(
        text,
        style = body.copy(color = if (selected) Color.White else Accent),
        modifier = Modifier
            .background(if (selected) Accent else Color.White, RoundedCornerShape(16.dp))
            .border(1.dp, Accent, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 6.dp),
    )
}

private fun saveSvgs(result: Result): String {
    val dir = File(System.getProperty("user.home"), "gs1-sample").apply { mkdirs() }
    val saved = mutableListOf<String>()
    result.dataMatrix?.let { File(dir, "datamatrix.svg").writeText(it.toSvg()); saved += "datamatrix.svg" }
    result.code128?.let { File(dir, "gs1-128.svg").writeText(it.toSvg()); saved += "gs1-128.svg" }
    result.qrCode?.let { File(dir, "qrcode.svg").writeText(it.toSvg()); saved += "qrcode.svg" }
    return if (saved.isEmpty()) "Nothing to save" else "Saved ${saved.joinToString()} in ${dir.absolutePath}"
}
