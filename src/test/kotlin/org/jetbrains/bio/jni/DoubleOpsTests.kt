package org.jetbrains.bio.jni

import org.apache.commons.math3.random.RandomDataGenerator
import org.apache.commons.math3.util.Precision
import org.junit.Assert.assertArrayEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import org.junit.runners.Parameterized.Parameters

@RunWith(Parameterized::class)
class DoubleVectorOpsTest(private val size: Int) {
    private val r = RandomDataGenerator()
    private val values1 = DoubleArray(size) { r.nextGamma(1.0, 3.0) }
    private val values2 = DoubleArray(size) { r.nextGamma(1.0, 3.0) }

    @Before fun setUp() = Loader.ensureLoaded()

    @Test fun plus() {
        val expected = DoubleArray(size) { values1[it] + values2[it] }
        val actual = DoubleArray(size).apply {
            NativeSpeedups.unsafePlus(values1, 0, values2, 0, this, 0, size)
        }

        assertArrayEquals(expected, actual, Precision.EPSILON)
    }

    @Test fun minus() {
        val expected = DoubleArray(size) { values1[it] - values2[it] }
        val actual = DoubleArray(size).apply {
            NativeSpeedups.unsafeMinus(values1, 0, values2, 0, this, 0, size)
        }

        assertArrayEquals(expected, actual, Precision.EPSILON)
    }

    @Test fun times() {
        val expected = DoubleArray(size) { values1[it] * values2[it] }
        val actual = DoubleArray(size).apply {
            NativeSpeedups.unsafeTimes(values1, 0, values2, 0, this, 0, size)
        }

        assertArrayEquals(expected, actual, Precision.EPSILON)
    }

    @Test fun div() {
        val expected = DoubleArray(size) { values1[it] / values2[it] }
        val actual = DoubleArray(size).apply {
            NativeSpeedups.unsafeDiv(values1, 0, values2, 0, this, 0, size)
        }

        assertArrayEquals(expected, actual, Precision.EPSILON)
    }

    companion object {
        @Parameters(name = "{0}")
        @JvmStatic fun `data`() = listOf(32, 100, 501, 1024)
    }
}

@RunWith(Parameterized::class)
class DoubleScalarOpsTest(private val size: Int) {
    private val r = RandomDataGenerator()
    private val values = DoubleArray(size) { r.nextGamma(1.0, 3.0) }
    private val update = r.nextGamma(1.0, 3.0)

    @Before fun setUp() = Loader.ensureLoaded()

    @Test fun plus() {
        val expected = DoubleArray(size) { values[it] + update }
        val actual = DoubleArray(size).apply {
            NativeSpeedups.unsafePlusScalar(values, 0, update, this, 0, size)
        }

        assertArrayEquals(expected, actual, Precision.EPSILON)
    }

    @Test fun minus() {
        val expected = DoubleArray(size) { values[it] - update }
        val actual = DoubleArray(size).apply {
            NativeSpeedups.unsafeMinusScalar(values, 0, update, this, 0, size)
        }

        assertArrayEquals(expected, actual, Precision.EPSILON)
    }

    @Test fun times() {
        val expected = DoubleArray(size) { values[it] * update }
        val actual = DoubleArray(size).apply {
            NativeSpeedups.unsafeTimesScalar(values, 0, update, this, 0, size)
        }

        assertArrayEquals(expected, actual, Precision.EPSILON)
    }

    @Test fun div() {
        val expected = DoubleArray(size) { values[it] / update }
        val actual = DoubleArray(size).apply {
            NativeSpeedups.unsafeDivScalar(values, 0, update, this, 0, size)
        }

        assertArrayEquals(expected, actual, Precision.EPSILON)
    }

    companion object {
        @Parameters(name = "{0}")
        @JvmStatic fun `data`() = listOf(32, 100, 501, 1024)
    }
}
