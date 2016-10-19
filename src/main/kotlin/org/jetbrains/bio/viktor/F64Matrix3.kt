package org.jetbrains.bio.viktor

import java.text.DecimalFormat
import java.util.*

/**
 * A specialization of [F64Matrix] for 3-D data.
 *
 * @author Sergei Lebedev
 * @since 0.1.0
 */
class F64Matrix3 internal constructor(data: DoubleArray, offset: Int,
                                      strides: IntArray, shape: IntArray)
:
        F64Matrix(data, offset, strides, shape),
        F64MatrixOps<F64Matrix3> {

    constructor(depth: Int, numRows: Int, numColumns: Int,
                data: DoubleArray = DoubleArray(depth * numRows * numColumns)) :
    this(data, 0,
         intArrayOf(numRows * numColumns, numColumns, 1),
         intArrayOf(depth, numRows, numColumns)) {}

    @Deprecated("", replaceWith = ReplaceWith("shape[0]"))
    val depth: Int get() = shape[0]
    @Deprecated("", replaceWith = ReplaceWith("shape[1]"))
    val rowsNumber: Int get() = shape[1]
    @Deprecated("", replaceWith = ReplaceWith("shape[2]"))
    val columnsNumber: Int get() = shape[2]

    override fun unwrap() = this

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
        return offset + d * strides[0] + r * strides[1] + c * strides[2]
    }

    override fun copy(): F64Matrix3 {
        val m = F64Matrix(depth, rowsNumber, columnsNumber)
        copyTo(m)
        return m
    }

    fun copyTo(other: F64Matrix3) {
        checkDimensions(other)
        // XXX we don't support varying strides at the moment, although
        // it's not hard to implement.
        require(Arrays.equals(strides, other.strides))
        System.arraycopy(data, 0, other.data, 0, data.size)
    }

    override fun F64Vector.reshapeLike(other: F64Matrix3): F64Matrix3 {
        return reshape(depth, rowsNumber, columnsNumber)
    }

    operator fun get(d: Int) = view(d)

    operator fun set(d: Int, other: F64Matrix2) = other.copyTo(view(d))

    operator fun set(d: Int, other: Double) = view(d).fill(other)

    fun view(d: Int): F64Matrix2 {
        if (d < 0 || d >= depth) {
            throw IndexOutOfBoundsException("d must be in [0, $depth)")
        }

        // TODO: generalize? use primary constructor?
        return F64Matrix2(data, d * strides.first(),
                          strides.sliceArray(1 until nDim),
                          shape.sliceArray(1 until nDim))
    }

    // XXX this can be done with a single allocation.
    operator fun get(d: Int, r: Int) = view(d)[r]

    operator fun set(d: Int, r: Int, other: Double) = view(d).set(r, other)

    operator fun set(d: Int, r: Int, other: F64Vector) = view(d).set(r, other)

    fun toArray() = Array(depth) { view(it).toArray() }

    // XXX: abstract this copy-paste into an interface? See
    // [F64Matrix2.toString].
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

    override fun equals(other: Any?) = when {
        this === other -> true
        other !is F64Matrix3 -> false
        !Arrays.equals(shape, other.shape) -> false
        else -> (0..depth - 1).all { this[it] == other[it] }
    }

    override fun hashCode(): Int {
        return (0..size - 1).fold(1) { acc, r -> 31 * acc + this[r].hashCode() }
    }
}

/** Reshapes this vector into a matrix in row-major order. */
fun F64Vector.reshape(depth: Int, numRows: Int, numColumns: Int): F64Matrix3 {
    require(depth * numRows * numColumns == size)
    return F64Matrix3(data, offset,
                      intArrayOf(numRows * numColumns * stride, numColumns * stride, stride),
                      intArrayOf(depth, numRows, numColumns))
}
