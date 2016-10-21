package org.jetbrains.bio.viktor

import org.apache.commons.math3.util.Precision
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Test
import kotlin.test.assertNotEquals

class F64Matrix3Slicing {
    private val m = F64Vector.of(0.0, 1.0,
                                 2.0, 3.0,
                                 4.0, 5.0).reshape(3, 1, 2) as F64Matrix

    @Test fun view() {
        assertEquals(F64Vector.of(0.0, 1.0).reshape(1, 2), m.view(0))
        assertEquals(F64Vector.of(2.0, 3.0).reshape(1, 2), m.view(1))
        assertEquals(F64Vector.of(4.0, 5.0).reshape(1, 2), m.view(2))
    }

    @Test(expected = IndexOutOfBoundsException::class) fun viewOutOfBounds() {
        m.view(42)
    }

    @Test fun reshape() {
        val v = F64Vector.of(0.0, 1.0,
                             2.0, 3.0,
                             4.0, 5.0)
        assertArrayEquals(arrayOf(arrayOf(doubleArrayOf(0.0, 1.0)),
                                  arrayOf(doubleArrayOf(2.0, 3.0)),
                                  arrayOf(doubleArrayOf(4.0, 5.0))),
                          (v.reshape(3, 1, 2) as F64Matrix).toArray())
        assertArrayEquals(arrayOf(arrayOf(doubleArrayOf(0.0),
                                          doubleArrayOf(1.0)),
                                  arrayOf(doubleArrayOf(2.0),
                                          doubleArrayOf(3.0)),
                                  arrayOf(doubleArrayOf(4.0),
                                          doubleArrayOf(5.0))),
                          (v.reshape(3, 2, 1) as F64Matrix).toArray())
    }

    @Test fun reshapeWithStride() {
        val v = F64Vector.create(doubleArrayOf(0.0, 1.0, 2.0, 3.0,
                                               4.0, 5.0, 6.0, 7.0),
                                 0, size = 4, stride = 2)
        assertArrayEquals(arrayOf(arrayOf(doubleArrayOf(0.0, 2.0)),
                                  arrayOf(doubleArrayOf(4.0, 6.0))),
                          (v.reshape(2, 1, 2) as F64Matrix).toArray())
        assertArrayEquals(arrayOf(arrayOf(doubleArrayOf(0.0),
                                          doubleArrayOf(2.0)),
                                  arrayOf(doubleArrayOf(4.0),
                                          doubleArrayOf(6.0))),
                          (v.reshape(2, 2, 1) as F64Matrix).toArray())
    }
}

class F64Matrix3GetSet {
    private val m = F64Vector.of(0.0, 1.0,
                                 2.0, 3.0,
                                 4.0, 5.0)
            .reshape(3, 1, 2) as F64Matrix

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
        val replacement = F64Matrix.full(m.shape[1], m.shape[2], 42.0)
        copy[0] = replacement
        assertEquals(replacement, copy[0])

        for (d in 1..m.shape[0] - 1) {
            assertNotEquals(replacement, copy[d])
            assertEquals(m[d], copy[d])
        }
    }

    @Test fun setMagicMatrixViaScalar() {
        val copy1 = m.copy()
        copy1[0] = 42.0
        val copy2 = m.copy()
        copy2[0] = F64Matrix.full(m.shape[1], m.shape[2], 42.0)
        assertEquals(copy1, copy2)
    }

    @Test fun setMagicVector() {
        val copy = m.copy()
        val replacement = F64Vector.full(m.shape[2], 42.0)
        (copy.view(0) as F64Matrix)[0] = replacement
        assertEquals(replacement, (copy.view(0) as F64Matrix)[0])

        for (d in 1..m.shape[0] - 1) {
            for (r in 1..m.shape[1] - 1) {
                for (c in 1..m.shape[2] - 1) {
                    assertNotEquals(replacement[c], copy[d, r, c])
                    assertEquals(m[d, r, c], copy[d, r, c])
                }
            }
        }
    }

    @Test fun setMagicVectorViaScalar() {
        val copy1 = m.copy()
        (copy1.view(1) as F64Matrix)[0] = 42.0
        val copy2 = m.copy()
        (copy2.view(1) as F64Matrix)[0] = F64Vector.full(m.shape[2], 42.0)
        assertEquals(copy1, copy2)
    }

    @Test fun setMagicScalar() {
        val copy = m.copy()
        val replacement = F64Matrix.full(m.shape[1], m.shape[2], 42.0)
        copy[0] = 42.0
        assertEquals(replacement, copy[0])

        for (d in 1..m.shape[0] - 1) {
            assertNotEquals(replacement, copy[d])
            assertEquals(m[d], copy[d])
        }
    }
}

class F64Matrix3OpsTest {
    @Test fun equals() {
        val m = F64Matrix(2, 3, 4) { i, j, k -> 1.0 * i + 2 * j + 3 * k }

        assertEquals(m, m)
        assertEquals(m, m.copy())
        assertNotEquals(m, m.exp())
    }

    @Test fun _toString() {
        assertEquals("[]", F64Matrix(0, 0, 0).toString())
        assertEquals("[[[0]]]", F64Matrix(1, 1, 1).toString())
        assertEquals("[[[0], [0]], [[0], [0]], [[0], [0]]]",
                     F64Matrix(3, 2, 1).toString())
    }
}