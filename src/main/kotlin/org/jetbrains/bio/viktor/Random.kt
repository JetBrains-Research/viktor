package org.jetbrains.bio.viktor

import org.apache.commons.math3.random.MersenneTwister
import org.apache.commons.math3.random.RandomGenerator

private val DEFAULT_RANDOM = MersenneTwister()

/**
 * Randomized linear-time selection algorithm.
 *
 * See https://en.wikipedia.org/wiki/Quickselect.
 */
object QuickSelect {
    /**
     * Returns the n-th order statistic of a given array.
     *
     * Invariant:  left <= n <= right
     */
    tailrec fun select(values: StridedVector, left: Int, right: Int, n: Int,
                       randomGenerator: RandomGenerator): Double {
        assert(left <= n && n <= right)

        if (left == right) {
            return values[left]
        }

        var split = left + randomGenerator.nextInt(right - left + 1)
        split = partition(values, left, right, split)
        return when {
            split == n -> values[n]
            split > n -> select(values, left, split - 1, n, randomGenerator)
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
 * Computes [q]-th order statistic over this vector.
 *
 * Partially sorts the vector **in-place**. Please copy the vector
 * prior to calling the method to avoid mutation.
 */
fun StridedVector.quantile(q: Double = 0.5,
                           randomGenerator: RandomGenerator = DEFAULT_RANDOM): Double {
    require(isNotEmpty()) { "no data" }
    val n = Math.ceil(q * (size - 1).toDouble()).toInt()
    return QuickSelect.select(this, 0, size - 1, n, randomGenerator)
}

/**
 * Randomly permutes the elements of this vector.
 *
 * See https://en.wikipedia.org/wiki/Fisher%E2%80%93Yates_shuffle.
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