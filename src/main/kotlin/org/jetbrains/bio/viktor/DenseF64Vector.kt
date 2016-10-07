package org.jetbrains.bio.viktor

/**
 * A contiguous strided vector.
 *
 * @author Sergei Lebedev
 * @since 0.1.0
 */
open class DenseF64Vector protected constructor(data: DoubleArray, offset: Int, size: Int) :
        F64Vector(data, offset, size, 1) {
    override fun unsafeIndex(pos: Int) = offset + pos

    override fun fill(init: Double) {
        data.fill(init, offset, offset + size)
    }

    override fun copyTo(other: F64Vector) {
        if (other is DenseF64Vector) {
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

        internal fun create(data: DoubleArray, offset: Int, size: Int): DenseF64Vector {
            return if (size <= DENSE_SPLIT_SIZE || !Loader.useNative) {
                SmallDenseF64Vector(data, offset, size)
            } else {
                LargeDenseF64Vector(data, offset, size)
            }
        }
    }
}

/**
 * A contiguous strided vector of size at most [DenseF64Vector.DENSE_SPLIT_SIZE].
 *
 * @author Sergei Lebedev
 * @since 0.1.0
 */
class SmallDenseF64Vector(data: DoubleArray, offset: Int, size: Int) :
        DenseF64Vector(data, offset, size)

/**
 * A contiguous vector of size at least `[DenseF64Vector.DENSE_SPLIT_SIZE] + 1`.
 *
 * @author Sergei Lebedev
 * @since 0.1.0
 */
class LargeDenseF64Vector(data: DoubleArray, offset: Int, size: Int) :
        DenseF64Vector(data, offset, size) {

    override fun mean() = NativeSpeedups.sum(data, offset, size) / size

    override fun sd() = NativeSpeedups.sd(data, offset, size)

    override fun sum() = NativeSpeedups.sum(data, offset, size)

    override fun cumSum() = NativeSpeedups.cumSum(data, offset, data, offset, size)

    override fun min() = NativeSpeedups.unsafeMin(data, offset, size)

    override fun max() = NativeSpeedups.unsafeMax(data, offset, size)

    override fun dot(other: F64Vector): Double {
        return if (other is LargeDenseF64Vector) {
            require(other.size == size) { "non-conformable arrays" }
            NativeSpeedups.unsafeDot(data, offset, other.data, other.offset, size)
        } else {
            super.dot(other)
        }
    }

    override fun expInPlace() {
        NativeSpeedups.unsafeExp(data, offset, data, 0, data.size)
    }

    override fun expm1InPlace() {
        NativeSpeedups.unsafeExpm1(data, offset, data, 0, data.size)
    }

    override fun logInPlace() {
        NativeSpeedups.unsafeLog(data, offset, data, 0, data.size)
    }

    override fun log1pInPlace() {
        NativeSpeedups.unsafeLog1p(data, offset, data, 0, data.size)
    }

    override fun logRescale() {
        NativeSpeedups.unsafeLogRescale(data, offset, data, offset, size)
    }

    override fun logSumExp() = NativeSpeedups.unsafeLogSumExp(data, offset, size)

    override fun logAddExp(other: F64Vector, dst: F64Vector) {
        if (other is DenseF64Vector && dst is DenseF64Vector) {
            checkSize(other)
            checkSize(dst)
            NativeSpeedups.unsafeLogAddExp(data, offset,
                                           other.data, other.offset,
                                           dst.data, dst.offset, size)
        } else {
            super.logAddExp(other, dst)
        }
    }

    override fun unaryMinus(): F64Vector {
        val v = copy()
        NativeSpeedups.unsafeNegate(data, offset, v.data, v.offset, v.size)
        return v
    }

    override fun plusAssign(update: Double) {
        NativeSpeedups.unsafePlusScalar(data, offset, update, data, offset, size)
    }

    override fun plusAssign(other: F64Vector) {
        if (other is DenseF64Vector) {
            checkSize(other)
            NativeSpeedups.unsafePlus(data, offset,
                                      other.data, other.offset,
                                      data, offset, size)
        } else {
            super.plusAssign(other)
        }
    }

    override fun minusAssign(update: Double) {
        NativeSpeedups.unsafeMinusScalar(data, offset, update, data, offset, size)
    }

    override fun minusAssign(other: F64Vector) {
        if (other is DenseF64Vector) {
            checkSize(other)
            NativeSpeedups.unsafeMinus(data, offset,
                                       other.data, other.offset,
                                       data, offset, size)
        } else {
            super.minusAssign(other)
        }
    }

    override fun timesAssign(update: Double) {
        NativeSpeedups.unsafeTimesScalar(data, offset, update, data, offset, size)
    }

    override fun timesAssign(other: F64Vector) {
        if (other is DenseF64Vector) {
            NativeSpeedups.unsafeTimes(data, offset,
                                       other.data, other.offset,
                                       data, offset, size)
        } else {
            super.timesAssign(other)
        }
    }

    override fun divAssign(update: Double) {
        NativeSpeedups.unsafeDivScalar(data, offset, update, data, offset, size)
    }

    override fun divAssign(other: F64Vector) {
        if (other is DenseF64Vector) {
            NativeSpeedups.unsafeDiv(data, offset,
                                     other.data, other.offset,
                                     data, offset, size)
        } else {
            super.divAssign(other)
        }
    }
}
