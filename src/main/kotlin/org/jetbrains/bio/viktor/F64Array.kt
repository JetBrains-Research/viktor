package org.jetbrains.bio.viktor

import org.apache.commons.math3.util.Precision
import java.util.*

interface F64Array {
    val data: DoubleArray
    val offset: Int
    val strides: IntArray
    val shape: IntArray

    /** Number of axes in this array. */
    val nDim: Int get() = shape.size

    /** Number of elements along the first dimension. */
    val size: Int get() = shape.first()

    /**
     * Returns `true` if this array is dense and `false` otherwise.
     *
     * Dense arrays are laid out in a single contiguous block
     * of memory.
     *
     * This allows to use SIMD operations, e.g. when computing the
     * sum of elements.
     */
    // This is inaccurate, but maybe sufficient for our use-case?
    // Check with http://docs.scipy.org/doc/numpy/reference/arrays.ndarray.html
    val isDense: Boolean get() = strides.last() == 1

    /** Returns a copy of the elements in this array. */
    fun copy(): F64Array

    /** Copies elements in this array to [other] array. */
    fun copyTo(other: F64Array)

    /** Reshapes this vector into a matrix in row-major order. */
    fun reshape(vararg shape: Int): F64Array {
        require(shape.product() == size) {
            "total size of the new matrix must be unchanged"
        }

        if (nDim > 1) {
            TODO()
        }

        val reshaped = shape.clone()
        reshaped[reshaped.lastIndex] = strides.single()
        for (i in reshaped.lastIndex - 1 downTo 0) {
            reshaped[i] = reshaped[i + 1] * shape[i + 1]
        }

        return F64Matrix(data, offset, reshaped, shape)
    }

    /**
     * Flattens the array into a vector in O(1) time.
     *
     * No data copying is performed, thus the operation is only applicable
     * to dense arrays.
     */
    fun flatten(): F64Vector

    /** An alias for [transpose]. */
    val T: F64Array get() = transpose()

    /** Constructs matrix transpose in O(1) time. */
    fun transpose(): F64Array = if (nDim < 2) {
        this
    } else {
        F64Matrix(data, offset, strides.reversedArray(), shape.reversedArray())
    }

    fun fill(init: Double)

    /**
     * Computes the mean of the elements.
     *
     * Optimized for dense vectors.
     */
    fun mean() = sum() / shape.product()

    /**
     * Returns the sum of the elements using balanced summation.
     *
     * Optimized for dense arrays.
     */
    fun sum(): Double

    /**
     * Returns the maximum element.
     *
     * Optimized for dense arrays.
     */
    fun max(): Double

    /**
     * Returns the unravelled index of the maximum element in the
     * flattened array.
     *
     * See [ravelIndex] and [unravelIndex] for details.
     */
    fun argMax(): Int

    /**
     * Returns the minimum element.
     *
     * Optimized for dense arrays.
     */
    fun min(): Double

    /**
     * Returns the unravelled index of the minimum element in the
     * flattened array.
     *
     * See [ravelIndex] and [unravelIndex] for details.
     */    
    fun argMin(): Int

    /**
     * Computes the exponent of each element of this array.
     *
     * Optimized for dense arrays.
     */
    fun expInPlace()

    fun exp() = copy().apply { expInPlace() }

    /**
     * Computes exp(x) - 1 for each element of this array.
     *
     * Optimized for dense arrays.
     *
     * @since 0.3.0
     */    
    fun expm1InPlace()

    fun expm1() = copy().apply { expm1InPlace() }

    /**
     * Computes the natural log of each element of this array.
     *
     * Optimized for dense arrays.
     */    
    fun logInPlace()

    fun log() = copy().apply { logInPlace() }

    /**
     * Computes log(1 + x) for each element of this array.
     *
     * Optimized for dense arrays.
     *
     * @since 0.3.0
     */    
    fun log1pInPlace()

    fun log1p() = copy().apply { log1pInPlace() }

    /**
     * Rescales the elements so that the sum is 1.0.
     *
     * The operation is done **in place**.
     */
    fun rescale() {
        this /= sum() + Precision.EPSILON * shape.product().toDouble()
    }

    /**
     * Rescales the element so that the exponent of the sum is 1.0.
     *
     * Optimized for dense arrays.
     *
     * The operation is done **in place**.
     */
    fun logRescale() {
        this -= logSumExp()
    }

    /**
     * Computes
     *
     *   log(exp(v[0]) + ... + exp(v[size - 1]))
     *
     * in a numerically stable way.
     */
    fun logSumExp(): Double

    infix fun logAddExp(other: F64Array) = copy().apply { logAddExp(other, this) }

    /**
     * Computes elementwise
     *
     *   log(exp(this[i]) + exp(other[i]))
     *
     * in a numerically stable way.
     */
    fun logAddExp(other: F64Array, dst: F64Array)

    operator fun unaryPlus() = this

    operator fun unaryMinus(): F64Array

    operator fun plus(other: F64Array) = copy().apply { this += other }

    operator fun plusAssign(other: F64Array)

    operator fun plus(update: Double) = copy().apply { this += update }

    operator fun plusAssign(update: Double)

    operator fun minus(other: F64Array) = copy().apply { this -= other }

    operator fun minusAssign(other: F64Array)

    operator fun minus(update: Double) = copy().apply { this -= update }

    operator fun minusAssign(update: Double)

    operator fun times(other: F64Array) = copy().apply { this *= other }

    operator fun timesAssign(other: F64Array)

    operator fun times(update: Double) = copy().apply { this *= update }

    operator fun timesAssign(update: Double)

    operator fun div(other: F64Array) = copy().apply { this /= other }

    operator fun divAssign(other: F64Array)

    operator fun div(update: Double) = copy().apply { this /= update }

    operator fun divAssign(update: Double)

    /** Ensures a given array has the same dimensions as this array. */
    fun checkShape(other: F64Array) {
        // Could relax this to "broadcastable".
        require(this === other || Arrays.equals(shape, other.shape))
    }

    fun toArray(): Any

    interface ViaFlatten<out SELF : F64Array> : F64Array.CastOps<SELF> {
        override fun fill(init: Double) = flatten().fill(init)

        override fun mean() = flatten().mean()

        override fun sum() = flatten().sum()

        override fun max() = flatten().max()

        override fun argMax() = flatten().argMax()

        override fun min() = flatten().min()

        override fun argMin() = flatten().argMin()

        override fun expInPlace() = flatten().expInPlace()

        override fun expm1InPlace() = flatten().expInPlace()

        override fun logInPlace() = flatten().logInPlace()

        override fun log1pInPlace() = flatten().log1pInPlace()

        override fun logSumExp() = flatten().logSumExp()

        override fun logRescale() = flatten().logRescale()

        override fun logAddExp(other: F64Array, dst: F64Array) {
            checkShape(other)
            checkShape(dst)
            flatten().logAddExp(other.flatten(), dst.flatten())
        }

        override fun unaryMinus() = copy().apply { (-flatten()).reshape(*shape) }

        override fun plusAssign(other: F64Array) {
            checkShape(other)
            flatten() += other.flatten()
        }

        override fun plusAssign(update: Double) { flatten() += update }

        override fun minusAssign(other: F64Array) {
            checkShape(other)
            flatten() -= other.flatten()
        }

        override fun minusAssign(update: Double) { flatten() -= update }

        override fun timesAssign(other: F64Array) {
            checkShape(other)
            flatten() *= other.flatten()
        }

        override fun timesAssign(update: Double) { flatten() *= update }

        override fun divAssign(other: F64Array) {
            checkShape(other)
            flatten() /= other.flatten()
        }

        override fun divAssign(update: Double) { flatten() /= update }

    }

    interface CastOps<out SELF : F64Array> : F64Array {
        override fun copy(): SELF

        override fun unaryPlus(): SELF = copy()

        override fun unaryMinus(): SELF

        override fun plus(other: F64Array): SELF = copy().apply { this += other }

        override fun plus(update: Double): SELF = copy().apply { this += update }

        override fun minus(other: F64Array): SELF = copy().apply { this -= other }

        override fun minus(update: Double): SELF = copy().apply { this -= update }

        override fun times(other: F64Array): SELF = copy().apply { this *= other }

        override fun times(update: Double): SELF = copy().apply { this *= update }

        override fun div(other: F64Array): SELF = copy().apply { this /= other }

        override fun div(update: Double): SELF = copy().apply { this /= update }
    }    
    
    companion object {
        internal operator fun invoke(data: DoubleArray, offset: Int,
                                     strides: IntArray, shape: IntArray): F64Array {
            return if (shape.size == 1) {
                F64Vector(data, offset, strides.single(), shape.single())
            } else {
                F64Matrix(data, offset, strides, shape)
            }
        }
    }
}

fun main(args: Array<String>) {
    val values = DoubleArray(1024)
    val v = values.asVector(0, values.size)
    v.reshape(2, 4, 128)
    v.reshape(8, 128)
}