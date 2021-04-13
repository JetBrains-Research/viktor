package org.jetbrains.bio.viktor

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Test
import kotlin.test.assertFailsWith

class F64FlatArraySlicingTest {

    @Test fun slice() {
        val v = F64Array.of(1.0, 2.0, 3.0)
        val slice = v.slice(1, 2)
        assertEquals(1, slice.size)
        assertEquals(F64Array.of(2.0), slice)

        slice[0] = 42.0
        assertEquals(F64Array.of(1.0, 42.0, 3.0), v)
    }

    @Test fun sliceMatrix() {
        val m = F64Array.of(
            1.0, 2.0, 3.0,
            4.0, 5.0, 6.0
        ).reshape(2, 3)
        assertEquals(
            F64Array.of(1.0, 2.0, 3.0).reshape(1, 3),
            m.slice(0, 1)
        )
        assertEquals(
            F64Array.of(4.0, 5.0, 6.0).reshape(1, 3),
            m.slice(1, 2)
        )
        assertEquals(
            F64Array.of(
                1.0,
                4.0
            ).reshape(2, 1),
            m.slice(0, 1, axis = 1)
        )
        assertEquals(
            F64Array.of(
                2.0, 3.0,
                5.0, 6.0
            ).reshape(2, 2),
            m.slice(1, 3, axis = 1)
        )
    }

    @Test fun sliceWithStep() {
        val v = F64Array.of(1.0, 2.0, 3.0, 4.0, 5.0, 6.0)

        v.slice(step = 2).let {
            assertEquals(3, it.size)
            assertEquals(F64Array.of(1.0, 3.0, 5.0), it)
        }

        v.slice(1, step = 2).let {
            assertEquals(3, it.size)
            assertEquals(F64Array.of(2.0, 4.0, 6.0), it)
        }

        v.slice(1, step = 3).let {
            assertEquals(2, it.size)
            assertEquals(F64Array.of(2.0, 5.0), it)
        }

        v.slice(1, step = 4).let {
            assertEquals(2, it.size)
            assertEquals(F64Array.of(2.0, 6.0), it)
        }
    }

    @Test(expected = IllegalStateException::class) fun sliceOutOfBounds() {
        F64Array(7).slice(10, 42)
    }

    @Test(expected = IllegalArgumentException::class) fun sliceFromNegative() {
        F64Array(7).slice(-1, 5)
    }

    @Test(expected = IllegalArgumentException::class) fun sliceToBeforeFrom() {
        F64Array(7).slice(3, 1)
    }

    @Test(expected = IllegalArgumentException::class) fun sliceStepNegative() {
        F64Array(7).slice(3, 5, -1)
    }

    @Test(expected = IllegalArgumentException::class) fun sliceInvalidAxis() {
        F64Array(7).slice(1, 3, axis = 1)
    }
}

class F64ArraySlicing {

    @Test fun rowView() {
        val m = F64Array.of(
            0.0, 1.0,
            2.0, 3.0,
            4.0, 5.0
        ).reshape(3, 2)
        assertEquals(F64Array.of(0.0, 1.0), m.V[0])
        assertEquals(F64Array.of(2.0, 3.0), m.V[1])
        assertEquals(F64Array.of(4.0, 5.0), m.V[2])

        assertFailsWith<IndexOutOfBoundsException> { m.V[42] }
    }

    @Test fun columnView() {
        val m = F64Array.of(
            0.0, 1.0,
            2.0, 3.0,
            4.0, 5.0
        ).reshape(3, 2)

        assertEquals(F64Array.of(0.0, 2.0, 4.0), m.V[_I, 0])
        assertEquals(F64Array.of(1.0, 3.0, 5.0), m.V[_I, 1])

        assertFailsWith<IndexOutOfBoundsException> { m.V[_I, 42] }
    }

    @Test fun view() {
        val m = F64Array.of(
            0.0, 1.0,
            2.0, 3.0,
            4.0, 5.0
        ).reshape(3, 1, 2)

        assertEquals(F64Array.of(0.0, 1.0).reshape(1, 2), m.V[0])
        assertEquals(F64Array.of(2.0, 3.0).reshape(1, 2), m.V[1])
        assertEquals(F64Array.of(4.0, 5.0).reshape(1, 2), m.V[2])

        assertFailsWith<IndexOutOfBoundsException> { m.V[42] }
    }

    @Test fun reshape2() {
        val v = F64Array.of(0.0, 1.0, 2.0, 3.0, 4.0, 5.0)
        assertArrayEquals(
            arrayOf(
                doubleArrayOf(0.0, 1.0, 2.0),
                doubleArrayOf(3.0, 4.0, 5.0)
            ),
            v.reshape(2, 3).toGenericArray()
        )
        assertArrayEquals(
            arrayOf(
                doubleArrayOf(0.0, 1.0),
                doubleArrayOf(2.0, 3.0),
                doubleArrayOf(4.0, 5.0)
            ),
            v.reshape(3, 2).toGenericArray()
        )
    }

    @Test fun reshape2WithStride() {
        val v = F64FlatArray.create(
            doubleArrayOf(
                0.0, 1.0, 2.0, 3.0,
                4.0, 5.0, 6.0, 7.0
            ),
            0, size = 4, stride = 2
        )
        assertArrayEquals(
            arrayOf(doubleArrayOf(0.0, 2.0), doubleArrayOf(4.0, 6.0)),
            v.reshape(2, 2).toGenericArray()
        )
    }

    @Test fun reshape3() {
        val v = F64Array.of(
            0.0, 1.0,
            2.0, 3.0,
            4.0, 5.0
        )
        assertArrayEquals(
            arrayOf(
                arrayOf(doubleArrayOf(0.0, 1.0)),
                arrayOf(doubleArrayOf(2.0, 3.0)),
                arrayOf(doubleArrayOf(4.0, 5.0))
            ),
            v.reshape(3, 1, 2).toGenericArray()
        )
        assertArrayEquals(
            arrayOf(
                arrayOf(doubleArrayOf(0.0), doubleArrayOf(1.0)),
                arrayOf(doubleArrayOf(2.0), doubleArrayOf(3.0)),
                arrayOf(doubleArrayOf(4.0), doubleArrayOf(5.0))
            ),
            v.reshape(3, 2, 1).toGenericArray()
        )
    }


    @Test fun reshape3WithStride() {
        val v = F64FlatArray.create(doubleArrayOf(0.0, 1.0, 2.0, 3.0,
            4.0, 5.0, 6.0, 7.0),
            0, size = 4, stride = 2)
        assertArrayEquals(
            arrayOf(
                arrayOf(doubleArrayOf(0.0, 2.0)),
                arrayOf(doubleArrayOf(4.0, 6.0))
            ),
            v.reshape(2, 1, 2).toGenericArray()
        )
        assertArrayEquals(
            arrayOf(
                arrayOf(doubleArrayOf(0.0), doubleArrayOf(2.0)),
                arrayOf(doubleArrayOf(4.0), doubleArrayOf(6.0))
            ),
            v.reshape(2, 2, 1).toGenericArray()
        )
    }

    @Test fun along0() {
        val a = F64Array.of(
            1.0, 2.0, 3.0,
            4.0, 5.0, 6.0
        ).reshape(2, 3)

        a.along(0).forEach { it /= it[0] }
        assertEquals(
            F64Array.of(
                1.0, 2.0, 3.0,
                1.0, 5.0 / 4.0, 6.0 / 4.0
            ).reshape(2, 3),
            a
        )
    }

    @Test fun along1() {
        val a = F64Array.of(1.0, 2.0, 3.0,
            4.0, 5.0, 6.0).reshape(2, 3)

        a.along(1).forEach { it /= it[1] }
        assertEquals(
            F64Array.of(
                1.0 / 4.0, 2.0 / 5.0, 3.0 / 6.0,
                1.0, 1.0, 1.0
            ).reshape(2, 3),
            a
        )
    }

    @Test fun view2() {
        val a = DoubleArray(8) { it.toDouble() }.asF64Array().reshape(2, 2, 2)
        val aView = a.view(0, 1)
        assertEquals(F64Array.of(0.0, 1.0, 4.0, 5.0).reshape(2, 2), aView)
        aView.expInPlace()
        assertEquals(F64Array.of(2.0, 3.0, 6.0, 7.0).reshape(2, 2), a.view(1, 1))
    }
}