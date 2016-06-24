package org.jetbrains.bio.jni

import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import org.junit.runners.Parameterized.Parameters
import java.util.*

class FixedValuesTest {
    @Test fun wholeArray() {
        assertEquals(0.8286257, DoubleStat.standardDeviation(VALUES), 1E-7)
        assertEquals(0.7861034, DoubleStat.standardDeviationBiased(VALUES), 1E-7)
    }

    @Test fun slices() {
        assertEquals(1.016512, DoubleStat.standardDeviation(VALUES, 3, 4), 1E-6)
        assertEquals(0.8803251, DoubleStat.standardDeviationBiased(VALUES, 3, 4), 1E-7)
    }

    @Test fun weighted() {
        assertEquals(0.9458158, DoubleStat.weightedStandardDeviationBiased(VALUES, WEIGHTS), 1E-7)
    }

    @Test fun weightedSlices() {
        assertEquals(0.6851563, DoubleStat.weightedStandardDeviationBiased(VALUES, 3, WEIGHTS, 2, 4), 1E-7)
    }

    companion object {

        private val VALUES = doubleArrayOf(1.5409738, 2.6926526, 0.8159389, 2.5009070,
                                           3.2777667, 1.5157005, 0.9984120, 2.3274278,
                                           1.7286019, 0.9756442)
        private val WEIGHTS = doubleArrayOf(0.04437868, 0.93508668, 0.09091827, 0.17638019,
                                            0.86624410, 0.24522868, 0.85157408, 0.17318330,
                                            0.07582913, 0.73878585)
    }
}

@RunWith(Parameterized::class)
class SDConsistencyTest(private val size: Int) {
    @Test fun unweighted() {
        val data = Random().doubles(size.toLong()).toArray()
        val expected = DoubleStatJava.standardDeviation(data, 0, size)
        val actual = NativeSpeedups.sd(data, 0, size)
        assertEquals(expected, actual, (expected + actual) * 1E-12)
    }

    @Test fun weighted() {
        val values = RANDOM.doubles(size.toLong()).toArray()
        val weights = RANDOM.doubles(size.toLong()).toArray()
        val expected = DoubleStatJava.weightedStandardDeviation(values, 0, weights, 0, size)
        val actual = NativeSpeedups.weightedSd(values, 0, weights, 0, size)
        assertEquals(expected, actual, expected * 1E-12)
    }

    companion object {
        private val RANDOM = Random()

        @Parameters
        @JvmStatic fun `data`() = listOf(1000, 1000000)
    }
}
