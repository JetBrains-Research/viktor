package org.jetbrains.bio.viktor

import org.junit.Test
import java.util.*
import kotlin.math.abs
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class MoreMathTest {
    @Test fun testLogAddExpEdgeCases() {
        val r = Random()
        val logx = -Math.abs(r.nextDouble())
        assertEquals(logx, Double.NEGATIVE_INFINITY logAddExp logx)
        assertEquals(logx, logx logAddExp Double.NEGATIVE_INFINITY)
        assertEquals(
            Double.NEGATIVE_INFINITY,
            Double.NEGATIVE_INFINITY logAddExp Double.NEGATIVE_INFINITY
        )
    }
}

class KahanSumTest {
    @Test fun testPrecision() {
        val bigNumber = 10000000
        for (d in 9..15) {
            // note that in each case 1/d is not precisely representable as a double,
            // which is bound to lead to accumulating rounding errors.
            val oneDth = 1.0 / d
            val preciseSum = KahanSum()
            var impreciseSum = 0.0
            for (i in 0 until bigNumber * d) {
                preciseSum += oneDth
                impreciseSum += oneDth
            }

            val imprecision = abs(impreciseSum - bigNumber)
            val precision = abs(preciseSum.result() - bigNumber)
            assertTrue(
                imprecision >= precision,
                "Kahan's algorithm yielded worse precision than ordinary summation: " +
                        "$precision is greater than $imprecision"
            )
        }
    }
}