package org.jetbrains.bio.viktor

/**
 * A contiguous strided vector.
 *
 * @author Sergei Lebedev
 * @since 0.1.0
 */
open class F64DenseFlatArray protected constructor(
        data: DoubleArray,
        offset: Int,
        size: Int
) : F64FlatArray(data, offset, 1, size) {

    override fun fill(init: Double) = data.fill(init, offset, offset + size)

    override fun copyTo(other: F64Array) {
        if (other is F64DenseFlatArray) {
            checkShape(other)
            System.arraycopy(data, offset, other.data, other.offset, size)
        } else {
            super.copyTo(other)
        }
    }

    override fun toDoubleArray() = data.copyOfRange(offset, offset + size)

    companion object {
        /**
         * We only use SIMD operations on vectors larger than the split boundary.
         */
        const val DENSE_SPLIT_SIZE = 16

        internal fun create(data: DoubleArray, offset: Int, size: Int): F64DenseFlatArray {
            return if (size <= DENSE_SPLIT_SIZE || !Loader.optimizationSupported) {
                F64SmallDenseArray(data, offset, size)
            } else {
                F64LargeDenseArray(data, offset, size)
            }
        }
    }
}

/**
 * A contiguous strided vector of size at most [F64DenseFlatArray.DENSE_SPLIT_SIZE].
 *
 * @author Sergei Lebedev
 * @since 0.1.0
 */
class F64SmallDenseArray(
        data: DoubleArray,
        offset: Int,
        size: Int
) : F64DenseFlatArray(data, offset, size)

/**
 * A contiguous vector of size at least `[F64DenseFlatArray.DENSE_SPLIT_SIZE] + 1`.
 *
 * @author Sergei Lebedev
 * @since 0.1.0
 */
class F64LargeDenseArray(
        data: DoubleArray,
        offset: Int,
        size: Int
) : F64DenseFlatArray(data, offset, size) {

    override fun sd() = NativeSpeedups.sd(data, offset, size)

    override fun balancedSum() = NativeSpeedups.sum(data, offset, size)

    override fun cumSum() = NativeSpeedups.cumSum(data, offset, data, offset, size)

    override fun min() = NativeSpeedups.unsafeMin(data, offset, size)

    override fun max() = NativeSpeedups.unsafeMax(data, offset, size)

    override fun dot(other: F64Array): Double {
        return if (other is F64LargeDenseArray) {
            require(other.size == size) { "non-conformable arrays" }
            NativeSpeedups.unsafeDot(data, offset, other.data, other.offset, size)
        } else {
            super.dot(other)
        }
    }

    override fun expInPlace() = NativeSpeedups.unsafeExpInPlace(data, offset, size)

    override fun expm1InPlace() = NativeSpeedups.unsafeExpm1InPlace(data, offset, size)

    override fun logInPlace() = NativeSpeedups.unsafeLogInPlace(data, offset, size)

    override fun log1pInPlace() = NativeSpeedups.unsafeLog1pInPlace(data, offset, size)

    override fun logRescale() = NativeSpeedups.unsafeLogRescale(data, offset, data, offset, size)

    override fun logSumExp() = NativeSpeedups.unsafeLogSumExp(data, offset, size)

    override fun logAddExp(other: F64Array, dst: F64Array) {
        if (other is F64DenseFlatArray && dst is F64DenseFlatArray) {
            checkShape(other)
            checkShape(dst)
            NativeSpeedups.unsafeLogAddExp(
                data, offset,
                other.data, other.offset,
                dst.data, dst.offset, size
            )
        } else {
            super.logAddExp(other, dst)
        }
    }

    override fun unaryMinus() =
            copy().apply { NativeSpeedups.unsafeNegateInPlace(data, offset, size) }

    override fun plusAssign(update: Double) =
            NativeSpeedups.unsafePlusScalarAssign(data, offset, size, update)

    override fun plusAssign(other: F64Array) {
        if (other is F64DenseFlatArray) {
            checkShape(other)
            NativeSpeedups.unsafePlusAssign(data, offset, other.data, other.offset, size)
        } else {
            super.plusAssign(other)
        }
    }

    override fun minusAssign(update: Double) =
            NativeSpeedups.unsafeMinusScalarAssign(data, offset, size, update)

    override fun minusAssign(other: F64Array) {
        if (other is F64DenseFlatArray) {
            checkShape(other)
            NativeSpeedups.unsafeMinusAssign(data, offset, other.data, other.offset, size)
        } else {
            super.minusAssign(other)
        }
    }

    override fun timesAssign(update: Double) =
            NativeSpeedups.unsafeTimesScalarAssign(data, offset, size, update)

    override fun timesAssign(other: F64Array) {
        if (other is F64DenseFlatArray) {
            NativeSpeedups.unsafeTimesAssign(data, offset, other.data, other.offset, size)
        } else {
            super.timesAssign(other)
        }
    }

    override fun divAssign(update: Double) = NativeSpeedups.unsafeDivScalarAssign(data, offset, size, update)

    override fun divAssign(other: F64Array) {
        if (other is F64DenseFlatArray) {
            NativeSpeedups.unsafeDivAssign(data, offset, other.data, other.offset, size)
        } else {
            super.divAssign(other)
        }
    }
}
