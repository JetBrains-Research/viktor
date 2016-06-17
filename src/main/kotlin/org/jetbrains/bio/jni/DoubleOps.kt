package org.jetbrains.bio.jni

object DoubleOpsNative {
    external fun unsafePlus(src1: DoubleArray, srcOffset1: Int,
                              src2: DoubleArray, srcOffset2: Int,
                              dst: DoubleArray, dstOffset: Int, length: Int)

    external fun unsafePlusScalar(src1: DoubleArray, srcOffset1: Int,
                                    update: Double,
                                    dst: DoubleArray, dstOffset: Int, length: Int)

    external fun unsafeMinus(src1: DoubleArray, srcOffset1: Int,
                               src2: DoubleArray, srcOffset2: Int,
                               dst: DoubleArray, dstOffset: Int, length: Int)

    external fun unsafeMinusScalar(src1: DoubleArray, srcOffset1: Int,
                                     update: Double,
                                     dst: DoubleArray, dstOffset: Int, length: Int)

    external fun unsafeNegate(src1: DoubleArray, srcOffset1: Int,
                                dst: DoubleArray, dstOffset: Int, length: Int)

    external fun unsafeTimes(src1: DoubleArray, srcOffset1: Int,
                               src2: DoubleArray, srcOffset2: Int,
                               dst: DoubleArray, dstOffset: Int, length: Int)

    external fun unsafeTimesScalar(src1: DoubleArray, srcOffset1: Int,
                                     update: Double,
                                     dst: DoubleArray, dstOffset: Int, length: Int)

    external fun unsafeDiv(src1: DoubleArray, srcOffset1: Int,
                             src2: DoubleArray, srcOffset2: Int,
                             dst: DoubleArray, dstOffset: Int, length: Int)

    external fun unsafeDivScalar(src1: DoubleArray, srcOffset1: Int,
                                   update: Double,
                                   dst: DoubleArray, dstOffset: Int, length: Int)

    external fun unsafeMin(values: DoubleArray, offset: Int, length: Int): Double
    external fun unsafeMax(values: DoubleArray, offset: Int, length: Int): Double
}
