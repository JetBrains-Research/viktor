package org.jetbrains.bio.viktor

import org.apache.commons.math3.complex.Complex
import org.jtransforms.fft.DoubleFFT_1D
import org.junit.Test
import kotlin.random.Random
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ComplexTimesTest {
    @Test
    fun random() {
        val src1 = F64Array(N, 2) { _, _ -> RANDOM.nextDouble() }
        val src2 = F64Array(N, 2) { _, _ -> RANDOM.nextDouble() }
        val dst = src1.complexTimes(src2)
        toComplex(src1).zip(toComplex(src2)).zip(toComplex(dst)).forEach { (s, z) ->
            val (x, y) = s
            val expected = x.multiply(y)
            assertEquals(expected, z)
        }
    }

    /**
     * Produce unaligned (and mutually unaligned) arrays.
     */
    @Test
    fun alignment() {
        val src1 = F64Array(2 * N + 8) { RANDOM.nextDouble() }
        val src2 = F64Array(2 * N + 8) { RANDOM.nextDouble() }
        for (i in 0 until 8) {
            for (j in 0 until 8) {
                src1.slice(i, i + 2 * N).reshape(N, 2).complexTimes(src2.slice(j, j + 2 * N).reshape(N ,2))
            }
        }
    }
}

class FFTTest {

    @Test
    fun basic() {
        val src = F64Array(N, 4, 2).apply {
            along(0).forEach {
                it.V[_I, 0] = doubleArrayOf(8.0, 4.0, 8.0, 0.0).asF64Array()
            }
        }.reshape(N * 4, 2)
        val actual = src.fft()
        assertTrue(src.shape.contentEquals(actual.shape), "FFT result has different shape")
        val expected = F64Array(N * 4, 2)
        expected[0, 0] = N * 20.0
        expected[N, 1] = N * -4.0
        expected[2 * N, 0] = N * 12.0
        expected[3 * N, 1] = N * 4.0
        assertEquals(expected, actual, 1E-14, "FFT failed")
    }

    @Test
    fun random() {
        val src = F64Array(N, 2) { _, _ -> RANDOM.nextDouble() }
        val actual = src.fft()
        DoubleFFT_1D(N.toLong()).complexForward(src.data) // in-place
        assertEquals(src, actual, 1E-14, "FFT failed")
    }

    /**
     * Produce unaligned arrays.
     */
    @Test
    fun alignment() {
        val src = F64Array(2 * N + 8) { RANDOM.nextDouble() }
        for (i in 0 until 8) {
            src.slice(i, i + 2 * N).reshape(N, 2).fft()
        }
    }

}

private const val N = 128
private val RANDOM = Random(42)
private fun assertEquals(expected: F64Array, actual: F64Array, delta: Double, message: String) {
    assertTrue(
        expected.shape.contentEquals(actual.shape),
        "$message: array shapes differ: expected ${expected.shape.contentToString()}, " +
                "actual ${actual.shape.contentToString()}"
    )
    val minDelta = (expected - actual).min()
    val maxDelta = (expected - actual).max()
    assertTrue(minDelta >= - delta, "$message: array difference ${-minDelta} exceeds delta $delta")
    assertTrue(maxDelta <= delta, "$message: array difference $maxDelta exceeds delta $delta")
}

private fun toComplex(a: F64Array) = a.along(0).map { Complex(it[0], it[1]) }