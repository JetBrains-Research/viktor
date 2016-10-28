package org.jetbrains.bio.viktor

/**
 * Returns the insertion index of [target] into a sorted vector.
 *
 * If [target] already appears in this vector, the returned
 * index is just before the leftmost occurrence of [target].
 *
 * @since 0.2.3
 */
fun F64Array.searchSorted(target: Double): Int {
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