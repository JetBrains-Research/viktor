package org.jetbrains.bio.viktor

import java.text.DecimalFormat
import java.util.stream.IntStream
import java.util.stream.Stream

/**
 * A specialization of [F64Matrix] for 2-D data.
 *
 * @author Sergei Lebedev
 * @since 0.1.0
 */
class F64Matrix2 internal constructor(
        val rowsNumber: Int, val columnsNumber: Int,
        val data: DoubleArray, val offset: Int,
        val rowStride: Int,
        val columnStride: Int) : FlatMatrixOps<F64Matrix2> {

    constructor(numRows: Int, numColumns: Int,
                data: DoubleArray = DoubleArray(numRows * numColumns)) :
    this(numRows, numColumns, data, 0, numColumns, 1) {}

    /** Returns the shape of this matrix. */
    val shape: IntArray get() = intArrayOf(rowsNumber, columnsNumber)

    /**
     * Dense matrices are laid out in a single contiguous block
     * of memory.
     *
     * This allows to use SIMD operations, e.g. when computing the
     * sum of elements.
     */
    internal val isDense: Boolean get() {
        return rowStride == columnsNumber && columnStride == 1
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
    private inline fun unsafeIndex(r: Int, c: Int) = offset + r * rowStride + c * columnStride

    /** Returns a view of the [r]-th row of this matrix. */
    fun rowView(r: Int): F64Vector {
        if (r < 0 || r >= rowsNumber) {
            throw IndexOutOfBoundsException("r must be in [0, $rowsNumber)")
        }

        return F64Vector.create(data, offset + rowStride * r, columnsNumber, columnStride)
    }

    /**
     * Returns a view of the [c]-th column of this matrix.
     */
    fun columnView(c: Int): F64Vector {
        if (c < 0 || c >= columnsNumber) {
            throw IndexOutOfBoundsException("c must be in [0, $columnsNumber)")
        }

        return F64Vector.create(data, offset + columnStride * c, rowsNumber, rowStride)
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

    /** An alias for [transpose]. */
    val T: F64Matrix2 get() = transpose()

    /** Constructs matrix transpose in O(1) time. */
    fun transpose() = F64Matrix2(columnsNumber, rowsNumber, data, offset,
                                 columnStride, rowStride)

    /** Returns a copy of the elements in this matrix. */
    override fun copy(): F64Matrix2 {
        val copy = F64Matrix2(rowsNumber, columnsNumber)
        copyTo(copy)
        return copy
    }

    /** Copies elements in this matrix to [other] matrix. */
    fun copyTo(other: F64Matrix2) {
        checkDimensions(other)
        if (rowStride == other.rowStride && columnStride == other.columnStride) {
            System.arraycopy(data, offset, other.data, other.offset,
                             rowsNumber * columnsNumber)
        } else {
            for (r in 0..rowsNumber - 1) {
                other[r] = this[r]
            }
        }
    }

    /**
     * Flattens the matrix into a vector in O(1) time.
     *
     * No data copying is performed, thus the operation is only applicable
     * to dense matrices.
     */
    override fun flatten(): F64Vector {
        check(isDense) { "matrix is not dense" }
        return data.asVector(offset, rowsNumber * columnsNumber)
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

    fun toArray() = Array(rowsNumber) { rowView(it).toArray() }

    fun toString(maxDisplay: Int,
                 format: DecimalFormat = DecimalFormat("#.####")): String {
        val sb = StringBuilder()
        sb.append('[')
        if (maxDisplay < rowsNumber) {
            for (r in 0..maxDisplay / 2 - 1) {
                sb.append(this[r].toString(maxDisplay, format)).append(", ")
            }

            sb.append("..., ")

            val leftover = maxDisplay - maxDisplay / 2
            for (r in rowsNumber - leftover..rowsNumber - 1) {
                sb.append(this[r].toString(maxDisplay, format))
                if (r < rowsNumber - 1) {
                    sb.append(", ")
                }
            }
        } else {
            for (r in 0..rowsNumber - 1) {
                sb.append(this[r].toString(maxDisplay, format))
                if (r < rowsNumber - 1) {
                    sb.append(", ")
                }
            }
        }

        sb.append(']')
        return sb.toString()
    }

    override fun toString() = toString(8)

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        } else if (other !is F64Matrix2) {
            return false
        }

        if (rowsNumber != other.rowsNumber || columnsNumber != other.columnsNumber) {
            return false
        }

        for (r in 0..rowsNumber - 1) {
            if (this[r] != other[r]) {
                return false
            }
        }

        return true
    }

    override fun hashCode(): Int {
        var acc = 1
        for (r in 0..rowsNumber - 1) {
            acc = 31 * acc + this[r].hashCode()
        }

        return acc
    }

    override fun checkDimensions(other: F64Matrix2) {
        check(this === other ||
              (rowsNumber == other.rowsNumber &&
               columnsNumber == other.columnsNumber)) { "non-conformable matrices" }
    }
}

/** Reshapes this vector into a matrix in row-major order. */
fun F64Vector.reshape(numRows: Int, numColumns: Int): F64Matrix2 {
    require(numRows * numColumns == size)
    return F64Matrix2(numRows, numColumns, data, offset,
                          numColumns * stride, stride)
}
