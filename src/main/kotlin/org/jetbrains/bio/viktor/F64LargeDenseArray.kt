package org.jetbrains.bio.viktor

/**
 * Represents a large dense array of 64-bit floating-point numbers.
 * This class extends `F64DenseFlatArray` to provide efficient operations
 * using vectorized API speed-ups for various mathematical computations.
 *
 * @author Oleg Shpynov
 * @since 2.0.0
 */
internal class F64LargeDenseArray(
    data: DoubleArray,
    offset: Int,
    size: Int
) : F64DenseFlatArray(data, offset, size) {

    override fun sd() = VectorApiSpeedups.unsafeSD(data, offset, length)

    override fun sum() = VectorApiSpeedups.unsafeSum(data, offset, length)

    override fun dot(other: F64Array): Double {
        return if ((forceVectorApi() || !isAarch64()) && other is F64LargeDenseArray) {
            checkShape(other)
            VectorApiSpeedups.unsafeDot(data, offset, other.data, other.offset, length)
        } else {
            super.dot(other)
        }
    }

    private inline fun nativeTransform(
        nativeOp: (DoubleArray, Int, DoubleArray, Int, Int) -> Unit,
    ): F64FlatArray {
        val dst = DoubleArray(length)
        nativeOp(dst, 0, data, offset, length)
        return create(dst, 0, length)
    }

    override fun expInPlace() =
        if (forceVectorApi() || !isAarch64())
            VectorApiSpeedups.unsafeExp(data, offset, data, offset, length)
        else
            super.expInPlace()

    override fun exp() =
        if (forceVectorApi() || !isAarch64())
            nativeTransform(VectorApiSpeedups::unsafeExp)
        else
            super.exp()

    override fun expm1InPlace() =
        if (forceVectorApi() || !isAarch64())
            VectorApiSpeedups.unsafeExpm1(data, offset, data, offset, length)
        else
            super.expm1InPlace()

    override fun expm1() =
        if (forceVectorApi() || !isAarch64())
            nativeTransform(VectorApiSpeedups::unsafeExpm1)
        else
            super.expm1()

    override fun logInPlace() =
        if (forceVectorApi() || !isAarch64())
            VectorApiSpeedups.unsafeLog(data, offset, data, offset, length)
        else
            super.logInPlace()

    override fun log() =
        if (forceVectorApi() || !isAarch64())
            nativeTransform(VectorApiSpeedups::unsafeLog)
        else
            super.log()

    override fun log1pInPlace() =
        if (forceVectorApi() || !isAarch64())
            VectorApiSpeedups.unsafeLog1p(data, offset, data, offset, length)
        else
            super.log1pInPlace()

    override fun log1p() =
        if (forceVectorApi() || !isAarch64())
            nativeTransform(VectorApiSpeedups::unsafeLog1p)
        else
            super.log1p()

    override fun logSumExp() =
        if (forceVectorApi() || !isAarch64())
            VectorApiSpeedups.unsafeLogSumExp(data, offset, length)
        else
            super.logSumExp()

}
