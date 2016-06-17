package org.jetbrains.bio.jni

object DoubleOpsNative {
    external fun criticalPlus(src1: DoubleArray, srcOffset1: Int,
                              src2: DoubleArray, srcOffset2: Int,
                              dst: DoubleArray, dstOffset: Int, length: Int)

    external fun criticalPlusScalar(src1: DoubleArray, srcOffset1: Int,
                                    update: Double,
                                    dst: DoubleArray, dstOffset: Int, length: Int)

    external fun criticalMinus(src1: DoubleArray, srcOffset1: Int,
                               src2: DoubleArray, srcOffset2: Int,
                               dst: DoubleArray, dstOffset: Int, length: Int)

    external fun criticalMinusScalar(src1: DoubleArray, srcOffset1: Int,
                                     update: Double,
                                     dst: DoubleArray, dstOffset: Int, length: Int)

    external fun criticalNegate(src1: DoubleArray, srcOffset1: Int,
                                dst: DoubleArray, dstOffset: Int, length: Int)

    external fun criticalTimes(src1: DoubleArray, srcOffset1: Int,
                               src2: DoubleArray, srcOffset2: Int,
                               dst: DoubleArray, dstOffset: Int, length: Int)

    external fun criticalTimesScalar(src1: DoubleArray, srcOffset1: Int,
                                     update: Double,
                                     dst: DoubleArray, dstOffset: Int, length: Int)

    external fun criticalDiv(src1: DoubleArray, srcOffset1: Int,
                             src2: DoubleArray, srcOffset2: Int,
                             dst: DoubleArray, dstOffset: Int, length: Int)

    external fun criticalDivScalar(src1: DoubleArray, srcOffset1: Int,
                                   update: Double,
                                   dst: DoubleArray, dstOffset: Int, length: Int)

    external fun unsafeMin(values: DoubleArray, offset: Int, length: Int): Double
    external fun unsafeMax(values: DoubleArray, offset: Int, length: Int): Double
}
