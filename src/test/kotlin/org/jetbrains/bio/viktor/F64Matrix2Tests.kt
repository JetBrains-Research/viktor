package org.jetbrains.bio.viktor

import org.apache.commons.math3.util.Precision
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Test
import kotlin.test.assertNotEquals

class F64Matrix2Slicing {
    private val m = F64Vector.of(0.0, 1.0,
                                 2.0, 3.0,
                                 4.0, 5.0).reshape(3, 2) as F64Matrix

    @Test fun transposeUnit() {
        val m = F64Matrix(1, 1)
        assertEquals(m, m.T)
    }

    @Test fun transpose() {
        assertEquals(F64Vector.of(0.0, 2.0, 4.0,
                                  1.0, 3.0, 5.0).reshape(2, 3),
                     m.T)
    }

    @Test fun rowView() {
        assertEquals(F64Vector.of(0.0, 1.0), m.view(0, along = 0) as F64Vector)
        assertEquals(F64Vector.of(2.0, 3.0), m.view(1, along = 0) as F64Vector)
        assertEquals(F64Vector.of(4.0, 5.0), m.view(2, along = 0) as F64Vector)
    }

    @Test(expected = IndexOutOfBoundsException::class) fun rowViewOutOfBounds() {
        m.view(42, along = 0) as F64Vector
    }

    @Test fun columnView() {
        assertEquals(F64Vector.of(0.0, 2.0, 4.0), m.view(0, along = 1) as F64Vector)
        assertEquals(F64Vector.of(1.0, 3.0, 5.0), m.view(1, along = 1) as F64Vector)
    }

    @Test(expected = IndexOutOfBoundsException::class) fun columnViewOutOfBounds() {
        m.view(42, along = 1) as F64Vector
    }

    @Test fun reshape() {
        val v = F64Vector.of(0.0, 1.0, 2.0, 3.0, 4.0, 5.0)
        assertArrayEquals(arrayOf(doubleArrayOf(0.0, 1.0, 2.0),
                                  doubleArrayOf(3.0, 4.0, 5.0)),
                          (v.reshape(2, 3) as F64Matrix).toArray())
        assertArrayEquals(arrayOf(doubleArrayOf(0.0, 1.0),
                                  doubleArrayOf(2.0, 3.0),
                                  doubleArrayOf(4.0, 5.0)),
                          (v.reshape(3, 2) as F64Matrix).toArray())
    }

    @Test fun reshapeWithStride() {
        val v = F64Vector.create(doubleArrayOf(0.0, 1.0, 2.0, 3.0,
                                               4.0, 5.0, 6.0, 7.0),
                                 0, size = 4, stride = 2)
        assertArrayEquals(arrayOf(doubleArrayOf(0.0, 2.0),
                                  doubleArrayOf(4.0, 6.0)),
                          (v.reshape(2, 2) as F64Matrix).toArray())
    }
}

class F64Matrix2GetSet {
    private val m = F64Vector.of(0.0, 1.0,
                                 2.0, 3.0,
                                 4.0, 5.0).reshape(3, 2) as F64Matrix

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
        copy[0] = 42.0
        assertEquals(F64Vector.full(copy.shape[1], 42.0), copy[0])
    }

    @Test fun setMagicRowVector() {
        val copy = m.copy()
        val v = F64Vector.full(copy.shape[1], 42.0)
        copy[0] = v
        assertEquals(v, copy[0])

        for (r in 1..copy.shape[0] - 1) {
            assertNotEquals(v, copy[r])
            assertEquals(m[r], copy[r])
        }
    }

    @Test fun setMagicColumnScalar() {
        val copy = m.copy()
        copy[_I, 0] = 42.0
        assertEquals(F64Vector.full(copy.shape[0], 42.0), copy[_I, 0])
    }

    @Test fun setMagicColumnVector() {
        val copy = m.copy()
        val v = F64Vector.full(copy.shape[0], 42.0)
        copy[_I, 0] = v
        assertEquals(v, copy[_I, 0])

        for (c in 1..copy.shape[1] - 1) {
            assertNotEquals(v, copy[_I, c])
            assertEquals(m[_I, c], copy[_I, c])
        }
    }
}

class F64Matrix2OpsTest {
    @Test fun unaryMinus() {
        val m = F64Vector.of(0.0, 1.0,
                             2.0, 3.0,
                             4.0, 5.0).reshape(3, 2) as F64Matrix
        val copy = m.copy()

        assertEquals(m, -(-m))
        assertEquals(-(m[0] as F64Vector)[0], (-m)[0, 0], Precision.EPSILON)

        // Make sure [m] is unchanged!
        assertEquals(copy, m)
    }

    @Test fun equals() {
        val m = F64Vector.of(0.0, 1.0,
                             2.0, 3.0,
                             4.0, 5.0).reshape(3, 2)

        assertEquals(m, m)
        assertEquals(m, m.copy())
        assertNotEquals(m, m.T)
    }

    @Test fun _toString() {
        assertEquals("[]", F64Matrix(0, 0).toString())
        assertEquals("[[]]", F64Matrix(1, 0).toString())
        assertEquals("[[0], [0]]", F64Matrix(2, 1).toString())
    }

    @Test fun toStringLarge() {
        val v = F64Vector(1024) { it.toDouble() }
        assertEquals("[[0, 1], [2, 3], ..., [1020, 1021], [1022, 1023]]",
                     v.reshape(512, 2).toString(4))
        assertEquals("[[0, 1, ..., 510, 511], [512, 513, ..., 1022, 1023]]",
                     v.reshape(2, 512).toString(4))
    }
}
