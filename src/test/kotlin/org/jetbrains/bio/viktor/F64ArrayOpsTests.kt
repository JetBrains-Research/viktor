package org.jetbrains.bio.viktor

import org.apache.commons.math3.stat.StatUtils
import org.apache.commons.math3.util.FastMath
import org.apache.commons.math3.util.Precision
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import org.junit.runners.Parameterized.Parameters
import java.util.*
import java.util.stream.DoubleStream
import java.util.stream.IntStream
import kotlin.math.sqrt
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class F64FlatArrayAgainstRTest {
    @Test fun whole() {
        val v = VALUES.asF64Array()
        assertEquals(18.37403, v.sum(), 1E-5)
        assertEquals(1.837403, v.mean(), 1E-6)
        assertEquals(18.37403, v.balancedSum(), 1E-5)
        assertEquals(0.8286257, v.sd(), 1E-7)
    }

    @Test fun slices() {
        val v = VALUES.asF64Array(offset = 3, size = 4)
        assertEquals(8.292786, v.sum(), 1E-6)
        assertEquals(2.073197, v.mean(), 1E-6)
        assertEquals(8.292786, v.balancedSum(), 1E-6)
        assertEquals(1.016512, v.sd(), 1E-6)
    }

    @Test fun weighted() {
        val v = VALUES.asF64Array()
        val w = WEIGHTS.asF64Array()
        assertEquals(8.417747, v.dot(w), 1E-6)
    }

    @Test fun weightedSlices() {
        val v = VALUES.asF64Array(offset = 3, size = 4)
        val w = WEIGHTS.asF64Array(offset = 2, size = 4)
        assertEquals(2.363317, v.dot(w), 1E-6)
    }

    companion object {
        /**
         * The VALUES were produced by R command "rgamma(10, 4, 2)"
         * The WEIGHTS were produced by R command "runif(10)"
         * The expected statistics were calculated in R
         */
        private val VALUES = doubleArrayOf(
            1.5409738, 2.6926526, 0.8159389, 2.5009070, 3.2777667,
            1.5157005, 0.9984120, 2.3274278, 1.7286019, 0.9756442
        )
        private val WEIGHTS = doubleArrayOf(
            0.04437868, 0.93508668, 0.09091827, 0.17638019, 0.86624410,
            0.24522868, 0.85157408, 0.17318330, 0.07582913, 0.73878585
        )
    }
}

@RunWith(Parameterized::class)
class F64FlatArrayOpsTest(private val v: F64Array) {
    @Test fun contains() {
        for (i in 0 until v.size) {
            assertTrue(v[i] in v)
        }
    }

    @Test fun equals() {
        assertEquals(v, v)
        assertEquals(v, v.toDoubleArray().asF64Array())
        assertNotEquals(v, gappedArray(2..4))
        assertNotEquals(v, gappedArray(1..30))
    }

    @Test fun _toString() {
        assertEquals("[]", F64Array(0).toString())
        assertEquals("[42]", F64Array.of(42.0).toString())
        assertEquals("[0, 1, 2, 3]", gappedArray(0..3).toString())
    }

    @Test fun toStringLarge() {
        val v = gappedArray(0..1023)
        assertEquals("[0, ..., 1023]", v.toString(2))
        assertEquals("[0, ..., 1022, 1023]", v.toString(3))
        assertEquals("[0, 1, ..., 1022, 1023]", v.toString(4))
    }

    @Test fun toStringNanInf() {
        val v = F64Array.of(
            Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY, 42.0
        )
        assertEquals("[nan, inf, -inf, 42]", v.toString())
    }

    @Test fun fill() {
        val copy = v.copy()
        copy.fill(42.0)
        assertEquals(F64Array.full(copy.size, 42.0), copy)
    }

    @Test fun reversed() {
        assertEquals(
            F64Array.of(3.0, 2.0, 1.0),
            F64Array.of(1.0, 2.0, 3.0).reversed()
        )
        assertEquals(
            F64Array.of(4.0, 5.0, 6.0, 1.0, 2.0, 3.0).reshape(2, 3),
            F64Array.of(1.0, 2.0, 3.0, 4.0, 5.0, 6.0).reshape(2, 3).reversed()
        )
        assertEquals(
            F64Array.of(3.0, 2.0, 1.0, 6.0, 5.0, 4.0).reshape(2, 3),
            F64Array.of(1.0, 2.0, 3.0, 4.0, 5.0, 6.0).reshape(2, 3).reversed(axis = 1)
        )
    }

    @Test fun reversedReversed() {
        val v = F64Array.of(3.0, 2.0, 1.0)
        assertEquals(v, v.reversed().reversed())
    }

    @Test fun dot() {
        assertEquals((0 until v.size).sumByDouble { v[it] * v[it] }, v.dot(v), 1E-10)
    }

    @Test fun mean() {
        assertEquals(
            (0 until v.size).sumByDouble { v[it] } / v.size, v.mean(), Precision.EPSILON
        )
    }

    @Test fun sd() {
        assertEquals(sqrt(StatUtils.variance(v.toDoubleArray())), v.sd(), 1E-10)
    }

    @Test fun sum() {
        assertEquals((0 until v.size).sumByDouble { v[it] }, v.sum(), 1E-10)
    }

    @Test fun cumSum() {
        val copy = v.copy()
        copy.cumSum()

        var acc = 0.0
        for (i in 0 until v.size) {
            acc += v[i]
            assertEquals(acc, copy[i], 1e-10)
        }
    }

    @Test fun argMinMax() {
        val values = v.toDoubleArray()
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

class F64ArrayOpsTest {
    @Test fun unaryMinus() {
        val m = F64Array.of(
            0.0, 1.0,
            2.0, 3.0,
            4.0, 5.0
        ).reshape(3, 2)
        val copy = m.copy()

        assertEquals(m, -(-m))
        assertEquals(-(m.V[0])[0], (-m)[0, 0], Precision.EPSILON)

        // Make sure [m] is unchanged!
        assertEquals(copy, m)
    }

    @Test fun equals() {
        val m = F64Array(2, 3, 4) { i, j, k -> 1.0 * i + 2 * j + 3 * k }

        assertEquals(m, m)
        assertEquals(m, m.copy())
        assertNotEquals(m, m.exp())
    }

    @Test fun _toString2() {
        assertEquals("[]", F64Array(0, 0).toString())
        assertEquals("[[]]", F64Array(1, 0).toString())
        assertEquals("[[0], [0]]", F64Array(2, 1).toString())
    }

    @Test fun toString2Large() {
        val v = F64Array(1024) { it.toDouble() }
        assertEquals(
            "[[0, 1], [2, 3], ..., [1020, 1021], [1022, 1023]]",
            v.reshape(512, 2).toString(4)
        )
        assertEquals(
            "[[0, 1, ..., 510, 511], [512, 513, ..., 1022, 1023]]",
            v.reshape(2, 512).toString(4)
        )
    }

    @Test fun _toString3() {
        assertEquals("[]", F64Array(0, 0, 0).toString())
        assertEquals("[[[0]]]", F64Array(1, 1, 1).toString())
        assertEquals(
            "[[[0], [0]], [[0], [0]], [[0], [0]]]",
            F64Array(3, 2, 1).toString()
        )
    }
}

@RunWith(Parameterized::class)
class F64VectorMathTest(private val v: F64Array) {
    @Test fun exp() {
        val expV = (v / v.max()).exp()
        for (i in 0 until v.size) {
            assertEquals(FastMath.exp(v[i] / v.max()), expV[i], 1E-8)
        }
    }

    @Test fun expm1() {
        val expm1V = (v / v.max()).expm1()
        for (i in 0 until v.size) {
            assertEquals(FastMath.expm1(v[i] / v.max()), expm1V[i], 1E-8)
        }
    }

    @Test fun log() {
        val logV = v.log()
        for (i in 0 until v.size) {
            assertEquals(FastMath.log(v[i]), logV[i], 1E-8)
        }
    }

    @Test fun log1p() {
        val log1pV = v.log1p()
        for (i in 0 until v.size) {
            assertEquals(FastMath.log1p(v[i]), log1pV[i], 1E-8)
        }
    }

    @Test fun rescale() {
        val scaled = v.copy()
        scaled.rescale()
        assertEquals(1.0, scaled.sum(), 1E-8)
    }

    @Test fun logRescale() {
        val scaled = v.copy()
        scaled.logRescale()
        assertEquals(1.0, FastMath.exp(scaled.logSumExp()), 1E-8)
    }

    @Test fun logAddExp() {
        val vLaeV = v logAddExp v
        for (i in 0 until v.size) {
            assertEquals(v[i] logAddExp v[i], vLaeV[i], 1e-8)
        }
    }

    @Test fun logSumExp() {
        assertEquals(
            (0 until v.size).sumByDouble { FastMath.exp(v[it]) }, FastMath.exp(v.logSumExp()), 1e-6
        )
    }

    @Test fun logExp() {
        val copy = v.copy()
        v.expInPlace()
        v.logInPlace()
        (0 until v.size).forEach { i ->
            assertEquals(copy[i], v[i], 1e-6)
        }
    }

    companion object {
        @Parameters(name = "{0}")
        @JvmStatic fun `data`() = CASES
    }
}

@RunWith(Parameterized::class)
class F64FlatVectorArithTest(private val v: F64Array) {
    @Test fun unaryPlus() = assertEquals(v, +v)

    @Test fun unaryMinus() = assertEquals(v, -(-v))

    @Test fun plus() {
        val u = v + v
        for (pos in 0 until v.size) {
            assertEquals(v[pos] + v[pos], u[pos], Precision.EPSILON)
        }
    }

    @Test fun plusScalar() {
        val u = v + 42.0
        for (pos in 0 until v.size) {
            assertEquals(v[pos] + 42, u[pos], Precision.EPSILON)
        }
    }

    @Test fun minus() {
        val u = v - F64Array.full(v.size, 42.0)
        for (pos in 0 until v.size) {
            assertEquals(v[pos] - 42.0, u[pos], Precision.EPSILON)
        }
    }

    @Test fun minusScalar() {
        val u = v - 42.0
        for (pos in 0 until v.size) {
            assertEquals(v[pos] - 42.0, u[pos], Precision.EPSILON)
        }
    }

    @Test fun times() {
        val u = v * v
        for (pos in 0 until v.size) {
            assertEquals(v[pos] * v[pos], u[pos], Precision.EPSILON)
        }
    }

    @Test fun timesScalar() {
        val u = v * 42.0
        for (pos in 0 until v.size) {
            assertEquals(v[pos] * 42, u[pos], Precision.EPSILON)
        }
    }

    @Test fun div() {
        val u = v / F64Array.full(v.size, 42.0)
        for (pos in 0 until v.size) {
            assertEquals(v[pos] / 42.0, u[pos], Precision.EPSILON)
        }
    }

    @Test fun divScalar() {
        val u = v / 42.0
        for (pos in 0 until v.size) {
            assertEquals(v[pos] / 42.0, u[pos], Precision.EPSILON)
        }
    }

    companion object {
        @Parameters(name = "{0}")
        @JvmStatic fun `data`() = CASES
    }
}

private val CASES = listOf(
    // Gapped.
    gappedArray(1..3),
    // Dense small.
    doubleArrayOf(1.0, 2.0, 3.0).asF64Array(),
    // Dense large.
    Random().doubles(F64DenseFlatArray.DENSE_SPLIT_SIZE + 1L).toArray().asF64Array(),
    // Dense large subarray.
    Random().doubles(3 * (F64DenseFlatArray.DENSE_SPLIT_SIZE + 1L)).toArray()
            .asF64Array(F64DenseFlatArray.DENSE_SPLIT_SIZE + 1, F64DenseFlatArray.DENSE_SPLIT_SIZE + 1)
)

private fun gappedArray(r: IntRange): F64Array {
    // The NaN gaps are there for two reasons:
    //
    // 1. to ensure 'offset' and 'stride' are used correctly,
    // 2. to force the use of fallback implementation.
    val values = IntStream.range(r.start, r.endInclusive + 1)
            .mapToDouble(Int::toDouble)
            .flatMap { DoubleStream.of(Double.NaN, it) }
            .toArray()
    return F64FlatArray(values, offset = 1, size = r.last + 1 - r.first, stride = 2)
}
