package org.jetbrains.bio.viktor

import org.junit.Assert
import org.junit.Test

class F64ArrayAgainstRTest {
    @Test
    fun whole() {
        val v = VALUES.asF64Array()
        Assert.assertEquals(18.37403, v.sum(), 1E-5)
        Assert.assertEquals(1.837403, v.mean(), 1E-6)
        Assert.assertEquals(0.8286257, v.sd(), 1E-7)
    }

    @Test
    fun slices() {
        val v = VALUES.asF64Array(offset = 3, size = 4)
        Assert.assertEquals(8.292786, v.sum(), 1E-6)
        Assert.assertEquals(2.073197, v.mean(), 1E-6)
        Assert.assertEquals(1.016512, v.sd(), 1E-6)
    }

    @Test
    fun weighted() {
        val v = VALUES.asF64Array()
        val w = WEIGHTS.asF64Array()
        Assert.assertEquals(8.417747, v.dot(w), 1E-6)
    }

    @Test
    fun weightedSlices() {
        val v = VALUES.asF64Array(offset = 3, size = 4)
        val w = WEIGHTS.asF64Array(offset = 2, size = 4)
        Assert.assertEquals(2.363317, v.dot(w), 1E-6)
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