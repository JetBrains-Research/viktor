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
        val v = Random().doubles(size.toLong()).toArray().asF64Array()

        val expected = KahanSum()
        for (i in 0 until v.length) {
            expected.feed(v[i])
        }

        assertEquals(expected.result(), v.sum(), 1e-8)
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
        val v = r.doubles(size.toLong()).toArray().asF64Array()
        val w = r.doubles(size.toLong()).toArray().asF64Array()

        val expected = KahanSum()
        for (i in 0 until v.length) {
            expected.feed(v[i] * w[i])
        }

        assertEquals(expected.result(), v.dot(w), 1e-8)
    }

    @Test fun intAccuracy() {
        val r = Random()
        val v = r.doubles(size.toLong()).toArray().asF64Array()
        val w = r.ints(-1000, 1000).limit(size.toLong()).toArray()

        val expected = KahanSum()
        for (i in 0 until v.length) {
            expected.feed(v[i] * w[i])
        }

        assertEquals(expected.result(), v.dot(w), 1e-8)
    }

    @Test fun shortAccuracy() {
        val r = Random()
        val v = r.doubles(size.toLong()).toArray().asF64Array()
        // there are no short streams
        val w = ShortArray(size)
        for (i in 0 until size) {
            w[i] = (r.nextInt(2000) - 1000).toShort()
        }
        val expected = KahanSum()
        for (i in 0 until v.length) {
            expected.feed(v[i] * w[i])
        }

        assertEquals(expected.result(), v.dot(w), 1e-8)
    }

    companion object {
        @Parameters(name = "{0}")
        @JvmStatic fun `data`() = listOf(32, 64, 100, 500)
    }
}
