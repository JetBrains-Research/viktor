package org.jetbrains.bio.viktor

import java.text.DecimalFormat
import java.util.*

/**
 * An n-dimensional specialization of [F64Array].
 *
 * @author Sergei Lebedev
 * @since 0.4.0
 */
class F64Matrix internal constructor(
        override val data: DoubleArray,
        override val offset: Int,
        override val strides: IntArray,
        override val shape: IntArray) : F64Array.ViaFlatten<F64Matrix> {

    override fun copy(): F64Matrix {
        val copy = F64Matrix(*shape)
        copyTo(copy)
        return copy
    }

    override fun copyTo(other: F64Array) {
        checkShape(other)
        other as F64Matrix
        if (Arrays.equals(strides, other.strides)) {
            System.arraycopy(data, offset, other.data, other.offset,
                             shape.product())
        } else {
            for (r in 0..size - 1) {
                this[r].copyTo(other[r])
            }
        }
    }

    override fun flatten(): F64Vector {
        check(isDense) { "matrix is not dense" }
        return data.asVector(offset, shape.product())
    }

    override fun transpose(): F64Matrix {
        return F64Matrix(data, offset, strides.reversedArray(), shape.reversedArray())
    }

    /**
     * An indexer for accessing individual array entries without broadcasting.
     */
    val ix: Indexer = Indexer(this)

    class Indexer internal constructor(private val m: F64Matrix) {
        /**
         * Generic getter.
         *
         * Note that it could be at least 1.5x slower than specialized versions.
         */
        operator fun get(vararg indices: Int): Double {
            return safeIndex({ indices }) { m.data[unsafeIndex(indices)] }
        }

        operator fun get(r: Int, c: Int): Double {
            return safeIndex({ intArrayOf(r, c) }) { m.data[unsafeIndex(r, c)] }
        }

        operator fun get(d: Int, r: Int, c: Int): Double {
            return safeIndex({ intArrayOf(d, r, c) }) { m.data[unsafeIndex(d, r, c)] }
        }

        /**
         * Generic setter.
         *
         * Note that it could be at least 1.5x slower than specialized versions.
         */
        operator fun set(vararg indices: Int, value: Double) {
            require(indices.size == m.nDim) { "broadcasting set is not supported" }
            safeIndex({ indices }) { m.data[unsafeIndex(indices)] = value }
        }

        operator fun set(r: Int, c: Int, value: Double) {
            require(m.nDim == 2) { "broadcasting set is not supported" }
            safeIndex({ intArrayOf(r, c) }) { m.data[unsafeIndex(r, c)] = value }
        }

        operator fun set(d: Int, r: Int, c: Int, value: Double) {
            require(m.nDim == 3) { "broadcasting set is not supported" }
            safeIndex({ intArrayOf(d, r, c) }) { m.data[unsafeIndex(d, r, c)] = value }
        }

        @Suppress("nothing_to_inline")
        private inline fun unsafeIndex(r: Int, c: Int): Int {
            return m.offset + r * m.strides[0] + c * m.strides[1]
        }

        @Suppress("nothing_to_inline")
        private inline fun unsafeIndex(d: Int, r: Int, c: Int): Int {
            return m.offset + d * m.strides[0] + r * m.strides[1] + c * m.strides[2]
        }

        @Suppress("nothing_to_inline")
        private inline fun unsafeIndex(indices: IntArray): Int {
            return m.strides.foldIndexed(m.offset) { i, acc, stride -> acc + indices[i] * stride }
        }

        private inline fun <T> safeIndex(indices: () -> IntArray, block: () -> T): T {
            try {
                return block()
            } catch (e: IndexOutOfBoundsException) {
                @Suppress("name_shadowing") val indices = indices()
                val reason = when {
                    indices.size > m.nDim -> "too many indices"
                    indices.size < m.nDim -> "too few indices"
                    else -> "(${indices.joinToString(", ")}) out of bounds " +
                            "for shape ${m.shape.joinToString(", ")}"
                }

                throw IndexOutOfBoundsException(reason)
            }
        }
    }

    /**
     * A less-verbose alias to [view].
     *
     * Please do NOT abuse this shortcut by double-indexing, i.e. don't
     * do `m[i][j]`, write `m[i, j]` instead.
     */
    operator fun get(vararg indices: Int) = view0(indices)

    operator fun set(vararg indices: Int, other: F64Array) {
        other.copyTo(view0(indices))
    }

    operator fun set(vararg indices: Int, init: Double) = view0(indices).fill(init)

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
    operator fun get(any: _I, c: Int) = view(c, along = 1)

    @Suppress("unused_parameter")
    operator fun set(any: _I, c: Int, other: F64Array) {
        other.copyTo(view(c, along = 1))
    }

    @Suppress("unused_parameter")
    operator fun set(any: _I, c: Int, init: Double) {
        view(c, along = 1).fill(init)
    }

    /** Returns a view of this matrix along the specified axis. */
    fun view(index: Int, along: Int = 0): F64Array {
        checkIndex("along", along, nDim)
        checkIndex("index", index, shape[along])
        return F64Array(data, offset + strides[along] * index,
                        strides.remove(along), shape.remove(along))
    }

    /**
     * Computes a nested view over the first axis.
     *
     * Here be dragons!
     */
    private fun view0(indices: IntArray): F64Array {
        require(indices.size < nDim) { "too many indices" }
        // XXX this never raises CCE because all the intermediate
        //     results are guaranteed to be [F64Matrix] by the above
        //     check.
        return indices.fold(this as F64Array) { m, pos -> (m as F64Matrix).view(pos) }
    }

    /** Returns a view of the [r]-th row of this matrix. */
    @Deprecated("", ReplaceWith("view(r, along = 0) as F64Vector"))
    fun rowView(r: Int) = view(r, along = 0) as F64Vector

    /** Returns a view of the [c]-th column of this matrix. */
    @Deprecated("", ReplaceWith("view(c, along = 1) as F64Vector"))
    fun columnView(c: Int) = view(c, along = 1) as F64Vector

    override fun toArray(): Array<*> = Array(size) { view(it).toArray() }

    override fun toString(maxDisplay: Int, format: DecimalFormat): String {
        val sb = StringBuilder()
        sb.append('[')
        if (maxDisplay < size) {
            for (r in 0..maxDisplay / 2 - 1) {
                sb.append(this[r].toString(maxDisplay, format)).append(", ")
            }

            sb.append("..., ")

            val leftover = maxDisplay - maxDisplay / 2
            for (r in size - leftover..size - 1) {
                sb.append(this[r].toString(maxDisplay, format))
                if (r < size - 1) {
                    sb.append(", ")
                }
            }
        } else {
            for (r in 0..size - 1) {
                sb.append(this[r].toString(maxDisplay, format))
                if (r < size - 1) {
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
        other !is F64Matrix -> false
        !Arrays.equals(shape, other.shape) -> false
        else -> (0..size - 1).all { view(it) == other.view(it) }
    }

    override fun hashCode() = (0..size - 1).fold(1) { acc, r ->
        31 * acc + view(r).hashCode()
    }

    companion object {
        operator fun invoke(vararg indices: Int): F64Matrix {
            require(indices.size >= 2)
            return F64Vector(indices.product()).reshape(*indices) as F64Matrix
        }

        fun full(vararg indices: Int, init: Double): F64Matrix {
            return invoke(*indices).apply { fill(init) }
        }

        operator inline fun invoke(numRows: Int, numColumns: Int,
                                   block: (Int, Int) -> Double): F64Matrix {
            return invoke(numRows, numColumns).apply {
                for (r in 0..numRows - 1) {
                    for (c in 0..numColumns - 1) {
                        ix[r, c] = block(r, c)
                    }
                }
            }
        }

        operator inline fun invoke(depth: Int, numRows: Int, numColumns: Int,
                                   block: (Int, Int, Int) -> Double): F64Matrix {
            return invoke(depth, numRows, numColumns).apply {
                for (d in 0..depth - 1) {
                    for (r in 0..numRows - 1) {
                        for (c in 0..numColumns - 1) {
                            ix[d, r, c] = block(d, r, c)
                        }
                    }
                }
            }
        }

        /** Creates a 2-D matrix with rows summing to one. */
        fun stochastic(size: Int) = full(size, size, init = 1.0 / size)

        /** Creates a 3-D matrix with [stochastic] sub-matrices. */
        fun indexedStochastic(depth: Int, size: Int): F64Matrix {
            return full(depth, size, size, init = 1.0 / size)
        }
    }
}