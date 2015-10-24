package org.jetbrains.bio.viktor

import java.util.*

/**
 * A specialization of [StridedMatrix] for 3-D data.
 *
 * @author Sergei Lebedev
 * @since 0.1.0
 */
open class StridedMatrix3 internal constructor(
        val depth: Int, val rowsNumber: Int, val columnsNumber: Int,
        protected val data: DoubleArray,
        private val depthStride: Int,
        private val rowStride: Int,
        private val columnStride: Int) {

    constructor(depth: Int, numRows: Int, numColumns: Int) :
    this(depth, numRows, numColumns,
         DoubleArray(depth * numRows * numColumns),
         numRows * numColumns, numColumns, 1) {
    }

    /**
     * Dense matrices are laid out in a single contiguous block
     * of memory. This allows to use SIMD operations, e.g. when
     * computing the sum of elements.
     */
    protected val isDense: Boolean get() {
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

    private fun unsafeIndex(d: Int, r: Int, c: Int): Int {
        return d * depthStride + r * rowStride + c * columnStride
    }

    fun copy(): StridedMatrix3 {
        val copy = StridedMatrix(depth, rowsNumber, columnsNumber)
        copyTo(copy)
        return copy
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

    fun flatten(): StridedVector {
        check(isDense) { "matrix is not dense" }
        return data.asStrided()
    }

    operator fun get(d: Int): StridedMatrix2 = view(d)

    operator fun set(d: Int, other: StridedMatrix2) = other.copyTo(view(d))

    operator fun set(d: Int, other: Double) = view(d).fill(other)

    fun view(d: Int): StridedMatrix2 {
        require(d >= 0 && d < depth) { "d must be in [0, $depth)" }
        return StridedMatrix2(rowsNumber, columnsNumber, d * depthStride,
                              data, rowStride, columnStride)
    }

    // XXX this can be done with a single allocation.
    operator fun get(d: Int, r: Int) = view(d)[r]

    operator fun set(d: Int, r: Int, other: Double) = view(d).set(r, other)

    operator fun set(d: Int, r: Int, other: StridedVector) = view(d).set(r, other)

    fun fill(init: Double): Unit = flatten().fill(init)

    fun sum() = flatten().sum()

    fun max() = flatten().max()

    fun argMax() = flatten().argMax()

    fun min() = flatten().min()

    fun argMin() = flatten().argMin()

    fun logSumExp() = flatten().logSumExp()

    fun logRescale() = flatten().logRescale()

    fun expInPlace() = flatten().expInPlace()

    fun exp(): StridedMatrix3 {
        val m = copy()
        m.expInPlace()
        return m
    }

    fun logInPlace() = flatten().logInPlace()

    fun log(): StridedMatrix3 {
        val m = copy()
        m.logInPlace()
        return m
    }

    fun logAddExp(other: StridedMatrix3, dst: StridedMatrix3) {
        checkDimensions(other)
        checkDimensions(dst)
        flatten().logAddExp(other.flatten(), dst.flatten())
    }

    fun toArray() = Array(depth) { view(it).toArray() }

    override fun toString() = Arrays.deepToString(toArray())

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
            if (this[depth] != other[depth]) {
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

    private fun checkDimensions(other: StridedMatrix3) {
        check(this === other ||
              (depth == other.depth && rowsNumber == other.rowsNumber &&
               columnsNumber == other.columnsNumber)) { "non-conformable matrices" }
    }
}
