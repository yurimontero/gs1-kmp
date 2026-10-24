package io.github.yurimontero.gs1.compose

import kotlin.test.Test
import kotlin.test.assertEquals

class ModuleGeometryTest {

    @Test
    fun usesWholePixelsWhenPossible() {
        assertEquals(10f, moduleSize(200f, 20))
        assertEquals(9f, moduleSize(199f, 20))
        assertEquals(1f, moduleSize(25f, 20))
    }

    @Test
    fun fallsBackToFractionalModulesWhenTooSmall() {
        assertEquals(0.5f, moduleSize(10f, 20))
    }

    @Test
    fun handlesEmptyInput() {
        assertEquals(0f, moduleSize(0f, 20))
        assertEquals(0f, moduleSize(100f, 0))
    }

    @Test
    fun centresOnWholePixels() {
        assertEquals(9f, centeredOffset(199f, 180f))
        assertEquals(0f, centeredOffset(100f, 120f))
    }
}
