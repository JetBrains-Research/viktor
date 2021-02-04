package org.jetbrains.bio.viktor

import org.apache.commons.math3.special.Gamma
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
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class F64FlatArrayAgainstRTest {
    @Test fun whole() {
        val v = VALUES.asF64Array()
        assertEquals(18.37403, v.sum(), 1E-5)
        assertEquals(1.837403, v.mean(), 1E-6)
        assertEquals(0.8286257, v.sd(), 1E-7)
    }

    @Test fun slices() {
        val v = VALUES.asF64Array(offset = 3, size = 4)
        assertEquals(8.292786, v.sum(), 1E-6)
        assertEquals(2.073197, v.mean(), 1E-6)
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
        v.asSequence().forEach {
            assertTrue(it in v)
        }
    }

    @Test fun equals() {
        assertEquals(v, v)
        assertEquals(v, v.copy())
        assertEquals(v.copy(), v)

        if (v.nDim == 1) {
            assertEquals(v, v.toDoubleArray().asF64Array())
        } else {
            assertEquals(v, v.toGenericArray().toF64Array())
        }

        assertNotEquals(v, gappedArray(2..4))
        assertNotEquals(v, gappedArray(1..30))
    }

    @Test fun toStringNormal() {
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
        assertEquals(F64Array.full(*v.shape, init = 42.0), copy)
    }

    @Test fun dot() {
        if (v.nDim != 1) return // only applicable to flat arrays
        assertEquals((0 until v.size).sumByDouble { v[it] * v[it] }, v.dot(v), 1E-10)
    }

    @Test fun dotWithNotDense() {
        if (v.nDim != 1) return // only applicable to flat arrays
        val other = F64Array(v.size, 2) { _, _ -> Random().nextDouble() }.V[_I, 0]
        assertEquals((0 until v.size).sumByDouble { v[it] * other[it] }, v.dot(other), 1E-10)
    }

    @Test fun mean() {
        assertEquals(v.asSequence().sumByDouble { it } / v.shape.product(), v.mean(), 1E-10)
    }

    @Test fun sd() {
        if (v.nDim != 1) return // only applicable to flat arrays
        assertEquals(sqrt(StatUtils.variance(v.toDoubleArray())), v.sd(), 1E-10)
    }

    @Test fun sum() {
        assertEquals(v.asSequence().sumByDouble { it }, v.sum(), 1E-10)
    }

    @Test fun cumSum() {
        if (v.nDim != 1) return // only applicable to flat arrays
        val copy = v.copy()
        copy.cumSum()

        var acc = 0.0
        for (i in 0 until v.size) {
            acc += v[i]
            assertEquals(acc, copy[i], 1e-10)
        }
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
        assertNotEquals(m.toArray(), m)
        assertNotEquals(m, m.flatten())
    }

    @Test fun toString2() {
        assertEquals("[[0], [0]]", F64Array(2, 1).toString())
        assertEquals("[[0, 0]]", F64Array(1, 2).toString())
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

    @Test fun toString3() {
        assertEquals("[[[0]]]", F64Array(1, 1, 1).toString())
        assertEquals(
            "[[[0], [0]], [[0], [0]], [[0], [0]]]",
            F64Array(3, 2, 1).toString()
        )
    }
}

@RunWith(Parameterized::class)
class F64VectorMathTest(private val v: F64Array) {

    @Test fun transform() {
        val logGamma = v.transform(Gamma::logGamma)
        v.asSequence().zip(logGamma.asSequence()).forEach { (vx, logGammaVx) ->
            assertEquals(Gamma.logGamma(vx), logGammaVx, 1E-30)
        }
    }

    @Test fun exp() {
        val vMax = v.max()
        val expV = (v / vMax).exp()
        v.asSequence().zip(expV.asSequence()).forEach { (vx, expVx) ->
            assertEquals(FastMath.exp(vx / vMax), expVx, 1E-8)
        }
    }

    @Test fun expm1() {
        val vMax = v.max()
        val expm1V = (v / vMax).expm1()
        v.asSequence().zip(expm1V.asSequence()).forEach { (vx, expm1Vx) ->
            assertEquals(FastMath.expm1(vx / vMax), expm1Vx, 1E-8)
        }
    }

    @Test fun log() {
        val logV = v.log()
        v.asSequence().zip(logV.asSequence()).forEach { (vx, logVx) ->
            assertEquals(FastMath.log(vx), logVx, 1E-8)
        }
    }

    @Test fun log1p() {
        val log1pV = v.log1p()
        v.asSequence().zip(log1pV.asSequence()).forEach { (vx, log1pVx) ->
            assertEquals(FastMath.log1p(vx), log1pVx, 1E-8)
        }
    }

    @Test fun transformInPlace() {
        assertEquals(v.transform(Gamma::logGamma), v.copy().apply { transformInPlace(Gamma::logGamma) })
    }

    @Test fun transformInPlaceNotDense() {
        if (v.nDim != 1) return // flat arrays only
        val notDense = F64Array(v.size, 2).V[_I, 0]
        notDense.V[_I] = v
        notDense.transformInPlace(Gamma::logGamma)
        assertEquals(v.transform(Gamma::logGamma), notDense)
    }

    @Test fun transformInPlaceNotFlattenable() {
        if (v.nDim != 1) return // flat arrays only
        val notFlattenable = F64Array(2, 3, v.size).V[_I, 1]
        val doubleV = F64Array.concatenate(v.reshape(1, v.size), v.reshape(1, v.size))
        notFlattenable.V[_I] = doubleV
        notFlattenable.transformInPlace(Gamma::logGamma)
        assertEquals(doubleV.transform(Gamma::logGamma), notFlattenable)
    }

    @Test fun expInPlace() {
        assertEquals(v.exp(), v.copy().apply { expInPlace() })
    }

    @Test fun expm1InPlace() {
        assertEquals(v.expm1(), v.copy().apply { expm1InPlace() })
    }

    @Test fun logInPlace() {
        assertEquals(v.log(), v.copy().apply { logInPlace() })
    }

    @Test fun log1pInPlace() {
        assertEquals(v.log1p(), v.copy().apply { log1pInPlace() })
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
        v.asSequence().zip(vLaeV.asSequence()).forEach { (vx, vLaeVx) ->
            assertEquals(vx logAddExp vx, vLaeVx, 1e-8)
        }
    }

    @Test fun logAddExpWithNotDense() {
        if (v.nDim != 1) return // only applicable to flat arrays
        val other = F64Array(v.size, 2) { _, _ -> Random().nextDouble() }.V[_I, 0]
        val vLaeO = v logAddExp other
        (0 until v.size).forEach { pos ->
            assertEquals(v[pos] logAddExp other[pos], vLaeO[pos], Precision.EPSILON)
        }
    }

    @Test fun logAddExpAssign() {
        assertEquals(v logAddExp v, v.copy().apply { logAddExpAssign(v) })
    }

    @Test fun logAddExpAssignWithNotDense() {
        if (v.nDim != 1) return // only applicable to flat arrays
        val other = F64Array(v.size, 2) { _, _ -> Random().nextDouble() }.V[_I, 0]
        assertEquals(v logAddExp other, v.copy().apply { logAddExpAssign(other) })
    }

    @Test fun logSumExp() {
        assertEquals(
            v.asSequence().sumByDouble { FastMath.exp(it) }, FastMath.exp(v.logSumExp()), 1e-6
        )
    }

    @Test fun logExp() {
        val copy = v.copy()
        v.expInPlace()
        v.logInPlace()
        v.asSequence().zip(copy.asSequence()).forEach { (vx, copyx) ->
            assertEquals(copyx, vx, 1e-6)
        }
    }

    @Test fun min() {
        val sequenceMin = v.asSequence().minOrNull()
        assertNotNull(sequenceMin, "Sequential min of an array was null")
        assertEquals(sequenceMin, v.min(), 0.0)
        if (v.nDim == 1) {
            // argMin is only applicable to flat arrays
            assertEquals(v[v.argMin()], v.min(), 0.0)
        }
    }

    @Test fun max() {
        val sequenceMax = v.asSequence().maxOrNull()
        assertNotNull(sequenceMax, "Sequential max of an array was null")
        assertEquals(sequenceMax, v.max(), 0.0)
        if (v.nDim == 1) {
            // argMax is only applicable to flat arrays
            assertEquals(v[v.argMax()], v.max(), 0.0)
        }
    }

    companion object {
        @Parameters(name = "{0}")
        @JvmStatic fun `data`() = CASES
    }
}

@RunWith(Parameterized::class)
class F64FlatVectorArithmeticTest(private val v: F64Array) {
    @Test fun unaryPlus() = assertEquals(v, +v)

    @Test fun unaryMinus() = assertEquals(v, -(-v))

    @Test fun plus() {
        val u = v.copy().apply { fill(42.0) } + v
        v.asSequence().zip(u.asSequence()).forEach { (vx, ux) ->
            assertEquals(vx + 42.0, ux, Precision.EPSILON)
        }
    }

    @Test fun plusScalar() {
        val u = v + 42.0
        v.asSequence().zip(u.asSequence()).forEach { (vx, ux) ->
            assertEquals(vx + 42, ux, Precision.EPSILON)
        }
    }

    @Test fun plusWithNotDense() {
        if (v.nDim != 1) return // this is a test for flat arrays
        val other = F64Array(v.size, 2) { _, _ -> Random().nextDouble() }.V[_I, 0]
        val u = v + other
        (0 until v.size).forEach { pos ->
            assertEquals(v[pos] + other[pos], u[pos], Precision.EPSILON)
        }
    }

    @Test fun plusAssignWithNotDense() {
        if (v.nDim != 1) return // this is a test for flat arrays
        val other = F64Array(v.size, 2) { _, _ -> Random().nextDouble() }.V[_I, 0]
        assertEquals(v + other, v.copy().apply { this += other })
    }

    @Test fun minus() {
        val u = v.copy().apply { fill(42.0) } - v
        v.asSequence().zip(u.asSequence()).forEach { (vx, ux) ->
            assertEquals(42.0 - vx, ux, Precision.EPSILON)
        }
    }

    @Test fun minusScalar() {
        val u = v - 42.0
        v.asSequence().zip(u.asSequence()).forEach { (vx, ux) ->
            assertEquals(vx - 42.0, ux, Precision.EPSILON)
        }
    }

    @Test fun minusWithNotDense() {
        if (v.nDim != 1) return // this is a test for flat arrays
        val other = F64Array(v.size, 2) { _, _ -> Random().nextDouble() }.V[_I, 0]
        val u = v - other
        (0 until v.size).forEach { pos ->
            assertEquals(v[pos] - other[pos], u[pos], Precision.EPSILON)
        }
    }

    @Test fun times() {
        val u = v.copy().apply { fill(42.0) } * v
        v.asSequence().zip(u.asSequence()).forEach { (vx, ux) ->
            assertEquals(vx * 42.0, ux, Precision.EPSILON)
        }
    }

    @Test fun timesScalar() {
        val u = v * 42.0
        v.asSequence().zip(u.asSequence()).forEach { (vx, ux) ->
            assertEquals(vx * 42, ux, Precision.EPSILON)
        }
    }

    @Test fun timesWithNotDense() {
        if (v.nDim != 1) return // this is a test for flat arrays
        val other = F64Array(v.size, 2) { _, _ -> Random().nextDouble() }.V[_I, 0]
        val u = v * other
        (0 until v.size).forEach { pos ->
            assertEquals(v[pos] * other[pos], u[pos], Precision.EPSILON)
        }
    }

    @Test fun div() {
        // since the left argument is replaced by a copy, and we want to test
        // non-standard arrays, `v` goes to the right side of div.
        val u = v.copy().apply { fill(42.0) } / v
        v.asSequence().zip(u.asSequence()).forEach { (vx, ux) ->
            assertEquals(42.0 / vx, ux, Precision.EPSILON)
        }
    }

    @Test fun divScalar() {
        val u = v / 42.0
        v.asSequence().zip(u.asSequence()).forEach { (vx, ux) ->
            assertEquals(vx / 42.0, ux, Precision.EPSILON)
        }
    }

    @Test fun divWithNotDense() {
        if (v.nDim != 1) return // this is a test for flat arrays
        val other = F64Array(v.size, 2) { _, _ -> Random().nextDouble() }.V[_I, 0]
        val u = v / other
        (0 until v.size).forEach { pos ->
            assertEquals(v[pos] / other[pos], u[pos], Precision.EPSILON)
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
            .asF64Array(F64DenseFlatArray.DENSE_SPLIT_SIZE + 1, F64DenseFlatArray.DENSE_SPLIT_SIZE + 1),
    // Non-flattenable array
    Random().doubles(4 * 3 * 2).toArray().asF64Array().reshape(4, 3, 2).view(1, 1)
)

private fun gappedArray(r: IntRange): F64Array {
    // The NaN gaps are there for two reasons:
    //
    // 1. to ensure 'offset' and 'stride' are used correctly,
    // 2. to force the use of fallback implementation.
    val values = IntStream.range(r.first, r.last + 1)
            .mapToDouble(Int::toDouble)
            .flatMap { DoubleStream.of(Double.NaN, it) }
            .toArray()
    return F64FlatArray(values, offset = 1, size = r.last + 1 - r.first, stride = 2)
}
