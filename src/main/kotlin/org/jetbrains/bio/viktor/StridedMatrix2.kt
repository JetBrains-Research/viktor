package org.jetbrains.bio.viktor

import java.util.*
import java.util.stream.IntStream
import java.util.stream.Stream

/**
 * A specialization of [StridedMatrix] for 2-D data.
 *
 * @author Sergei Lebedev
 * @since 0.1.0
 */
class StridedMatrix2 internal constructor(
        val rowsNumber: Int, val columnsNumber: Int,
        val data: DoubleArray, val offset: Int,
        val rowStride: Int,
        val columnStride: Int) {

    constructor(numRows: Int, numColumns: Int) :
    // Use row-major order by default.
    this(numRows, numColumns, DoubleArray(numRows * numColumns), 0, numColumns, 1) {}

    /**
     * Dense matrices are laid out in a single contiguous block
     * of memory.
     *
     * This allows to use SIMD operations, e.g. when computing the
     * sum of elements.
     */
    private val isDense: Boolean get() {
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
    fun rowView(r: Int): StridedVector {
        require(r >= 0 && r < rowsNumber) { "r must be in [0, $rowsNumber)" }
        return StridedVector.create(data, offset + rowStride * r, columnsNumber, columnStride)
    }

    /**
     * Returns a view of the [c]-th column of this matrix.
     */
    fun columnView(c: Int): StridedVector {
        require(c >= 0 && c < columnsNumber) { "c must be in [0, $columnsNumber)" }
        return StridedVector.create(data, offset + columnStride * c, rowsNumber, rowStride)
    }

    /**
     * A less-verbose alias to [rowView].
     *
     * Please do NOT abuse this shortcut by double-indexing, i.e. don't
     * do `m[i][j]`, write `m[i, j]` instead.
     */
    operator fun get(r: Int) = rowView(r)

    operator fun set(r: Int, other: StridedVector) = other.copyTo(rowView(r))

    operator fun set(r: Int, init: Double) = rowView(r).fill(init)

    /**
     * A less-verbose alias to [columnView].
     *
     * Use in conjunction with [_I], e.g. `m[_I, i]`.
     */
    operator fun get(any: _I, c: Int) = columnView(c)

    operator fun set(any: _I, c: Int, other: StridedVector) = other.copyTo(columnView(c))

    operator fun set(any: _I, c: Int, init: Double) = columnView(c).fill(init)

    operator fun set(row: Int, any: _I, init: Double) = columnView(row).fill(init)

    /** An alias for [transpose]. */
    val T: StridedMatrix2 get() = transpose()

    /** Constructs matrix transpose in O(1) time. */
    fun transpose() = StridedMatrix2(columnsNumber, rowsNumber, data, offset,
                                     columnStride, rowStride)

    /**
     * Flattens the matrix into a vector in O(1) time.
     *
     * No data copying is performed, thus the operation is only applicable
     * to dense matrices.
     */
    fun flatten(): StridedVector {
        check(isDense) { "matrix is not dense" }
        return StridedVector.create(data, offset, rowsNumber * columnsNumber, 1)
    }

    /** Returns a copy of the elements in this matrix. */
    fun copy(): StridedMatrix2 {
        val copy = StridedMatrix2(rowsNumber, columnsNumber)
        copyTo(copy)
        return copy
    }

    /** Copies elements in this matrix to [other] matrix. */
    fun copyTo(other: StridedMatrix2) {
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
     * Returns a stream of row or column views of the matrix.
     *
     * @param axis axis to go along, 0 stands for columns, 1 for rows.
     */
    fun along(axis: Int): Stream<StridedVector> = when (axis) {
        0 -> IntStream.range(0, columnsNumber).mapToObj { columnView(it) }
        1 -> IntStream.range(0, rowsNumber).mapToObj { rowView(it) }
        else -> throw IllegalArgumentException(axis.toString())
    }

    fun fill(init: Double) = flatten().fill(init)

    fun mean() = flatten().mean()

    fun sum() = flatten().sum()

    fun max() = flatten().max()

    fun argMax() = flatten().argMax()

    fun min() = flatten().min()

    fun argMin() = flatten().argMin()

    fun logSumExp() = flatten().logSumExp()

    fun logRescale() = flatten().logRescale()

    fun expInPlace() = flatten().expInPlace()

    fun exp() = copy().apply { expm1InPlace() }

    fun expm1InPlace() = flatten().expInPlace()

    fun expm1() = copy().apply { expm1InPlace() }

    fun logInPlace() = flatten().logInPlace()

    fun log() = copy().apply { logInPlace() }

    fun log1pInPlace() = flatten().logInPlace()

    fun log1p() = copy().apply { log1pInPlace() }

    fun logAddExp(other: StridedMatrix2, dst: StridedMatrix2) {
        checkDimensions(other)
        checkDimensions(dst)
        flatten().logAddExp(other.flatten(), dst.flatten())
    }

    operator fun plus(other: StridedMatrix2): StridedMatrix2 {
        checkDimensions(other)
        return (flatten() + other.flatten()).reshape(rowsNumber, columnsNumber)
    }

    fun toArray() = Array(rowsNumber) { rowView(it).toArray() }

    private fun toString(maxDisplay: Int): String {
        if (Math.max(rowsNumber, columnsNumber) <= maxDisplay) {
            return Arrays.deepToString(toArray())
        } else {
            val numRows = Math.min(rowsNumber, maxDisplay)
            val sb = StringBuilder()
            sb.append('[')
            for (r in 0..numRows - 1) {
                sb.append(rowView(r))
            }

            if (numRows > maxDisplay) {
                sb.append(", ...]")
            } else {
                sb.append(']')
            }

            return sb.toString()
        }
    }

    override fun toString() = toString(8)

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        } else if (other !is StridedMatrix2) {
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

    private fun checkDimensions(other: StridedMatrix2) {
        check(this === other ||
              (rowsNumber == other.rowsNumber &&
               columnsNumber == other.columnsNumber)) { "non-conformable matrices" }
    }
}
