package org.jetbrains.bio.jni

import org.apache.commons.math3.random.RandomDataGenerator
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import org.junit.runners.Parameterized.Parameters

@RunWith(Parameterized::class)
class DoubleMathConsistencyTest(private val size: Int) {
    private val values = RandomDataGenerator().let { r ->
        DoubleArray(size) { r.nextGamma(1.0, 3.0) }
    }

    @Test fun exp() {
        val expected = DoubleArray(size).apply {
            DoubleMathJava.exp(values, 0, this, 0, size)
        }
        val actual = DoubleArray(size).apply {
            NativeSpeedups.unsafeExp(values, 0, this, 0, size)
        }

        assertArrayEquals(expected, actual, 1e-7)
    }

    @Test fun expm1() {
        val expected = DoubleArray(size).apply {
            DoubleMathJava.expm1(values, 0, this, 0, size)
        }
        val actual = DoubleArray(size).apply {
            NativeSpeedups.unsafeExpm1(values, 0, this, 0, size)
        }

        assertArrayEquals(expected, actual, 1e-10)
    }

    @Test fun log() {
        val expected = DoubleArray(size).apply {
            DoubleMathJava.log(values, 0, this, 0, size)
        }
        val actual = DoubleArray(size).apply {
            NativeSpeedups.unsafeLog(values, 0, this, 0, size)
        }

        assertArrayEquals(expected, actual, 1e-10)
    }

    @Test fun log1p() {
        val expected = DoubleArray(size).apply {
            DoubleMathJava.log1p(values, 0, this, 0, size)
        }
        val actual = DoubleArray(size).apply {
            NativeSpeedups.unsafeLog1p(values, 0, this, 0, size)
        }

        assertArrayEquals(expected, actual, 1e-10)
    }

    @Test fun logSumExp() {
        val expected = DoubleMathJava.logSumExp(values, 0, size)
        val actual = NativeSpeedups.unsafeLogSumExp(values, 0, size)
        assertEquals(expected, actual, 1e-10)
    }

    @Test fun logAddExp() {
        val other = RandomDataGenerator().let { r ->
            DoubleArray(size) { r.nextGamma(1.0, 3.0) }
        }

        val expected = DoubleArray(size).apply {
            DoubleMathJava.logAddExp(values, 0, other, 0, this, 0, size)
        }
        val actual = DoubleArray(size).apply {
            NativeSpeedups.unsafeLogAddExp(values, 0, other, 0, this, 0, size)
        }

        assertArrayEquals(expected, actual, 1e-10)
    }

    companion object {
        @Parameters(name = "{0}")
        @JvmStatic fun `data`() = listOf(32, 100, 501, 1024)
    }
}
