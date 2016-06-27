package org.jetbrains.bio.viktor

object NativeSpeedups {
    init {
        Loader.ensureLoaded()
    }

    external fun unsafePlus(src1: DoubleArray, srcOffset1: Int,
                            src2: DoubleArray, srcOffset2: Int,
                            dst: DoubleArray, dstOffset: Int, length: Int)

    external fun unsafeMinus(src1: DoubleArray, srcOffset1: Int,
                             src2: DoubleArray, srcOffset2: Int,
                             dst: DoubleArray, dstOffset: Int, length: Int)

    external fun unsafeTimes(src1: DoubleArray, srcOffset1: Int,
                             src2: DoubleArray, srcOffset2: Int,
                             dst: DoubleArray, dstOffset: Int, length: Int)

    external fun unsafeDiv(src1: DoubleArray, srcOffset1: Int,
                           src2: DoubleArray, srcOffset2: Int,
                           dst: DoubleArray, dstOffset: Int, length: Int)

    external fun unsafeNegate(src1: DoubleArray, srcOffset1: Int,
                              dst: DoubleArray, dstOffset: Int, length: Int)

    external fun unsafePlusScalar(src1: DoubleArray, srcOffset1: Int, update: Double,
                                  dst: DoubleArray, dstOffset: Int, length: Int)

    external fun unsafeMinusScalar(src1: DoubleArray, srcOffset1: Int, update: Double,
                                   dst: DoubleArray, dstOffset: Int, length: Int)

    external fun unsafeTimesScalar(src1: DoubleArray, srcOffset1: Int, update: Double,
                                   dst: DoubleArray, dstOffset: Int, length: Int)

    external fun unsafeDivScalar(src1: DoubleArray, srcOffset1: Int, update: Double,
                                 dst: DoubleArray, dstOffset: Int, length: Int)

    external fun unsafeScalarDiv(update: Double, src1: DoubleArray, srcOffset1: Int,
                                 dst: DoubleArray, dstOffset: Int, length: Int)

    external fun unsafeMin(values: DoubleArray, offset: Int, length: Int): Double

    external fun unsafeMax(values: DoubleArray, offset: Int, length: Int): Double

    external fun unsafeExp(src: DoubleArray, srcOffset: Int,
                           dst: DoubleArray, dstOffset: Int, length: Int)

    external fun unsafeExpm1(src: DoubleArray, srcOffset: Int,
                             dst: DoubleArray, dstOffset: Int, length: Int)

    external fun unsafeLog(src: DoubleArray, srcOffset: Int,
                           dst: DoubleArray, dstOffset: Int, length: Int)

    external fun unsafeLog1p(src: DoubleArray, srcOffset: Int,
                             dst: DoubleArray, dstOffset: Int, length: Int)

    external fun unsafeLogSumExp(src: DoubleArray, srcOffset: Int, length: Int): Double

    external fun unsafeLogAddExp(
            src1: DoubleArray, srcOffset1: Int,
            src2: DoubleArray, srcOffset2: Int,
            dst: DoubleArray, dstOffset: Int, length: Int)

    external fun unsafeLogRescale(
            src: DoubleArray, srcOffset: Int,
            dst: DoubleArray, dstOffset: Int, length: Int)

    external fun unsafeDot(src1: DoubleArray, srcOffset1: Int,
                           src2: DoubleArray, srcOffset2: Int, length: Int): Double

    external fun sum(values: DoubleArray, offset: Int, length: Int): Double

    external fun weightedSum(values: DoubleArray, valuesOffset: Int,
                             weights: DoubleArray, weightsOffset: Int, length: Int): Double

    external fun weightedMean(values: DoubleArray, valuesOffset: Int,
                              weights: DoubleArray, weightsOffset: Int, length: Int): Double

    external fun sd(values: DoubleArray, offset: Int, length: Int): Double

    external fun cumSum(source: DoubleArray, sourceOffset: Int,
                        dest: DoubleArray, destOffset: Int, length: Int)
}
