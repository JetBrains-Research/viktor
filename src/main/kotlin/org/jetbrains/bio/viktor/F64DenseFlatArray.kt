package org.jetbrains.bio.viktor

/**
 * A contiguous vector.
 *
 * @author Sergei Lebedev
 * @since 0.1.0
 */
internal sealed class F64DenseFlatArray(
    data: DoubleArray,
    offset: Int,
    size: Int
) : F64FlatArray(data, offset, 1, size) {

    override fun fill(init: Double) = data.fill(init, offset, offset + size)

    override fun copy(): F64FlatArray {
        val copyData = DoubleArray(size)
        System.arraycopy(data, offset, copyData, 0, size)
        return create(copyData, 0, size)
    }

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
internal class F64SmallDenseArray(
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
internal class F64LargeDenseArray(
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
        if (!NativeSpeedups.unsafeExp(data, offset, data, offset, size)) super.expInPlace()
    }

    override fun exp(): F64Array {
        val res = DoubleArray(size)
        if (NativeSpeedups.unsafeExp(res, 0, data, offset, size)) {
            return F64FlatArray(res)
        }
        return super.exp()
    }

    override fun expm1InPlace() {
        if (!NativeSpeedups.unsafeExpm1(data, offset, data, offset, size)) super.expm1InPlace()
    }

    override fun expm1(): F64Array {
        val res = DoubleArray(size)
        if (NativeSpeedups.unsafeExpm1(res, 0, data, offset, size)) {
            return F64FlatArray(res)
        }
        return super.expm1()
    }

    override fun logInPlace() {
        if (!NativeSpeedups.unsafeLog(data, offset, data, offset, size)) super.logInPlace()
    }

    override fun log(): F64Array {
        val res = DoubleArray(size)
        if (NativeSpeedups.unsafeLog(res, 0, data, offset, size)) {
            return F64FlatArray(res)
        }
        return super.log()
    }

    override fun log1pInPlace(){
        if (!NativeSpeedups.unsafeLog1p(data, offset, data, offset, size)) super.log1pInPlace()
    }

    override fun log1p(): F64Array {
        val res = DoubleArray(size)
        if (NativeSpeedups.unsafeLog1p(res, 0, data, offset, size)) {
            return F64FlatArray(res)
        }
        return super.log1p()
    }

    override fun logSumExp() = NativeSpeedups.unsafeLogSumExp(data, offset, size)

    override fun logAddExpAssign(other: F64Array) {
        if (other is F64LargeDenseArray) {
            checkShape(other)
            if (NativeSpeedups.unsafeLogAddExp(data, offset, data, offset, other.data, other.offset, size)) return
        }
        super.logAddExpAssign(other)
    }

    override fun logAddExp(other: F64Array): F64Array {
        if (other is F64LargeDenseArray) {
            checkShape(other)
            val res = DoubleArray(size)
            if (NativeSpeedups.unsafeLogAddExp(res, 0, data, offset, other.data, other.offset, size)) {
                return F64FlatArray(res)
            }
        }
        return super.logAddExp(other)
    }
}
