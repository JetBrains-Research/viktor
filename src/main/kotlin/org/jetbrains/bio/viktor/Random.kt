package org.jetbrains.bio.viktor

import org.apache.commons.math3.random.MersenneTwister
import org.apache.commons.math3.random.RandomGenerator

private val DEFAULT_RANDOM = MersenneTwister()

/**
 * Randomized linear-time selection algorithm.
 *
 * See https://en.wikipedia.org/wiki/Quickselect.
 *
 * @since 0.2.0
 */
internal object QuickSelect {
    /**
     * Returns the n-th order statistic of a given array.
     *
     * Invariant:  left <= n <= right
     */
    tailrec fun select(values: StridedVector,
                       left: Int, right: Int, n: Int,
                       randomGenerator: RandomGenerator): Double {
        assert(left <= n && n <= right)

        if (left == right) {
            return values[left]
        }

        var split = left + randomGenerator.nextInt(right - left + 1)
        split = partition(values, left, right, split)
        return when {
            split == n -> values[n]
            split > n  -> select(values, left, split - 1, n, randomGenerator)
            else -> select(values, split + 1, right, n, randomGenerator)
        }
    }

    /**
     * Partitions values around the pivot.
     *
     * Invariants: p = partition(values, left, right, p)
     * for all i <  p: values[i] <  values[p]
     * for all i >= p: values[p] >= values[p]
     */
    fun partition(values: StridedVector, left: Int, right: Int, p: Int): Int {
        val pivot = values[p]
        values.swap(p, right)  // move to end.

        var ptr = left
        for (i in left..right - 1) {
            if (values[i] < pivot) {
                values.swap(i, ptr)
                ptr++
            }
        }

        values.swap(right, ptr)
        return ptr
    }
}

private fun StridedVector.swap(i: Int, j: Int) {
    val tmp = unsafeGet(i)
    unsafeSet(i, unsafeGet(j))
    unsafeSet(j, tmp)
}

/**
 * Computes the [q]-th order statistic over this vector.
 *
 * The implementation follows that of Commons Math. See JavaDoc of
 * [Percentile] for computational details.
 *
 * The vector is modified in-place. Do a [copy] of the vector
 * to avoid mutation if necessary.
 *
 * @since 0.2.0
 */
fun StridedVector.quantile(q: Double = 0.5,
                           randomGenerator: RandomGenerator = DEFAULT_RANDOM): Double {
    require(isNotEmpty()) { "no data" }
    val pos = (size + 1) * q
    val d = pos - Math.floor(pos)
    return when {
        pos < 1     -> min()
        pos >= size -> max()
        else -> {
            val lo = QuickSelect.select(
                    this, 0, size - 1, pos.toInt() - 1, randomGenerator)
            val hi = QuickSelect.select(
                    this, 0, size - 1, pos.toInt(), randomGenerator)
            return lo + d * (hi - lo)
        }
    }
}

/**
 * Randomly permutes the elements of this vector.
 *
 * See https://en.wikipedia.org/wiki/Fisher%E2%80%93Yates_shuffle.
 *
 * @since 0.2.0
 */
fun StridedVector.shuffle(randomGenerator: RandomGenerator = DEFAULT_RANDOM) {
    if (size <= 1) {
        return
    }

    for (i in 0..size - 2) {
        val j = randomGenerator.nextInt(size - i)
        swap(i, i + j)
    }
}