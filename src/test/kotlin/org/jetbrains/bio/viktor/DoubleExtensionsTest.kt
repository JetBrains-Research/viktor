package org.jetbrains.bio.viktor

import org.junit.Test
import kotlin.test.assertEquals

class DoubleExtensionsTest {
    @Test fun plus() {
        val v = StridedVector(10) { it.toDouble() }
        val incremented = StridedVector(10) { it + 1.0 }
        assertEquals(incremented, 1.0 + v)
    }

    @Test fun minus() {
        val v = StridedVector(10) { it.toDouble() }
        val reversed = v.copy().apply { reverse() }
        assertEquals(reversed, 9.0 - v)
    }

    @Test fun times() {
        val v = StridedVector(10) { it.toDouble() }
        val scaled = StridedVector(10) { it * 42.0 }
        assertEquals(scaled, 42.0 * v)
    }

    @Test fun div() {
        val v = StridedVector(10) { it.toDouble() }
        val scaled = StridedVector(10) { 1.0 / it }
        assertEquals(scaled, 1.0 / v)
    }
}
