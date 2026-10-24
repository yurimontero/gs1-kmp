package io.github.yurimontero.gs1.compose

import kotlin.math.floor

/**
 * Size of one module when [count] modules must fit in [available] pixels: a whole number of pixels
 * whenever possible, so every module has the same size and edges stay sharp.
 */
internal fun moduleSize(available: Float, count: Int): Float {
    if (count <= 0 || available <= 0f) return 0f
    val exact = available / count
    return if (exact >= 1f) floor(exact) else exact
}

/** Offset that centres [used] pixels in [available] pixels, rounded down to a whole pixel. */
internal fun centeredOffset(available: Float, used: Float): Float = floor((available - used) / 2f).coerceAtLeast(0f)
