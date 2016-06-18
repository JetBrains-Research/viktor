package org.jetbrains.bio.jni

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import org.junit.runners.Parameterized.Parameters
import java.util.*

class FixedValuesTest {
    @Test fun wholeArray() {
        assertEquals(18.37403, DoubleStat.sum(VALUES), 1E-5)
        assertEquals(1.837403, DoubleStat.mean(VALUES), 1E-6)
        assertEquals(0.8286257, DoubleStat.standardDeviation(VALUES), 1E-7)
        assertEquals(0.7861034, DoubleStat.standardDeviationBiased(VALUES), 1E-7)
    }

    @Test fun slices() {
        assertEquals(8.292786, DoubleStat.sum(VALUES, 3, 4), 1E-6)
        assertEquals(2.073197, DoubleStat.mean(VALUES, 3, 4), 1E-6)
        assertEquals(1.016512, DoubleStat.standardDeviation(VALUES, 3, 4), 1E-6)
        assertEquals(0.8803251, DoubleStat.standardDeviationBiased(VALUES, 3, 4), 1E-7)
    }

    @Test fun weighted() {
        assertEquals(8.417747, DoubleStat.weightedSum(VALUES, WEIGHTS), 1E-6)
        assertEquals(2.005367, DoubleStat.weightedMean(VALUES, WEIGHTS), 1E-6)
        assertEquals(0.9458158, DoubleStat.weightedStandardDeviationBiased(VALUES, WEIGHTS), 1E-7)
    }

    @Test fun weightedSlices() {
        assertEquals(2.363317, DoubleStat.weightedSum(VALUES, 3, WEIGHTS, 2, 4), 1E-6)
        assertEquals(1.714075, DoubleStat.weightedMean(VALUES, 3, WEIGHTS, 2, 4), 1E-6)
        assertEquals(0.6851563, DoubleStat.weightedStandardDeviationBiased(VALUES, 3, WEIGHTS, 2, 4), 1E-7)
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
class SDFractionsTest(private val num: Double,
                      private val den: Double,
                      private val size: Int) {
    @Test fun fractions() {
        val fraction = num / den
        val values = DoubleArray(size) { fraction }
        assertEquals(0.0, DoubleStat.standardDeviation(values),
                     fraction * size * 1E-14)
    }

    companion object {
        @Parameters
        @JvmStatic fun `data`() = listOf(arrayOf(1.0, 2.0, 3000000),
                                         arrayOf(3.0, 5.0, 6000000))
    }
}

@RunWith(Parameterized::class)
class SDProgressionTest(private val from: Double, private val to: Double,
                        private val size: Int) {
    @Test fun progression() {
        val values = DoubleArray(size) { from + it * (to - from) / (size - 1) }
        val actual = DoubleStat.standardDeviation(values)
        val expected = Math.sqrt(size * (to - from) * (size + 1) * (to - from)
                                         / (12.0 * (size - 1) * (size - 1)))
        assertEquals(expected, actual, (actual + expected) * 1E-12)
    }

    companion object {
        @Parameters
        @JvmStatic fun `data`() = listOf(arrayOf(0, 1, 3000001),
                                         arrayOf(-1, 1, 6000001))
    }
}

@RunWith(Parameterized::class)
class SDConsistencyTest(private val size: Int) {
    @Before fun setUp() = Loader.ensureLoaded()

    @Test fun unweighted() {
        val data = Random().doubles(size.toLong()).toArray()
        val expected = DoubleStatJava.standardDeviation(data, 0, size)
        val actual = NativeSpeedups.standardDeviation(data, 0, size)
        assertEquals(expected, actual, (expected + actual) * 1E-12)
    }

    @Test fun weighted() {
        val values = RANDOM.doubles(size.toLong()).toArray()
        val weights = RANDOM.doubles(size.toLong()).toArray()
        val expected = DoubleStatJava.weightedStandardDeviation(values, 0, weights, 0, size)
        val actual = NativeSpeedups.weightedSD(values, 0, weights, 0, size)
        assertEquals(expected, actual, expected * 1E-12)
    }

    companion object {
        private val RANDOM = Random()

        @Parameters
        @JvmStatic fun `data`() = listOf(1000, 1000000)
    }
}

@RunWith(Parameterized::class)
class MeanConsistencyTest(private val size: Int) {
    @Before fun setUp() = Loader.ensureLoaded()

    @Test fun weighted() {
        val values = RANDOM.doubles(size.toLong()).toArray()
        val weights = RANDOM.doubles(size.toLong()).toArray()
        val expected = DoubleStatJava.weightedMean(values, 0, weights, 0, size)
        val actual = NativeSpeedups.weightedMean(values, 0, weights, 0, size)
        assertEquals(expected, actual, expected * 1E-12)
    }

    companion object {
        private val RANDOM = Random()

        @Parameters
        @JvmStatic fun `data`() = listOf(1000, 1000000)
    }
}

@RunWith(Parameterized::class)
class SumFractionsTest(private val num: Double,
                       private val den: Double,
                       private val size: Int) {
    @Test fun fractions() {
        val fraction = num / den
        val values = DoubleArray(size) { fraction }
        val expected = num * size / den
        assertEquals(expected, DoubleStat.sum(values), expected * 1E-12)
    }

    companion object {
        @Parameters
        @JvmStatic fun `data`() = listOf(arrayOf(1.0, 2.0, 3000000),
                                         arrayOf(3.0, 5.0, 6000000))
    }
}

@RunWith(Parameterized::class)
class SumProgressionTest(private val from: Double, private val to: Double,
                         private val size: Int) {
    @Test fun progression() {
        val values = DoubleArray(size) { from + it * (to - from) / (size - 1) }
        val actual = DoubleStat.sum(values)
        val expected = size * (from + to) / 2
        val absolute = if (from >= 0 || to <= 0)
            Math.abs(expected)
        else
            (to * to + from * from) * size / 2.0 / (to - from)
        assertEquals(expected, actual, absolute * 1E-12)
    }

    companion object {
        @Parameters
        @JvmStatic fun `data`() = listOf(arrayOf(0, 1, 3000001),
                                         arrayOf(-1, 1, 6000001))
    }
}

@RunWith(Parameterized::class)
class SumConsistencyTest(private val size: Int) {
    @Before fun setUp() = Loader.ensureLoaded()

    @Test fun unweighted() {
        val values = RANDOM.doubles(size.toLong()).toArray()
        val expected = DoubleStatJava.sum(values, 0, size)
        val actual = NativeSpeedups.sum(values, 0, size)
        assertEquals(expected, actual, expected * 1E-12)
    }

    @Test fun weighted() {
        val values = RANDOM.doubles(size.toLong()).toArray()
        val weights = RANDOM.doubles(size.toLong()).toArray()
        val expected = DoubleStatJava.weightedSum(values, 0, weights, 0, size)
        val actual = NativeSpeedups.weightedSum(values, 0, weights, 0, size)
        assertEquals(expected, actual, expected * 1E-12)
    }

    companion object {
        private val RANDOM = Random()

        @Parameters
        @JvmStatic fun `data`() = listOf(1000, 1000000)
    }
}

@Ignore
@RunWith(Parameterized::class)
class PrefixSumFractionsTest(private val num: Double,
                             private val den: Double,
                             private val size: Int) {
    @Test fun fractions() {
        val fraction = num / den
        val values = DoubleArray(size) { fraction }
        val actual = DoubleStat.prefixSum(values)
        for (i in 0..size - 1) {
            val expected = (i + 1) * (num / den)
            assertEquals(expected, actual[i], expected * 1E-12)
        }
    }

    companion object {
        @Parameters
        @JvmStatic fun `data`() = listOf(arrayOf(1.0, 2.0, 3000000),
                                         arrayOf(3.0, 5.0, 6000000))
    }
}

@Ignore
@RunWith(Parameterized::class)
class PrefixSumProgressionTest(private val from: Double, private val to: Double,
                               private val size: Int) {
    @Test fun progression() {
        val values = DoubleArray(size) { from + it * (to - from) / (size - 1) }
        val actual = DoubleStat.prefixSum(values)
        val expectedSum = size * (from + to) / 2
        val absolute = if (from >= 0 || to <= 0)
            Math.abs(expectedSum)
        else
            (to * to + from * from) * size / 2.0 / (to - from)
        for (i in 0..size - 1) {
            val expected = (i + 1) * (from + values[i]) / 2
            assertEquals(expected, actual[i], absolute * 1E-12)
        }
    }

    companion object {
        @Parameters
        @JvmStatic fun `data`() = listOf(arrayOf(0, 1, 3000001),
                                         arrayOf(-1, 1, 6000001))
    }
}

@Ignore
@RunWith(Parameterized::class)
class PrefixSumConsistencyTest(private val size: Int) {
    @Before fun setUp() = Loader.ensureLoaded()

    @Test fun unweighted() {
        val values = RANDOM.doubles(size.toLong()).toArray()
        val expected = DoubleArray(size).apply { DoubleStatJava.prefixSum(values, 0, this, 0, size) }
        val actual = DoubleArray(size).apply { NativeSpeedups.prefixSum(values, 0, this, 0, size) }
        assertArrayEquals(expected, actual, 1e-6)
    }

    companion object {
        private val RANDOM = Random()

        @Parameters
        @JvmStatic fun `data`() = listOf(1000, 1000000)
    }
}
