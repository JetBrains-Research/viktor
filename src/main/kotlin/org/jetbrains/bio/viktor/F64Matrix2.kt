package org.jetbrains.bio.viktor

/**
 * A specialization of [F64Matrix] for 2-D data.
 *
 * @author Sergei Lebedev
 * @since 0.1.0
 */
class F64Matrix2 internal constructor(data: DoubleArray, offset: Int,
                                      strides: IntArray, shape: IntArray)
:
        F64Matrix(data, offset, strides, shape) {

    constructor(numRows: Int, numColumns: Int,
                data: DoubleArray = DoubleArray(numRows * numColumns))
    : this(data, 0, intArrayOf(numColumns, 1), intArrayOf(numRows, numColumns)) {}

//    fun toString(maxDisplay: Int,
//                 format: DecimalFormat = DecimalFormat("#.####")): String {
//        val sb = StringBuilder()
//        sb.append('[')
//        if (maxDisplay < size) {
//            for (r in 0..maxDisplay / 2 - 1) {
//                sb.append(this[r].toString(maxDisplay, format)).append(", ")
//            }
//
//            sb.append("..., ")
//
//            val leftover = maxDisplay - maxDisplay / 2
//            for (r in size - leftover..size - 1) {
//                sb.append(this[r].toString(maxDisplay, format))
//                if (r < size - 1) {
//                    sb.append(", ")
//                }
//            }
//        } else {
//            for (r in 0..size - 1) {
//                sb.append(this[r].toString(maxDisplay, format))
//                if (r < size - 1) {
//                    sb.append(", ")
//                }
//            }
//        }
//
//        sb.append(']')
//        return sb.toString()
//    }
}

/** Reshapes this vector into a matrix in row-major order. */
fun F64Vector.reshape(numRows: Int, numColumns: Int): F64Matrix2 {
    return reshape(*intArrayOf(numRows, numColumns)) as F64Matrix2
}
