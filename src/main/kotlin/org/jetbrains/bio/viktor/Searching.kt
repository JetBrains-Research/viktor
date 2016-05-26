package org.jetbrains.bio.viktor

/**
 * Returns the insertion index of [target] into a sorted vector.
 *
 * If [target] already appears in this vector, the returned
 * index is just before the leftmost occurrence of [target].
 */
fun StridedVector.searchSorted(target: Double): Int {
    var lo = 0
    var hi = size
    while (lo < hi) {
        val mid = (lo + hi) ushr 1
        when {
            target <= this[mid] -> hi = mid
            else                -> lo = mid + 1
        }
    }

    return lo
}

/** Returns the index of the minimum element. */
fun StridedVector. argMin(): Int {
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

/** Returns the index of the maxmimum element. */
fun StridedVector.argMax(): Int {
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