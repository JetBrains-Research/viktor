package org.jetbrains.bio.viktor

import java.text.DecimalFormat

/**
 * A specialization of [StridedMatrix] for 3-D data.
 *
 * @author Sergei Lebedev
 * @since 0.1.0
 */
class StridedMatrix3 internal constructor(
        val depth: Int, val rowsNumber: Int, val columnsNumber: Int,
        val data: DoubleArray, val offset: Int,
        val depthStride: Int, val rowStride: Int, val columnStride: Int)
:
        FlatMatrixOps<StridedMatrix3> {

    constructor(depth: Int, numRows: Int, numColumns: Int,
                data: DoubleArray = DoubleArray(depth * numRows * numColumns)) :
    this(depth, numRows, numColumns, data,
         0, numRows * numColumns, numColumns, 1) {
    }

    /** Returns the shape of this matrix. */
    val shape: IntArray get() = intArrayOf(depth, rowsNumber, columnsNumber)

    /**
     * Dense matrices are laid out in a single contiguous block
     * of memory.
     *
     * This allows to use SIMD operations, e.g. when computing the
     * sum of elements.
     */
    private val isDense: Boolean get() {
        return depthStride == rowsNumber * columnsNumber &&
               rowStride == columnsNumber &&
               columnStride == 1
    }

    operator fun get(d: Int, r: Int, c: Int): Double {
        try {
            return unsafeGet(d, r, c)
        } catch (ignored: ArrayIndexOutOfBoundsException) {
            throw IndexOutOfBoundsException("index out of bounds: ($d, $r, $c)")
        }
    }

    private fun unsafeGet(d: Int, r: Int, c: Int) = data[unsafeIndex(d, r, c)]

    operator fun set(d: Int, r: Int, c: Int, value: Double) {
        try {
            unsafeSet(d, r, c, value)
        } catch (ignored: ArrayIndexOutOfBoundsException) {
            throw IndexOutOfBoundsException("index out of bounds: ($d, $r, $c)")
        }
    }

    private fun unsafeSet(d: Int, r: Int, c: Int, value: Double) {
        data[unsafeIndex(d, r, c)] = value
    }

    @Suppress("nothing_to_inline")
    private inline fun unsafeIndex(d: Int, r: Int, c: Int): Int {
        return offset + d * depthStride + r * rowStride + c * columnStride
    }

    override fun copy(): StridedMatrix3 {
        val m = StridedMatrix(depth, rowsNumber, columnsNumber)
        copyTo(m)
        return m
    }

    fun copyTo(other: StridedMatrix3) {
        checkDimensions(other)
        // XXX we don't support varying strides at the moment, although
        // it's not hard to implement.
        require(depthStride == other.depthStride &&
                rowStride == other.rowStride &&
                columnStride == other.columnStride)
        System.arraycopy(data, 0, other.data, 0, data.size)
    }

    override fun flatten(): StridedVector {
        check(isDense) { "matrix is not dense" }
        return data.asStrided()
    }

    operator fun get(d: Int) = view(d)

    operator fun set(d: Int, other: StridedMatrix2) = other.copyTo(view(d))

    operator fun set(d: Int, other: Double) = view(d).fill(other)

    fun view(d: Int): StridedMatrix2 {
        if (d < 0 || d >= depth) {
            throw IndexOutOfBoundsException("d must be in [0, $depth)")
        }

        return StridedMatrix2(rowsNumber, columnsNumber, data,
                              d * depthStride, rowStride, columnStride)
    }

    // XXX this can be done with a single allocation.
    operator fun get(d: Int, r: Int) = view(d)[r]

    operator fun set(d: Int, r: Int, other: Double) = view(d).set(r, other)

    operator fun set(d: Int, r: Int, other: StridedVector) = view(d).set(r, other)

    fun toArray() = Array(depth) { view(it).toArray() }

    // XXX: abstract this copy-paste into an interface? See
    // [StridedMatrix2.toString].
    fun toString(maxDisplay: Int,
                 format: DecimalFormat = DecimalFormat("#.####")): String {
        val sb = StringBuilder()
        sb.append('[')
        if (maxDisplay < depth) {
            for (r in 0..maxDisplay / 2 - 1) {
                sb.append(this[r].toString(maxDisplay, format)).append(", ")
            }

            sb.append("..., ")

            val leftover = maxDisplay - maxDisplay / 2
            for (r in depth - leftover..depth - 1) {
                sb.append(this[r].toString(maxDisplay, format))
                if (r < depth - 1) {
                    sb.append(", ")
                }
            }
        } else {
            for (r in 0..depth - 1) {
                sb.append(this[r].toString(maxDisplay, format))
                if (r < depth - 1) {
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
        } else if (other !is StridedMatrix3) {
            return false
        }

        if (depth != other.depth || rowsNumber != other.rowsNumber ||
            columnsNumber != other.columnsNumber) {
            return false
        }

        for (d in 0..depth - 1) {
            if (this[d] != other[d]) {
                return false
            }
        }

        return true
    }

    override fun hashCode(): Int {
        var acc = 1
        for (d in 0..depth - 1) {
            acc = 31 * acc + this[d].hashCode()
        }

        return acc
    }

    override fun checkDimensions(other: StridedMatrix3) {
        check(this === other ||
              (depth == other.depth && rowsNumber == other.rowsNumber &&
               columnsNumber == other.columnsNumber)) { "non-conformable matrices" }
    }
}

/** Reshapes this vector into a matrix in row-major order. */
fun StridedVector.reshape(depth: Int, numRows: Int, numColumns: Int): StridedMatrix3 {
    require(depth * numRows * numColumns == size)
    return StridedMatrix3(depth, numRows, numColumns, data, offset,
                          numRows * numColumns * stride, numColumns * stride, stride)
}
