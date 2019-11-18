package org.jetbrains.bio.viktor

import org.apache.commons.math3.util.FastMath
import org.apache.commons.math3.util.Precision
import java.text.DecimalFormat
import kotlin.math.ln
import kotlin.math.ln1p

/**
 * An 1-dimensional specialization of [F64Array].
 *
 * @since 0.4.0
 */
open class F64FlatArray protected constructor(
        data: DoubleArray,
        offset: Int,
        stride: Int,
        size: Int
) : F64Array(data, offset, intArrayOf(stride), intArrayOf(size)) {

    override val isFlattenable get() = true

    override fun flatten() = this

    override fun contains(other: Double): Boolean {
        for (pos in 0 until size) {
            if (unsafeGet(pos) == other) {
                return true
            }
        }

        return false
    }

    override fun along(axis: Int) = unsupported()

    override fun copyTo(other: F64Array) {
        checkShape(other)
        for (pos in 0 until size) {
            other.unsafeSet(pos, unsafeGet(pos))
        }
    }

    override fun fill(init: Double) {
        for (pos in 0 until size) {
            unsafeSet(pos, init)
        }
    }

    override fun reorder(indices: IntArray, axis: Int) {
        if (axis == 0) {
            reorderInternal(this, indices, axis,
                get = { pos -> unsafeGet(pos) },
                set = { pos, value -> unsafeSet(pos, value) })
        } else {
            unsupported()
        }
    }

    override fun dot(other: ShortArray) = balancedDot { other[it].toDouble() }

    override fun dot(other: IntArray) = balancedDot { other[it].toDouble() }

    override fun dot(other: F64Array) = balancedDot { other[it] }

    /** See [sum]. */
    private inline fun balancedDot(getter: (Int) -> Double): Double {
        var accUnaligned = 0.0
        var remaining = size
        while (remaining % 4 != 0) {
            remaining--
            accUnaligned += unsafeGet(remaining) * getter(remaining)
        }

        val stack = DoubleArray(31 - 2)
        var p = 0
        var i = 0
        while (i < remaining) {
            // Shift.
            var v = unsafeGet(i) * getter(i) + unsafeGet(i + 1) * getter(i + 1)
            val w = unsafeGet(i + 2) * getter(i + 2) + unsafeGet(i + 3) * getter(i + 3)
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
    override fun sum(): Double {
        var accUnaligned = 0.0
        var remaining = size
        while (remaining % 4 > 0) {
                accUnaligned += unsafeGet(--remaining)
            }
        val stack = DoubleArray(31 - 2)
        var p = 0
        var i = 0
        while (i < remaining) {
                // Shift.
                var v = unsafeGet(i) + unsafeGet(i + 1)
                val w = unsafeGet(i + 2) + unsafeGet(i + 3)
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

    override fun cumSum() {
        val acc = KahanSum()
        for (pos in 0 until size) {
            acc += unsafeGet(pos)
            unsafeSet(pos, acc.result())
        }
    }


    override fun min() = unsafeGet(argMin())

    override fun argMin(): Int {
        var minValue = Double.POSITIVE_INFINITY
        var res = 0
        for (pos in 0 until size) {
            val value = unsafeGet(pos)
            if (value <= minValue) {
                minValue = value
                res = pos
            }
        }
        return res
    }

    override fun max() = unsafeGet(argMax())

    override fun argMax(): Int {
        var maxValue = Double.NEGATIVE_INFINITY
        var res = 0
        for (pos in 0 until size) {
            val value = unsafeGet(pos)
            if (value >= maxValue) {
                maxValue = value
                res = pos
            }
        }
        return res
    }

    override fun expInPlace() {
        for (pos in 0 until size) {
            unsafeSet(pos, FastMath.exp(unsafeGet(pos)))
        }
    }

    override fun expm1InPlace() {
        for (pos in 0 until size) {
            unsafeSet(pos, FastMath.expm1(unsafeGet(pos)))
        }
    }

    override fun logInPlace() {
        for (pos in 0 until size) {
            unsafeSet(pos, ln(unsafeGet(pos)))
        }
    }

    override fun log1pInPlace() {
        for (pos in 0 until size) {
            unsafeSet(pos, ln1p(unsafeGet(pos)))
        }
    }

    override fun logSumExp(): Double {
        val offset = max()
        val acc = KahanSum()
        for (pos in 0 until size) {
            acc += FastMath.exp(unsafeGet(pos) - offset)
        }

        return ln(acc.result()) + offset
    }

    override fun logAddExpAssign(other: F64Array) {
        for (pos in 0 until size) {
            unsafeSet(pos, unsafeGet(pos) logAddExp other.unsafeGet(pos))
        }
    }

    override fun unaryMinus(): F64Array {
        val v = copy()
        for (pos in 0 until size) {
            v.unsafeSet(pos, -unsafeGet(pos))
        }

        return v
    }

    override fun plusAssign(other: F64Array) {
        checkShape(other)
        for (pos in 0 until size) {
            unsafeSet(pos, unsafeGet(pos) + other.unsafeGet(pos))
        }
    }

    override fun plusAssign(update: Double) {
        for (pos in 0 until size) {
            unsafeSet(pos, unsafeGet(pos) + update)
        }
    }

    override fun minusAssign(other: F64Array) {
        checkShape(other)
        for (pos in 0 until size) {
            unsafeSet(pos, unsafeGet(pos) - other.unsafeGet(pos))
        }
    }

    override fun minusAssign(update: Double) {
        for (pos in 0 until size) {
            unsafeSet(pos, unsafeGet(pos) - update)
        }
    }

    override fun timesAssign(other: F64Array) {
        checkShape(other)
        for (pos in 0 until size) {
            unsafeSet(pos, unsafeGet(pos) * other.unsafeGet(pos))
        }
    }

    override fun timesAssign(update: Double) {
        for (pos in 0 until size) {
            unsafeSet(pos, unsafeGet(pos) * update)
        }
    }

    override fun divAssign(other: F64Array) {
        checkShape(other)
        for (pos in 0 until size) {
            unsafeSet(pos, unsafeGet(pos) / other.unsafeGet(pos))
        }
    }

    override fun divAssign(update: Double) {
        for (pos in 0 until size) {
            unsafeSet(pos, unsafeGet(pos) / update)
        }
    }

    override fun reshape(vararg shape: Int): F64Array {
        shape.forEach { require(it > 0) { "Shape must be positive but was $it" } }
        check(shape.product() == size) {
            "total size of the new array must be unchanged"
        }
        return when {
            this.shape.contentEquals(shape) -> this
            else -> {
                val reshaped = shape.clone()
                reshaped[reshaped.lastIndex] = strides.single()
                for (i in reshaped.lastIndex - 1 downTo 0) {
                    reshaped[i] = reshaped[i + 1] * shape[i + 1]
                }

                invoke(data, offset, reshaped, shape)
            }
        }
    }

    override fun asSequence(): Sequence<Double> = (0 until size).asSequence().map(this::unsafeGet)

    override fun toArray() = toDoubleArray()

    override fun toGenericArray() = unsupported()

    override fun toDoubleArray() = DoubleArray(size) { unsafeGet(it) }

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
            for (pos in 0 until maxDisplay / 2) {
                sb.append(format.safeFormat(this[pos])).append(", ")
            }

            sb.append("..., ")

            val leftover = maxDisplay - maxDisplay / 2
            for (pos in size - leftover until size) {
                sb.append(format.safeFormat(this[pos]))
                if (pos < size - 1) {
                    sb.append(", ")
                }
            }
        } else {
            for (pos in 0 until size) {
                sb.append(format.safeFormat(this[pos]))
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
        other !is F64FlatArray -> false // an instance of F64Array can't be flat
        size != other.size -> false
        else -> (0 until size).all {
            Precision.equals(unsafeGet(it), other.unsafeGet(it))
        }
    }

    override fun hashCode() = (0 until size).fold(1) { acc, pos ->
        // XXX calling #hashCode results in boxing, see KT-7571.
        31 * acc + java.lang.Double.hashCode(unsafeGet(pos))
    }

    companion object {
        internal operator fun invoke(
                data: DoubleArray,
                offset: Int = 0,
                stride: Int = 1,
                size: Int = data.size
        ): F64FlatArray {
            // require(offset + (size - 1) * stride < data.size) { "not enough data" }
            // this check is not needed since we control all invocations of this internal method
            return if (stride == 1) {
                F64DenseFlatArray.create(data, offset, size)
            } else {
                F64FlatArray(data, offset, stride, size)
            }
        }
    }
}
