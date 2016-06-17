package org.jetbrains.bio.viktor

import org.jetbrains.bio.jni.DoubleMathNative
import org.jetbrains.bio.jni.DoubleOpsNative
import org.jetbrains.bio.jni.DoubleStatNative
import org.jetbrains.bio.jni.Loader

/**
 * A contiguous strided vector.
 *
 * @author Sergei Lebedev
 * @since 0.1.0
 */
open class DenseVector protected constructor(data: DoubleArray, offset: Int, size: Int) :
        StridedVector(data, offset, size, 1) {
    override fun unsafeIndex(pos: Int) = offset + pos

    override fun fill(init: Double) {
        data.fill(init, offset, offset + size)
    }

    override fun copyTo(other: StridedVector) {
        if (other is DenseVector) {
            checkSize(other)
            System.arraycopy(data, offset, other.data, other.offset, size)
        } else {
            super.copyTo(other)
        }
    }

    override fun toArray() = data.copyOfRange(offset, offset + size)

    companion object {
        /**
         * We only use SIMD operations on vectors larger than the split boundary.
         */
        const val DENSE_SPLIT_SIZE = 16

        internal fun create(data: DoubleArray, offset: Int, size: Int): DenseVector {
            return if (size <= DENSE_SPLIT_SIZE || !Loader.useNative) {
                SmallDenseVector(data, offset, size)
            } else {
                LargeDenseVector(data, offset, size)
            }
        }
    }
}

/**
 * A contiguous strided vector of size at most [DenseVector.DENSE_SPLIT_SIZE].
 *
 * @author Sergei Lebedev
 * @since 0.1.0
 */
class SmallDenseVector(data: DoubleArray, offset: Int, size: Int) :
        DenseVector(data, offset, size)

/**
 * A contiguous vector of size at least `[DenseVector.DENSE_SPLIT_SIZE] + 1`.
 *
 * @author Sergei Lebedev
 * @since 0.1.0
 */
class LargeDenseVector(data: DoubleArray, offset: Int, size: Int) :
        DenseVector(data, offset, size) {

    override fun mean() = DoubleStatNative.sum(data, offset, size) / size

    override fun sum() = DoubleStatNative.sum(data, offset, size)

    override fun sumSq(): Double {
        val copy = copy()
        copy *= copy
        return copy.sum()
    }

    override fun cumSum() = DoubleStatNative.prefixSum(data, offset, data, offset, size)

    override fun min() = DoubleOpsNative.unsafeMin(data, offset, size)

    override fun max() = DoubleOpsNative.unsafeMax(data, offset, size)

    override fun dot(other: DoubleArray): Double {
        require(other.size == size) { "non-conformable arrays" }
        return DoubleMathNative.unsafeDot(data, offset, other, 0, size)
    }

    override fun dot(other: StridedVector): Double {
        return if (other is LargeDenseVector) {
            require(other.size == size) { "non-conformable arrays" }
            DoubleMathNative.unsafeDot(data, offset, other.data, other.offset, size)
        } else {
            super.dot(other)
        }
    }

    override fun expInPlace() {
        DoubleMathNative.unsafeExp(data, offset, data, 0, data.size)
    }

    override fun logInPlace() {
        DoubleMathNative.unsafeLog(data, offset, data, 0, data.size)
    }

    override fun logRescale() = DoubleMathNative.logRescale(data, offset, size)

    override fun logSumExp() = DoubleMathNative.logSumExp(data, offset, size)

    override fun logAddExp(other: StridedVector, dst: StridedVector) {
        if (other is DenseVector && dst is DenseVector) {
            checkSize(other)
            checkSize(dst)
            DoubleMathNative.logAddExp(data, offset,
                                       other.data, other.offset,
                                       dst.data, dst.offset, size)
        } else {
            super.logAddExp(other, dst)
        }
    }

    override fun unaryMinus(): StridedVector {
        val v = copy()
        DoubleOpsNative.unsafeNegate(data, offset, v.data, v.offset, v.size)
        return v
    }

    override fun plusAssign(update: Double) {
        DoubleOpsNative.unsafePlusScalar(data, offset, update, data, offset, size)
    }

    override fun plusAssign(other: StridedVector) {
        if (other is DenseVector) {
            checkSize(other)
            DoubleOpsNative.unsafePlus(data, offset,
                                       other.data, other.offset,
                                       data, offset, size)
        } else {
            super.plusAssign(other)
        }
    }

    override fun minusAssign(update: Double) {
        DoubleOpsNative.unsafeMinusScalar(data, offset, update, data, offset, size)
    }

    override fun minusAssign(other: StridedVector) {
        if (other is DenseVector) {
            checkSize(other)
            DoubleOpsNative.unsafeMinus(data, offset,
                                        other.data, other.offset,
                                        data, offset, size)
        } else {
            super.plusAssign(other)
        }
    }

    override fun timesAssign(update: Double) {
        DoubleOpsNative.unsafeTimesScalar(data, offset, update, data, offset, size)
    }

    override fun timesAssign(other: StridedVector) {
        if (other is DenseVector) {
            DoubleOpsNative.unsafeTimes(data, offset,
                                        other.data, other.offset,
                                        data, offset, size)
        } else {
            super.timesAssign(other)
        }
    }
}