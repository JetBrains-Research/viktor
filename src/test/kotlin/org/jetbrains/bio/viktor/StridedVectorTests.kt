package org.jetbrains.bio.viktor

import org.apache.commons.math3.stat.StatUtils
import org.apache.commons.math3.util.FastMath
import org.apache.commons.math3.util.Precision
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import org.junit.runners.Parameterized.Parameters
import java.util.*
import java.util.stream.DoubleStream
import java.util.stream.IntStream
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class F64VectorCreationTest {
    @Test fun createSpecialization() {
        assertTrue(F64Vector.create(doubleArrayOf(1.0), stride = 10) !is DenseF64Vector)
        assertTrue(F64Vector.create(doubleArrayOf(1.0)) is DenseF64Vector)
        assertTrue(F64Vector.create(doubleArrayOf(1.0, 2.0), offset = 1, size = 1) is DenseF64Vector)
    }

    @Test fun of() {
        assertArrayEquals(doubleArrayOf(1.0),
                          F64Vector.of(1.0).toArray(), Precision.EPSILON)
        assertArrayEquals(doubleArrayOf(1.0, 2.0),
                          F64Vector.of(1.0, 2.0).toArray(), Precision.EPSILON)
        assertArrayEquals(doubleArrayOf(1.0, 2.0, 3.0),
                          F64Vector.of(1.0, 2.0, 3.0).toArray(), Precision.EPSILON)
    }

    @Test fun asStrided() {
        assertEquals(F64Vector.of(1.0), doubleArrayOf(1.0).asVector())
        assertEquals(F64Vector.of(3.0),
                     doubleArrayOf(1.0, 2.0, 3.0).asVector(offset = 2, size = 1))
    }

    @Test fun asStridedView() {
        val values = doubleArrayOf(1.0, 2.0, 3.0)
        val v = values.asVector(offset = 2, size = 1)
        v[0] = 42.0
        assertArrayEquals(doubleArrayOf(1.0, 2.0, 42.0), values, Precision.EPSILON)
    }

    @Test fun invoke() {
        assertEquals(F64Vector.of(1.0, 2.0, 3.0),
                     F64Vector(3) { it + 1.0 })
    }

    @Test fun stochastic() {
        val v = F64Vector.stochastic(2)
        assertEquals(2, v.size)
        assertEquals(F64Vector.of(.5, .5), v)
    }

    @Test fun full() {
        val v = F64Vector.full(2, 42.0)
        assertEquals(2, v.size)
        assertEquals(F64Vector.of(42.0, 42.0), v)
    }

    @Test fun concatenate() {
        assertEquals(F64Vector.of(1.0, 2.0, 3.0, 4.0, 5.0),
                     F64Vector.concatenate(F64Vector.of(1.0, 2.0),
                                           F64Vector.of(3.0),
                                           F64Vector.of(4.0, 5.0)))
    }

    @Test fun append() {
        assertEquals(F64Vector.of(1.0, 2.0),
                     F64Vector.of(1.0, 2.0).append(F64Vector(0)))
        assertEquals(F64Vector.of(1.0, 2.0),
                     F64Vector(0).append(F64Vector.of(1.0, 2.0)))
        assertEquals(F64Vector.of(1.0, 2.0, 3.0, 4.0, 5.0),
                     F64Vector.of(1.0, 2.0).append(F64Vector.of(3.0, 4.0, 5.0)))
    }

    @Test fun copy() {
        val v = F64Vector.of(1.0, 2.0, 3.0)
        val copy = v.copy()
        assertEquals(v, copy)
        v[0] = 42.0
        assertEquals(1.0, copy[0], Precision.EPSILON)
    }
}

class F64VectorSlicing {
    @Test fun transpose() {
        assertEquals(F64Vector.of(1.0),
                     F64Vector.of(1.0).T.view(0, along = 1) as F64Vector)
        assertEquals(F64Vector.of(1.0, 2.0),
                     F64Vector.of(1.0, 2.0).T.view(0, along = 1) as F64Vector)
        assertEquals(F64Vector.of(1.0, 2.0, 3.0),
                     F64Vector.of(1.0, 2.0, 3.0).T.view(0, along = 1) as F64Vector)
    }

    @Test fun slice() {
        val v = F64Vector.of(1.0, 2.0, 3.0)
        val slice = v.slice(1, 2)
        assertEquals(1, slice.size)
        assertEquals(F64Vector.of(2.0), slice)

        slice[0] = 42.0
        assertEquals(F64Vector.of(1.0, 42.0, 3.0), v)
    }

    @Test fun sliceWithStep() {
        val v = F64Vector.of(1.0, 2.0, 3.0, 4.0, 5.0, 6.0)

        v.slice(step = 2).let {
            assertEquals(3, it.size)
            assertEquals(F64Vector.of(1.0, 3.0, 5.0), it)
        }

        v.slice(1, step = 2).let {
            assertEquals(3, it.size)
            assertEquals(F64Vector.of(2.0, 4.0, 6.0), it)
        }

        v.slice(1, step = 3).let {
            assertEquals(2, it.size)
            assertEquals(F64Vector.of(2.0, 5.0), it)
        }

        v.slice(1, step = 4).let {
            assertEquals(2, it.size)
            assertEquals(F64Vector.of(2.0, 6.0), it)
        }
    }

    @Test(expected = IndexOutOfBoundsException::class) fun sliceOutOfBounds() {
        F64Vector(0).slice(0, 42)
    }
}

@RunWith(Parameterized::class)
class StridedVectorGetSet(private val values: DoubleArray,
                          private val offset: Int,
                          size: Int,
                          private val stride: Int) {

    private val v = F64Vector.create(values, offset, stride, size)

    @Test fun get() {
        for (i in v.indices) {
            assertEquals(values[offset + i * stride], v[i], Precision.EPSILON)
        }
    }

    @Test(expected = IndexOutOfBoundsException::class) fun getOutOfBounds() {
        v[100500]
    }

    @Test fun set() {
        for (i in v.indices) {
            val copy = v.copy()
            copy[i] = 42.0
            assertEquals(42.0, copy[i], Precision.EPSILON)

            // Ensure all other elements are unchanged.
            for (j in v.indices) {
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
        copy[_I] = 42.0

        assertEquals(F64Vector.full(copy.size, 42.0), copy)
    }

    @Test fun setMagicVector() {
        val other = F64Vector.full(v.size, 42.0)
        val copy = v.copy()
        copy[_I] = other

        assertEquals(other, copy)
    }

    companion object {
        @Parameters(name = "F64Vector({1}, {2}, {3})")
        @JvmStatic fun `data`() = listOf(
                // Normal case.
                arrayOf(doubleArrayOf(1.0, 2.0, 3.0), 0, 3, 1),
                // Offset and stride.
                arrayOf(doubleArrayOf(1.0, 2.0, 3.0), 1, 1, 3),
                // Empty array.
                arrayOf(doubleArrayOf(1.0, 2.0, 3.0), 3, 0, 1))
    }
}

private val CASES = listOf(
        // Gapped.
        (1..3).toStrided(),
        // Dense small.
        doubleArrayOf(1.0, 2.0, 3.0).asVector(),
        // Dense large.
        Random().doubles(DenseF64Vector.DENSE_SPLIT_SIZE + 1L).toArray().asVector())

@RunWith(Parameterized::class)
class F64VectorOpsTest(private val v: F64Vector) {
    @Test fun contains() {
        for (i in v) {
            assertTrue(i.toDouble() in v)
        }
    }

    @Test fun equals() {
        assertEquals(v, v)
        assertEquals(v, v.toArray().asVector())
        assertNotEquals(v, (2..4).toStrided())
        assertNotEquals(v, (1..30).toStrided())
    }

    @Test fun _toString() {
        assertEquals("[]", F64Vector(0).toString())
        assertEquals("[42]", F64Vector.of(42.0).toString())
        assertEquals("[0, 1, 2, 3]", (0..3).toStrided().toString())
    }

    @Test fun toStringLarge() {
        val v = (0..1023).toStrided()
        assertEquals("[0, ..., 1023]", v.toString(2))
        assertEquals("[0, ..., 1022, 1023]", v.toString(3))
        assertEquals("[0, 1, ..., 1022, 1023]", v.toString(4))
    }

    @Test fun toStringNanInf() {
        val v = F64Vector.of(Double.NaN, Double.POSITIVE_INFINITY,
                             Double.NEGATIVE_INFINITY, 42.0)
        assertEquals("[nan, inf, -inf, 42]", v.toString())
    }

    @Test fun fill() {
        val copy = v.copy()
        copy.fill(42.0)
        assertEquals(F64Vector.full(copy.size, 42.0), copy)
    }

    @Test fun reverse() {
        val copy = v.copy()
        copy.reverse()
        copy.reverse()
        assertEquals(v, copy)
    }

    @Test fun dot() {
        assertEquals(v.indices.sumByDouble { v[it] * v[it] }, v.dot(v), 1e-10)
    }

    @Test fun mean() {
        assertEquals(v.indices.sumByDouble { v[it] } / v.size, v.mean(),
                     Precision.EPSILON)
    }

    @Test fun sd() {
        assertEquals(Math.sqrt(StatUtils.variance(v.toArray())), v.sd(), 1e-10)
    }

    @Test fun sum() {
        assertEquals(v.indices.sumByDouble { v[it] }, v.sum(), 1e-10)
    }

    @Test fun cumSum() {
        val copy = v.copy()
        copy.cumSum()

        var acc = 0.0
        for (i in v.indices) {
            acc += v[i]
            assertEquals(acc, copy[i], 1e-10)
        }
    }

    @Test fun argMinMax() {
        val values = v.toArray()
        val min = values.min()!!
        assertEquals(min, v.min(), Precision.EPSILON)
        assertEquals(values.indexOf(min), v.argMin())

        val max = values.max()!!
        assertEquals(v.max(), max, Precision.EPSILON)
        assertEquals(values.indexOf(max), v.argMax())
    }

    companion object {
        @Parameters(name = "{0}")
        @JvmStatic fun `data`() = CASES
    }
}

class F64VectorAgainstRTest {
    @Test fun whole() {
        val v = VALUES.asVector()
        assertEquals(18.37403, v.sum(), 1E-5)
        assertEquals(1.837403, v.mean(), 1E-6)
        assertEquals(18.37403, v.balancedSum(), 1E-5)
        assertEquals(0.8286257, v.sd(), 1E-7)
    }

    @Test fun slices() {
        val v = VALUES.asVector(offset = 3, size = 4)
        assertEquals(8.292786, v.sum(), 1E-6)
        assertEquals(2.073197, v.mean(), 1E-6)
        assertEquals(8.292786, v.balancedSum(), 1E-6)
        assertEquals(1.016512, v.sd(), 1E-6)
    }

    @Test fun weighted() {
        val v = VALUES.asVector()
        val w = WEIGHTS.asVector()
        assertEquals(8.417747, v.dot(w), 1E-6)
    }

    @Test fun weightedSlices() {
        val v = VALUES.asVector(offset = 3, size = 4)
        val w = WEIGHTS.asVector(offset = 2, size = 4)
        assertEquals(2.363317, v.dot(w), 1E-6)
    }

    companion object {
        /**
         * The VALUES were produced by R command "rgamma(10, 4, 2)"
         * The WEIGHTS were produced by R command "runif(10)"
         * The expected statistics were calculated in R
         */
        private val VALUES = doubleArrayOf(1.5409738, 2.6926526, 0.8159389, 2.5009070,
                                           3.2777667, 1.5157005, 0.9984120, 2.3274278,
                                           1.7286019, 0.9756442)
        private val WEIGHTS = doubleArrayOf(0.04437868, 0.93508668, 0.09091827, 0.17638019,
                                            0.86624410, 0.24522868, 0.85157408, 0.17318330,
                                            0.07582913, 0.73878585)
    }
}

@RunWith(Parameterized::class)
class F64VectorMathTest(private val v: F64Vector) {
    @Test fun exp() {
        val expV = (v / v.max()).exp()
        expV as F64Vector
        for (i in v.indices) {
            assertEquals(FastMath.exp(v[i] / v.max()), expV[i], 1e-8)
        }
    }

    @Test fun expm1() {
        val expm1V = (v / v.max()).expm1()
        expm1V as F64Vector
        for (i in v.indices) {
            assertEquals(FastMath.expm1(v[i] / v.max()), expm1V[i], 1e-8)
        }
    }

    @Test fun log() {
        val logV = v.log()
        logV as F64Vector
        for (i in v.indices) {
            assertEquals(FastMath.log(v[i]), logV[i], 1e-8)
        }
    }

    @Test fun log1p() {
        val log1pV = v.log1p()
        log1pV as F64Vector
        for (i in v.indices) {
            assertEquals(FastMath.log1p(v[i]), log1pV[i], 1e-8)
        }
    }

    @Test fun rescale() {
        val scaled = v.copy()
        scaled.rescale()
        assertEquals(1.0, scaled.sum(), 1e-8)
    }

    @Test fun logRescale() {
        val scaled = v.copy()
        scaled.logRescale()
        assertEquals(1.0, FastMath.exp(scaled.logSumExp()), 1e-8)
    }

    @Test fun logAddExp() {
        val vLaeV = v logAddExp v
        vLaeV as F64Vector
        for (i in v.indices) {
            assertEquals(v[i] logAddExp v[i], vLaeV[i], 1e-8)
        }
    }

    @Test fun logSumExp() {
        assertEquals(v.indices.sumByDouble { FastMath.exp(v[it]) },
                     FastMath.exp(v.logSumExp()), 1e-6)
    }

    companion object {
        @Parameters(name = "{0}")
        @JvmStatic fun `data`() = CASES
    }
}

@RunWith(Parameterized::class)
class F64VectorArithTest(private val v: F64Vector) {
    @Test fun unaryPlus() = assertEquals(v, +v)

    @Test fun unaryMinus() = assertEquals(v, -(-v))

    @Test fun plus() {
        val u = v + v
        for (pos in 0..v.size - 1) {
            assertEquals(v[pos] + v[pos], u[pos], Precision.EPSILON)
        }
    }

    @Test fun plusScalar() {
        val u = v + 42.0
        for (pos in 0..v.size - 1) {
            assertEquals(v[pos] + 42, u[pos], Precision.EPSILON)
        }
    }

    @Test fun minus() {
        val u = v - F64Vector.full(v.size, 42.0)
        for (pos in 0..v.size - 1) {
            assertEquals(v[pos] - 42.0, u[pos], Precision.EPSILON)
        }
    }

    @Test fun minusScalar() {
        val u = v - 42.0
        for (pos in 0..v.size - 1) {
            assertEquals(v[pos] - 42.0, u[pos], Precision.EPSILON)
        }
    }

    @Test fun times() {
        val u = v * v
        for (pos in 0..v.size - 1) {
            assertEquals(v[pos] * v[pos], u[pos], Precision.EPSILON)
        }
    }

    @Test fun timesScalar() {
        val u = v * 42.0
        for (pos in 0..v.size - 1) {
            assertEquals(v[pos] * 42, u[pos], Precision.EPSILON)
        }
    }

    @Test fun div() {
        val u = v / F64Vector.full(v.size, 42.0)
        for (pos in 0..v.size - 1) {
            assertEquals(v[pos] / 42.0, u[pos], Precision.EPSILON)
        }
    }

    @Test fun divScalar() {
        val u = v / 42.0
        for (pos in 0..v.size - 1) {
            assertEquals(v[pos] / 42.0, u[pos], Precision.EPSILON)
        }
    }

    companion object {
        @Parameters(name = "{0}")
        @JvmStatic fun `data`() = CASES
    }
}

private fun IntRange.toStrided(): F64Vector {
    // The NaN gaps are there for two reasons:
    //
    // 1. to ensure 'offset' and 'stride' are used correctly,
    // 2. to force the use of fallback implementation.
    val values = IntStream.range(start, endInclusive + 1)
            .mapToDouble(Int::toDouble)
            .flatMap { DoubleStream.of(Double.NaN, it) }
            .toArray()
    return F64Vector.create(values, offset = 1,
                            size = endInclusive + 1 - start, stride = 2)
}
