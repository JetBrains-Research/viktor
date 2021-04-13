package org.jetbrains.bio.viktor

internal object NativeSpeedups {

    init {
        Loader.ensureLoaded()
    }

    external fun unsafeMin(values: DoubleArray, offset: Int, length: Int): Double

    external fun unsafeMax(values: DoubleArray, offset: Int, length: Int): Double

    external fun unsafeExp(dst: DoubleArray, dstOffset: Int, src: DoubleArray, srcOffset: Int, length: Int): Boolean

    external fun unsafeExpm1(dst: DoubleArray, dstOffset: Int, src: DoubleArray, srcOffset: Int, length: Int): Boolean

    external fun unsafeLog(dst: DoubleArray, dstOffset: Int, src: DoubleArray, srcOffset: Int, length: Int): Boolean

    external fun unsafeLog1p(dst: DoubleArray, dstOffset: Int, src: DoubleArray, srcOffset: Int, length: Int): Boolean

    external fun unsafeLogSumExp(src: DoubleArray, srcOffset: Int, length: Int): Double

    external fun unsafeLogAddExp(
        dst: DoubleArray,
        dstOffset: Int,
        src1: DoubleArray,
        srcOffset1: Int,
        src2: DoubleArray,
        srcOffset2: Int,
        length: Int
    ): Boolean

    external fun unsafeDot(
        src1: DoubleArray,
        srcOffset1: Int,
        src2: DoubleArray,
        srcOffset2: Int,
        length: Int
    ): Double

    external fun unsafeSum(values: DoubleArray, offset: Int, length: Int): Double

    external fun unsafeSD(values: DoubleArray, offset: Int, length: Int): Double

    external fun unsafeCumSum(dest: DoubleArray, destOffset: Int, length: Int): Boolean

    external fun unsafeFFT(dst: DoubleArray, dstOffset: Int, src: DoubleArray, srcOffset: Int, length: Int): Boolean

    external fun unsafeComplexTimes(
        dst: DoubleArray,
        dstOffset: Int,
        src1: DoubleArray,
        srcOffset1: Int,
        src2: DoubleArray,
        srcOffset2: Int,
        length: Int
    ): Boolean
}
