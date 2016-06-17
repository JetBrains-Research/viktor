package org.jetbrains.bio.viktor

import org.apache.commons.math3.distribution.NormalDistribution
import org.apache.commons.math3.util.Precision
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.stream.IntStream
import kotlin.test.assertTrue

class LargeDenseVectorTest{
    @Test fun testLogAddExp() {
        val d = NormalDistribution(0.0, 42.0)
        val v1 = d.sample(128).asStrided()
        val v2 = d.sample(128).asStrided()
        val dst = v1.logAddExp(v2)

        for (i in 0..dst.size - 1) {
            assertTrue(Precision.equals(v1[i] logAddExp v2[i],
                                        dst[i], 5))
        }
    }

    @Test fun testLogRescale() {
        val v = NormalDistribution(0.0, 42.0).sample(128).asStrided()
        v.logRescale()
        assertTrue(Precision.equals(1.0, Math.exp(v.logSumExp()), 1e-8))
    }

    @Test fun testRescale() {
        val v = getLargeDenseVector()
        v.rescale()
        assertTrue(Precision.equals(1.0, v.sum(), 5))
    }

    @Test fun testArgMinMax() {
        val v = getLargeDenseVector()
        assertEquals(v.min(), v.toArray().min()!!, Precision.EPSILON)
        assertEquals(v.argMin(), 0)
        assertEquals(v.max(), v.toArray().max()!!, Precision.EPSILON)
        assertEquals(v.argMax(), v.size - 1)
    }

    @Test fun testAdd() {
        val v = getLargeDenseVector()
        val u = v + v
        for (pos in 0..v.size - 1) {
            assertEquals(v[pos] + v[pos], u[pos], Precision.EPSILON)
        }
    }

    @Test fun testAddUpdate() {
        val v = getLargeDenseVector()
        val u = v.plus(42.0)
        for (pos in 0..v.size - 1) {
            assertEquals(v[pos] + 42, u[pos], Precision.EPSILON)
        }
    }

    private fun getLargeDenseVector(): StridedVector {
        return IntStream.range(0, DenseVector.DENSE_SPLIT_SIZE * 2)
                .mapToDouble { it.toDouble() }
                .toArray().asStrided()
    }
}