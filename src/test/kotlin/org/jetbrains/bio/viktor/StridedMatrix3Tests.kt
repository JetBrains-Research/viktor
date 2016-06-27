package org.jetbrains.bio.viktor

import org.apache.commons.math3.util.Precision
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Test
import kotlin.test.assertNotEquals

class StridedMatrix3Slicing {
    private val m = StridedVector.of(0.0, 1.0,
                                     2.0, 3.0,
                                     4.0, 5.0).reshape(3, 1, 2)

    @Test fun view() {
        assertEquals(StridedVector.of(0.0, 1.0).reshape(1, 2), m.view(0))
        assertEquals(StridedVector.of(2.0, 3.0).reshape(1, 2), m.view(1))
        assertEquals(StridedVector.of(4.0, 5.0).reshape(1, 2), m.view(2))
    }

    @Test(expected = IndexOutOfBoundsException::class) fun viewOutOfBounds() {
        m.view(42)
    }

    @Test fun reshape() {
        val v = StridedVector.of(0.0, 1.0,
                                 2.0, 3.0,
                                 4.0, 5.0)
        assertArrayEquals(arrayOf(arrayOf(doubleArrayOf(0.0, 1.0)),
                                  arrayOf(doubleArrayOf(2.0, 3.0)),
                                  arrayOf(doubleArrayOf(4.0, 5.0))),
                          v.reshape(3, 1, 2).toArray())
        assertArrayEquals(arrayOf(arrayOf(doubleArrayOf(0.0),
                                          doubleArrayOf(1.0)),
                                  arrayOf(doubleArrayOf(2.0),
                                          doubleArrayOf(3.0)),
                                  arrayOf(doubleArrayOf(4.0),
                                          doubleArrayOf(5.0))),
                          v.reshape(3, 2, 1).toArray())
    }

    @Test fun reshapeWithStride() {
        val v = StridedVector.create(doubleArrayOf(0.0, 1.0, 2.0, 3.0,
                                                   4.0, 5.0, 6.0, 7.0),
                                     0, 4, stride = 2)
        assertArrayEquals(arrayOf(arrayOf(doubleArrayOf(0.0, 2.0)),
                                  arrayOf(doubleArrayOf(4.0, 6.0))),
                          v.reshape(2, 1, 2).toArray())
        assertArrayEquals(arrayOf(arrayOf(doubleArrayOf(0.0),
                                          doubleArrayOf(2.0)),
                                  arrayOf(doubleArrayOf(4.0),
                                          doubleArrayOf(6.0))),
                          v.reshape(2, 2, 1).toArray())
    }
}

class StridedMatrix3GetSet {
    private val m = StridedVector.of(0.0, 1.0,
                                     2.0, 3.0,
                                     4.0, 5.0).reshape(3, 1, 2)

    @Test fun get() {
        assertEquals(0.0, m[0, 0, 0], Precision.EPSILON)
        assertEquals(1.0, m[0, 0, 1], Precision.EPSILON)
        assertEquals(2.0, m[1, 0, 0], Precision.EPSILON)
        assertEquals(3.0, m[1, 0, 1], Precision.EPSILON)
        assertEquals(4.0, m[2, 0, 0], Precision.EPSILON)
        assertEquals(5.0, m[2, 0, 1], Precision.EPSILON)
    }

    @Test(expected = IndexOutOfBoundsException::class) fun getOutOfBounds() {
        m[42, 42, 42]
    }

    @Test fun set() {
        val copy = m.copy()
        copy[1, 0, 1] = 42.0
        assertEquals(42.0, copy[1, 0, 1], Precision.EPSILON)
    }

    @Test(expected = IndexOutOfBoundsException::class) fun setOutOfBounds() {
        m[42, 42, 42] = 100500.0
    }

    @Test fun setMagicMatrix() {
        val copy = m.copy()
        val replacement = StridedMatrix.full(m.rowsNumber, m.columnsNumber, 42.0)
        copy[0] = replacement
        assertEquals(replacement, copy[0])

        for (d in 1..m.depth - 1) {
            assertNotEquals(replacement, copy[d])
            assertEquals(m[d], copy[d])
        }
    }

    @Test fun setMagicMatrixViaScalar() {
        val copy1 = m.copy()
        copy1[0] = 42.0
        val copy2 = m.copy()
        copy2[0] = StridedMatrix.full(m.rowsNumber, m.columnsNumber, 42.0)
        assertEquals(copy1, copy2)
    }

    @Test fun setMagicVector() {
        val copy = m.copy()
        val replacement = StridedVector.full(m.columnsNumber, 42.0)
        copy[0, 0] = replacement
        assertEquals(replacement, copy[0, 0])

        for (d in 1..m.depth - 1) {
            for (r in 1..m.rowsNumber - 1) {
                assertNotEquals(replacement, copy[d, r])
                assertEquals(m[d, r], copy[d, r])
            }
        }
    }

    @Test fun setMagicVectorViaScalar() {
        val copy1 = m.copy()
        copy1[1, 0] = 42.0
        val copy2 = m.copy()
        copy2[1, 0] = StridedVector.full(m.columnsNumber, 42.0)
        assertEquals(copy1, copy2)
    }

    @Test fun setMagicScalar() {
        val copy = m.copy()
        val replacement = StridedMatrix.full(m.rowsNumber, m.columnsNumber, 42.0)
        copy[0] = 42.0
        assertEquals(replacement, copy[0])

        for (d in 1..m.depth - 1) {
            assertNotEquals(replacement, copy[d])
            assertEquals(m[d], copy[d])
        }
    }
}