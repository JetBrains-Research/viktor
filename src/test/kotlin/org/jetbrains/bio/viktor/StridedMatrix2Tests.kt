package org.jetbrains.bio.viktor

import org.apache.commons.math3.util.Precision
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Test
import kotlin.test.assertNotEquals

class StridedMatrix2Slicing {
    private val m = StridedVector.of(0.0, 1.0,
                                     2.0, 3.0,
                                     4.0, 5.0).reshape(3, 2)

    @Test fun transposeUnit() {
        val m = StridedMatrix(1, 1)
        assertEquals(m, m.T)
    }

    @Test fun transpose() {
        assertEquals(StridedVector.of(0.0, 2.0, 4.0,
                                      1.0, 3.0, 5.0).reshape(2, 3),
                     m.T)
    }

    @Test fun rowView() {
        assertEquals(StridedVector.of(0.0, 1.0), m.rowView(0))
        assertEquals(StridedVector.of(2.0, 3.0), m.rowView(1))
        assertEquals(StridedVector.of(4.0, 5.0), m.rowView(2))
    }

    @Test(expected = IndexOutOfBoundsException::class) fun rowViewOutOfBounds() {
        m.rowView(42)
    }

    @Test fun columnView() {
        assertEquals(StridedVector.of(0.0, 2.0, 4.0), m.columnView(0))
        assertEquals(StridedVector.of(1.0, 3.0, 5.0), m.columnView(1))
    }

    @Test(expected = IndexOutOfBoundsException::class) fun columnViewOutOfBounds() {
        m.columnView(42)
    }

    @Test fun reshape() {
        val v = StridedVector.of(0.0, 1.0, 2.0, 3.0, 4.0, 5.0)
        assertArrayEquals(arrayOf(doubleArrayOf(0.0, 1.0, 2.0),
                                  doubleArrayOf(3.0, 4.0, 5.0)),
                          v.reshape(2, 3).toArray())
        assertArrayEquals(arrayOf(doubleArrayOf(0.0, 1.0),
                                  doubleArrayOf(2.0, 3.0),
                                  doubleArrayOf(4.0, 5.0)),
                          v.reshape(3, 2).toArray())
    }

    @Test fun reshapeWithStride() {
        val v = StridedVector.create(doubleArrayOf(0.0, 1.0, 2.0, 3.0,
                                                   4.0, 5.0, 6.0, 7.0),
                                     0, 4, stride = 2)
        assertArrayEquals(arrayOf(doubleArrayOf(0.0, 2.0),
                                  doubleArrayOf(4.0, 6.0)),
                          v.reshape(2, 2).toArray())
    }
}

class StridedMatrix2GetSet {
    private val m = StridedVector.of(0.0, 1.0,
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
        copy[0] = 42.0
        assertEquals(StridedVector.full(copy.columnsNumber, 42.0), copy[0])
    }

    @Test fun setMagicRowVector() {
        val copy = m.copy()
        val v = StridedVector.full(copy.columnsNumber, 42.0)
        copy[0] = v
        assertEquals(v, copy[0])

        for (r in 1..copy.rowsNumber - 1) {
            assertNotEquals(v, copy[r])
            assertEquals(m[r], copy[r])
        }
    }

    @Test fun setMagicColumnScalar() {
        val copy = m.copy()
        copy[_I, 0] = 42.0
        assertEquals(StridedVector.full(copy.rowsNumber, 42.0), copy[_I, 0])
    }

    @Test fun setMagicColumnVector() {
        val copy = m.copy()
        val v = StridedVector.full(copy.rowsNumber, 42.0)
        copy[_I, 0] = v
        assertEquals(v, copy[_I, 0])

        for (c in 1..copy.columnsNumber - 1) {
            assertNotEquals(v, copy[_I, c])
            assertEquals(m[_I, c], copy[_I, c])
        }
    }
}

class StridedMatrix2OpsTest {
    @Test fun equals() {
        val m = StridedVector.of(0.0, 1.0,
                                 2.0, 3.0,
                                 4.0, 5.0).reshape(3, 2)

        assertEquals(m, m)
        assertEquals(m, m.copy())
        assertNotEquals(m, m.T)
    }

    @Test fun _toString() {
        assertEquals("[]", StridedMatrix(0, 0).toString())
        assertEquals("[[]]", StridedMatrix(1, 0).toString())
        assertEquals("[[0], [0]]", StridedMatrix(2, 1).toString())
    }

    @Test fun toStringLarge() {
        val v = StridedVector(1024) { it.toDouble() }
        assertEquals("[[0, 1], [2, 3], ..., [1020, 1021], [1022, 1023]]",
                     v.reshape(512, 2).toString(4))
        assertEquals("[[0, 1, ..., 510, 511], [512, 513, ..., 1022, 1023]]",
                     v.reshape(2, 512).toString(4))
    }
}