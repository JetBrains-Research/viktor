package org.jetbrains.bio.jni

/**
 * Main entry point for SIMD optimized calculations.
 *
 * @author Alexey Dievsky
 * @date 2/9/15
 */
@SuppressWarnings("unused")
object DoubleMath {
    init {
        Loader.ensureLoaded()
    }

    fun exp(src: DoubleArray, srcOffset: Int,
            dst: DoubleArray, dstOffset: Int, length: Int) {
        checkOffsetAndLength(src, srcOffset, length)
        checkOffsetAndLength(dst, dstOffset, length)
        if (Loader.useNative) {
            NativeSpeedups.unsafeExp(src, srcOffset, dst, dstOffset, length)
        } else {
            DoubleMathJava.exp(src, srcOffset, dst, dstOffset, length)
        }
    }

    fun exp(src: DoubleArray, dst: DoubleArray) {
        checkLength(src, dst)
        exp(src, 0, dst, 0, src.size)
    }

    fun expm1(src: DoubleArray, srcOffset: Int,
              dst: DoubleArray, dstOffset: Int, length: Int) {
        checkOffsetAndLength(src, srcOffset, length)
        checkOffsetAndLength(dst, dstOffset, length)
        if (Loader.useNative) {
            NativeSpeedups.unsafeExpm1(src, srcOffset, dst, dstOffset, src.size)
        } else {
            DoubleMathJava.expm1(src, srcOffset, dst, dstOffset, length)
        }
    }

    fun expm1(src: DoubleArray, dst: DoubleArray) {
        checkLength(src, dst)
        expm1(src, 0, dst, 0, src.size)
    }

    fun log(src: DoubleArray, srcOffset: Int,
            dst: DoubleArray, dstOffset: Int, length: Int) {
        checkOffsetAndLength(src, srcOffset, length)
        checkOffsetAndLength(dst, dstOffset, length)
        if (Loader.useNative) {
            NativeSpeedups.unsafeLog(src, srcOffset, dst, dstOffset, src.size)
        } else {
            DoubleMathJava.log(src, srcOffset, dst, dstOffset, length)
        }
    }

    fun log(src: DoubleArray, dst: DoubleArray) {
        checkLength(src, dst)
        log(src, 0, dst, 0, src.size)
    }

    fun log1p(src: DoubleArray, srcOffset: Int,
              dst: DoubleArray, dstOffset: Int, length: Int) {
        checkOffsetAndLength(src, srcOffset, length)
        checkOffsetAndLength(dst, dstOffset, length)
        if (Loader.useNative) {
            NativeSpeedups.unsafeLog1p(src, srcOffset, dst, dstOffset, src.size)
        } else {
            DoubleMathJava.log1p(src, srcOffset, dst, dstOffset, length)
        }
    }

    fun log1p(src: DoubleArray, dst: DoubleArray) {
        checkLength(src, dst)
        log1p(src, 0, dst, 0, src.size)
    }

    fun logSumExp(src: DoubleArray, srcOffset: Int = 0, length: Int = src.size): Double {
        return if (Loader.useNative)
            NativeSpeedups.unsafeLogSumExp(src, srcOffset, length)
        else
            DoubleMathJava.logSumExp(src, srcOffset, length)
    }

    fun logAddExp(src1: DoubleArray, srcOffset1: Int,
                  src2: DoubleArray, srcOffset2: Int,
                  dst: DoubleArray, dstOffset: Int, length: Int) {
        if (Loader.useNative) {
            NativeSpeedups.unsafeLogAddExp(src1, srcOffset1, src2, srcOffset2,
                                           dst, dstOffset, length)
        } else {
            DoubleMathJava.logAddExp(src1, srcOffset1, src2, srcOffset2,
                                     dst, dstOffset, length)
        }
    }

    fun logAddExp(src1: DoubleArray, src2: DoubleArray,
                  dst: DoubleArray) {
        logAddExp(src1, 0, src2, 0, dst, 0, dst.size)
    }

    fun logRescale(src: DoubleArray, srcOffset: Int,
                   dst: DoubleArray, dstOffset: Int, length: Int) {
        if (Loader.useNative) {
            NativeSpeedups.unsafeLogRescale(src, srcOffset, dst, dstOffset, length)
        } else {
            checkOffsetAndLength(src, srcOffset, length)
            checkOffsetAndLength(dst, dstOffset, length)
            val logSum = DoubleMathJava.logSumExp(src, srcOffset, length)
            for (i in 0..length - 1) {
                dst[i + dstOffset] = src[i + srcOffset] - logSum
            }
        }
    }
}

internal object DoubleMathJava {

    fun exp(src: DoubleArray, srcOffset: Int,
            dst: DoubleArray, dstOffset: Int, length: Int) {
        for (i in 0..length - 1) {
            dst[dstOffset + i] = Math.exp(src[srcOffset + i])
        }
    }

    fun expm1(src: DoubleArray, srcOffset: Int,
              dst: DoubleArray, dstOffset: Int, length: Int) {
        for (i in 0..length - 1) {
            dst[dstOffset + i] = Math.expm1(src[srcOffset + i])
        }
    }

    fun log(src: DoubleArray, srcOffset: Int,
            dst: DoubleArray, dstOffset: Int, length: Int) {
        for (i in 0..length - 1) {
            dst[dstOffset + i] = Math.log(src[srcOffset + i])
        }
    }

    fun log1p(src: DoubleArray, srcOffset: Int,
              dst: DoubleArray, dstOffset: Int, length: Int) {
        for (i in 0..length - 1) {
            dst[dstOffset + i] = Math.log1p(src[srcOffset + i])
        }
    }

    fun max(src: DoubleArray, offset: Int, length: Int): Double {
        var res = java.lang.Double.NEGATIVE_INFINITY
        for (i in 0..length - 1) {
            if (src[i + offset] > res) {
                res = src[i + offset]
            }
        }
        return res
    }

    fun logSumExp(src: DoubleArray, srcOffset: Int, length: Int): Double {
        checkOffsetAndLength(src, srcOffset, length)
        val max = max(src, srcOffset, length)
        var res = 0.0
        for (i in 0..length - 1) {
            res += Math.exp(src[i + srcOffset] - max)
        }
        return Math.log(res) + max
    }

    fun logAddExp(a: Double, b: Double) = when {
        a.isInfinite() && a < 0 -> b
        b.isInfinite() && b < 0 -> a
        else -> Math.max(a, b) + Math.log1p(Math.exp(-Math.abs(a - b)))
    }

    fun logAddExp(src1: DoubleArray, src2: DoubleArray,
                  dst: DoubleArray) {
        logAddExp(src1, 0, src2, 0, dst, 0, dst.size)
    }

    fun logAddExp(src1: DoubleArray, srcOffset1: Int,
                  src2: DoubleArray, srcOffset2: Int,
                  dst: DoubleArray, dstOffset: Int, length: Int) {
        checkOffsetAndLength(src1, srcOffset1, length)
        checkOffsetAndLength(src2, srcOffset2, length)
        checkOffsetAndLength(dst, dstOffset, length)
        for (i in 0..length - 1) {
            dst[i + dstOffset] = logAddExp(src1[i + srcOffset1], src2[i + srcOffset2])
        }
    }

    fun logRescale(src: DoubleArray, srcOffset: Int,
                   dst: DoubleArray, dstOffset: Int, length: Int) {
        checkOffsetAndLength(src, srcOffset, length)
        checkOffsetAndLength(dst, dstOffset, length)
        val logSum = logSumExp(src, srcOffset, length)
        for (i in 0..length - 1) {
            dst[i + dstOffset] = src[i + srcOffset] - logSum
        }
    }
}
