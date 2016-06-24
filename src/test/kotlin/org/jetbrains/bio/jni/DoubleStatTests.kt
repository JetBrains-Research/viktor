package org.jetbrains.bio.jni

import org.junit.Assert.assertEquals
import org.junit.Test

class FixedValuesTest {
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
