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

    private inline fun denseTransformInPlace(op: (Double) -> Double) {
        for (i in 0 until size) {
            data[i + offset] = op.invoke(data[i + offset])
        }
    }

    private inline fun denseTransform(op: (Double) -> Double): F64FlatArray {
        val res = DoubleArray(size)
        for (i in 0 until size) {
            res[i] = op.invoke(data[i + offset])
        }
        return create(res, 0, size)
    }

    private inline fun denseEBEInPlace(other: F64DenseFlatArray, op: (Double, Double) -> Double) {
        for (i in 0 until size) {
            data[i + offset] = op.invoke(data[i + offset], other.data[i + other.offset])
        }
    }

    private inline fun ebeInPlace(
        other: F64Array,
        op: (Double, Double) -> Double,
        superOp: F64Array.(F64Array) -> Unit
    ) {
        if (other is F64DenseFlatArray) {
            checkShape(other)
            denseEBEInPlace(other, op)
        } else {
            this.superOp(other)
        }
    }

    private inline fun denseEBE(other: F64DenseFlatArray, op: (Double, Double) -> Double): F64DenseFlatArray {
        val res = DoubleArray(size)
        for (i in 0 until size) {
            res[i] = op.invoke(data[i + offset], other.data[i + other.offset])
        }
        return create(res, 0, size)
    }

    private inline fun ebe(
        other: F64Array,
        op: (Double, Double) -> Double,
        superOp: F64Array.(F64Array) -> F64FlatArray
    ): F64FlatArray = if (other is F64DenseFlatArray) {
        checkShape(other)
        denseEBE(other, op)
    } else {
        this.superOp(other)
    }

    override fun transformInPlace(op: (Double) -> Double) = denseTransformInPlace(op)

    override fun transform(op: (Double) -> Double): F64FlatArray = denseTransform(op)

    /* Arithmetic */

    /* Arithmetic binary operations */

    /* Addition */

    override fun plusAssign(other: F64Array) = ebeInPlace(other, { a, b -> a + b }, { super.plusAssign(it) })

    override fun plus(other: F64Array) = ebe(other, { a, b -> a + b }, { super.plus(it) })

    /* Subtraction */

    override fun minusAssign(other: F64Array) = ebeInPlace(other, { a, b -> a - b }, { super.minusAssign(it) })

    override fun minus(other: F64Array) = ebe(other, { a, b -> a - b }, { super.minus(it) })

    /* Multiplication */

    override fun timesAssign(other: F64Array) = ebeInPlace(other, { a, b -> a * b }, { super.timesAssign(it) })

    override fun times(other: F64Array) = ebe(other, { a, b -> a * b }, { super.times(it) })

    /* Division */

    override fun divAssign(other: F64Array) = ebeInPlace(other, { a, b -> a / b }, { super.divAssign(it) })

    override fun div(other: F64Array) = ebe(other, { a, b -> a / b }, { super.div(it) })

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

    private inline fun nativeTransform(
        nativeOp: (DoubleArray, Int, DoubleArray, Int, Int) -> Boolean,
        superOp: F64FlatArray.() -> F64FlatArray
    ): F64FlatArray {
        val res = DoubleArray(size)
        if (nativeOp(res, 0, data, offset, size)) {
            return create(res, 0, size)
        }
        return superOp()
    }

    override fun expInPlace() {
        if (!NativeSpeedups.unsafeExp(data, offset, data, offset, size)) super.expInPlace()
    }

    override fun exp() = nativeTransform(NativeSpeedups::unsafeExp) { super.exp() }

    override fun expm1InPlace() {
        if (!NativeSpeedups.unsafeExpm1(data, offset, data, offset, size)) super.expm1InPlace()
    }

    override fun expm1() = nativeTransform(NativeSpeedups::unsafeExpm1) { super.expm1() }

    override fun logInPlace() {
        if (!NativeSpeedups.unsafeLog(data, offset, data, offset, size)) super.logInPlace()
    }

    override fun log() = nativeTransform(NativeSpeedups::unsafeLog) { super.log() }

    override fun log1pInPlace(){
        if (!NativeSpeedups.unsafeLog1p(data, offset, data, offset, size)) super.log1pInPlace()
    }

    override fun log1p() = nativeTransform(NativeSpeedups::unsafeLog1p) { super.log1p() }

    override fun logSumExp() = NativeSpeedups.unsafeLogSumExp(data, offset, size)

    override fun logAddExpAssign(other: F64Array) {
        if (other is F64LargeDenseArray) {
            checkShape(other)
            if (NativeSpeedups.unsafeLogAddExp(data, offset, data, offset, other.data, other.offset, size)) return
        }
        super.logAddExpAssign(other)
    }

    override fun logAddExp(other: F64Array): F64FlatArray {
        if (other is F64LargeDenseArray) {
            checkShape(other)
            val res = DoubleArray(size)
            if (NativeSpeedups.unsafeLogAddExp(res, 0, data, offset, other.data, other.offset, size)) {
                return create(res, 0, size)
            }
        }
        return super.logAddExp(other)
    }
}
