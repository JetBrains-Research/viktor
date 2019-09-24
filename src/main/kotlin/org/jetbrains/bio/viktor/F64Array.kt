package org.jetbrains.bio.viktor

import org.apache.commons.math3.util.Precision
import java.text.DecimalFormat
import java.util.*
import kotlin.math.min
import kotlin.math.sqrt

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
        val shape: IntArray
) {

    /** Number of axes in this array. */
    val nDim: Int get() = shape.size

    /** Number of elements along the first axis. */
    val size: Int get() = shape.first()

    private val unrollDim: Int
    private val unrollSize: Int
    private val unrollStride: Int

    /**
     * Returns `true` if this array can be flattened using [flatten].
     *
     * Flattenable array's elements are laid out with a constant stride. This allows to use simple loops when iterating.
     *
     * A particular case of a flattenable array is a dense array whose elements occupy a contiguous block of memory.
     * Large dense arrays employ SIMD optimizations, see [F64LargeDenseArray].
     */
    open val isFlattenable get() = (unrollDim == nDim)

    init {
        val (d, s) = calculateUnrollDimAndStride(strides, shape)
        unrollDim = d
        unrollStride = s
        unrollSize = shape.slice(0 until unrollDim).toIntArray().product()
    }

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

    @Suppress("nothing_to_inline")
    private inline fun outOfBounds(indices: IntArray, shape: IntArray): Nothing {
        val nDim = shape.size
        val reason = when {
            indices.size > nDim -> "too many indices"
            indices.size < nDim -> "too few indices"
            else -> "(${indices.joinToString(", ")}) out of bounds " +
                    "for shape ${shape.joinToString(", ")}"
        }

        throw IndexOutOfBoundsException(reason)
    }

    /**
     * Returns a sequence of views along the specified [axis].
     *
     * For example, for a 2-D array `axis = 0` means "for each row",
     * and `axis = 1` "for each column".
     */
    open fun along(axis: Int): Sequence<F64Array> = (0 until shape[axis]).asSequence().map { view(it, axis) }

    /** Returns a view of this array along the specified [axis]. */
    fun view(index: Int, axis: Int = 0): F64Array {
        checkIndex("axis", axis, nDim)
        checkIndex("index", index, shape[axis])
        return invoke(
            data, offset + strides[axis] * index,
            strides.remove(axis), shape.remove(axis)
        )
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

    private fun unrollToFlat(): Sequence<F64FlatArray> {
        if (isFlattenable) return sequenceOf(flatten())
        return unrollOnce().flatMap { it.unrollToFlat() }
    }

    private fun commonUnrollToFlat(other: F64Array): Sequence<Pair<F64FlatArray, F64FlatArray>> {
        checkShape(other)
        val commonUnrollDim = min(unrollDim, other.unrollDim)
        return if (commonUnrollDim == nDim) {
            sequenceOf(flatten() to other.flatten())
        } else {
            unrollOnce(commonUnrollDim).zip(other.unrollOnce(commonUnrollDim)).flatMap { (a, b) ->
                a.commonUnrollToFlat(b)
            }
        }
    }

    private fun unrollOnce(n: Int = unrollDim): Sequence<F64Array> {
        require(n <= unrollDim) { "can't unroll $n dimensions, only $unrollDim are unrollable" }
        val newStrides = strides.slice(n until nDim).toIntArray()
        val newShape = shape.slice(n until nDim).toIntArray()
        val currentUnrollStride = if (n == unrollDim) unrollStride else run {
            var nonTrivialN = n - 1
            while (nonTrivialN >= 0 && shape[nonTrivialN] <= 1) nonTrivialN--
            if (nonTrivialN >= 0) strides[nonTrivialN] else 0
        }
        return (0 until unrollSize).asSequence().map { i ->
            invoke(data, offset + currentUnrollStride * i, newStrides, newShape)
        }
    }

    /** A broadcasted viewer for this array. */
    @delegate:Transient
    val V: Viewer by lazy(LazyThreadSafetyMode.NONE) { Viewer(this) }

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
    open fun copyTo(other: F64Array): Unit = commonUnrollToFlat(other).forEach { (a, b) -> a.copyTo(b) }

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
            this.shape.contentEquals(shape) -> this
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
        return concatenate(this, other, axis = axis)
    }

    /**
     * Flattens the array into a 1-D view in O(1) time.
     *
     * No data copying is performed, thus the operation is only applicable
     * to flattenable arrays.
     */
    open fun flatten(): F64FlatArray {
        check(isFlattenable) { "array can't be flattened" }
        return F64FlatArray.invoke(data, offset, unrollStride, unrollSize)
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

    /**
     * Fills this array with a given [init] value.
     */
    open fun fill(init: Double): Unit = flatten().fill(init)

    /** Applies a given permutation of indices to the elements in the array. */
    open fun reorder(indices: IntArray, axis: Int = 0) {
        reorderInternal(
            this, indices, axis,
            get = { pos -> view(pos, axis).copy() },
            set = { pos, value -> value.copyTo(view(pos, axis)) }
        )
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
        return sqrt((s2 - s * s / size) / (size - 1))
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
        for (pos in 0 until size) {
            acc += unsafeGet(pos)
            unsafeSet(pos, acc.result())
        }
    }

    /**
     * Returns the maximum element.
     *
     * Optimized for dense arrays.
     */
    open fun max(): Double = unrollToFlat().map { it.max() }.max() ?: Double.NEGATIVE_INFINITY

    /**
     * Returns the minimum element.
     *
     * Optimized for dense arrays.
     */
    open fun min(): Double = unrollToFlat().map { it.min() }.min() ?: Double.POSITIVE_INFINITY

    /**
     * Replaces each element of this array with its exponent.
     *
     * Optimized for dense arrays.
     */
    open fun expInPlace(): Unit = unrollToFlat().forEach { it.expInPlace() }

    fun exp() = copy().apply { expInPlace() }

    /**
     * Computes exp(x) - 1 for each element of this array.
     *
     * Optimized for dense arrays.
     *
     * @since 0.3.0
     */
    open fun expm1InPlace(): Unit = unrollToFlat().forEach { it.expm1InPlace() }

    fun expm1() = copy().apply { expm1InPlace() }

    /**
     * Computes the natural log of each element of this array.
     *
     * Optimized for dense arrays.
     */
    open fun logInPlace(): Unit = unrollToFlat().forEach { it.logInPlace() }

    fun log() = copy().apply { logInPlace() }

    /**
     * Computes log(1 + x) for each element of this array.
     *
     * Optimized for dense arrays.
     *
     * @since 0.3.0
     */
    open fun log1pInPlace(): Unit = unrollToFlat().forEach { it.log1pInPlace() }

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
    open fun logSumExp(): Double = unrollToFlat().map { it.logSumExp() }.logSumExp()

    open fun logAddExpAssign(other: F64Array): Unit =
            commonUnrollToFlat(other).forEach { (a, b) -> a.logAddExpAssign(b) }

    infix fun logAddExp(other: F64Array): F64Array = copy().apply { logAddExpAssign(other) }

    /**
     * Computes elementwise
     *
     *     log(exp(this[i]) + exp(other[i]))
     *
     * in a numerically stable way.
     */
    @Deprecated("Three-argument syntax is deprecated", ReplaceWith("logAddExpAssign(other)"))
    open fun logAddExp(other: F64Array, dst: F64Array) {
        if (dst === this) {
            logAddExpAssign(other)
        } else {
            (this logAddExp other).copyTo(dst)
        }

    }

    operator fun unaryPlus() = this

    open operator fun unaryMinus(): F64Array = copy().apply { (-flatten()).reshape(*shape) }

    operator fun plus(other: F64Array) = copy().apply { this += other }

    open operator fun plusAssign(other: F64Array): Unit = commonUnrollToFlat(other).forEach { (a, b) -> a += b }

    operator fun plus(update: Double) = copy().apply { this += update }

    open operator fun plusAssign(update: Double): Unit = unrollToFlat().forEach { it += update }

    operator fun minus(other: F64Array) = copy().apply { this -= other }

    open operator fun minusAssign(other: F64Array): Unit = commonUnrollToFlat(other).forEach { (a, b) -> a -= b }

    operator fun minus(update: Double) = copy().apply { this -= update }

    open operator fun minusAssign(update: Double): Unit = unrollToFlat().forEach { it -= update }

    operator fun times(other: F64Array) = copy().apply { this *= other }

    open operator fun timesAssign(other: F64Array): Unit = commonUnrollToFlat(other).forEach { (a, b) -> a *= b }

    operator fun times(update: Double) = copy().apply { this *= update }

    open operator fun timesAssign(update: Double): Unit = unrollToFlat().forEach { it *= update }

    operator fun div(other: F64Array) = copy().apply { this /= other }

    open operator fun divAssign(other: F64Array): Unit = commonUnrollToFlat(other).forEach { (a, b) -> a /= b }

    operator fun div(update: Double) = copy().apply { this /= update }

    open operator fun divAssign(update: Double): Unit = unrollToFlat().forEach { it /= update }

    /** Ensures a given array has the same dimensions as this array. */
    fun checkShape(other: F64Array): F64Array {
        // Could relax this to "broadcastable".
        require(this === other || shape.contentEquals(other.shape)) {
            "operands shapes do not match ${shape.contentToString()} ${other.shape.contentToString()}"
        }
        return other
    }

    // XXX must be overridden in flat array.
    open fun toArray(): Any = toGenericArray()

    // XXX must be overridden in flat array.
    open fun toGenericArray(): Array<*> = Array(size) { view(it).toArray() }

    // XXX must be overridden in flat array.
    open fun toDoubleArray(): DoubleArray = throw UnsupportedOperationException()

    // XXX must be overridden in flat array.
    open fun toString(
            maxDisplay: Int,
            format: DecimalFormat = DecimalFormat("#.####")
    ): String {
        val sb = StringBuilder()
        sb.append('[')
        if (maxDisplay < size) {
            for (r in 0 until maxDisplay / 2) {
                sb.append(V[r].toString(maxDisplay, format)).append(", ")
            }

            sb.append("..., ")

            val leftover = maxDisplay - maxDisplay / 2
            for (r in size - leftover until size) {
                sb.append(V[r].toString(maxDisplay, format))
                if (r < size - 1) {
                    sb.append(", ")
                }
            }
        } else {
            for (r in 0 until size) {
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

    // XXX must be overridden in flat array.
    override fun equals(other: Any?): Boolean = when {
        this === other -> true
        other !is F64Array -> false
        !shape.contentEquals(other.shape) -> false
        else -> (0 until size).all { view(it) == other.view(it) }
    }

    // XXX must be overridden in flat array.
    override fun hashCode(): Int = (0 until size).fold(1) { acc, r ->
        31 * acc + view(r).hashCode()
    }

    companion object {
        /** Creates a zero-filled array of a given [shape]. */
        operator fun invoke(vararg shape: Int): F64Array {
            return F64FlatArray(DoubleArray(shape.product()))
                    .reshape(*shape)
        }

        inline operator fun invoke(size: Int, block: (Int) -> Double): F64Array {
            return invoke(size).apply {
                for (i in 0 until size) {
                    this[i] = block(i)
                }
            }
        }

        inline operator fun invoke(
                numRows: Int,
                numColumns: Int,
                block: (Int, Int) -> Double
        ): F64Array {
            return invoke(numRows, numColumns).apply {
                for (r in 0 until numRows) {
                    for (c in 0 until numColumns) {
                        this[r, c] = block(r, c)
                    }
                }
            }
        }

        inline operator fun invoke(
                depth: Int,
                numRows: Int,
                numColumns: Int,
                block: (Int, Int, Int) -> Double
        ): F64Array {
            return invoke(depth, numRows, numColumns).apply {
                for (d in 0 until depth) {
                    for (r in 0 until numRows) {
                        for (c in 0 until numColumns) {
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
                require(other.shape.remove(axis).contentEquals(first.shape.remove(axis))) {
                    "input array shapes must be exactly equal for all dimensions except $axis"
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

        private fun calculateUnrollDimAndStride(strides: IntArray, shape: IntArray): Pair<Int, Int> {
            require(strides.size == shape.size) {
                "Strides and shape have different sizes (${strides.size} and ${shape.size})"
            }
            var prevStride = 0
            var unrollable = true
            var unrollDim = 0
            var unrollStride = 0
            for (i in strides.indices) {
                require(shape[i] >= 0) { "Shape values must be non-negative, but got ${shape[i]}" }
                if (shape[i] <= 1) {
                    if (unrollable) unrollDim = i + 1
                    continue
                }
                require(strides[i] > 0) { "Strides must be strictly positive, but got ${strides[i]}" }
                require(prevStride == 0 || prevStride >= strides[i] * shape[i]) {
                    "Strides-shape condition is violated for dimension $i: $prevStride is less than " +
                            "${strides[i]} * ${shape[i]}"
                }
                if (unrollable && (prevStride == 0 || prevStride == strides[i] * shape[i])) {
                    unrollDim = i + 1
                    unrollStride = strides[i]
                } else {
                    unrollable = false
                }
                prevStride = strides[i]
            }
            return unrollDim to unrollStride
        }

        /** "Smart" constructor. */
        internal operator fun invoke(
                data: DoubleArray,
                offset: Int,
                strides: IntArray,
                shape: IntArray
        ): F64Array {
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

/** Copies the elements of this nested array into [F64Array] of the same shape. */
fun Array<*>.toF64Array(): F64Array {
    val shape = guessShape()
    return flatten(this).asF64Array().reshape(*shape)
}

/** Flattens a nested [DoubleArray]. */
private fun flatten(a: Array<*>): DoubleArray {
    return Arrays.stream(a).flatMapToDouble {
        when (it) {
            is DoubleArray -> Arrays.stream(it)
            is Array<*> -> Arrays.stream(flatten(it))
            else -> unsupported()
        }
    }.toArray()
}

/** No validation, therefore "check". */
private fun Array<*>.guessShape(): IntArray {
    check(isNotEmpty())
    return when (val tip = first()) {
        is DoubleArray -> intArrayOf(size, tip.size)
        is Array<*> -> intArrayOf(size) + tip.guessShape()
        else -> unsupported()
    }
}

/**
 * A special object used to denote all indices.
 *
 * @since 0.1.1 Renamed to `_I` because all-underscore names are reserved
 *              for internal use in Kotlin.
 */
object _I {}