package org.jetbrains.bio.strided

import info.yeppp.Core
import org.apache.commons.math3.util.FastMath
import org.apache.commons.math3.util.Precision
import org.jetbrains.bio.jni.DoubleStat
import org.jetbrains.bio.jni.SIMDMath
import java.util.*

/**
 * Wrap a given array of elements. The array will not be copied.
 */
fun DoubleArray.asStrided() = StridedVector.create(this, 0, size(), 1)

/**
 * Strided floating point containers.
 *
 * @author Sergei Lebedev
 * @since 0.1.0
 */
open class StridedVector(protected val data: DoubleArray,
                         protected val offset: Int,
                         protected val size: Int,
                         private val stride: Int) {
    companion object {
        /**
         * Create a zero-filled vector of a given `size`.
         */
        operator fun invoke(size: Int) = create(DoubleArray(size), 0, size, 1)

        operator inline fun invoke(size: Int, block: (Int) -> Double): StridedVector {
            val v = this(size)
            for (i in 0..size - 1) {
                v[i] = block(i)
            }
            return v
        }

        /**
         * Create a vector with given elements.
         */
        @JvmStatic fun of(first: Double, vararg rest: Double): StridedVector {
            val data = DoubleArray(rest.size() + 1)
            data[0] = first
            System.arraycopy(rest, 0, data, 1, rest.size())
            return data.asStrided()
        }

        /**
         * Creates an array with elements summing to one.
         */
        @JvmStatic fun stochastic(size: Int): StridedVector {
            return full(size, 1.0 / size)
        }

        @JvmStatic fun full(size: Int, init: Double): StridedVector {
            val v = this(size)
            v.fill(init)
            return v
        }

        /**
         * Wrap a given array of elements. The array will not be copied.
         *
         * TODO: remove this once we get rid of all Java usages.
         */
        @JvmStatic fun wrap(data: DoubleArray): StridedVector {
            return create(data, 0, data.size(), 1)
        }

        fun create(data: DoubleArray, offset: Int, size: Int, stride: Int): StridedVector {
            require(offset + size <= data.size()) { "not enough data" }
            return if (stride == 1) {
                DenseVector.create(data, offset, size)
            } else {
                StridedVector(data, offset, size, stride)
            }
        }
    }

    inline val indices: IntRange get() = 0..size() - 1

    operator fun get(pos: Int): Double {
        try {
            return unsafeGet(pos)
        } catch (ignored: ArrayIndexOutOfBoundsException) {
            throw IndexOutOfBoundsException("index out of bounds: $pos")
        }
    }

    protected open fun unsafeIndex(pos: Int): Int = offset + pos * stride

    private fun unsafeGet(pos: Int): Double = data[unsafeIndex(pos)]

    operator fun set(pos: Int, value: Double) {
        try {
            unsafeSet(pos, value)
        } catch (ignored: ArrayIndexOutOfBoundsException) {
            throw IndexOutOfBoundsException("index out of bounds: $pos")
        }
    }

    private fun unsafeSet(pos: Int, value: Double) {
        data[unsafeIndex(pos)] = value
    }

    fun slice(from: Int, to: Int = size()): StridedVector {
        return StridedVector(data, offset + from, to - from, stride)
    }

    operator fun set(any: _, init: Double): Unit = fill(init)

    operator fun set(any: _, other: StridedVector): Unit = other.copyTo(this)

    open fun fill(init: Double) {
        for (pos in 0..size - 1) {
            unsafeSet(pos, init)
        }
    }

    fun reverse() {
        for (i in 0..size / 2 - 1) {
            val tmp = unsafeGet(size - 1 - i)
            unsafeSet(size - 1 - i, unsafeGet(i))
            unsafeSet(i, tmp)
        }
    }

    /** A useful shortcut for column-vector. */
    inline val T: StridedMatrix2 get() = transpose()

    fun transpose() = reshape(size, 1)

    fun copy(): StridedVector {
        val copy = StridedVector(size)
        copyTo(copy)
        return copy
    }

    open fun copyTo(other: StridedVector) {
        require(size == other.size)
        for (pos in 0..size - 1) {
            other.unsafeSet(pos, unsafeGet(pos))
        }
    }

    fun sort(reverse: Boolean = false) = reorder(sorted(reverse))

    @JvmOverloads fun sorted(reverse: Boolean = false): IntArray {
        // XXX we can do this more efficiently, if needed.
        val indexedValues = if (reverse) {
            toArray().withIndex().sortedByDescending { it.value }
        } else {
            toArray().withIndex().sortedBy { it.value }
        }

        val indices = IntArray(size())
        for (pos in 0..size - 1) {
            indices[pos] = indexedValues[pos].index
        }

        return indices
    }

    fun reorder(indices: IntArray) {
        require(size == indices.size())
        val copy = indices.clone()
        for (pos in 0..size - 1) {
            val value = unsafeGet(pos)
            var j = pos
            while (true) {
                val k = copy[j]
                copy[j] = j
                if (k == pos) {
                    unsafeSet(j, value)
                    break
                } else {
                    unsafeSet(j, unsafeGet(k))
                    j = k
                }
            }
        }
    }

    fun reshape(numRows: Int, numColumns: Int): StridedMatrix2 {
        require(numRows * numColumns == size)
        return StridedMatrix2(numRows, numColumns, offset, data,
                numColumns * stride, stride)
    }

    fun dot(other: ShortArray): Double {
        require(other.size() == size) { "non-conformable arrays" }
        var acc = 0.0
        for (pos in 0..size - 1) {
            acc += unsafeGet(pos) * other[pos].toDouble()
        }

        return acc
    }

    fun dot(other: IntArray): Double {
        require(other.size() == size) { "non-conformable arrays" }
        var acc = 0.0
        for (pos in 0..size - 1) {
            acc += unsafeGet(pos) * other[pos].toDouble()
        }

        return acc
    }

    open fun dot(other: DoubleArray): Double {
        require(other.size() == size) { "non-conformable arrays" }
        var acc = 0.0
        for (pos in 0..size - 1) {
            acc += unsafeGet(pos) * other[pos]
        }

        return acc
    }

    open fun sum(): Double {
        val acc = KahanSum.create()
        for (pos in 0..size - 1) {
            acc += unsafeGet(pos)
        }

        return acc.result()
    }

    open fun cumSum() {
        val acc = KahanSum.create()
        for (pos in 0..size - 1) {
            acc += unsafeGet(pos)
            unsafeSet(pos, acc.result())
        }
    }

    open fun min() = unsafeGet(argMin())

    fun argMin(): Int {
        require(size > 0) { "no data" }
        var minPos = 0
        var minValue = java.lang.Double.POSITIVE_INFINITY
        for (pos in 0..size - 1) {
            val value = unsafeGet(pos)
            if (value < minValue) {
                minPos = pos
                minValue = value
            }
        }

        return minPos
    }

    open fun max() = unsafeGet(argMax())

    fun argMax(): Int {
        require(size > 0) { "no data" }
        var maxPos = 0
        var maxValue = java.lang.Double.NEGATIVE_INFINITY
        for (pos in 0..size - 1) {
            val value = unsafeGet(pos)
            if (value > maxValue) {
                maxPos = pos
                maxValue = value
            }
        }

        return maxPos
    }

    fun exp(): StridedVector {
        val copy = copy()
        copy.expInPlace()
        return copy
    }

    open fun expInPlace() {
        for (pos in 0..size - 1) {
            unsafeSet(pos, FastMath.exp(unsafeGet(pos)))
        }
    }

    fun log(): StridedVector {
        val copy = copy()
        copy.logInPlace()
        return copy
    }

    open fun logInPlace() {
        for (pos in 0..size - 1) {
            unsafeSet(pos, Math.log(unsafeGet(pos)))
        }
    }

    fun rescale() {
        val total = sum() + Precision.EPSILON * size.toDouble()
        this *= 1.0 / total
    }

    open fun logRescale() {
        val logTotal = logSumExp()
        for (pos in 0..size - 1) {
            unsafeSet(pos, unsafeGet(pos) - logTotal)
        }
    }

    open fun logSumExp(): Double {
        val offset = max()
        val sum = KahanSum.create()
        for (pos in 0..size - 1) {
            sum += FastMath.exp(unsafeGet(pos) - offset)
        }

        return Math.log(sum.result()) + offset
    }

    fun logAddExp(other: StridedVector): StridedVector {
        val v = copy()
        v.logAddExp(other, v)
        return v
    }

    open fun logAddExp(other: StridedVector, dst: StridedVector) {
        checkSize(other)
        checkSize(dst)
        for (pos in 0..size - 1) {
            dst.unsafeSet(pos, MoreMath.logAddExp(unsafeGet(pos), other.unsafeGet(pos)))
        }
    }

    operator fun plus(other: StridedVector): StridedVector {
        val v = copy()
        v += other
        return v
    }

    operator open fun plusAssign(other: StridedVector) {
        checkSize(other)
        for (pos in 0..size - 1) {
            unsafeSet(pos, unsafeGet(pos) + other.unsafeGet(pos))
        }
    }

    operator fun plus(update: Double): StridedVector {
        val v = copy()
        v += update
        return v
    }

    operator open fun plusAssign(update: Double) {
        for (pos in 0..size - 1) {
            unsafeSet(pos, unsafeGet(pos) + update)
        }
    }

    operator fun times(value: Double): StridedVector {
        val v = copy()
        v *= value
        return v
    }

    operator open fun timesAssign(value: Double) {
        for (pos in 0..size - 1) {
            unsafeSet(pos, unsafeGet(pos) * value)
        }
    }

    fun size(): Int = size

    open fun toArray(): DoubleArray {
        val res = DoubleArray(size)
        for (pos in 0..size - 1) {
            res[pos] = unsafeGet(pos)
        }

        return res
    }

    /** Creates an iterator over the elements of the array. */
    operator fun iterator(): DoubleIterator = object : DoubleIterator() {
        var i = 0

        override fun hasNext(): Boolean = i < size

        override fun nextDouble(): Double = unsafeGet(i++)
    }

    fun toString(maxDisplay: Int): String {
        return if (size <= maxDisplay) {
            Arrays.toString(toArray())
        } else {
            val sb = StringBuilder()
            sb.append('[')
            for (pos in 0..maxDisplay - 1) {
                sb.append(this[pos])
                if (pos < maxDisplay - 1) {
                    sb.append(", ")
                }
            }
            sb.append(", ...]")
            sb.toString()
        }
    }

    override fun toString(): String = toString(8)

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        } else if (other !is StridedVector) {
            return false
        }

        if (size() != other.size()) {
            return false
        }

        for (pos in 0..size() - 1) {
            if (!Precision.equals(unsafeGet(pos), other.unsafeGet(pos))) {
                return false
            }
        }

        return true
    }

    override fun hashCode(): Int {
        var acc = 1
        for (pos in 0..size() - 1) {
            // XXX calling #hashCode results in boxing, see KT-7571.
            acc = 31 * java.lang.Double.hashCode(unsafeGet(pos))
        }

        return acc
    }

    protected fun checkSize(other: StridedVector) {
        require(size == other.size) { "non-conformable arrays" }
    }
}

/**
 * A contiguous strided vector.
 *
 * @author Sergei Lebedev
 * @since 0.1.0
 */
open class DenseVector protected constructor(data: DoubleArray, offset: Int, size: Int) :
        StridedVector(data, offset, size, 1) {
    override fun unsafeIndex(pos: Int): Int = offset + pos

    override fun fill(init: Double) {
        Arrays.fill(data, offset, offset + size, init)
    }

    override fun copyTo(other: StridedVector) {
        if (other is DenseVector) {
            checkSize(other)
            System.arraycopy(data, offset, other.data, other.offset, size)
        } else {
            super.copyTo(other)
        }
    }

    override fun toArray(): DoubleArray = data.copyOfRange(offset, offset + size)

    companion object {
        /**
         * We only use SIMD operations on vectors larger than the split boundary.
         */
        val DENSE_SPLIT_SIZE = 16

        fun create(data: DoubleArray, offset: Int, size: Int): DenseVector {
            return if (size <= DENSE_SPLIT_SIZE) {
                SmallDenseVector(data, offset, size)
            } else {
                LargeDenseVector(data, offset, size)
            }
        }
    }
}

/**
 * A contiguous strided vector of size at most [DenseVector.DENSE_SPLIT_SIZE].
 *
 * @author Sergei Lebedev
 * @since 0.1.0
 */
class SmallDenseVector(data: DoubleArray, offset: Int, size: Int) :
        DenseVector(data, offset, size)

/**
 * A contiguous vector of size at least `[DenseVector.DENSE_SPLIT_SIZE] + 1`.
 *
 * @author Sergei Lebedev
 * @since 0.1.0
 */
class LargeDenseVector(data: DoubleArray, offset: Int, size: Int) :
        DenseVector(data, offset, size) {

    override fun sum(): Double = DoubleStat.sum(data, offset, size)

    override fun cumSum() = DoubleStat.prefixSum(data, offset, data, offset, size)

    override fun min(): Double = Core.Min_V64f_S64f(data, offset, size)

    override fun max(): Double = Core.Max_V64f_S64f(data, offset, size)

    override fun dot(other: DoubleArray): Double {
        require(other.size() == size) { "non-conformable arrays" }
        return Core.DotProduct_V64fV64f_S64f(data, offset, other, 0, size)
    }

    override fun expInPlace() {
        info.yeppp.Math.Exp_V64f_V64f(data, 0, data, 0, data.size())
    }

    override fun logInPlace() {
        info.yeppp.Math.Log_V64f_V64f(data, 0, data, 0, data.size())
    }

    override fun logRescale() = SIMDMath.logRescale(data, offset, size)

    override fun logSumExp() = SIMDMath.logSumExp(data, offset, size)

    override fun logAddExp(other: StridedVector, dst: StridedVector) {
        if (other is DenseVector && dst is DenseVector) {
            checkSize(other)
            checkSize(dst)
            SIMDMath.logAddExp(data, offset, other.data, other.offset,
                    dst.data, dst.offset, size)
        } else {
            super.logAddExp(other, dst)
        }
    }

    override fun plusAssign(other: StridedVector) {
        if (other is DenseVector) {
            checkSize(other)
            Core.Add_V64fV64f_V64f(data, offset, other.data, other.offset,
                    data, offset, size)
        } else {
            super.plusAssign(other)
        }
    }

    override fun plusAssign(update: Double) {
        Core.Add_V64fS64f_V64f(data, offset, update, data, offset, size)
    }

    override fun timesAssign(value: Double) {
        Core.Multiply_IV64fS64f_IV64f(data, offset, value, size)
    }
}