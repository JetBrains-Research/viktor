package org.jetbrains.bio.jni

object DoubleStat {
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
            NativeSpeedups.weightedSd(values, valuesOffset, weights, weightsOffset, length)
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
}

internal object DoubleStatJava {
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
}
