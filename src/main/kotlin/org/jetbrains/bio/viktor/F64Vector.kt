package org.jetbrains.bio.viktor

import org.apache.commons.math3.util.FastMath
import org.apache.commons.math3.util.Precision
import java.text.DecimalFormat

/**
 * Wraps a given array of elements. The array will not be copied.
 */
fun DoubleArray.asVector(offset: Int = 0, size: Int = this.size): F64Vector {
    return F64Vector.create(this, offset, size, 1)
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
 * ```
 * data = [0, 1, 2, 3, 4, 5]
 * offset = 1
 * size = 2
 * stride = 3
 * ```
 *
 * corresponds to a vector with elements
 *
 * ```
 * [1, 4]
 * ```
 *
 * Vectors with `stride` equal to 1 are called called *dense*. The
 * distinction is important because some of the operations can be
 * significantly optimized for dense vectors.
 *
 * @author Sergei Lebedev
 * @since 0.1.0
 */
open class F64Vector internal constructor(
        /** Raw data array. */
        override val data: DoubleArray,
        /** Offset of the first vector element in the raw data array. */
        override val offset: Int,
        /** Number of elements in the raw data array to use. */
        override val size: Int,
        /** Indexing step. */
        private val stride: Int) : F64Array.CastOps<F64Vector> {

    override val strides: IntArray get() = intArrayOf(stride)

    /** Returns the shape of this vector. */
    override val shape: IntArray get() = intArrayOf(size)

    val indices: IntRange get() = 0..size - 1

    override fun flatten() = this

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
     * @param step indexing step.
     */
    fun slice(from: Int = 0, to: Int = size, step: Int = 1): F64Vector {
        if (from < 0 || to < from || to > size) {
            throw IndexOutOfBoundsException()
        }

        return F64Vector(data, offset + from, (to - from + step - 1) / step,
                             stride * step)
    }

    operator fun set(any: _I, init: Double) = fill(init)

    operator fun set(any: _I, other: F64Vector) = other.copyTo(this)

    operator fun contains(other: Double): Boolean {
        for (pos in 0..size - 1) {
            if (unsafeGet(pos) == other) {
                return true
            }
        }

        return false
    }

    override fun fill(init: Double) {
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

    override fun copy(): F64Vector {
        val copy = F64Vector(size)
        copyTo(copy)
        return copy
    }

    override fun copyTo(other: F64Array) {
        checkShape(other)
        other as F64Vector
        for (pos in 0..size - 1) {
            other.unsafeSet(pos, unsafeGet(pos))
        }
    }

    /** An alias for [transpose]. */
    override val T: F64Matrix get() = transpose()

    /**
     * Constructs a column-vector view of this vector in O(1) time.
     *
     * A column vector is a matrix with [size] rows and a single column,
     * e.g. `[1, 2, 3]^T` is `[[1], [2], [3]]`.
     */
    override fun transpose(): F64Matrix = reshape(1, size) as F64Matrix

    /**
     * Appends this vector to another vector.
     *
     * @since 0.2.3
     */
    fun append(other: F64Vector) = concatenate(this, other)

    /**
     * Computes a dot product of this vector with an array.
     */
    infix fun dot(other: ShortArray) = balancedDot { other[it].toDouble() }

    /**
     * Computes a dot product of this vector with an array.
     */
    infix fun dot(other: IntArray) = balancedDot { other[it].toDouble() }

    /**
     * Computes a dot product of this vector with an array.
     */
    infix fun dot(other: DoubleArray) = dot(other.asVector())

    /**
     * Computes a dot product between the two vectors.
     *
     * Optimized for dense vectors.
     */
    open infix fun dot(other: F64Vector) = balancedDot { other[it] }

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

    override fun sum() = balancedSum()

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

    override fun min() = unsafeGet(argMin())

    override fun argMin(): Int {
        require(size > 0) { "no data" }
        var minPos = 0
        var minValue = Double.POSITIVE_INFINITY
        for (pos in 0..size - 1) {
            val value = unsafeGet(pos)
            if (value < minValue) {
                minPos = pos
                minValue = value
            }
        }

        return minPos
    }

    override fun max() = unsafeGet(argMax())

    override fun argMax(): Int {
        require(size > 0) { "no data" }
        var maxPos = 0
        var maxValue = Double.NEGATIVE_INFINITY
        for (pos in 0..size - 1) {
            val value = unsafeGet(pos)
            if (value > maxValue) {
                maxPos = pos
                maxValue = value
            }
        }

        return maxPos
    }

    override fun expInPlace() {
        for (pos in 0..size - 1) {
            unsafeSet(pos, FastMath.exp(unsafeGet(pos)))
        }
    }

    override fun expm1InPlace() {
        for (pos in 0..size - 1) {
            unsafeSet(pos, FastMath.expm1(unsafeGet(pos)))
        }
    }

    override fun logInPlace() {
        for (pos in 0..size - 1) {
            unsafeSet(pos, Math.log(unsafeGet(pos)))
        }
    }

    override fun log1pInPlace() {
        for (pos in 0..size - 1) {
            unsafeSet(pos, Math.log1p(unsafeGet(pos)))
        }
    }

    override fun logSumExp(): Double {
        val offset = max()
        val acc = KahanSum()
        for (pos in 0..size - 1) {
            acc += FastMath.exp(unsafeGet(pos) - offset)
        }

        return Math.log(acc.result()) + offset
    }

    override fun logAddExp(other: F64Array, dst: F64Array) {
        checkShape(other)
        checkShape(dst)
        other as F64Vector
        dst as F64Vector
        for (pos in 0..size - 1) {
            dst.unsafeSet(pos, unsafeGet(pos) logAddExp other.unsafeGet(pos))
        }
    }

    override fun unaryMinus(): F64Vector {
        // XXX 'v' is always dense but it might be too small to benefit
        //     from SIMD.
        val v = copy()
        for (pos in 0..size - 1) {
            v.unsafeSet(pos, -unsafeGet(pos))
        }

        return v
    }

    override fun plusAssign(other: F64Array) {
        checkShape(other)
        other as F64Vector
        for (pos in 0..size - 1) {
            unsafeSet(pos, unsafeGet(pos) + other.unsafeGet(pos))
        }
    }

    override fun plusAssign(update: Double) {
        for (pos in 0..size - 1) {
            unsafeSet(pos, unsafeGet(pos) + update)
        }
    }

    override fun minusAssign(other: F64Array) {
        checkShape(other)
        other as F64Vector
        for (pos in 0..size - 1) {
            unsafeSet(pos, unsafeGet(pos) - other.unsafeGet(pos))
        }
    }

    override fun minusAssign(update: Double) {
        for (pos in 0..size - 1) {
            unsafeSet(pos, unsafeGet(pos) - update)
        }
    }

    override fun timesAssign(other: F64Array) {
        checkShape(other)
        other as F64Vector
        for (pos in 0..size - 1) {
            unsafeSet(pos, unsafeGet(pos) * other.unsafeGet(pos))
        }
    }

    override fun timesAssign(update: Double) {
        for (pos in 0..size - 1) {
            unsafeSet(pos, unsafeGet(pos) * update)
        }
    }

    override fun divAssign(other: F64Array) {
        checkShape(other)
        other as F64Vector
        for (pos in 0..size - 1) {
            unsafeSet(pos, unsafeGet(pos) / other.unsafeGet(pos))
        }
    }

    override fun divAssign(update: Double) {
        for (pos in 0..size - 1) {
            unsafeSet(pos, unsafeGet(pos) / update)
        }
    }

    fun isEmpty() = size == 0

    fun isNotEmpty() = size > 0

    override fun toArray() = DoubleArray(size) { unsafeGet(it) }

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

    override fun toString() = toString(8)

    override fun equals(other: Any?) = when {
        this === other -> true
        other !is F64Vector -> false
        size != other.size -> false
        else -> (0..size - 1).all {
            Precision.equals(unsafeGet(it), other.unsafeGet(it))
        }
    }

    override fun hashCode() = (0..size - 1).fold(1) { acc, pos ->
        // XXX calling #hashCode results in boxing, see KT-7571.
        31 * acc + java.lang.Double.hashCode(unsafeGet(pos))
    }

    companion object {
        /**
         * Create a zero-filled vector of a given `size`.
         */
        operator fun invoke(size: Int) = create(DoubleArray(size), 0, size, 1)

        operator inline fun invoke(size: Int, block: (Int) -> Double): F64Vector {
            val v = this(size)
            for (i in 0..size - 1) {
                v[i] = block(i)
            }
            return v
        }

        /**
         * Creates a vector with given elements.
         */
        @JvmStatic fun of(first: Double, vararg rest: Double): F64Vector {
            val data = DoubleArray(rest.size + 1)
            data[0] = first
            System.arraycopy(rest, 0, data, 1, rest.size)
            return data.asVector()
        }

        /** Creates an array with elements summing to one. */
        @JvmStatic fun stochastic(size: Int): F64Vector {
            return full(size, 1.0 / size)
        }

        /** Creates an array filled with a given [init] element. */
        @JvmStatic fun full(size: Int, init: Double): F64Vector {
            val v = this(size)
            v.fill(init)
            return v
        }

        /**
         * Joins a sequence of vectors into a single vector.
         *
         * @since 0.2.3
         */
        @JvmStatic fun concatenate(first: F64Vector, vararg rest: F64Vector): F64Vector {
            val size = first.size + rest.sumBy { it.size }
            val result = F64Vector(size)
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
                            size: Int = data.size, stride: Int = 1): F64Vector {
            require(offset + size <= data.size) { "not enough data" }
            return if (stride == 1) {
                DenseF64Vector.create(data, offset, size)
            } else {
                F64Vector(data, offset, size, stride)
            }
        }
    }
}