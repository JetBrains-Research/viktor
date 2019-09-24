package org.jetbrains.bio.viktor

internal object NativeSpeedups {

    init {
        Loader.ensureLoaded()
    }

    external fun unsafePlusAssign(dst: DoubleArray, dstOffset: Int, src: DoubleArray, srcOffset: Int, length: Int)

    external fun unsafeMinusAssign(dst: DoubleArray, dstOffset: Int, src: DoubleArray, srcOffset: Int, length: Int)

    external fun unsafeTimesAssign(dst: DoubleArray, dstOffset: Int, src: DoubleArray, srcOffset: Int, length: Int)

    external fun unsafeDivAssign(dst: DoubleArray, dstOffset: Int, src: DoubleArray, srcOffset: Int, length: Int)

    external fun unsafeNegateInPlace(dst: DoubleArray, dstOffset: Int, length: Int)

    external fun unsafePlusScalarAssign(dst: DoubleArray, dstOffset: Int, length: Int, update: Double)

    external fun unsafeMinusScalarAssign(dst: DoubleArray, dstOffset: Int, length: Int, update: Double)

    external fun unsafeTimesScalarAssign(dst: DoubleArray, dstOffset: Int, length: Int, update: Double)

    external fun unsafeDivScalarAssign(dst: DoubleArray, dstOffset: Int, length: Int, update: Double)

    external fun unsafeScalarDivAssign(dst: DoubleArray, dstOffset: Int, length: Int, update: Double)

    external fun unsafeMin(values: DoubleArray, offset: Int, length: Int): Double

    external fun unsafeMax(values: DoubleArray, offset: Int, length: Int): Double

    external fun unsafeExpInPlace(dst: DoubleArray, dstOffset: Int, length: Int)

    external fun unsafeExpm1InPlace(dst: DoubleArray, dstOffset: Int, length: Int)

    external fun unsafeLogInPlace(dst: DoubleArray, dstOffset: Int, length: Int)

    external fun unsafeLog1pInPlace(dst: DoubleArray, dstOffset: Int, length: Int)

    external fun unsafeLogSumExp(src: DoubleArray, srcOffset: Int, length: Int): Double

    external fun unsafeLogAddExp(dst: DoubleArray, dstOffset: Int, src: DoubleArray, srcOffset: Int, length: Int)

    external fun unsafeDot(
            src1: DoubleArray,
            srcOffset1: Int,
            src2: DoubleArray,
            srcOffset2: Int,
            length: Int
    ): Double

    external fun sum(values: DoubleArray, offset: Int, length: Int): Double

    external fun weightedSum(
            values: DoubleArray,
            valuesOffset: Int,
            weights: DoubleArray,
            weightsOffset: Int,
            length: Int
    ): Double

    external fun weightedMean(
            values: DoubleArray,
            valuesOffset: Int,
            weights: DoubleArray,
            weightsOffset: Int,
            length: Int
    ): Double

    external fun sd(values: DoubleArray, offset: Int, length: Int): Double

    external fun cumSum(dest: DoubleArray, destOffset: Int, length: Int)
}
