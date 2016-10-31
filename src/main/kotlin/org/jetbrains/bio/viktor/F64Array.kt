package org.jetbrains.bio.viktor

import org.apache.commons.math3.util.Precision
import java.text.DecimalFormat
import java.util.*

/**
 * A strided n-dimensional array stored in a [DoubleArray].
 *
 * The term *strided* means that unlike regular [DoubleArray] the
 * elements of an [F64Array] can be at arbitrary index intervals
 * (strides) from each other. For example
 *
 * ```
 * data = [0, 1, 2, 3, 4, 5]
 * offset = 1
 * shape = [2]
 * strides = [3]
 * ```
 *
 * corresponds to a 1-D array with elements
 *
 * ```
 * [1, 4]
 * ```
 *
 * Arrays with last stride equal to 1 are called called *dense*.
 * The distinction is important because some of the operations
 * can be significantly optimized for dense arrays.
 *
 * @author Sergei Lebedev
 * @since 0.4.0
 */
open class F64Array protected constructor(
        /** Raw data array. */
        val data: DoubleArray,
        /** Offset of the first vector element in the raw data array. */
        val offset: Int,
        /** Indexing steps along each axis. */
        val strides: IntArray,
        /** Number of elements along each axis. */
        val shape: IntArray) {

    /** Number of axes in this array. */
    val nDim: Int get() = shape.size

    /** Number of elements along the first axis. */
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

    /**
     * Generic getter.
     *
     * Note that it could be at least 1.5x slower than specialized versions.
     */
    operator fun get(vararg indices: Int): Double {
        require(indices.size == nDim) { "broadcasting get is not supported" }
        return safeIndex({ indices }) { data[unsafeIndex(indices)] }
    }

    operator fun get(pos: Int): Double {
        require(nDim == 1) { "broadcasting get is not supported" }
        return safeIndex({ intArrayOf(pos) }) { data[unsafeIndex(pos)] }
    }

    operator fun get(r: Int, c: Int): Double {
        require(nDim == 2) { "broadcasting get is not supported" }
        return safeIndex({ intArrayOf(r, c) }) { data[unsafeIndex(r, c)] }
    }

    operator fun get(d: Int, r: Int, c: Int): Double {
        require(nDim == 3) { "broadcasting get is not supported" }
        return safeIndex({ intArrayOf(d, r, c) }) { data[unsafeIndex(d, r, c)] }
    }

    /**
     * Generic setter.
     *
     * Note that it could be at least 1.5x slower than specialized versions.
     */
    operator fun set(vararg indices: Int, value: Double) {
        require(indices.size == nDim) { "broadcasting set is not supported" }
        safeIndex({ indices }) { data[unsafeIndex(indices)] = value }
    }

    operator fun set(pos: Int, value: Double) {
        require(nDim == 1) { "broadcasting set is not supported" }
        safeIndex({ intArrayOf(pos) }) { data[unsafeIndex(pos)] = value }
    }

    operator fun set(r: Int, c: Int, value: Double) {
        require(nDim == 2) { "broadcasting set is not supported" }
        safeIndex({ intArrayOf(r, c) }) { data[unsafeIndex(r, c)] = value }
    }

    operator fun set(d: Int, r: Int, c: Int, value: Double) {
        require(nDim == 3) { "broadcasting set is not supported" }
        safeIndex({ intArrayOf(d, r, c) }) { data[unsafeIndex(d, r, c)] = value }
    }

    // XXX required for fallback implementations in [F64FlatVector].
    @Suppress("nothing_to_inline")
    internal inline fun unsafeGet(pos: Int): Double = data[unsafeIndex(pos)]

    @Suppress("nothing_to_inline")
    internal inline fun unsafeSet(pos: Int, value: Double) {
        data[unsafeIndex(pos)] = value
    }

    @Suppress("nothing_to_inline")
    private inline fun unsafeIndex(pos: Int): Int {
        return offset + pos * strides[0]
    }

    @Suppress("nothing_to_inline")
    private inline fun unsafeIndex(r: Int, c: Int): Int {
        return offset + r * strides[0] + c * strides[1]
    }

    @Suppress("nothing_to_inline")
    private inline fun unsafeIndex(d: Int, r: Int, c: Int): Int {
        return offset + d * strides[0] + r * strides[1] + c * strides[2]
    }

    @Suppress("nothing_to_inline")
    private inline fun unsafeIndex(indices: IntArray): Int {
        return strides.foldIndexed(offset) { i, acc, stride -> acc + indices[i] * stride }
    }

    private inline fun <T> safeIndex(indices: () -> IntArray, block: () -> T): T {
        try {
            return block()
        } catch (e: IndexOutOfBoundsException) {
            outOfBounds(indices(), shape)
        }
    }

    /**
     * Returns a sequence of views along the specified [axis].
     *
     * For example, for a 2-D array `axis = 0` means "for each row",
     * and `axis = 1` "for each column".
     */
    open fun along(axis: Int): Sequence<F64Array> {
        return (0..shape[axis] - 1).asSequence()
                .map { view(it, axis) }
    }

    /** Returns a view of this array along the specified [axis]. */
    fun view(index: Int, axis: Int = 0): F64Array {
        checkIndex("axis", axis, nDim)
        checkIndex("index", index, shape[axis])
        return invoke(data, offset + strides[axis] * index,
                      strides.remove(axis), shape.remove(axis))
    }

    /**
     * Computes a nested view over the first axis.
     *
     * Here be dragons!
     */
    @Suppress("nothing_to_inline")
    private inline fun view0(indices: IntArray): F64Array {
        require(indices.size < nDim) { "too many indices" }
        return indices.fold(this) { m, pos -> m.view(pos) }
    }

    /** A broadcasted viewer for this array. */
    val V: Viewer = Viewer(this)

    class Viewer(private val a: F64Array) {
        /**
         * A less-verbose alias to [view].
         *
         * Please do NOT abuse this shortcut by double-indexing, i.e. don't
         * do `m[i][j]`, write `m[i, j]` instead.
         */
        operator fun get(vararg indices: Int) = a.view0(indices)

        operator fun set(vararg indices: Int, other: F64Array) {
            other.copyTo(a.view0(indices))
        }

        operator fun set(vararg indices: Int, init: Double) {
            a.view0(indices).fill(init)
        }

        /** A less-verbose alias to [copyTo]. */
        @Suppress("unused_parameter")
        operator fun set(vararg any: _I, other: F64Array) {
            require(any.size < a.nDim) { "too many axes" }
            other.copyTo(a)
        }

        /**
         * A less-verbose alias to [view].
         *
         * Use in conjunction with [_I], e.g. `m[_I, i]`.
         */
        // XXX we could generalize this in a way similar to the above method.
        //     However, after the resulting methods could only be called via
        //     method call syntax with explicit parameter names. E.g.
        //
        //         get(any: _I, vararg rest: _I, c: Int, other: F64Array)
        //
        //     should be called as get(_I, _I, c = 42) and not [_I, _I, 42].
        @Suppress("unused_parameter")
        operator fun get(any: _I, c: Int) = a.view(c, axis = 1)

        @Suppress("unused_parameter")
        operator fun set(any: _I, c: Int, other: F64Array) {
            other.copyTo(a.view(c, axis = 1))
        }

        @Suppress("unused_parameter")
        operator fun set(any: _I, c: Int, init: Double) {
            a.view(c, axis = 1).fill(init)
        }
    }

    /** Returns a copy of the elements in this array. */
    fun copy(): F64Array {
        val copy = invoke(*shape)
        copyTo(copy)
        return copy
    }

    /** Copies elements in this array to [other] array. */
    open fun copyTo(other: F64Array) {
        checkShape(other)
        if (Arrays.equals(strides, other.strides)) {
            System.arraycopy(data, offset, other.data, other.offset,
                             shape.product())
        } else {
            for (r in 0..size - 1) {
                V[r].copyTo(other.V[r])
            }
        }
    }

    /** A less verbose alternative to [copyTo]. */
    operator fun set(vararg any: _I, other: F64Array) = when {
        any.size > nDim -> throw IllegalArgumentException("too many axes")
        any.size < nDim -> throw IllegalArgumentException("too few axes")
        else -> other.copyTo(this)
    }

    /** Reshapes this vector into a matrix in row-major order. */
    fun reshape(vararg shape: Int): F64Array {
        require(shape.product() == size) {
            "total size of the new matrix must be unchanged"
        }

        return when {
            nDim > 1 -> TODO()
            Arrays.equals(this.shape, shape) -> this
            else -> {
                val reshaped = shape.clone()
                reshaped[reshaped.lastIndex] = strides.single()
                for (i in reshaped.lastIndex - 1 downTo 0) {
                    reshaped[i] = reshaped[i + 1] * shape[i + 1]
                }

                invoke(data, offset, reshaped, shape)
            }
        }
    }

    /**
     * Appends this array to another array.
     *
     * @since 0.2.3
     */
    fun append(other: F64Array, axis: Int = 0): F64Array {
        return F64Array.concatenate(this, other, axis = axis)
    }

    /**
     * Flattens the array into a 1-D view in O(1) time.
     *
     * No data copying is performed, thus the operation is only applicable
     * to dense arrays.
     */
    open fun flatten(): F64Array {
        check(isDense) { "matrix is not dense" }
        return data.asF64Array(offset, shape.product())
    }

    /** An alias for [transpose]. */
    val T: F64Array get() = transpose()

    /** Constructs matrix transpose in O(1) time. */
    open fun transpose(): F64Array {
        return invoke(data, offset, strides.reversedArray(), shape.reversedArray())
    }

    /**
     * Creates a sliced view of this array in O(1) time.
     *
     * @param from the first index of the slice (inclusive).
     * @param to the last index of the slice (exclusive).
     * @param step indexing step.
     * @param axis to slice along.
     */
    fun slice(from: Int = 0, to: Int = -1, step: Int = 1, axis: Int = 0): F64Array {
        val axisTo = if (to == -1) shape[axis] else to
        if (from < 0 || axisTo < from || axisTo > shape[axis]) {
            throw IndexOutOfBoundsException()
        }

        val sliceStrides = strides.clone().apply { this[axis] *= step }
        val sliceShape = shape.clone().apply {
            this[axis] = (axisTo - from + step - 1) / step
        }
        return invoke(data, offset + from * strides[axis], sliceStrides, sliceShape)
    }

    open operator fun contains(other: Double): Boolean = flatten().contains(other)

    /** Fills this array with a given [init] value. */
    open fun fill(init: Double): Unit = flatten().fill(init)

    fun reversed(axis: Int = 0): F64Array {
        return invoke(data, offset + strides[axis] * (shape[axis] - 1),
                      strides.clone().apply { this[axis] *= -1 },
                      shape)
    }

    /** Applies a given permutation of indices to the elements in the array. */
    open fun reorder(indices: IntArray, axis: Int = 0) {
        reorderInternal(this, indices, axis,
                        get = { pos -> view(pos, axis).copy() },
                        set = { pos, value -> value.copyTo(view(pos, axis)) })
    }

    /** A less verbose alternative to [fill]. */
    operator fun set(vararg any: _I, value: Double) = when {
        any.size > nDim -> throw IllegalArgumentException("too many axes")
        any.size < nDim -> throw IllegalArgumentException("too few axes")
        else -> fill(value)
    }

    /** Computes a dot product between two 1-D arrays. */
    open infix fun dot(other: ShortArray): Double = unsupported()

    /** Computes a dot product between two 1-D arrays. */
    open infix fun dot(other: IntArray): Double = unsupported()

    /** Computes a dot product between two 1-D arrays. */
    infix fun dot(other: DoubleArray): Double = dot(other.asF64Array())

    /**
     * Computes a dot product between two 1-D arrays.
     *
     * Optimized for dense arrays.
     */
    open infix fun dot(other: F64Array): Double = unsupported()

    /**
     * Computes the mean of the elements.
     *
     * Optimized for dense arrays.
     */
    fun mean() = sum() / shape.product()

    /**
     * Computes the unbiased standard deviation of the elements.
     *
     * Optimized for dense arrays.
     *
     * @since 0.3.0
     */
    open fun sd(): Double {
        val s = sum()
        val s2 = dot(this)
        return Math.sqrt((s2 - s * s / size) / (size - 1))
    }

    /**
     * Returns the sum of the elements.
     *
     * Optimized for dense arrays.
     */
    open fun sum(): Double = flatten().sum()

    /**
     * Returns the sum of the elements using balanced summation.
     *
     * Optimized for dense arrays.
     */
    open fun balancedSum(): Double = flatten().balancedSum()

    /**
     * Computes cumulative sum of the elements.
     *
     * The operation is done **in place**.
     *
     * Available only for 1-D arrays.
     */
    open fun cumSum() {
        check(nDim == 1)
        val acc = KahanSum()
        for (pos in 0..size - 1) {
            acc += unsafeGet(pos)
            unsafeSet(pos, acc.result())
        }
    }

    /**
     * Returns the maximum element.
     *
     * Optimized for dense arrays.
     */
    open fun max(): Double = flatten().max()

    /**
     * Returns the unravelled index of the maximum element in the
     * flattened array.
     *
     * See [ravelMultiIndex] and [unravelIndex] for details.
     */
    open fun argMax(): Int = flatten().argMax()

    /**
     * Returns the minimum element.
     *
     * Optimized for dense arrays.
     */
    open fun min(): Double = flatten().min()

    /**
     * Returns the unravelled index of the minimum element in the
     * flattened array.
     *
     * See [ravelMultiIndex] and [unravelIndex] for details.
     */
    open fun argMin(): Int = flatten().argMin()

    /**
     * Computes the exponent of each element of this array.
     *
     * Optimized for dense arrays.
     */
    open fun expInPlace(): Unit = flatten().expInPlace()

    fun exp() = copy().apply { expInPlace() }

    /**
     * Computes exp(x) - 1 for each element of this array.
     *
     * Optimized for dense arrays.
     *
     * @since 0.3.0
     */
    open fun expm1InPlace(): Unit = flatten().expm1InPlace()

    fun expm1() = copy().apply { expm1InPlace() }

    /**
     * Computes the natural log of each element of this array.
     *
     * Optimized for dense arrays.
     */
    open fun logInPlace(): Unit = flatten().logInPlace()

    fun log() = copy().apply { logInPlace() }

    /**
     * Computes log(1 + x) for each element of this array.
     *
     * Optimized for dense arrays.
     *
     * @since 0.3.0
     */
    open fun log1pInPlace(): Unit = flatten().log1pInPlace()

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
    // TODO: add axis.
    open fun logSumExp(): Double = flatten().logSumExp()

    infix fun logAddExp(other: F64Array): F64Array {
        return copy().apply { logAddExp(other, this) }
    }

    /**
     * Computes elementwise
     *
     *   log(exp(this[i]) + exp(other[i]))
     *
     * in a numerically stable way.
     */
    open fun logAddExp(other: F64Array, dst: F64Array) {
        flatten().logAddExp(checkShape(other).flatten(), checkShape(dst).flatten())
    }

    operator fun unaryPlus() = this

    open operator fun unaryMinus(): F64Array = copy().apply { (-flatten()).reshape(*shape) }

    operator fun plus(other: F64Array) = copy().apply { this += other }

    open operator fun plusAssign(other: F64Array) {
        flatten() += checkShape(other).flatten()
    }

    operator fun plus(update: Double) = copy().apply { this += update }

    open operator fun plusAssign(update: Double) {
        flatten() += update
    }

    operator fun minus(other: F64Array) = copy().apply { this -= other }

    open operator fun minusAssign(other: F64Array) {
        flatten() -= checkShape(other).flatten()
    }

    operator fun minus(update: Double) = copy().apply { this -= update }

    open operator fun minusAssign(update: Double) {
        flatten() -= update
    }

    operator fun times(other: F64Array) = copy().apply { this *= other }

    open operator fun timesAssign(other: F64Array) {
        flatten() *= checkShape(other).flatten()
    }

    operator fun times(update: Double) = copy().apply { this *= update }

    open operator fun timesAssign(update: Double) {
        flatten() *= update
    }

    operator fun div(other: F64Array) = copy().apply { this /= other }

    open operator fun divAssign(other: F64Array) {
        flatten() /= checkShape(other).flatten()
    }

    operator fun div(update: Double) = copy().apply { this /= update }

    open operator fun divAssign(update: Double) {
        flatten() /= update
    }

    /** Ensures a given array has the same dimensions as this array. */
    fun checkShape(other: F64Array): F64Array {
        // Could relax this to "broadcastable".
        require(this === other || Arrays.equals(shape, other.shape)) {
            "operands shapes do not match " +
            "${Arrays.toString(shape)} ${Arrays.toString(other.shape)}"
        }
        return other
    }

    // XXX must be overriden in flat array.
    open fun toArray(): Any = toGenericArray()

    // XXX must be overriden in flat array.
    open fun toGenericArray(): Array<*> = Array(size) { view(it).toArray() }

    // XXX must be overriden in flat array.
    open fun toDoubleArray(): DoubleArray = throw UnsupportedOperationException()

    // XXX must be overriden in flat array.
    open fun toString(maxDisplay: Int,
                 format: DecimalFormat = DecimalFormat("#.####")): String {
        val sb = StringBuilder()
        sb.append('[')
        if (maxDisplay < size) {
            for (r in 0..maxDisplay / 2 - 1) {
                sb.append(V[r].toString(maxDisplay, format)).append(", ")
            }

            sb.append("..., ")

            val leftover = maxDisplay - maxDisplay / 2
            for (r in size - leftover..size - 1) {
                sb.append(V[r].toString(maxDisplay, format))
                if (r < size - 1) {
                    sb.append(", ")
                }
            }
        } else {
            for (r in 0..size - 1) {
                sb.append(V[r].toString(maxDisplay, format))
                if (r < size - 1) {
                    sb.append(", ")
                }
            }
        }

        sb.append(']')
        return sb.toString()
    }

    override fun toString() = toString(8)

    // XXX must be overriden in flat array.
    override fun equals(other: Any?): Boolean = when {
        this === other -> true
        other !is F64Array -> false
        !Arrays.equals(shape, other.shape) -> false
        else -> (0..size - 1).all { view(it) == other.view(it) }
    }

    // XXX must be overriden in flat array.
    override fun hashCode(): Int = (0..size - 1).fold(1) { acc, r ->
        31 * acc + view(r).hashCode()
    }

    companion object {
        /** Creates a zero-filled array of a given [shape]. */
        operator fun invoke(vararg shape: Int): F64Array {
            return F64FlatArray(DoubleArray(shape.product()))
                    .reshape(*shape)
        }

        operator inline fun invoke(size: Int, block: (Int) -> Double): F64Array {
            return invoke(size).apply {
                for (i in 0..size - 1) {
                    this[i] = block(i)
                }
            }
        }

        operator inline fun invoke(numRows: Int, numColumns: Int,
                                   block: (Int, Int) -> Double): F64Array {
            return invoke(numRows, numColumns).apply {
                for (r in 0..numRows - 1) {
                    for (c in 0..numColumns - 1) {
                        this[r, c] = block(r, c)
                    }
                }
            }
        }

        operator inline fun invoke(depth: Int, numRows: Int, numColumns: Int,
                                   block: (Int, Int, Int) -> Double): F64Array {
            return invoke(depth, numRows, numColumns).apply {
                for (d in 0..depth - 1) {
                    for (r in 0..numRows - 1) {
                        for (c in 0..numColumns - 1) {
                            this[d, r, c] = block(d, r, c)
                        }
                    }
                }
            }
        }

        /** Creates a vector with given elements. */
        fun of(first: Double, vararg rest: Double): F64Array {
            val data = DoubleArray(rest.size + 1)
            data[0] = first
            System.arraycopy(rest, 0, data, 1, rest.size)
            return data.asF64Array()
        }

        /** Creates a vector filled with a given [init] element. */
        fun full(size: Int, init: Double) = invoke(size).apply { fill(init) }

        /** Creates a matrix filled with a given [init] element. */
        fun full(vararg indices: Int, init: Double): F64Array {
            return invoke(*indices).apply { fill(init) }
        }

        /**
         * Joins a sequence of arrays into a single array.
         *
         * @since 0.2.3
         */
        fun concatenate(first: F64Array, vararg rest: F64Array, axis: Int = 0): F64Array {
            for (other in rest) {
                if (!Arrays.equals(other.shape.remove(axis),
                                   first.shape.remove(axis))) {
                    throw IllegalArgumentException(
                            "input array shapes must be exactly equal " +
                            "for all dimensions except $axis")
                }
            }

            val shape = first.shape.clone().apply {
                this[axis] = first.shape[axis] + rest.sumBy { it.shape[axis] }
            }

            val result = invoke(*shape)
            var offset = 0
            for (a in arrayOf(first, *rest)) {
                if (a.size > 0) {
                    a.copyTo(result.slice(offset, offset + a.shape[axis], axis = axis))
                    offset += a.shape[axis]
                }
            }

            return result
        }

        /** "Smart" constructor. */
        internal operator fun invoke(data: DoubleArray, offset: Int,
                                     strides: IntArray, shape: IntArray): F64Array {
            return if (shape.size == 1) {
                F64FlatArray(data, offset, strides.single(), shape.single())
            } else {
                F64Array(data, offset, strides, shape)
            }
        }
    }
}

/** Wraps a given array of elements. The array will not be copied. */
fun DoubleArray.asF64Array(offset: Int = 0, size: Int = this.size): F64Array {
    return F64FlatArray(this, offset, 1, size)
}
