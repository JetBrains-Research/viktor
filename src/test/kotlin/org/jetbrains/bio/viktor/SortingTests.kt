package org.jetbrains.bio.viktor

import org.apache.commons.math3.util.Precision
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Test
import java.lang.IllegalArgumentException
import java.lang.UnsupportedOperationException
import java.util.*
import kotlin.test.assertTrue

class SortingTests {
    @Test fun partition() {
        assertEquals(
                F64Array.of(1.0, 2.0, 3.0, 4.0),
                F64Array.of(3.0, 4.0, 2.0, 1.0).apply { partition(2) }
        )
    }

    @Test fun partitionInternal() {
        val values = F64Array.of(0.0, 1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0)
        val length = values.size
        for (p in 0 until length) {
            values.shuffle()
            val pivot = values[p]
            val split = values.partition(p, 0, length - 1)
            for (i in 0 until split) {
                assertTrue(values[i] < pivot, "<")
            }

            for (i in split until length) {
                assertTrue(values[i] >= pivot, ">=")
            }
        }
    }

    @Test fun sort() {
        val values = Random().doubles().limit(100).toArray()
        val v = values.clone().asF64Array()
        v.sort()
        assertArrayEquals(values.sortedArray(), v.toDoubleArray(),
                          Precision.EPSILON)
    }

    @Test fun argSort() {
        val v = F64Array.of(42.0, 2.0, -1.0, 0.0, 4.0, 2.0)
        val indices = v.argSort()
        val copy = v.toDoubleArray()
        copy.sort()

        for ((i, j) in indices.withIndex()) {
            assertEquals(copy[i], v[j], Precision.EPSILON)
        }
    }

    @Test fun argSortReverse() {
        val v = F64Array.of(42.0, 2.0, -1.0, 0.0, 4.0, 2.0)
        val indices = v.argSort(reverse = true)
        val copy = v.toDoubleArray()
        copy.sort()

        for ((i, j) in indices.withIndex()) {
            assertEquals(copy[copy.size - 1 - i], v[j], Precision.EPSILON)
        }
    }

    @Test fun argSortWithNaN() {
        val values = doubleArrayOf(42.0, 2.0, -1.0, 0.0, 4.0, 2.0)
        val indices = values.asF64Array().argSort()
        assertArrayEquals(intArrayOf(2, 3, 1, 5, 4, 0), indices)

        val v = F64FlatArray(
            doubleArrayOf(
                Double.NaN, Double.NaN,  // Prefix.
                                           42.0, Double.NaN, 2.0,
                                           Double.NaN, -1.0,
                                           Double.NaN, 0.0,
                                           Double.NaN, 4.0,
                                           Double.NaN, 2.0
            ),
                             offset = 2, size = values.size, stride = 2
        )
        v.reorder(indices)
        assertArrayEquals(
            doubleArrayOf(-1.0, 0.0, 2.0, 2.0, 4.0, 42.0),
                          v.toDoubleArray(), Precision.EPSILON
        )
    }

    @Test fun reorderFlat() {
        val v = F64Array.of(1.0, 2.0, 3.0)
        v.reorder(intArrayOf(2, 1, 0))
        assertEquals(F64Array.of(3.0, 2.0, 1.0), v)
    }

    @Test fun reorderMatrix0() {
        val m = F64Array.of(
            1.0, 2.0, 3.0,
                            4.0, 5.0, 6.0
        ).reshape(2, 3)
        m.reorder(intArrayOf(1, 0))
        assertEquals(
            F64Array.of(
                4.0, 5.0, 6.0,
                                 1.0, 2.0, 3.0
            ).reshape(2, 3),
                     m
        )
    }

    @Test fun reorderMatrix1() {
        val m = F64Array.of(
            1.0, 2.0, 3.0,
                            4.0, 5.0, 6.0
        ).reshape(2, 3)
        m.reorder(intArrayOf(2, 1, 0), axis = 1)
        assertEquals(
            F64Array.of(
                3.0, 2.0, 1.0,
                                 6.0, 5.0, 4.0
            ).reshape(2, 3),
                     m
        )
    }

    @Test(expected = IllegalArgumentException::class) fun reorderMatrix2() {
        val m = F64Array.of(
            1.0, 2.0, 3.0,
            4.0, 5.0, 6.0
        ).reshape(2, 3)
        m.reorder(intArrayOf(2, 1, 0), axis = 0)
    }
}
