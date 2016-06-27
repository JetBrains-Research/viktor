package org.jetbrains.bio.viktor

import org.junit.Test
import kotlin.test.assertEquals

class DoubleExtensionsTest {
    @Test fun plusVector() {
        val v = StridedVector(10) { it.toDouble() }
        val incremented = StridedVector(10) { it + 1.0 }
        assertEquals(incremented, 1.0 + v)
    }

    @Test fun plusMatrix() {
        val m = StridedMatrix(10, 2) { i, j -> i + 2.0 * j }
        val incremented = StridedMatrix(10, 2) { i, j -> m[i, j] + 1.0 }
        assertEquals(incremented, 1.0 + m)
    }

    @Test fun minusVector() {
        val v = StridedVector(10) { it.toDouble() }
        val reversed = v.copy().apply { reverse() }
        assertEquals(reversed, 9.0 - v)
    }

    @Test fun minusMatrix() {
        val m = StridedMatrix(10, 2) { i, j -> i + 2.0 * j }
        val decremented = StridedMatrix(10, 2) { i, j -> 42.0 - m[i, j] }
        assertEquals(decremented, 42.0 - m)
    }

    @Test fun timesVector() {
        val v = StridedVector(10) { it.toDouble() }
        val scaled = StridedVector(10) { it * 42.0 }
        assertEquals(scaled, 42.0 * v)
    }

    @Test fun timesMatrix() {
        val m = StridedMatrix(10, 2) { i, j -> i + 2.0 * j }
        val decremented = StridedMatrix(10, 2) { i, j -> 42.0 * m[i, j] }
        assertEquals(decremented, 42.0 * m)
    }

    @Test fun divVector() {
        val v = StridedVector(10) { it.toDouble() }
        val scaled = StridedVector(10) { 1.0 / it }
        assertEquals(scaled, 1.0 / v)
    }

    @Test fun divMatrix() {
        val m = StridedMatrix(10, 2) { i, j -> i + 2.0 * j }
        val decremented = StridedMatrix(10, 2) { i, j -> 42.0 / m[i, j] }
        assertEquals(decremented, 42.0 / m)
    }
}
