package org.jetbrains.bio.viktor

import org.apache.commons.math3.util.Precision
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import org.junit.runners.Parameterized.Parameters
import kotlin.test.assertFailsWith
import kotlin.test.assertNotEquals

@RunWith(Parameterized::class)
class F64FlatArrayGetSetTest(
        private val values: DoubleArray,
        private val offset: Int,
        size: Int,
        private val stride: Int
) {

    private val v = F64FlatArray(values, offset, stride, size)

    @Test fun get() {
        for (i in 0 until v.size) {
            assertEquals(values[offset + i * stride], v[i], Precision.EPSILON)
        }
    }

    @Test(expected = IndexOutOfBoundsException::class) fun getOutOfBounds() {
        v[100500]
    }

    @Test fun set() {
        for (i in 0 until v.size) {
            val copy = v.copy()
            copy[i] = 42.0
            assertEquals(42.0, copy[i], Precision.EPSILON)

            // Ensure all other elements are unchanged.
            for (j in 0 until v.size) {
                if (j == i) {
                    continue
                }

                assertEquals("$i/$j", v[j], copy[j], Precision.EPSILON)
            }
        }
    }

    @Test(expected = IndexOutOfBoundsException::class) fun setOutOfBounds() {
        v[100500] = 42.0
    }

    @Test fun setMagicScalar() {
        val copy = v.copy()
        copy.V[_I] = 42.0

        assertEquals(F64Array.full(copy.size, 42.0), copy)
    }

    @Test fun setMagicVector() {
        val other = F64Array.full(v.size, 42.0)
        val copy = v.copy()
        copy.V[_I] = other

        assertEquals(other, copy)
    }

    companion object {
        @Parameters(name = "F64Array({1}, {2}, {3})")
        @JvmStatic fun `data`() = listOf(
            // Normal case.
            arrayOf(doubleArrayOf(1.0, 2.0, 3.0), 0, 3, 1),
            // Offset and stride.
            arrayOf(doubleArrayOf(1.0, 2.0, 3.0), 1, 1, 3),
            // Empty array.
            arrayOf(doubleArrayOf(1.0, 2.0, 3.0), 3, 0, 1))
    }
}

class F64ArrayGetSetTest {
    @Test fun get() {
        val m = F64Array.of(
            0.0, 1.0,
            2.0, 3.0,
            4.0, 5.0
        ).reshape(3, 2)

        assertEquals(0.0, m[0, 0], Precision.EPSILON)
        assertEquals(1.0, m[0, 1], Precision.EPSILON)
        assertEquals(2.0, m[1, 0], Precision.EPSILON)
        assertEquals(3.0, m[1, 1], Precision.EPSILON)
        assertEquals(4.0, m[2, 0], Precision.EPSILON)
        assertEquals(5.0, m[2, 1], Precision.EPSILON)

        assertFailsWith<IndexOutOfBoundsException> {
            m[42, 42]
        }
    }

    @Test fun set() {
        val m = F64Array.of(
            0.0, 1.0,
            2.0, 3.0,
            4.0, 5.0
        ).reshape(3, 2)
        val copy = m.copy()
        copy[0, 1] = 42.0
        assertEquals(42.0, copy[0, 1], Precision.EPSILON)

        assertFailsWith<IndexOutOfBoundsException> {
            m[42, 42] = 100500.0
        }
    }

    @Test fun setMagicRowScalar() {
        val m = F64Array.of(
            0.0, 1.0,
            2.0, 3.0,
            4.0, 5.0
        ).reshape(3, 2)
        val copy = m.copy()
        copy.V[0] = 42.0
        assertEquals(F64Array.full(copy.shape[1], 42.0), copy.V[0])
    }

    @Test fun setMagicRowVector() {
        val m = F64Array.of(
            0.0, 1.0,
            2.0, 3.0,
            4.0, 5.0
        ).reshape(3, 2)
        val copy = m.copy()
        val v = F64Array.full(copy.shape[1], 42.0)
        copy.V[0] = v
        assertEquals(v, copy.V[0])

        for (r in 1 until copy.shape[0]) {
            assertNotEquals(v, copy.V[r])
            assertEquals(m.V[r], copy.V[r])
        }
    }

    @Test fun setMagicColumnScalar() {
        val m = F64Array.of(
            0.0, 1.0,
            2.0, 3.0,
            4.0, 5.0
        ).reshape(3, 2)
        val copy = m.copy()
        copy.V[_I, 0] = 42.0
        assertEquals(F64Array.full(copy.shape[0], 42.0), copy.V[_I, 0])
    }

    @Test fun setMagicColumnVector() {
        val m = F64Array.of(
            0.0, 1.0,
            2.0, 3.0,
            4.0, 5.0
        ).reshape(3, 2)
        val copy = m.copy()
        val v = F64Array.full(copy.shape[0], 42.0)
        copy.V[_I, 0] = v
        assertEquals(v, copy.V[_I, 0])

        for (c in 1 until copy.shape[1]) {
            assertNotEquals(v, copy.V[_I, c])
            assertEquals(m.V[_I, c], copy.V[_I, c])
        }
    }

    @Test fun setMagicMatrix() {
        val m = F64Array.of(
            0.0, 1.0,
            2.0, 3.0,
            4.0, 5.0
        ).reshape(3, 1, 2)

        val copy = m.copy()
        val replacement = F64Array.full(m.shape[1], m.shape[2], init = 42.0)
        copy.V[0] = replacement
        assertEquals(replacement, copy.V[0])

        for (d in 1 until m.shape[0]) {
            assertNotEquals(replacement, copy.V[d])
            assertEquals(m.V[d], copy.V[d])
        }
    }

    @Test fun setMagicArray() {
        val m = F64Array.of(
            0.0, 1.0,
            2.0, 3.0,
            4.0, 5.0
        ).reshape(3, 1, 2)
        val copy = m.copy()
        val replacement = m.copy().apply { fill(42.0) }
        copy.V[_I] = replacement
        assertEquals(replacement, copy)
        assertNotEquals(replacement, m)
    }

    @Test fun setMagicMatrixViaScalar() {
        val m = F64Array.of(
            0.0, 1.0,
            2.0, 3.0,
            4.0, 5.0
        ).reshape(3, 1, 2)

        val copy1 = m.copy()
        copy1.V[0] = 42.0
        val copy2 = m.copy()
        copy2.V[0] = F64Array.full(m.shape[1], m.shape[2], init = 42.0)
        assertEquals(copy1, copy2)
    }

    @Test fun setMagicArrayViaScalar() {
        val m = F64Array.of(
            0.0, 1.0,
            2.0, 3.0,
            4.0, 5.0
        ).reshape(3, 1, 2)
        val copy = m.copy()
        copy.V[_I] = 42.0
        assertNotEquals(copy, m)
        m.fill(42.0)
        assertEquals(copy, m)
    }
}