package org.jetbrains.bio.viktor

import org.apache.commons.math3.util.Precision
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Test
import kotlin.test.assertNotEquals

class F64Matrix2Slicing {
    private val m = F64Array.of(0.0, 1.0,
                                2.0, 3.0,
                                4.0, 5.0).reshape(3, 2)

    @Test fun transposeUnit() {
        val m = F64Array(1, 1)
        assertEquals(m, m.T)
    }

    @Test fun transpose() {
        assertEquals(F64Array.of(0.0, 2.0, 4.0,
                                 1.0, 3.0, 5.0).reshape(2, 3),
                     m.T)
    }

    @Test fun rowView() {
        assertEquals(F64Array.of(0.0, 1.0), m.view[0])
        assertEquals(F64Array.of(2.0, 3.0), m.view[1])
        assertEquals(F64Array.of(4.0, 5.0), m.view[2])
    }

    @Test(expected = IndexOutOfBoundsException::class) fun rowViewOutOfBounds() {
        m[42]
    }

    @Test fun columnView() {
        assertEquals(F64Array.of(0.0, 2.0, 4.0), m.view[_I, 0])
        assertEquals(F64Array.of(1.0, 3.0, 5.0), m.view[_I, 1])
    }

    @Test(expected = IndexOutOfBoundsException::class) fun columnViewOutOfBounds() {
        m.view[_I, 42]
    }

    @Test fun reshape() {
        val v = F64Array.of(0.0, 1.0, 2.0, 3.0, 4.0, 5.0)
        assertArrayEquals(arrayOf(doubleArrayOf(0.0, 1.0, 2.0),
                                  doubleArrayOf(3.0, 4.0, 5.0)),
                          v.reshape(2, 3).toGenericArray())
        assertArrayEquals(arrayOf(doubleArrayOf(0.0, 1.0),
                                  doubleArrayOf(2.0, 3.0),
                                  doubleArrayOf(4.0, 5.0)),
                          v.reshape(3, 2).toGenericArray())
    }

    @Test fun reshapeWithStride() {
        val v = F64FlatArray(doubleArrayOf(0.0, 1.0, 2.0, 3.0,
                                           4.0, 5.0, 6.0, 7.0),
                             0, size = 4, stride = 2)
        assertArrayEquals(arrayOf(doubleArrayOf(0.0, 2.0),
                                  doubleArrayOf(4.0, 6.0)),
                          v.reshape(2, 2).toGenericArray())
    }
}

class F64Matrix2GetSet {
    private val m = F64Array.of(0.0, 1.0,
                                2.0, 3.0,
                                4.0, 5.0).reshape(3, 2)

    @Test fun get() {
        assertEquals(0.0, m[0, 0], Precision.EPSILON)
        assertEquals(1.0, m[0, 1], Precision.EPSILON)
        assertEquals(2.0, m[1, 0], Precision.EPSILON)
        assertEquals(3.0, m[1, 1], Precision.EPSILON)
        assertEquals(4.0, m[2, 0], Precision.EPSILON)
        assertEquals(5.0, m[2, 1], Precision.EPSILON)
    }

    @Test(expected = IndexOutOfBoundsException::class) fun getOutOfBounds() {
        m[42, 42]
    }

    @Test fun set() {
        val copy = m.copy()
        copy[0, 1] = 42.0
        assertEquals(42.0, copy[0, 1], Precision.EPSILON)
    }

    @Test(expected = IndexOutOfBoundsException::class) fun setOutOfBounds() {
        m[42, 42] = 100500.0
    }

    @Test fun setMagicRowScalar() {
        val copy = m.copy()
        copy.view[0] = 42.0
        assertEquals(F64Array.full(copy.shape[1], 42.0), copy.view[0])
    }

    @Test fun setMagicRowVector() {
        val copy = m.copy()
        val v = F64Array.full(copy.shape[1], 42.0)
        copy.view[0] = v
        assertEquals(v, copy.view[0])

        for (r in 1..copy.shape[0] - 1) {
            assertNotEquals(v, copy.view[r])
            assertEquals(m.view[r], copy.view[r])
        }
    }

    @Test fun setMagicColumnScalar() {
        val copy = m.copy()
        copy.view[_I, 0] = 42.0
        assertEquals(F64Array.full(copy.shape[0], 42.0), copy.view[_I, 0])
    }

    @Test fun setMagicColumnVector() {
        val copy = m.copy()
        val v = F64Array.full(copy.shape[0], 42.0)
        copy.view[_I, 0] = v
        assertEquals(v, copy.view[_I, 0])

        for (c in 1..copy.shape[1] - 1) {
            assertNotEquals(v, copy.view[_I, c])
            assertEquals(m.view[_I, c], copy.view[_I, c])
        }
    }
}

class F64Matrix2OpsTest {
    @Test fun unaryMinus() {
        val m = F64Array.of(0.0, 1.0,
                            2.0, 3.0,
                            4.0, 5.0).reshape(3, 2)
        val copy = m.copy()

        assertEquals(m, -(-m))
        assertEquals(-(m.view[0])[0], (-m)[0, 0], Precision.EPSILON)

        // Make sure [m] is unchanged!
        assertEquals(copy, m)
    }

    @Test fun equals() {
        val m = F64Array.of(0.0, 1.0,
                            2.0, 3.0,
                            4.0, 5.0).reshape(3, 2)

        assertEquals(m, m)
        assertEquals(m, m.copy())
        assertNotEquals(m, m.T)
    }

    @Test fun _toString() {
        assertEquals("[]", F64Array(0, 0).toString())
        assertEquals("[[]]", F64Array(1, 0).toString())
        assertEquals("[[0], [0]]", F64Array(2, 1).toString())
    }

    @Test fun toStringLarge() {
        val v = F64Array(1024) { it.toDouble() }
        assertEquals("[[0, 1], [2, 3], ..., [1020, 1021], [1022, 1023]]",
                     v.reshape(512, 2).toString(4))
        assertEquals("[[0, 1, ..., 510, 511], [512, 513, ..., 1022, 1023]]",
                     v.reshape(2, 512).toString(4))
    }
}
