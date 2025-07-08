package org.jetbrains.bio.viktor

import jdk.incubator.vector.DoubleVector
import jdk.incubator.vector.VectorOperators
import java.lang.Double
import kotlin.DoubleArray
import kotlin.Int
import kotlin.math.*
import kotlin.run

/**
 * This class provides optimized implementations of various mathematical operations
 * using the Java 21 Vector API for better performance.
 *
 * The Vector API allows for SIMD (Single Instruction, Multiple Data) operations,
 * which can significantly improve performance for these types of operations.
 */
object VectorApiSpeedups {
    /**
     * Computes the exponential function (e^x) for each element in the source array.
     *
     * Implemented using Vector API for SIMD operations.
     */
    fun unsafeExp(
        dst: DoubleArray,
        dstOffset: Int,
        src: DoubleArray,
        srcOffset: Int,
        size: Int
    ) {
        val species = DoubleVector.SPECIES_PREFERRED
        val upperBound = size - (size % species.length())

        // Process elements in chunks using SIMD operations
        run {
            var i = 0
            while (i < upperBound) {
                var v = DoubleVector.fromArray(species, src, srcOffset + i)
                v = v.lanewise(VectorOperators.EXP)
                v.intoArray(dst, dstOffset + i)
                i += species.length()
            }
        }

        // Process remaining elements
        for (i in upperBound..<size) {
            dst[dstOffset + i] = exp(src[srcOffset + i])
        }
    }

    /**
     * Computes e^x - 1 for each element in the source array.
     *
     * Implemented using Vector API for SIMD operations.
     */
    fun unsafeExpm1(
        dst: DoubleArray,
        dstOffset: Int,
        src: DoubleArray,
        srcOffset: Int,
        size: Int
    ) {
        val species = DoubleVector.SPECIES_PREFERRED
        val upperBound = size - (size % species.length())

        // Process elements in chunks using SIMD operations
        run {
            var i = 0
            while (i < upperBound) {
                var v = DoubleVector.fromArray(species, src, srcOffset + i)
                v = v.lanewise(VectorOperators.EXPM1)
                v.intoArray(dst, dstOffset + i)
                i += species.length()
            }
        }

        // Process remaining elements
        for (i in upperBound..<size) {
            dst[dstOffset + i] = expm1(src[srcOffset + i])
        }
    }

    /**
     * Computes the natural logarithm (ln(x)) for each element in the source array.
     *
     * Implemented using Vector API for SIMD operations.
     */
    fun unsafeLog(
        dst: DoubleArray,
        dstOffset: Int,
        src: DoubleArray,
        srcOffset: Int,
        size: Int
    ) {
        val species = DoubleVector.SPECIES_PREFERRED
        val upperBound = size - (size % species.length())

        // Process elements in chunks using SIMD operations
        run {
            var i = 0
            while (i < upperBound) {
                var v = DoubleVector.fromArray(species, src, srcOffset + i)
                v = v.lanewise(VectorOperators.LOG)
                v.intoArray(dst, dstOffset + i)
                i += species.length()
            }
        }

        // Process remaining elements
        for (i in upperBound..<size) {
            dst[dstOffset + i] = ln(src[srcOffset + i])
        }
    }

    /**
     * Computes ln(1 + x) for each element in the source array.
     *
     * Implemented using Vector API for SIMD operations.
     */
    fun unsafeLog1p(
        dst: DoubleArray,
        dstOffset: Int,
        src: DoubleArray,
        srcOffset: Int,
        size: Int
    ) {
        val species = DoubleVector.SPECIES_PREFERRED
        val upperBound = size - (size % species.length())

        // Process elements in chunks using SIMD operations
        run {
            var i = 0
            while (i < upperBound) {
                var v = DoubleVector.fromArray(species, src, srcOffset + i)
                v = v.lanewise(VectorOperators.LOG1P)
                v.intoArray(dst, dstOffset + i)
                i += species.length()
            }
        }

        // Process remaining elements
        for (i in upperBound..<size) {
            dst[dstOffset + i] = ln1p(src[srcOffset + i])
        }
    }

    /**
     * Computes the sum of all elements in the source array.
     *
     * Implemented using Vector API for SIMD operations.
     */
    fun unsafeSum(
        src: DoubleArray,
        srcOffset: Int,
        size: Int
    ): kotlin.Double {
        val species = DoubleVector.SPECIES_PREFERRED
        val upperBound = size - (size % species.length())

        // Process elements in chunks using SIMD operations
        var sum = DoubleVector.zero(species)
        run {
            var i = 0
            while (i < upperBound) {
                val v = DoubleVector.fromArray(species, src, srcOffset + i)
                sum = sum.add(v)
                i += species.length()
            }
        }

        // Reduce vector to scalar sum
        var result = sum.reduceLanes(VectorOperators.ADD)

        // Add remaining elements
        for (i in upperBound..<size) {
            result += src[srcOffset + i]
        }

        return result
    }

    /**
     * Computes the standard deviation of the elements in the source array.
     *
     * Implemented using Vector API for SIMD operations.
     */
    fun unsafeSD(
        src: DoubleArray,
        srcOffset: Int,
        size: Int
    ): kotlin.Double {
        val species = DoubleVector.SPECIES_PREFERRED
        val upperBound = size - (size % species.length())

        // Process elements in chunks using SIMD operations
        var sumVector = DoubleVector.zero(species)
        var sumSqVector = DoubleVector.zero(species)

        run {
            var i = 0
            while (i < upperBound) {
                val v = DoubleVector.fromArray(species, src, srcOffset + i)
                sumVector = sumVector.add(v)
                sumSqVector = sumSqVector.add(v.mul(v))
                i += species.length()
            }
        }

        // Reduce vectors to scalar values
        var sum = sumVector.reduceLanes(VectorOperators.ADD)
        var sumSq = sumSqVector.reduceLanes(VectorOperators.ADD)

        // Process remaining elements
        for (i in upperBound..<size) {
            val value = src[srcOffset + i]
            sum += value
            sumSq += value * value
        }

        return sqrt((sumSq - sum * sum / size) / (size - 1))
    }

    /**
     * Computes the dot product of two arrays.
     *
     * Implemented using Vector API for SIMD operations.
     */
    fun unsafeDot(
        src1: DoubleArray,
        src1Offset: Int,
        src2: DoubleArray,
        src2Offset: Int,
        size: Int
    ): kotlin.Double {
        val species = DoubleVector.SPECIES_PREFERRED
        val upperBound = size - (size % species.length())

        // Process elements in chunks using SIMD operations
        var resultVector = DoubleVector.zero(species)
        run {
            var i = 0
            while (i < upperBound) {
                val v1 = DoubleVector.fromArray(species, src1, src1Offset + i)
                val v2 = DoubleVector.fromArray(species, src2, src2Offset + i)
                resultVector = resultVector.add(v1.mul(v2))
                i += species.length()
            }
        }

        // Reduce vector to scalar result
        var result = resultVector.reduceLanes(VectorOperators.ADD)

        // Process remaining elements
        for (i in upperBound..<size) {
            result += src1[src1Offset + i] * src2[src2Offset + i]
        }

        return result
    }

    /**
     * Computes the log-sum-exp of the elements in the source array.
     * This is a numerically stable way to compute log(sum(exp(x))).
     *
     * Implemented using Vector API for SIMD operations.
     */
    fun unsafeLogSumExp(
        src: DoubleArray,
        srcOffset: Int,
        size: Int
    ): kotlin.Double {
        // First find the maximum value (can't easily vectorize this due to reduction)
        var max = Double.NEGATIVE_INFINITY
        for (i in 0..<size) {
            max = max(max, src[srcOffset + i])
        }

        val species = DoubleVector.SPECIES_PREFERRED
        val upperBound = size - (size % species.length())

        // Create a vector filled with the max value
        val maxVector = DoubleVector.broadcast(species, max)
        var sumVector = DoubleVector.zero(species)

        // Process elements in chunks using SIMD operations
        run {
            var i = 0
            while (i < upperBound) {
                val v = DoubleVector.fromArray(species, src, srcOffset + i)
                // Compute exp(x - max) using vector operations
                val expVector = v.sub(maxVector).lanewise(VectorOperators.EXP)
                sumVector = sumVector.add(expVector)
                i += species.length()
            }
        }

        // Reduce vector to scalar sum
        var sum = sumVector.reduceLanes(VectorOperators.ADD)

        // Process remaining elements
        for (i in upperBound..<size) {
            sum += exp(src[srcOffset + i] - max)
        }

        return max + ln(sum)
    }

}
