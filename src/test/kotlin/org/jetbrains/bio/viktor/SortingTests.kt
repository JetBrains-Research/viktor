package org.jetbrains.bio.viktor

import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SortingTests {
    @Test fun partition() {
        assertEquals(
                StridedVector.of(1.0, 2.0, 3.0, 4.0),
                StridedVector.of(3.0, 4.0, 2.0, 1.0).apply { partition(2) })
    }

    @Test fun partitionInternal() {
        val values = StridedVector.of(0.0, 1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0)
        val length = values.size
        for (p in 0..length - 1) {
            values.shuffle()
            val pivot = values[p]
            val split = values.partition(p, 0, length - 1)
            for (i in 0..split - 1) {
                assertTrue(values[i] < pivot, "<")
            }

            for (i in split..length - 1) {
                assertTrue(values[i] >= pivot, ">=")
            }
        }
    }
}