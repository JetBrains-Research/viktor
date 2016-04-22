package org.jetbrains.bio.viktor

import org.apache.commons.math3.util.CombinatoricsUtils
import org.apache.commons.math3.util.Precision
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.*
import java.util.stream.IntStream
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class QuickSelectTest {
    @Test fun testPartition() {
        val values = doubleArrayOf(0.0, 1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0)
                .asStrided()
        val length = values.size
        for (p in 0..length - 1) {
            values.shuffle()
            val pivot = values[p]
            val split = QuickSelect.partition(values, 0, length - 1, p)
            for (i in 0..split - 1) {
                assertTrue(values[i] < pivot, "<")
            }

            for (i in split..length - 1) {
                assertTrue(values[i] >= pivot, ">=")
            }
        }
    }

    @Test fun testArrayQuantile() {
        val values = IntStream.range(0, 1024).mapToDouble { it.toDouble() }
                .toArray().asStrided()
        for (i in values.indices) {
            values.shuffle()
            val q = i.toDouble() / values.size
            assertEquals(i.toDouble(), values.quantile(q), message = "$q")
        }
    }

    @Test(expected = IllegalArgumentException::class) fun testArrayQuantileEmpty() {
        doubleArrayOf().asStrided().quantile(0.5)
    }

    @Test fun testArrayQuantileSingleton() {
        val values = doubleArrayOf(42.0).asStrided()
        assertEquals(42.0, values.quantile(0.0), Precision.EPSILON)
        assertEquals(42.0, values.quantile(0.6), Precision.EPSILON)
        assertEquals(42.0, values.quantile(1.0), Precision.EPSILON)
    }

    @Test fun testArrayQuantileLarge() {
        val values = Random().doubles(1 shl 16).toArray().asStrided()
        assertEquals(values.max(), values.quantile(1.0), Precision.EPSILON)
    }
}

class ShuffleTest {
    @Test fun distribution() {
        val values = doubleArrayOf(0.0, 1.0, 2.0, 3.0).asStrided()
        val counts = HashMap<StridedVector, Int>()

        val `n!` = CombinatoricsUtils.factorial(4).toInt()
        for (i in 0..5000 * `n!`) {
            values.shuffle()
            val p = values.copy()
            counts[p] = (counts[p] ?: 0) + 1
        }

        assertEquals(`n!`, counts.size)

        val total = counts.values.sum()
        for (count in counts.values) {
            val p = count.toDouble() / total
            assertEquals(1.0 / `n!`, p, 1e-2)
        }
    }
}