package org.jetbrains.bio.viktor

import org.junit.Test
import kotlin.test.assertEquals

class DoubleExtensionsTest {
    @Test fun minus() {
        val v = StridedVector(10) { it.toDouble() }
        val reversed = v.copy().apply { reverse() }
        assertEquals(9.0 - v, reversed)
    }
}
