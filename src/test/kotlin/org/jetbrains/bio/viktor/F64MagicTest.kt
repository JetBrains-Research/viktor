package org.jetbrains.bio.viktor

import org.junit.Assert.assertArrayEquals
import org.junit.Test
import kotlin.test.assertEquals

class F64MagicTest {
    @Test fun ravelUnravel() {
        for (indices in arrayOf(intArrayOf(1, 2, 3),
                                intArrayOf(3, 2, 1),
                                intArrayOf(1, 1, 1),
                                intArrayOf(0, 0, 0))) {
            val shape = intArrayOf(4, 5, 6)
            assertArrayEquals(indices, unravelIndex(ravelMultiIndex(indices, shape), shape))
        }
    }

    @Test(expected = IndexOutOfBoundsException::class) fun unravelImproper() {
        unravelIndex(421, intArrayOf(4, 5, 6))
    }

    // See examples in `numpy.unravel_index` docstring.
    @Test fun unravelAgainstNumPy() {
        assertArrayEquals(intArrayOf(3, 4), unravelIndex(22, intArrayOf(7, 6)))
        assertArrayEquals(intArrayOf(6, 1), unravelIndex(37, intArrayOf(7, 6)))
        assertArrayEquals(intArrayOf(6, 5), unravelIndex(41, intArrayOf(7, 6)))

        assertArrayEquals(intArrayOf(3, 1, 4, 1),
                          unravelIndex(1621, intArrayOf(6, 7, 8, 9)))
    }

    // See examples in `numpy.ravel_multi_index` docstring.
    @Test fun ravelAgainstNumPy() {
        assertEquals(22, ravelMultiIndex(intArrayOf(3, 4), intArrayOf(7, 6)))
        assertEquals(37, ravelMultiIndex(intArrayOf(6, 1), intArrayOf(7, 6)))
        assertEquals(41, ravelMultiIndex(intArrayOf(6, 5), intArrayOf(7, 6)))

        assertEquals(1621, ravelMultiIndex(intArrayOf(3, 1, 4, 1), intArrayOf(6, 7, 8, 9)))
    }
}