package org.jetbrains.bio.viktor

/**
 * A contiguous vector.
 *
 * @author Sergei Lebedev
 * @since 0.1.0
 */
sealed class F64DenseFlatArray(
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
            return if (size <= DENSE_SPLIT_SIZE || !Loader.nativeLibraryLoaded) {
                F64SmallDenseArray(data, offset, size)
            } else {
                F64LargeDenseArray(data, offset, size)
            }
        }
    }
}

/**
 * A contiguous vector of size at most [F64DenseFlatArray.DENSE_SPLIT_SIZE].
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

    override fun sd() = NativeSpeedups.unsafeSD(data, offset, size)

    override fun sum() = NativeSpeedups.unsafeSum(data, offset, size)

    override fun cumSum() {
        if (!NativeSpeedups.unsafeCumSum(data, offset, size)) super.cumSum()
    }

    override fun min() = NativeSpeedups.unsafeMin(data, offset, size)

    override fun max() = NativeSpeedups.unsafeMax(data, offset, size)

    override fun dot(other: F64Array): Double {
        return if (other is F64LargeDenseArray) {
            checkShape(other)
            NativeSpeedups.unsafeDot(data, offset, other.data, other.offset, size)
        } else {
            super.dot(other)
        }
    }

    override fun expInPlace() {
        if (!NativeSpeedups.unsafeExpInPlace(data, offset, size)) super.expInPlace()
    }

    override fun expm1InPlace() {
        if (!NativeSpeedups.unsafeExpm1InPlace(data, offset, size)) super.expm1InPlace()
    }

    override fun logInPlace() {
        if (!NativeSpeedups.unsafeLogInPlace(data, offset, size)) super.logInPlace()
    }

    override fun log1pInPlace(){
        if (!NativeSpeedups.unsafeLog1pInPlace(data, offset, size)) super.log1pInPlace()
    }

    override fun logSumExp() = NativeSpeedups.unsafeLogSumExp(data, offset, size)

    override fun logAddExpAssign(other: F64Array) {
        if (other is F64LargeDenseArray) {
            checkShape(other)
            if (NativeSpeedups.unsafeLogAddExp(data, offset, other.data, other.offset, size)) return
        }
        super.logAddExpAssign(other)
    }

    override fun unaryMinus() = copy().apply { NativeSpeedups.unsafeNegateInPlace(data, offset, size) }

    override fun plusAssign(update: Double) {
        if (!NativeSpeedups.unsafePlusScalarAssign(data, offset, size, update)) super.plusAssign(update)
    }

    override fun plusAssign(other: F64Array) {
        if (other is F64DenseFlatArray) {
            checkShape(other)
            if (NativeSpeedups.unsafePlusAssign(data, offset, other.data, other.offset, size)) return
        }
        super.plusAssign(other)
    }

    override fun minusAssign(update: Double) {
        if (!NativeSpeedups.unsafeMinusScalarAssign(data, offset, size, update)) super.minusAssign(update)
    }

    override fun minusAssign(other: F64Array) {
        if (other is F64DenseFlatArray) {
            checkShape(other)
            if (NativeSpeedups.unsafeMinusAssign(data, offset, other.data, other.offset, size)) return
        }
        super.minusAssign(other)
    }

    override fun timesAssign(update: Double) {
        if (!NativeSpeedups.unsafeTimesScalarAssign(data, offset, size, update)) super.timesAssign(update)
    }

    override fun timesAssign(other: F64Array) {
        if (other is F64DenseFlatArray) {
            checkShape(other)
            if (NativeSpeedups.unsafeTimesAssign(data, offset, other.data, other.offset, size)) return
        }
        super.timesAssign(other)
    }

    override fun divAssign(update: Double) {
        if (!NativeSpeedups.unsafeDivScalarAssign(data, offset, size, update)) super.divAssign(update)
    }

    override fun divAssign(other: F64Array) {
        if (other is F64DenseFlatArray) {
            checkShape(other)
            if (NativeSpeedups.unsafeDivAssign(data, offset, other.data, other.offset, size)) return
        } else {
            super.divAssign(other)
        }
    }
}
