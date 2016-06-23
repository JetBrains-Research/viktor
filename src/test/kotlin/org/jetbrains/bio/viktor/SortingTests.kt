package org.jetbrains.bio.viktor

import org.apache.commons.math3.util.Precision
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.*
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

    @Test fun sort() {
        val values = Random().doubles().limit(100).toArray()
        val v = values.clone().asStrided()
        v.sort()
        assertArrayEquals(values.sortedArray(), v.toArray(), Precision.EPSILON)
    }

    @Test fun argSort() {
        val v = StridedVector.of(42.0, 2.0, -1.0, 0.0, 4.0, 2.0)
        val indices = v.argSort()
        val copy = v.toArray()
        copy.sort()

        for ((i, j) in indices.withIndex()) {
            assertEquals(copy[i], v[j], Precision.EPSILON)
        }
    }

    @Test fun argSortReverse() {
        val v = StridedVector.of(42.0, 2.0, -1.0, 0.0, 4.0, 2.0)
        val indices = v.argSort(reverse = true)
        val copy = v.toArray()
        copy.sort()

        for ((i, j) in indices.withIndex()) {
            assertEquals(copy[copy.size - 1 - i], v[j], Precision.EPSILON)
        }
    }

    @Test fun argSortWithNaN() {
        val values = doubleArrayOf(42.0, 2.0, -1.0, 0.0, 4.0, 2.0)
        val indices = values.asStrided().argSort()
        assertArrayEquals(intArrayOf(2, 3, 1, 5, 4, 0), indices)

        val v = StridedVector.create(doubleArrayOf(Double.NaN, Double.NaN, // Prefix.
                                                   42.0, Double.NaN, 2.0,
                                                   Double.NaN, -1.0,
                                                   Double.NaN, 0.0,
                                                   Double.NaN, 4.0,
                                                   Double.NaN, 2.0),
                                     offset = 2, size = values.size, stride = 2)
        v.reorder(indices)
        assertArrayEquals(doubleArrayOf(-1.0, 0.0, 2.0, 2.0, 4.0, 42.0),
                          v.toArray(), Precision.EPSILON)
    }
}