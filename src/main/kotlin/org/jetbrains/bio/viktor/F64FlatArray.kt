package org.jetbrains.bio.viktor

import org.apache.commons.math3.util.FastMath
import org.apache.commons.math3.util.Precision
import java.text.DecimalFormat

/**
 * An 1-dimensional specialization of [F64Array].
 *
 * @since 0.4.0
 */
open class F64FlatArray protected constructor(data: DoubleArray, offset: Int,
                                              stride: Int, size: Int)
:
        F64Array(data, offset, intArrayOf(stride), intArrayOf(size)) {

    override fun flatten() = this

    /**
     * Constructs a column-vector view of this vector in O(1) time.
     *
     * A column vector is a matrix with [size] rows and a single column,
     * e.g. `[1, 2, 3]^T` is `[[1], [2], [3]]`.
     */
    override fun transpose() = reshape(size, 1)

    override fun contains(other: Double): Boolean {
        for (pos in 0..size - 1) {
            if (ix.unsafeGet(pos) == other) {
                return true
            }
        }

        return false
    }

    override fun copyTo(other: F64Array) {
        checkShape(other)
        for (pos in 0..size - 1) {
            other.ix.unsafeSet(pos, ix.unsafeGet(pos))
        }
    }

    override fun fill(init: Double) {
        for (pos in 0..size - 1) {
            ix.unsafeSet(pos, init)
        }
    }

    override fun dot(other: ShortArray) = balancedDot { other[it].toDouble() }

    override fun dot(other: IntArray) = balancedDot { other[it].toDouble() }

    override fun dot(other: F64Array) = balancedDot { other.ix[it] }

    /** See [balancedSum]. */
    private inline fun F64FlatArray.balancedDot(getter: (Int) -> Double): Double {
        var accUnaligned = 0.0
        var remaining = size
        while (remaining % 4 != 0) {
            remaining--
            accUnaligned += ix.unsafeGet(remaining) * getter(remaining)
        }

        val stack = DoubleArray(31 - 2)
        var p = 0
        var i = 0
        while (i < remaining) {
            // Shift.
            var v = (ix.unsafeGet(i) * getter(i) +
                     ix.unsafeGet(i + 1) * getter(i + 1))
            val w = (ix.unsafeGet(i + 2) * getter(i + 2) +
                     ix.unsafeGet(i + 3) * getter(i + 3))
            v += w

            // Reduce.
            var bitmask = 4
            while (i and bitmask != 0) {
                v += stack[--p]
                bitmask = bitmask shl 1
            }
            stack[p++] = v
            i += 4
        }

        var acc = 0.0
        while (p > 0) {
            acc += stack[--p]
        }
        return acc + accUnaligned
    }

    /**
     * Summation algorithm balancing accuracy with throughput.
     *
     * References
     * ----------
     *
     * Dalton et al. "SIMDizing pairwise sums", 2014.
     */
    override fun balancedSum(): Double {
        var accUnaligned = 0.0
        var remaining = size
        while (remaining % 4 > 0) {
            accUnaligned += ix.unsafeGet(--remaining)
        }

        val stack = DoubleArray(31 - 2)
        var p = 0
        var i = 0
        while (i < remaining) {
            // Shift.
            var v = ix.unsafeGet(i) + ix.unsafeGet(i + 1)
            val w = ix.unsafeGet(i + 2) + ix.unsafeGet(i + 3)
            v += w

            // Reduce.
            var bitmask = 4
            while (i and bitmask != 0) {
                v += stack[--p]
                bitmask = bitmask shl 1
            }
            stack[p++] = v
            i += 4
        }

        var acc = 0.0
        while (p > 0) {
            acc += stack[--p]
        }

        return acc + accUnaligned
    }

    override fun sum() = balancedSum()

    /**
     * Computes cumulative sum of the elements.
     *
     * The operation is done **in place**.
     */
    // TODO: generalize to n-d?
    open fun cumSum() {
        val acc = KahanSum()
        for (pos in 0..size - 1) {
            acc += ix.unsafeGet(pos)
            ix.unsafeSet(pos, acc.result())
        }
    }

    override fun min() = ix.unsafeGet(argMin())

    override fun argMin(): Int {
        require(size > 0) { "no data" }
        var minPos = 0
        var minValue = Double.POSITIVE_INFINITY
        for (pos in 0..size - 1) {
            val value = ix.unsafeGet(pos)
            if (value < minValue) {
                minPos = pos
                minValue = value
            }
        }

        return minPos
    }

    override fun max() = ix.unsafeGet(argMax())

    override fun argMax(): Int {
        require(size > 0) { "no data" }
        var maxPos = 0
        var maxValue = Double.NEGATIVE_INFINITY
        for (pos in 0..size - 1) {
            val value = ix.unsafeGet(pos)
            if (value > maxValue) {
                maxPos = pos
                maxValue = value
            }
        }

        return maxPos
    }

    override fun expInPlace() {
        for (pos in 0..size - 1) {
            ix.unsafeSet(pos, FastMath.exp(ix.unsafeGet(pos)))
        }
    }

    override fun expm1InPlace() {
        for (pos in 0..size - 1) {
            ix.unsafeSet(pos, FastMath.expm1(ix.unsafeGet(pos)))
        }
    }

    override fun logInPlace() {
        for (pos in 0..size - 1) {
            ix.unsafeSet(pos, Math.log(ix.unsafeGet(pos)))
        }
    }

    override fun log1pInPlace() {
        for (pos in 0..size - 1) {
            ix.unsafeSet(pos, Math.log1p(ix.unsafeGet(pos)))
        }
    }

    override fun logSumExp(): Double {
        val offset = max()
        val acc = KahanSum()
        for (pos in 0..size - 1) {
            acc += FastMath.exp(ix.unsafeGet(pos) - offset)
        }

        return Math.log(acc.result()) + offset
    }

    override fun logAddExp(other: F64Array, dst: F64Array) {
        checkShape(other)
        checkShape(dst)
        for (pos in 0..size - 1) {
            dst.ix.unsafeSet(pos, ix.unsafeGet(pos) logAddExp other.ix.unsafeGet(pos))
        }
    }

    override fun unaryMinus(): F64Array {
        // XXX 'v' is always dense but it might be too small to benefit
        //     from SIMD.
        val v = copy()
        for (pos in 0..size - 1) {
            v.ix.unsafeSet(pos, -ix.unsafeGet(pos))
        }

        return v
    }

    override fun plusAssign(other: F64Array) {
        checkShape(other)
        for (pos in 0..size - 1) {
            ix.unsafeSet(pos, ix.unsafeGet(pos) + other.ix.unsafeGet(pos))
        }
    }

    override fun plusAssign(update: Double) {
        for (pos in 0..size - 1) {
            ix.unsafeSet(pos, ix.unsafeGet(pos) + update)
        }
    }

    override fun minusAssign(other: F64Array) {
        checkShape(other)
        for (pos in 0..size - 1) {
            ix.unsafeSet(pos, ix.unsafeGet(pos) - other.ix.unsafeGet(pos))
        }
    }

    override fun minusAssign(update: Double) {
        for (pos in 0..size - 1) {
            ix.unsafeSet(pos, ix.unsafeGet(pos) - update)
        }
    }

    override fun timesAssign(other: F64Array) {
        checkShape(other)
        for (pos in 0..size - 1) {
            ix.unsafeSet(pos, ix.unsafeGet(pos) * other.ix.unsafeGet(pos))
        }
    }

    override fun timesAssign(update: Double) {
        for (pos in 0..size - 1) {
            ix.unsafeSet(pos, ix.unsafeGet(pos) * update)
        }
    }

    override fun divAssign(other: F64Array) {
        checkShape(other)
        for (pos in 0..size - 1) {
            ix.unsafeSet(pos, ix.unsafeGet(pos) / other.ix.unsafeGet(pos))
        }
    }

    override fun divAssign(update: Double) {
        for (pos in 0..size - 1) {
            ix.unsafeSet(pos, ix.unsafeGet(pos) / update)
        }
    }

    override fun toArray() = toDoubleArray()

    override fun toGenericArray() = unsupported()

    override fun toDoubleArray() = DoubleArray(size) { ix.unsafeGet(it) }

    /**
     * A version of [DecimalFormat.format] which doesn't produce ?
     * for [Double.NaN] and infinities.
     */
    private fun DecimalFormat.safeFormat(value: Double) = when {
        value.isNaN() -> "nan"
        value == Double.POSITIVE_INFINITY -> "inf"
        value == Double.NEGATIVE_INFINITY -> "-inf"
        else -> format(value)
    }

    override fun toString(maxDisplay: Int, format: DecimalFormat): String {
        val sb = StringBuilder()
        sb.append('[')

        if (maxDisplay < size) {
            for (pos in 0..maxDisplay / 2 - 1) {
                sb.append(format.safeFormat(ix[pos])).append(", ")
            }

            sb.append("..., ")

            val leftover = maxDisplay - maxDisplay / 2
            for (pos in size - leftover..size - 1) {
                sb.append(format.safeFormat(ix[pos]))
                if (pos < size - 1) {
                    sb.append(", ")
                }
            }
        } else {
            for (pos in 0..size - 1) {
                sb.append(format.safeFormat(ix[pos]))
                if (pos < size - 1) {
                    sb.append(", ")
                }
            }
        }

        sb.append(']')
        return sb.toString()
    }

    override fun equals(other: Any?) = when {
        this === other -> true
        other !is F64Array -> false
        size != other.size -> false
        else -> (0..size - 1).all {
            Precision.equals(ix.unsafeGet(it), other.ix.unsafeGet(it))
        }
    }

    override fun hashCode() = (0..size - 1).fold(1) { acc, pos ->
        // XXX calling #hashCode results in boxing, see KT-7571.
        31 * acc + java.lang.Double.hashCode(ix.unsafeGet(pos))
    }

    companion object {
        internal operator fun invoke(data: DoubleArray, offset: Int = 0,
                                     stride: Int = 1,
                                     size: Int = data.size): F64FlatArray {
            require(offset + (size - 1) * stride <= data.size) { "not enough data" }
            return if (stride == 1) {
                F64DenseFlatArray.create(data, offset, size)
            } else {
                F64FlatArray(data, offset, stride, size)
            }
        }
    }
}