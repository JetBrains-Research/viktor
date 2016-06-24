package org.jetbrains.bio.viktor

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

        assertEquals(expected.result(), v.balancedSum(), 1e-8)
    }

    companion object {
        @Parameters(name = "{0}")
        @JvmStatic fun `data`() = listOf(32, 64, 100, 500)
    }
}

@RunWith(Parameterized::class)
class BalancedDotTest(private val size: Int) {
    @Test fun accuracy() {
        val r = Random()
        val v = r.doubles(size.toLong()).toArray().asStrided()
        val w = r.doubles(size.toLong()).toArray().asStrided()

        val expected = KahanSum()
        for (i in v.indices) {
            expected.feed(v[i] * w[i])
        }

        assertEquals(expected.result(), v.balancedDot(w), 1e-8)
    }

    companion object {
        @Parameters(name = "{0}")
        @JvmStatic fun `data`() = listOf(32, 64, 100, 500)
    }
}
