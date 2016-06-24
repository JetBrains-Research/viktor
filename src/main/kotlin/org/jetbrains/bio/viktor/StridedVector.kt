package org.jetbrains.bio.viktor

import org.apache.commons.math3.util.FastMath
import org.apache.commons.math3.util.Precision
import java.text.DecimalFormat

/**
 * Wraps a given array of elements. The array will not be copied.
 */
fun DoubleArray.asStrided(offset: Int = 0, size: Int = this.size): StridedVector {
    return StridedVector.create(this, offset, size, 1)
}

/**
 * A strided vector stored in a [DoubleArray].
 *
 * Vector is backed by the raw [data] array, which is guaranteed to
 * contain at least [size] elements starting from the [offset] index.
 *
 * The term *strided* means that unlike regular [DoubleArray] the
 * elements of a vector can be at arbitrary index intervals (strides)
 * from each other. For example
 *
 *     data = [0, 1, 2, 3, 4, 5]
 *     offset = 1
 *     size = 2
 *     stride = 3
 *
 * corresponds to a vector with elements
 *
 *     [1, 4]
 *
 * Vectors with `stride` equal to 1 are caled called *dense*. The
 * distinction is important because some of the operations can be
 * significantly optimized for dense vectors.
 *
 * @author Sergei Lebedev
 * @since 0.1.0
 */
open class StridedVector internal constructor(
        /** Raw data array. */
        val data: DoubleArray,
        /** Offset of the first vector element in the raw data array. */
        val offset: Int,
        /** Number of elements in the raw data array to use. */
        val size: Int,
        /** Indexing step. */
        val stride: Int) {

    val indices: IntRange get() = 0..size - 1

    operator fun get(pos: Int): Double {
        try {
            return unsafeGet(pos)
        } catch (ignored: ArrayIndexOutOfBoundsException) {
            throw IndexOutOfBoundsException("index out of bounds: $pos")
        }
    }

    protected open fun unsafeIndex(pos: Int) = offset + pos * stride

    @Suppress("nothing_to_inline")
    internal inline fun unsafeGet(pos: Int) = data[unsafeIndex(pos)]

    operator fun set(pos: Int, value: Double) {
        try {
            unsafeSet(pos, value)
        } catch (ignored: ArrayIndexOutOfBoundsException) {
            throw IndexOutOfBoundsException("index out of bounds: $pos")
        }
    }

    @Suppress("nothing_to_inline")
    internal inline fun unsafeSet(pos: Int, value: Double) {
        data[unsafeIndex(pos)] = value
    }

    /**
     * Creates a sliced view of this vector in O(1) time.
     *
     * @param from the first index of the slice (inclusive).
     * @param to the last index of the slice (exclusive).
     */
    fun slice(from: Int, to: Int = size): StridedVector {
        if (from < 0 || to < from || to > size) {
            throw IndexOutOfBoundsException()  // TODO: detailed error.
        }

        return StridedVector(data, offset + from, to - from, stride)
    }

    operator fun set(any: _I, init: Double) = fill(init)

    operator fun set(any: _I, other: StridedVector) = other.copyTo(this)

    operator fun contains(other: Double): Boolean {
        for (pos in 0..size - 1) {
            if (unsafeGet(pos) == other) {
                return true
            }
        }

        return false
    }

    open fun fill(init: Double) {
        for (pos in 0..size - 1) {
            unsafeSet(pos, init)
        }
    }

    fun reverse() {
        for (i in 0..size / 2 - 1) {
            val tmp = unsafeGet(size - 1 - i)
            unsafeSet(size - 1 - i, unsafeGet(i))
            unsafeSet(i, tmp)
        }
    }

    /** An alias for [transpose]. */
    val T: StridedMatrix2 get() = transpose()

    /**
     * Constructs a column-vector view of this vector in O(1) time.
     *
     * A column vector is a matrix with [size] rows and a single column,
     * e.g. `[1, 2, 3]^T` is `[[1], [2], [3]]`.
     */
    fun transpose() = reshape(size, 1)

    /**
     * Appends this vector to another vector.
     *
     * @since 0.2.3
     */
    fun append(other: StridedVector) = concatenate(this, other)

    /** Returns a copy of the elements in this vector. */
    fun copy(): StridedVector {
        val copy = StridedVector(size)
        copyTo(copy)
        return copy
    }

    /**
     * Copies the elements in this vector to [other].
     *
     * Optimized for dense vectors.
     */
    open fun copyTo(other: StridedVector) {
        require(size == other.size)
        for (pos in 0..size - 1) {
            other.unsafeSet(pos, unsafeGet(pos))
        }
    }

    /** Reshapes this vector into a matrix in row-major order. */
    fun reshape(numRows: Int, numColumns: Int): StridedMatrix2 {
        require(numRows * numColumns == size)
        return StridedMatrix2(numRows, numColumns, data, offset,
                              numColumns * stride, stride)
    }

    /**
     * Computes a dot product of this vector with an array.
     */
    infix fun dot(other: ShortArray): Double = balancedDot { other[it].toDouble() }

    /**
     * Computes a dot product of this vector with an array.
     */
    infix fun dot(other: IntArray): Double = balancedDot { other[it].toDouble() }

    /**
     * Computes a dot product of this vector with an array.
     *
     * Optimized for dense vectors.
     */
    open infix fun dot(other: DoubleArray) = balancedDot { other[it] }

    /**
     * Computes a dot product between the two vectors.
     *
     * Optimized for dense vectors.
     */
    open infix fun dot(other: StridedVector): Double = balancedDot { other[it] }

    /**
     * Computes the mean of the elements.
     *
     * Optimized for dense vectors.
     */
    open fun mean() = sum() / size

    /**
     * Computes the unbiased standard deviation of the elements.
     *
     * Optimized for dense vectors.
     *
     * @since 0.3.0
     */
    open fun sd(): Double {
        val s = sum()
        val s2 = dot(this)
        return Math.sqrt((s2 - s * s / size) / (size - 1))
    }

    /**
     * Returns the sum of the elements using balanced summation.
     *
     * Optimized for dense vectors.
     */
    open fun sum() = balancedSum()

    /**
     * Returns the sum of the squares of elements.
     *
     * Optimized for dense vectors.
     */
    fun sumSq() = dot(this)

    /**
     * Computes cumulative sum of the elements.
     *
     * The operation is done **in place**.
     */
    open fun cumSum() {
        val acc = KahanSum()
        for (pos in 0..size - 1) {
            acc += unsafeGet(pos)
            unsafeSet(pos, acc.result())
        }
    }

    /**
     * Returns the minimum element.
     *
     * Optimized for dense vectors.
     */
    open fun min() = unsafeGet(argMin())

    /**
     * Returns the maximum element.
     *
     * Optimized for dense vectors.
     */
    open fun max() = unsafeGet(argMax())

    fun exp(): StridedVector {
        val copy = copy()
        copy.expInPlace()
        return copy
    }

    /**
     * Computes the exponent of each element of this vector.
     *
     * Optimized for dense vectors.
     */
    open fun expInPlace() {
        for (pos in 0..size - 1) {
            unsafeSet(pos, FastMath.exp(unsafeGet(pos)))
        }
    }

    fun expm1(): StridedVector {
        val copy = copy()
        copy.expm1InPlace()
        return copy
    }

    /**
     * Computes exp(x) - 1 for each element of this vector.
     *
     * Optimized for dense vectors.
     *
     * @since 0.3.0
     */
    open fun expm1InPlace() {
        for (pos in 0..size - 1) {
            unsafeSet(pos, FastMath.expm1(unsafeGet(pos)))
        }
    }

    fun log(): StridedVector {
        val copy = copy()
        copy.logInPlace()
        return copy
    }

    /**
     * Computes the natural log of each element of this vector.
     *
     * Optimized for dense vectors.
     */
    open fun logInPlace() {
        for (pos in 0..size - 1) {
            unsafeSet(pos, Math.log(unsafeGet(pos)))
        }
    }

    fun log1p(): StridedVector {
        val copy = copy()
        copy.log1pInPlace()
        return copy
    }

    /**
     * Computes log(1 + x) for each element of this vector.
     *
     * Optimized for dense vectors.
     *
     * @since 0.3.0
     */
    open fun log1pInPlace() {
        for (pos in 0..size - 1) {
            unsafeSet(pos, Math.log1p(unsafeGet(pos)))
        }
    }

    /**
     * Rescales the elements so that the sum is 1.0.
     *
     * The operation is done **in place**.
     */
    fun rescale() {
        val total = sum() + Precision.EPSILON * size.toDouble()
        this /= total
    }

    /**
     * Rescales the element so that the exponent of the sum is 1.
     *
     * Optimized for dense vectors.
     *
     * The operation is done **in place**.
     */
    open fun logRescale() {
        this -= logSumExp()
    }

    /**
     * Computes
     *
     *   log(exp(v[0]) + ... + exp(v[size - 1]))
     *
     * in a numerically stable way.
     */
    open fun logSumExp(): Double {
        val offset = max()
        val acc = KahanSum()
        for (pos in 0..size - 1) {
            acc += FastMath.exp(unsafeGet(pos) - offset)
        }

        return Math.log(acc.result()) + offset
    }

    infix fun logAddExp(other: StridedVector): StridedVector {
        val v = copy()
        v.logAddExp(other, v)
        return v
    }

    open fun logAddExp(other: StridedVector, dst: StridedVector) {
        checkSize(other)
        checkSize(dst)
        for (pos in 0..size - 1) {
            dst.unsafeSet(pos, unsafeGet(pos) logAddExp other.unsafeGet(pos))
        }
    }

    operator fun unaryPlus() = this

    open operator fun unaryMinus(): StridedVector {
        // XXX 'v' is always dense but it might be too small to benefit
        //     from SIMD.
        val v = copy()
        for (pos in 0..size - 1) {
            v.unsafeSet(pos, -unsafeGet(pos))
        }

        return v
    }

    operator fun plus(other: StridedVector): StridedVector {
        val v = copy()
        v += other
        return v
    }

    operator open fun plusAssign(other: StridedVector) {
        checkSize(other)
        for (pos in 0..size - 1) {
            unsafeSet(pos, unsafeGet(pos) + other.unsafeGet(pos))
        }
    }

    operator fun plus(update: Double): StridedVector {
        val v = copy()
        v += update
        return v
    }

    operator open fun plusAssign(update: Double) {
        for (pos in 0..size - 1) {
            unsafeSet(pos, unsafeGet(pos) + update)
        }
    }

    operator fun minus(other: StridedVector): StridedVector {
        val v = copy()
        v -= other
        return v
    }

    operator open fun minusAssign(other: StridedVector) {
        checkSize(other)
        for (pos in 0..size - 1) {
            unsafeSet(pos, unsafeGet(pos) - other.unsafeGet(pos))
        }
    }

    operator fun minus(update: Double): StridedVector {
        val v = copy()
        v -= update
        return v
    }

    operator open fun minusAssign(update: Double) {
        for (pos in 0..size - 1) {
            unsafeSet(pos, unsafeGet(pos) - update)
        }
    }

    operator fun times(value: Double): StridedVector {
        val v = copy()
        v *= value
        return v
    }

    operator open fun timesAssign(update: Double) {
        for (pos in 0..size - 1) {
            unsafeSet(pos, unsafeGet(pos) * update)
        }
    }

    operator fun times(other: StridedVector): StridedVector {
        val v = copy()
        v *= other
        return v
    }

    operator open fun timesAssign(other: StridedVector) {
        checkSize(other)
        for (pos in 0..size - 1) {
            unsafeSet(pos, unsafeGet(pos) * other.unsafeGet(pos))
        }
    }

    operator fun div(value: Double): StridedVector {
        val v = copy()
        v /= value
        return v
    }

    operator open fun divAssign(update: Double) {
        for (pos in 0..size - 1) {
            unsafeSet(pos, unsafeGet(pos) / update)
        }
    }

    operator fun div(other: StridedVector): StridedVector {
        val v = copy()
        v /= other
        return v
    }

    operator open fun divAssign(other: StridedVector) {
        checkSize(other)
        for (pos in 0..size - 1) {
            unsafeSet(pos, unsafeGet(pos) / other.unsafeGet(pos))
        }
    }

    fun isEmpty() = size == 0

    fun isNotEmpty() = size > 0

    open fun toArray(): DoubleArray {
        val res = DoubleArray(size)
        for (pos in 0..size - 1) {
            res[pos] = unsafeGet(pos)
        }

        return res
    }

    /** Creates an iterator over the elements of the array. */
    operator fun iterator(): DoubleIterator = object : DoubleIterator() {
        var i = 0

        override fun hasNext() = i < size

        override fun nextDouble() = unsafeGet(i++)
    }

    /**
     * A version of [DecimalFormat.format] which doesn't produce ?
     * for [Double.NaN] and infinities.
     */
    private fun DecimalFormat.safeFormat(value: Double) = when {
        value.isNaN() -> "nan"
        value == Double.POSITIVE_INFINITY -> "inf"
        value == Double.NEGATIVE_INFINITY -> "-inf"
        else -> format(value)
    }

    fun toString(maxDisplay: Int,
                 format: DecimalFormat = DecimalFormat("#.####")): String {
        val sb = StringBuilder()
        sb.append('[')

        if (maxDisplay < size) {
            for (pos in 0..maxDisplay / 2 - 1) {
                sb.append(format.safeFormat(this[pos])).append(", ")
            }

            sb.append("..., ")

            val leftover = maxDisplay - maxDisplay / 2
            for (pos in size - leftover..size - 1) {
                sb.append(format.safeFormat(this[pos]))
                if (pos < size - 1) {
                    sb.append(", ")
                }
            }
        } else {
            for (pos in 0..size - 1) {
                sb.append(format.safeFormat(this[pos]))
                if (pos < size - 1) {
                    sb.append(", ")
                }
            }
        }

        sb.append(']')
        return sb.toString()
    }

    override fun toString() = toString(16)

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        } else if (other !is StridedVector) {
            return false
        }

        if (size != other.size) {
            return false
        }

        for (pos in 0..size - 1) {
            if (!Precision.equals(unsafeGet(pos), other.unsafeGet(pos))) {
                return false
            }
        }

        return true
    }

    override fun hashCode(): Int {
        var acc = 1
        for (pos in 0..size - 1) {
            // XXX calling #hashCode results in boxing, see KT-7571.
            acc = 31 * java.lang.Double.hashCode(unsafeGet(pos))
        }

        return acc
    }

    @Suppress("nothing_to_inline")
    internal inline fun checkSize(other: StridedVector) {
        require(size == other.size) { "non-conformable arrays" }
    }

    companion object {
        /**
         * Create a zero-filled vector of a given `size`.
         */
        operator fun invoke(size: Int) = create(DoubleArray(size), 0, size, 1)

        operator inline fun invoke(size: Int, block: (Int) -> Double): StridedVector {
            val v = this(size)
            for (i in 0..size - 1) {
                v[i] = block(i)
            }
            return v
        }

        /**
         * Creates a vector with given elements.
         */
        @JvmStatic fun of(first: Double, vararg rest: Double): StridedVector {
            val data = DoubleArray(rest.size + 1)
            data[0] = first
            System.arraycopy(rest, 0, data, 1, rest.size)
            return data.asStrided()
        }

        /** Creates an array with elements summing to one. */
        @JvmStatic fun stochastic(size: Int): StridedVector {
            return full(size, 1.0 / size)
        }

        /** Creates an array filled with a given [init] element. */
        @JvmStatic fun full(size: Int, init: Double): StridedVector {
            val v = this(size)
            v.fill(init)
            return v
        }

        /**
         * Joins a sequence of vectors into a single vector.
         *
         * @since 0.2.3
         */
        @JvmStatic fun concatenate(first: StridedVector, vararg rest: StridedVector): StridedVector {
            val size = first.size + rest.sumBy { it.size }
            val result = StridedVector(size)
            var offset = 0
            for (v in arrayOf(first, *rest)) {
                if (v.isNotEmpty()) {
                    v.copyTo(result.slice(offset, offset + v.size))
                    offset += v.size
                }
            }

            return result
        }

        internal fun create(data: DoubleArray, offset: Int = 0,
                            size: Int = data.size, stride: Int = 1): StridedVector {
            require(offset + size <= data.size) { "not enough data" }
            return if (stride == 1) {
                DenseVector.create(data, offset, size)
            } else {
                StridedVector(data, offset, size, stride)
            }
        }
    }
}
