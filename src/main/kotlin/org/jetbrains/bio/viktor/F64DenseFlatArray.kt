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

    override fun clone(): F64DenseFlatArray = create(data.clone(), offset, size)

    private inline fun denseTransformInPlace(op: (Double) -> Double) {
        val dst = data
        var dstOffset = offset
        val dstEnd = dstOffset + size
        while (dstOffset < dstEnd) {
            dst[dstOffset] = op.invoke(dst[dstOffset])
            dstOffset++
        }
    }

    private inline fun denseTransform(op: (Double) -> Double): F64FlatArray {
        val dst = DoubleArray(size)
        val src = data
        var srcOffset = offset
        val length = size
        if (srcOffset == 0) {
            for (i in 0 until length) {
                dst[i] = op.invoke(src[i])
            }
        } else {
            for (i in 0 until length) {
                dst[i] = op.invoke(src[srcOffset])
                srcOffset++
            }
        }
        return create(dst, 0, size)
    }

    private inline fun denseEBEInPlace(other: F64DenseFlatArray, op: (Double, Double) -> Double) {
        val dst = data
        val src = other.data
        var dstOffset = offset
        var srcOffset = other.offset
        val length = size
        if (dstOffset == 0 && srcOffset == 0) {
            for (i in 0 until length) {
                dst[i] = op.invoke(dst[i], src[i])
            }
        } else {
            val dstEnd = dstOffset + length
            while (dstOffset < dstEnd) {
                dst[dstOffset] = op.invoke(dst[dstOffset], src[srcOffset])
                dstOffset++
                srcOffset++
            }
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
        val dst = DoubleArray(size)
        val src1 = data
        val src2 = other.data
        var src1Offset = offset
        var src2Offset = other.offset
        val length = size
        if (src1Offset == 0 && src2Offset == 0) {
            for (i in 0 until length) {
                dst[i] = op.invoke(src1[i], src2[i])
            }
        } else {
            for (i in 0 until length) {
                dst[i] = op.invoke(src1[src1Offset], src2[src2Offset])
                src1Offset++
                src2Offset++
            }
        }
        return create(dst, 0, size)
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

    /**
     * Performs complex assignment multiplication.
     *
     * Flat arrays are not interpretable as complex if they have more or less than two elements.
     * This internal function assumes that all checks and casts have already been performed.
     */
    override fun doComplexTimesAssign(other: F64FlatArray) {
        val dst = data
        val src = other.data
        var dstOffset = offset
        var srcOffset = other.offset
        val length = size / 2
        repeat(length) {
            val xRe = dst[dstOffset]
            dstOffset++
            val xIm = dst[dstOffset]
            val yRe = src[srcOffset]
            srcOffset++
            val yIm = src[srcOffset]
            srcOffset++
            dst[dstOffset - 1] = xRe * yRe - xIm * yIm
            dst[dstOffset] = xRe * yIm + xIm * yRe
            dstOffset++
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

    private inline fun nativeTransform(
        nativeOp: (DoubleArray, Int, DoubleArray, Int, Int) -> Boolean,
        superOp: F64FlatArray.() -> F64FlatArray
    ): F64FlatArray {
        val dst = DoubleArray(size)
        if (nativeOp(dst, 0, data, offset, size)) {
            return create(dst, 0, size)
        }
        return superOp()
    }

    override fun doComplexTimesAssign(other: F64FlatArray) {
        if (other is F64LargeDenseArray) {
            checkShape(other)
            if (NativeSpeedups.unsafeComplexTimes(data, offset, data, offset, other.data, other.offset, size)) return
        }
        super.doComplexTimesAssign(other)
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
