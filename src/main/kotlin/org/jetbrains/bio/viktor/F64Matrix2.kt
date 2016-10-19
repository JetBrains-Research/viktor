package org.jetbrains.bio.viktor

import java.text.DecimalFormat
import java.util.*
import java.util.stream.IntStream
import java.util.stream.Stream

/**
 * A specialization of [F64Matrix] for 2-D data.
 *
 * @author Sergei Lebedev
 * @since 0.1.0
 */
class F64Matrix2 internal constructor(data: DoubleArray, offset: Int,
                                      strides: IntArray, shape: IntArray)
:
        F64Matrix(data, offset, strides, shape),
        F64MatrixOps<F64Matrix2> {

    constructor(numRows: Int, numColumns: Int,
                data: DoubleArray = DoubleArray(numRows * numColumns))
    : this(data, 0, intArrayOf(numColumns, 1), intArrayOf(numRows, numColumns)) {}

    @Deprecated("", replaceWith = ReplaceWith("shape[0]"))
    val rowsNumber: Int get() = shape[0]
    @Deprecated("", replaceWith = ReplaceWith("shape[1]"))
    val columnsNumber: Int get() = shape[1]

    override fun unwrap() = this

    /** An alias for [transpose]. */
    val T: F64Matrix get() = transpose()

    /** Constructs matrix transpose in O(1) time. */
    fun transpose() = if (nDim < 2) {
        this
    } else {
        F64Matrix2(data, offset, strides.reversedArray(), shape.reversedArray())
    }

    operator fun get(r: Int, c: Int): Double {
        try {
            return unsafeGet(r, c)
        } catch (ignored: ArrayIndexOutOfBoundsException) {
            throw IndexOutOfBoundsException("index out of bounds: ($r, $c)")
        }
    }

    private fun unsafeGet(r: Int, c: Int) = data[unsafeIndex(r, c)]

    operator fun set(r: Int, c: Int, value: Double) {
        try {
            unsafeSet(r, c, value)
        } catch (ignored: ArrayIndexOutOfBoundsException) {
            throw IndexOutOfBoundsException("index out of bounds: ($r, $c)")
        }
    }

    private fun unsafeSet(r: Int, c: Int, value: Double) {
        data[unsafeIndex(r, c)] = value
    }

    @Suppress("nothing_to_inline")
    private inline fun unsafeIndex(r: Int, c: Int) = offset + r * strides[0] + c * strides[1]

    /** Returns a view of the [r]-th row of this matrix. */
    fun rowView(r: Int): F64Vector {
        if (r < 0 || r >= rowsNumber) {
            throw IndexOutOfBoundsException("r must be in [0, $rowsNumber)")
        }

        return F64Vector.create(data, offset + strides[0] * r, columnsNumber, strides[1])
    }

    /**
     * Returns a view of the [c]-th column of this matrix.
     */
    fun columnView(c: Int): F64Vector {
        if (c < 0 || c >= columnsNumber) {
            throw IndexOutOfBoundsException("c must be in [0, $columnsNumber)")
        }

        return F64Vector.create(data, offset + strides[1] * c, rowsNumber, strides[0])
    }

    /**
     * A less-verbose alias to [rowView].
     *
     * Please do NOT abuse this shortcut by double-indexing, i.e. don't
     * do `m[i][j]`, write `m[i, j]` instead.
     */
    operator fun get(r: Int) = rowView(r)

    operator fun set(r: Int, other: F64Vector) = other.copyTo(rowView(r))

    operator fun set(r: Int, init: Double) = rowView(r).fill(init)

    /**
     * A less-verbose alias to [columnView].
     *
     * Use in conjunction with [_I], e.g. `m[_I, i]`.
     */
    operator fun get(any: _I, c: Int) = columnView(c)

    operator fun set(any: _I, c: Int, other: F64Vector) = other.copyTo(columnView(c))

    operator fun set(any: _I, c: Int, init: Double) = columnView(c).fill(init)

    /** Returns a copy of the elements in this matrix. */
    override fun copy(): F64Matrix2 {
        val copy = F64Matrix2(rowsNumber, columnsNumber)
        copyTo(copy)
        return copy
    }

    /** Copies elements in this matrix to [other] matrix. */
    fun copyTo(other: F64Matrix2) {
        checkDimensions(other)
        if (Arrays.equals(strides, other.strides)) {
            System.arraycopy(data, offset, other.data, other.offset,
                             shape.product())
        } else {
            for (r in 0..size - 1) {
                other[r] = this[r]
            }
        }
    }

    override fun F64Vector.reshapeLike(other: F64Matrix2): F64Matrix2 {
        val (nRows, nCols) = other.shape
        return reshape(nRows, nCols)
    }

    /**
     * Returns a stream of row or column views of the matrix.
     *
     * @param axis axis to go along, 0 stands for columns, 1 for rows.
     */
    fun along(axis: Int): Stream<F64Vector> = when (axis) {
        0 -> IntStream.range(0, columnsNumber).mapToObj { columnView(it) }
        1 -> IntStream.range(0, rowsNumber).mapToObj { rowView(it) }
        else -> throw IllegalArgumentException(axis.toString())
    }

    fun toArray() = Array(size) { rowView(it).toArray() }

    fun toString(maxDisplay: Int,
                 format: DecimalFormat = DecimalFormat("#.####")): String {
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
        other !is F64Matrix2 -> false
        !Arrays.equals(shape, other.shape) -> false
        else -> (0..size - 1).all { this[it] == other[it] }
    }

    override fun hashCode(): Int {
        return (0..size - 1).fold(1) { acc, r -> 31 * acc + this[r].hashCode() }
    }
}

/** Reshapes this vector into a matrix in row-major order. */
fun F64Vector.reshape(numRows: Int, numColumns: Int): F64Matrix2 {
    require(numRows * numColumns == size)
    return F64Matrix2(data, offset,
                      intArrayOf(numColumns * stride, stride),
                      intArrayOf(numRows, numColumns))
}
