package org.jetbrains.bio.jni

object DoubleStat {

    init {
        Loader.ensureLoaded()
    }

    /**
     * Returns the (balanced) sum of `values` slice beginning at `offset` with length `length`.

     * @return The balanced sum calculated according to [this article](http://dl.acm.org/citation.cfm?id=2568070)
     * * on SIMDized sum calculations.
     */
    fun sum(values: DoubleArray, offset: Int = 0, length: Int = values.size): Double {
        checkOffsetAndLength(values, offset, length)
        return if (Loader.useNative)
            NativeSpeedups.sum(values, offset, length)
        else
            DoubleStatJava.sum(values, offset, length)
    }

    /**
     * Returns the arithmetic mean of `values` slice beginning at `offset` with length `length`.
     * The mean is calculated as [.sum] / `length`.
     */
    fun mean(values: DoubleArray, offset: Int = 0, length: Int = values.size): Double {
        checkOffsetAndLength(values, offset, length)
        if (length == 0) {
            return java.lang.Double.NaN
        }
        return sum(values, offset, length) / length
    }

    /**
     * Calculates the weighted sum of `values` and `weights` slices beginning at `valuesOffset`
     * and `weightsOffset` respectively with length `length`. The result is the same as calling
     * [.sum] on an array composed of values by weights products.
     */
    fun weightedSum(values: DoubleArray, valuesOffset: Int,
                    weights: DoubleArray, weightsOffset: Int, length: Int): Double {
        checkOffsetAndLength(values, valuesOffset, length)
        checkOffsetAndLength(weights, weightsOffset, length)
        return if (Loader.useNative)
            NativeSpeedups.weightedSum(values, valuesOffset, weights, weightsOffset, length)
        else
            DoubleStatJava.weightedSum(values, valuesOffset, weights, weightsOffset, length)
    }

    /**
     * Calculates the weighted sum of `values` and `weights`,
     * see [.weightedSum].
     */
    fun weightedSum(values: DoubleArray, weights: DoubleArray): Double {
        checkLength(values, weights)
        return weightedSum(values, 0, weights, 0, values.size)
    }

    /**
     * Calculates the weighted mean of `values` and `weights` slices beginning at `valuesOffset`
     * and `weightsOffset` respectively with length `length`. The result is the same as
     * [the weighted sum][.weightedSum] divided
     * by the [sum of weights][.sum].
     */
    fun weightedMean(values: DoubleArray, valuesOffset: Int,
                     weights: DoubleArray, weightsOffset: Int, length: Int): Double {
        checkOffsetAndLength(values, valuesOffset, length)
        checkOffsetAndLength(weights, weightsOffset, length)
        return if (Loader.useNative)
            NativeSpeedups.weightedMean(values, valuesOffset, weights, weightsOffset, length)
        else
            DoubleStatJava.weightedMean(values, valuesOffset, weights, weightsOffset, length)
    }

    /**
     * Calculates the weighted mean of `values` and `weights`,
     * see [.weightedMean].
     */
    fun weightedMean(values: DoubleArray, weights: DoubleArray): Double {
        checkLength(values, weights)
        return weightedMean(values, 0, weights, 0, values.size)
    }

    /**
     * Calculates the (unbiased) standard deviation of `values` slice beginning at `offset`
     * with length `length`.
     */
    fun standardDeviation(values: DoubleArray, offset: Int = 0, length: Int = values.size): Double {
        checkOffsetAndLength(values, offset, length)
        return if (Loader.useNative)
            NativeSpeedups.standardDeviation(values, offset, length)
        else
            DoubleStatJava.standardDeviation(values, offset, length)
    }

    /**
     * Calculates the biased standard deviation of `values` slice beginning at `offset`
     * with length `length`.
     */
    fun standardDeviationBiased(values: DoubleArray, offset: Int = 0, length: Int = values.size): Double {
        return Math.sqrt((length - 1) * 1.0 / length) * standardDeviation(values, offset, length)
    }

    /**
     * Calculates the biased weighted standard deviation of `values` and `weights` slices
     * beginning at `valuesOffset` and `weightsOffset` respectively with length `length`.
     * The result is defined as follows:
     * sqrt( sum(w_i * x_i^2) / sum(w_i) - (sum(w_i * x_i) / sum(w_i))^2 )
     * NB 1. If the value inside "sqrt" comes out negative due to precision failure, it is replaced by zero.
     * NB 2. There is no consensus on what an "unbiased weighted SD" might be, so it is not included in the library.
     */
    fun weightedStandardDeviationBiased(values: DoubleArray, valuesOffset: Int,
                                        weights: DoubleArray, weightsOffset: Int, length: Int): Double {
        return if (Loader.useNative)
            NativeSpeedups.weightedSD(values, valuesOffset, weights, weightsOffset, length)
        else
            DoubleStatJava.weightedStandardDeviation(values, valuesOffset, weights, weightsOffset, length)
    }

    /**
     * Calculates the biased weighted standard deviation of `values` and `weights`,
     * see [.weightedStandardDeviationBiased].
     */
    fun weightedStandardDeviationBiased(values: DoubleArray, weights: DoubleArray): Double {
        checkLength(values, weights)
        return weightedStandardDeviationBiased(values, 0, weights, 0, values.size)
    }

    /**
     * Returns a prefix sum array of `values`. The sums are calculated employing a modified Kahan-Babuska
     * algorithm for better precision, see [this article](http://cage.ugent.be/~klein/papers/floating-point.pdf)>
     * for details.
     * If the result is an array named `res`, then the following holds (up to precision errors):
     * `res[0] == values[0], res[1] == values[0] + values[1], ... res[values.length - 1] == sum(values)`
     */
    fun prefixSum(values: DoubleArray): DoubleArray {
        val res = DoubleArray(values.size)
        prefixSum(values, res)
        return res
    }

    /**
     * Writes the prefix sums of `source` values in the `dest` array,
     * see [.prefixSumInPlace].
     */
    fun prefixSum(source: DoubleArray, sourceOffset: Int,
                  dest: DoubleArray, destOffset: Int, length: Int) {
        checkOffsetAndLength(source, sourceOffset, length)
        checkOffsetAndLength(dest, destOffset, length)
        if (Loader.useNative) {
            NativeSpeedups.prefixSum(source, sourceOffset, dest, destOffset, length)
        } else {
            DoubleStatJava.prefixSum(source, sourceOffset, dest, destOffset, length)
        }
    }

    /**
     * Writes the prefix sums of `source` values in the `dest` array,
     * see [.prefixSumInPlace].
     */
    fun prefixSum(source: DoubleArray, dest: DoubleArray) {
        checkLength(source, dest)
        prefixSum(source, 0, dest, 0, source.size)
    }

    /**
     * Replaces the `values` by their prefix sums, see [.prefixSum].
     */
    fun prefixSumInPlace(values: DoubleArray) = prefixSum(values, values)
}

internal object DoubleStatJava {
    fun sum(values: DoubleArray, offset: Int, length: Int): Double {
        var unaligned_part = 0.0
        var effectiveArraySize = length
        while (effectiveArraySize % 4 != 0) {
            --effectiveArraySize
            unaligned_part += values[offset + effectiveArraySize]
        }

        val stack = DoubleArray(62)
        var p = 0
        run {
            var i = 0
            while (i < effectiveArraySize) {
                var v = values[offset + i] + values[offset + i + 1]
                val w = values[offset + i + 2] + values[offset + i + 3]

                v += w
                var bitmask = 4
                while (i and bitmask != 0) {
                    v += stack[p - 1]
                    bitmask = bitmask shl 1
                    --p
                }
                stack[p++] = v
                i += 4
            }
        }

        var vsum = 0.0
        for (i in p downTo 1) {
            vsum += stack[i - 1]
        }

        return vsum + unaligned_part
    }

    fun weightedSum(values: DoubleArray, valuesOffset: Int,
                    weights: DoubleArray, weightsOffset: Int, length: Int): Double {
        var unaligned_part = 0.0
        var effectiveArraySize = length
        while (effectiveArraySize % 4 != 0) {
            --effectiveArraySize
            unaligned_part += values[valuesOffset + effectiveArraySize] * weights[weightsOffset + effectiveArraySize]
        }

        val stack = DoubleArray(62)
        var p = 0
        run {
            var i = 0
            while (i < effectiveArraySize) {
                var v = values[valuesOffset + i] * weights[weightsOffset + i] + values[valuesOffset + i + 1] * weights[weightsOffset + i + 1]
                val w = values[valuesOffset + i + 2] * weights[weightsOffset + i + 2] + values[valuesOffset + i + 3] * weights[weightsOffset + i + 3]

                v += w
                var bitmask = 4
                while (i and bitmask != 0) {
                    v += stack[p - 1]
                    bitmask = bitmask shl 1
                    --p
                }
                stack[p++] = v
                i += 4
            }
        }

        var vsum = 0.0
        for (i in p downTo 1) {
            vsum += stack[i - 1]
        }

        return vsum + unaligned_part
    }

    fun weightedMean(values: DoubleArray, valuesOffset: Int,
                     weights: DoubleArray, weightsOffset: Int, length: Int): Double {
        var unaligned_part_vw = 0.0
        var unaligned_part_w = 0.0
        var effectiveArraySize = length
        while (effectiveArraySize % 4 != 0) {
            --effectiveArraySize
            unaligned_part_vw += values[valuesOffset + effectiveArraySize] * weights[weightsOffset + effectiveArraySize]
            unaligned_part_w += weights[weightsOffset + effectiveArraySize]
        }

        val stack_vw = DoubleArray(62)
        val stack_w = DoubleArray(62)
        var p = 0
        run {
            var i = 0
            while (i < effectiveArraySize) {
                var v_vw = values[valuesOffset + i] * weights[weightsOffset + i] + values[valuesOffset + i + 1] * weights[weightsOffset + i + 1]
                val w_vw = values[valuesOffset + i + 2] * weights[weightsOffset + i + 2] + values[valuesOffset + i + 3] * weights[weightsOffset + i + 3]
                var v_w = weights[weightsOffset + i] + weights[weightsOffset + i + 1]
                val w_w = weights[weightsOffset + i + 2] + weights[weightsOffset + i + 3]
                v_vw += w_vw
                v_w += w_w
                var bitmask = 4
                while (i and bitmask != 0) {
                    v_vw += stack_vw[p - 1]
                    v_w += stack_w[p - 1]
                    bitmask = bitmask shl 1
                    --p
                }
                stack_vw[p] = v_vw
                stack_w[p++] = v_w
                i += 4
            }
        }

        var vsum_vw = 0.0
        var vsum_w = 0.0
        for (i in p downTo 1) {
            vsum_vw += stack_vw[i - 1]
            vsum_w += stack_w[i - 1]
        }

        return (vsum_vw + unaligned_part_vw) / (vsum_w + unaligned_part_w)
    }

    fun standardDeviation(values: DoubleArray, offset: Int, length: Int): Double {
        var unaligned_part_vv = 0.0
        var unaligned_part_v = 0.0
        var effectiveArraySize = length
        while (effectiveArraySize % 4 != 0) {
            --effectiveArraySize
            val value = values[offset + effectiveArraySize]
            unaligned_part_vv += value * value
            unaligned_part_v += value
        }

        val stack_vv = DoubleArray(62)
        val stack_v = DoubleArray(62)
        var p = 0
        run {
            var i = 0
            while (i < effectiveArraySize) {
                var v_vv = values[offset + i] * values[offset + i] + values[offset + i + 1] * values[offset + i + 1]
                val w_vv = values[offset + i + 2] * values[offset + i + 2] + values[offset + i + 3] * values[offset + i + 3]
                var v_v = values[offset + i] + values[offset + i + 1]
                val w_v = values[offset + i + 2] + values[offset + i + 3]
                v_vv += w_vv
                v_v += w_v
                var bitmask = 4
                while (i and bitmask != 0) {
                    v_vv += stack_vv[p - 1]
                    v_v += stack_v[p - 1]
                    bitmask = bitmask shl 1
                    --p
                }
                stack_vv[p] = v_vv
                stack_v[p++] = v_v
                i += 4
            }
        }

        var vsum_vv = 0.0
        var vsum_v = 0.0
        for (i in p downTo 1) {
            vsum_vv += stack_vv[i - 1]
            vsum_v += stack_v[i - 1]
        }

        vsum_vv += unaligned_part_vv
        vsum_v += unaligned_part_v
        return Math.sqrt((vsum_vv - vsum_v * vsum_v / values.size) / (values.size - 1))
    }

    fun weightedStandardDeviation(values: DoubleArray, valuesOffset: Int,
                                  weights: DoubleArray, weightsOffset: Int, length: Int): Double {
        var unaligned_part_vvw = 0.0
        var unaligned_part_vw = 0.0
        var unaligned_part_w = 0.0
        var effectiveArraySize = length
        while (effectiveArraySize % 4 != 0) {
            --effectiveArraySize
            val value = values[valuesOffset + effectiveArraySize]
            val weight = weights[weightsOffset + effectiveArraySize]
            unaligned_part_vvw += value * value * weight
            unaligned_part_vw += value * weight
            unaligned_part_w += weight
        }

        val stack_vvw = DoubleArray(62)
        val stack_vw = DoubleArray(62)
        val stack_w = DoubleArray(62)
        var p = 0
        run {
            var i = 0
            while (i < effectiveArraySize) {
                var v_vvw = values[valuesOffset + i] * values[valuesOffset + i] * weights[weightsOffset + i] + values[valuesOffset + i + 1] * values[valuesOffset + i + 1] * weights[weightsOffset + i + 1]
                val w_vvw = values[valuesOffset + i + 2] * values[valuesOffset + i + 2] * weights[weightsOffset + i + 2] + values[valuesOffset + i + 3] * values[valuesOffset + i + 3] * weights[weightsOffset + i + 3]
                var v_vw = values[valuesOffset + i] * weights[weightsOffset + i] + values[valuesOffset + i + 1] * weights[weightsOffset + i + 1]
                val w_vw = values[valuesOffset + i + 2] * weights[weightsOffset + i + 2] + values[valuesOffset + i + 3] * weights[weightsOffset + i + 3]
                var v_w = weights[weightsOffset + i] + weights[weightsOffset + i + 1]
                val w_w = weights[weightsOffset + i + 2] + weights[weightsOffset + i + 3]
                v_vvw += w_vvw
                v_vw += w_vw
                v_w += w_w
                var bitmask = 4
                while (i and bitmask != 0) {
                    v_vvw += stack_vvw[p - 1]
                    v_vw += stack_vw[p - 1]
                    v_w += stack_w[p - 1]
                    bitmask = bitmask shl 1
                    --p
                }
                stack_vvw[p] = v_vvw
                stack_vw[p] = v_vw
                stack_w[p++] = v_w
                i += 4
            }
        }

        var vsum_vvw = 0.0
        var vsum_vw = 0.0
        var vsum_w = 0.0
        for (i in p downTo 1) {
            vsum_vvw += stack_vvw[i - 1]
            vsum_vw += stack_vw[i - 1]
            vsum_w += stack_w[i - 1]
        }

        vsum_vvw += unaligned_part_vvw
        vsum_vw += unaligned_part_vw
        vsum_w += unaligned_part_w
        return Math.sqrt(vsum_vvw / vsum_w - vsum_vw * vsum_vw / (vsum_w * vsum_w))
    }

    fun prefixSum(source: DoubleArray, sourceOffset: Int,
                  dest: DoubleArray, destOffset: Int, length: Int) {
        var accumulator = 0.0
        var compensator = 0.0
        for (i in 0..length - 1) {
            val v = source[sourceOffset + i]
            val newAccumulator = accumulator + v
            compensator += if (Math.abs(accumulator) >= Math.abs(v))
                accumulator - newAccumulator + v
            else
                v - newAccumulator + accumulator
            accumulator = newAccumulator
            dest[destOffset + i] = accumulator + compensator
        }
    }
}
