
import org.apache.commons.math3.distribution.NormalDistribution
import org.apache.commons.math3.stat.descriptive.summary.Sum
import org.apache.commons.math3.util.Precision
import org.jetbrains.bio.jni.SIMDMath
import org.jetbrains.bio.strided.*
import org.junit.Assert
import org.junit.Test
import java.util.stream.IntStream
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

public class StridedVectorTest {
    @Test fun testOf() {
        Assert.assertArrayEquals(doubleArrayOf(1.0),
                                 StridedVector.of(1.0).toArray(), Precision.EPSILON)
        Assert.assertArrayEquals(doubleArrayOf(1.0, 2.0),
                                 StridedVector.of(1.0, 2.0).toArray(), Precision.EPSILON)
        Assert.assertArrayEquals(doubleArrayOf(1.0, 2.0, 3.0),
                                 StridedVector.of(1.0, 2.0, 3.0).toArray(), Precision.EPSILON)
    }

    @Test fun testTranspose() {
        assertEquals(StridedVector.of(1.0), StridedVector.of(1.0).T.columnView(0))
        assertEquals(StridedVector.of(1.0, 2.0),
                     StridedVector.of(1.0, 2.0).T.columnView(0))
        assertEquals(StridedVector.of(1.0, 2.0, 3.0),
                     StridedVector.of(1.0, 2.0, 3.0).T.columnView(0))

        val m = StridedMatrix(2, 3) { i, j -> i + j * 42.0 }
        assertEquals(StridedVector.of(42.0, 43.0),
                     m.columnView(1).T.columnView(0))
    }

    @Test fun testArgMinMax() {
        val v = getRangeVector(4, 8)
        assertEquals(v.min(), 4.0)
        assertEquals(v.argMin(), 0)
        assertEquals(v.max(), 7.0)
        assertEquals(v.argMax(), v.size() - 1)
    }

    @Test fun testRollSumFallback() {
        val v = StridedMatrix.full(3, 5, 1.0).columnView(0)
        v.cumSum()
        for (i in 0..v.size() - 1) {
            assertEquals(i + 1, v[i].toInt())
        }
    }

    @Test fun testReorder() {
        val values = doubleArrayOf(42.0, 2.0, -1.0, 0.0, 4.0, 2.0).asStrided()
        val indices = values.sorted()
        Assert.assertArrayEquals(intArrayOf(2, 3, 1, 5, 4, 0), indices)

        val v = StridedVector.create(doubleArrayOf(Double.NaN, Double.NaN, // Prefix.
                                                   42.0, Double.NaN, 2.0,
                                                   Double.NaN, -1.0,
                                                   Double.NaN, 0.0,
                                                   Double.NaN, 4.0,
                                                   Double.NaN, 2.0), 2,
                                     values.size(), 2)
        v.reorder(indices)
        Assert.assertArrayEquals(doubleArrayOf(-1.0, 0.0, 2.0, 2.0, 4.0, 42.0),
                                 v.toArray(), Precision.EPSILON)
    }

    @Test fun testDotFast() {
        val v = getRangeVector(4, 8)
        val weights = doubleArrayOf(0.5, 0.1, 0.3, 0.02)
        assertEquals(Sum().evaluate(v.toArray(), weights), v dot weights)
    }

    @Test fun testRescale() {
        val v = getRangeVector(4, 8)
        v.rescale()
        assertTrue(Precision.equals(1.0, v.sum()))
    }

    @Test fun testAdd() {
        val v = getRangeVector(4, 8)
        val u = v + v
        for (pos in 0..v.size() - 1) {
            assertEquals(v[pos] + v[pos], u[pos])
        }
    }

    @Test fun testAddUpdate() {
        val v = getRangeVector(4, 8)
        val u = v + 42.0
        for (pos in 0..v.size() - 1) {
            assertEquals(v[pos] + 42, u[pos])
        }
    }

    @Test fun testEquals() {
        val v = getRangeVector(4, 8)
        assertEquals(v, v)
        assertNotEquals(v, getRangeVector(5, 8))
        assertNotEquals(v, getRangeVector(5, 9))
        assertNotEquals(v, StridedVector.of(42.0))
    }

    @Test fun testReshape() {
        val v = getRangeVector(0, 6)
        Assert.assertArrayEquals(arrayOf(doubleArrayOf(0.0, 1.0, 2.0),
                                         doubleArrayOf(3.0, 4.0, 5.0)),
                                 v.reshape(2, 3).toArray())
        Assert.assertArrayEquals(arrayOf(doubleArrayOf(0.0, 1.0),
                                         doubleArrayOf(2.0, 3.0),
                                         doubleArrayOf(4.0, 5.0)),
                                 v.reshape(3, 2).toArray())
    }

    @Test fun testReshapeWithStride() {
        val v = StridedVector.create(doubleArrayOf(0.0, 1.0, 2.0, 3.0,
                                                   4.0, 5.0, 6.0, 7.0),
                                     0, 4, stride = 2)
        Assert.assertArrayEquals(arrayOf(doubleArrayOf(0.0, 2.0),
                                         doubleArrayOf(4.0, 6.0)),
                                 v.reshape(2, 2).toArray())
    }

    private fun getRangeVector(a: Int, b: Int): StridedVector {
        return IntStream.range(a, b).mapToDouble { it.toDouble() }
                .toArray().asStrided()
    }
}

public class LargeDenseVectorTest{
    @Test fun testLogAddExp() {
        val d = NormalDistribution(0.0, 42.0)
        val v1 = d.sample(128).asStrided()
        val v2 = d.sample(128).asStrided()
        val dst = v1.logAddExp(v2)

        for (i in 0..dst.size() - 1) {
            assertTrue(Precision.equals(MoreMath.logAddExp(v1[i], v2[i]),
                                        dst[i], 5))
        }
    }

    @Test fun testLogRescale() {
        val v = NormalDistribution(0.0, 42.0).sample(128).asStrided()
        v.logRescale()
        assertTrue(Precision.equals(1.0, Math.exp(v.logSumExp()), 1e-8))
    }

    @Test fun testLogSumExp() {
        val data = NormalDistribution(0.0, 42.0).sample(128)
        val v = data.asStrided().copy()
        assertEquals(SIMDMath.logSumExp(data), v.logSumExp())
    }

    @Test fun testRescale() {
        val v = getLargeDenseVector()
        v.rescale()
        assertTrue(Precision.equals(1.0, v.sum(), 5))
    }

    @Test fun testArgMinMax() {
        val v = getLargeDenseVector()
        assertEquals(v.min(), v.toArray().min()!!)
        assertEquals(v.argMin(), 0)
        assertEquals(v.max(), v.toArray().max()!!)
        assertEquals(v.argMax(), v.size() - 1)
    }

    @Test fun testAdd() {
        val v = getLargeDenseVector()
        val u = v + v
        for (pos in 0..v.size() - 1) {
            assertEquals(v[pos] + v[pos], u[pos])
        }
    }

    @Test fun testAddUpdate() {
        val v = getLargeDenseVector()
        val u = v.plus(42.0)
        for (pos in 0..v.size() - 1) {
            assertEquals(v[pos] + 42, u[pos])
        }
    }

    private fun getLargeDenseVector(): StridedVector {
        return IntStream.range(0, DenseVector.DENSE_SPLIT_SIZE * 2)
                .mapToDouble { it.toDouble() }
                .toArray().asStrided()
    }
}