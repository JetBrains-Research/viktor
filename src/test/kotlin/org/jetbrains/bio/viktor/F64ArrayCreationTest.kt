package org.jetbrains.bio.viktor

import org.apache.commons.math3.util.Precision
import org.junit.Assert.assertEquals
import org.junit.Assert.assertArrayEquals
import org.junit.Test
import java.lang.IllegalStateException
import kotlin.test.assertTrue

class F64ArrayCreationTest {
    @Test fun specialization() {
        assertTrue(F64FlatArray(doubleArrayOf(1.0), stride = 10) !is F64DenseFlatArray)
        assertTrue(F64FlatArray(doubleArrayOf(1.0)) is F64DenseFlatArray)
        assertTrue(F64FlatArray(doubleArrayOf(1.0, 2.0), offset = 1, size = 1) is F64DenseFlatArray)
    }

    @Test fun of() {
        assertArrayEquals(
            doubleArrayOf(1.0), F64Array.of(1.0).toDoubleArray(), Precision.EPSILON
        )
        assertArrayEquals(
            doubleArrayOf(1.0, 2.0), F64Array.of(1.0, 2.0).toDoubleArray(), Precision.EPSILON
        )
        assertArrayEquals(
            doubleArrayOf(1.0, 2.0, 3.0), F64Array.of(1.0, 2.0, 3.0).toDoubleArray(), Precision.EPSILON
        )
    }

    @Test fun asF64Array() {
        assertEquals(F64Array.of(1.0), doubleArrayOf(1.0).asF64Array())
        assertEquals(
            F64Array.of(3.0), doubleArrayOf(1.0, 2.0, 3.0).asF64Array(offset = 2, size = 1)
        )
    }

    @Test fun asF64ArrayView() {
        val values = doubleArrayOf(1.0, 2.0, 3.0)
        val v = values.asF64Array(offset = 2, size = 1)
        v[0] = 42.0
        assertArrayEquals(doubleArrayOf(1.0, 2.0, 42.0), values, Precision.EPSILON)
    }

    @Test fun toF64Array() {
        assertEquals(
            F64Array.of(1.0, 2.0).reshape(1, 2),
            arrayOf(doubleArrayOf(1.0, 2.0)).toF64Array()
        )
        assertEquals(
            F64Array.of(1.0, 2.0).reshape(2, 1),
            arrayOf(doubleArrayOf(1.0), doubleArrayOf(2.0)).toF64Array()
        )

        assertEquals(
            F64Array.of(
                1.0, 2.0, 3.0,
                4.0, 5.0, 6.0
            ).reshape(2, 3),
            arrayOf(
                doubleArrayOf(1.0, 2.0, 3.0),
                doubleArrayOf(4.0, 5.0, 6.0)
            ).toF64Array()
        )
        assertEquals(
            F64Array.of(
                1.0, 2.0, 3.0,
                4.0, 5.0, 6.0
            ).reshape(3, 2),
            arrayOf(
                doubleArrayOf(1.0, 2.0),
                doubleArrayOf(3.0, 4.0),
                doubleArrayOf(5.0, 6.0)
            ).toF64Array()
        )

        assertEquals(
            F64Array.of(
                1.0, 2.0, 3.0, 4.0,
                5.0, 6.0, 7.0, 8.0
            ).reshape(2, 2, 2),
            arrayOf(
                arrayOf(
                    doubleArrayOf(1.0, 2.0),
                    doubleArrayOf(3.0, 4.0)
                ),
                arrayOf(
                    doubleArrayOf(5.0, 6.0),
                    doubleArrayOf(7.0, 8.0)
                )
            ).toF64Array())
    }

    @Test fun invoke() {
        assertEquals(
            F64Array.of(1.0, 2.0, 3.0),
            F64Array(3) { it + 1.0 }
        )
    }

    @Test fun full() {
        val v = F64Array.full(2, 42.0)
        assertEquals(2, v.size)
        assertEquals(F64Array.of(42.0, 42.0), v)
    }

    @Test fun concatenateFlat() {
        assertEquals(
            F64Array.of(1.0, 2.0, 3.0, 4.0, 5.0),
            F64Array.concatenate(
                F64Array.of(1.0, 2.0),
                F64Array.of(3.0),
                F64Array.of(4.0, 5.0)
            )
        )
    }

    @Test fun appendFlat() {
        assertEquals(
            F64Array.of(1.0, 2.0, 3.0, 4.0, 5.0),
            F64Array.of(1.0, 2.0).append(F64Array.of(3.0, 4.0, 5.0))
        )
    }

    @Test fun appendMatrix0() {
        assertEquals(
            F64Array.of(
                1.0, 2.0,
                3.0, 4.0,
                42.0, 42.0
            ).reshape(3, 2),
            F64Array.of(
                1.0, 2.0,
                3.0, 4.0
            ).reshape(2, 2).append(F64Array.of(42.0, 42.0).reshape(1, 2)))
    }

    @Test fun appendMatrix1() {
        assertEquals(
            F64Array.of(
                1.0, 2.0, 42.0,
                3.0, 4.0, 42.0
            ).reshape(2, 3),
            F64Array.of(
                1.0, 2.0,
                3.0, 4.0
            ).reshape(2, 2).append(F64Array.of(42.0, 42.0).reshape(2, 1), axis = 1)
        )
    }

    @Test(expected = IllegalArgumentException::class) fun appendMismatch() {
        F64Array.of(
            1.0, 2.0,
            3.0, 4.0
        ).reshape(2, 2).append(F64Array.of(42.0, 42.0).reshape(2, 1), axis = 0)
    }

    @Test fun copy() {
        val v = F64Array.of(1.0, 2.0, 3.0)
        val copy = v.copy()
        assertEquals(v, copy)
        v[0] = 42.0
        assertEquals(1.0, copy[0], Precision.EPSILON)
    }

    @Test fun reshapeNonFlat() {
        val v = F64Array.of(
            1.0, 2.0,
            3.0, 4.0
        )
        assertEquals(v.reshape(4, 1), v.reshape(2, 2).reshape(4, 1))
    }

    @Test(expected = IllegalStateException::class) fun reshapeSizeMismatch() {
        F64Array.of(
            1.0, 2.0,
            3.0, 4.0
        ).reshape(3, 2)
    }
}