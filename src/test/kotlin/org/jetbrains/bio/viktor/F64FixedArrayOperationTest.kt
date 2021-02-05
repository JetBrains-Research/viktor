package org.jetbrains.bio.viktor

import org.junit.Assert
import org.junit.Test
import kotlin.test.assertNotEquals

class F64FixedArrayOperationTest {

    @Test fun equals() {
        val m = F64Array(2, 3, 4) { i, j, k -> 1.0 * i + 2 * j + 3 * k }

        Assert.assertEquals(m, m)
        Assert.assertEquals(m, m.copy())
        assertNotEquals(m, m.exp())
        assertNotEquals(m.toArray(), m)
        assertNotEquals(m, m.flatten())
    }

    @Test fun toString2() {
        Assert.assertEquals("[[0], [0]]", F64Array(2, 1).toString())
        Assert.assertEquals("[[0, 0]]", F64Array(1, 2).toString())
    }

    @Test fun toString2Large() {
        val v = F64Array(1024) { it.toDouble() }
        Assert.assertEquals(
            "[[0, 1], [2, 3], ..., [1020, 1021], [1022, 1023]]",
            v.reshape(512, 2).toString(4)
        )
        Assert.assertEquals(
            "[[0, 1, ..., 510, 511], [512, 513, ..., 1022, 1023]]",
            v.reshape(2, 512).toString(4)
        )
    }

    @Test fun toString3() {
        Assert.assertEquals("[[[0]]]", F64Array(1, 1, 1).toString())
        Assert.assertEquals(
            "[[[0], [0]], [[0], [0]], [[0], [0]]]",
            F64Array(3, 2, 1).toString()
        )
    }

    @Test fun toStringNormal() {
        Assert.assertEquals("[42]", F64Array.of(42.0).toString())
        Assert.assertEquals("[0, 1, 2, 3]", gappedArray(0..3).toString())
    }

    @Test fun toStringLarge() {
        val v = gappedArray(0..1023)
        Assert.assertEquals("[0, ..., 1023]", v.toString(2))
        Assert.assertEquals("[0, ..., 1022, 1023]", v.toString(3))
        Assert.assertEquals("[0, 1, ..., 1022, 1023]", v.toString(4))
    }

    @Test fun toStringNanInf() {
        val v = F64Array.of(
            Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY, 42.0
        )
        Assert.assertEquals("[nan, inf, -inf, 42]", v.toString())
    }

}