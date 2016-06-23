package org.jetbrains.bio.viktor

import org.apache.commons.math3.util.Precision
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import org.junit.runners.Parameterized.Parameters
import java.util.*

@RunWith(Parameterized::class)
class BalancedSumTest(private val size: Int) {
    @Test fun accuracy() {
        val v = Random().doubles(size.toLong()).toArray().asStrided()

        val expected = KahanSum()
        for (value in v) {
            expected.feed(value)
        }

        assertEquals(expected.result(), v.balancedSum(), Precision.EPSILON)
    }

    companion object {
        @Parameters(name = "{0}")
        @JvmStatic fun `data`() = listOf(32, 64, 100, 500)
    }
}