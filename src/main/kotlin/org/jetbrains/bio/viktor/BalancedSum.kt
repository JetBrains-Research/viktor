package org.jetbrains.bio.viktor

/**
 * A summation algorithm balancing accuracy with throughput.
 *
 * References
 * ----------
 *
 * Dalton et al. "SIMDizing pairwise sums", 2014.
 */
internal fun StridedVector.balancedSum(): Double {
    var sumUnaligned = 0.0
    var remaining = size
    while (remaining % 4 > 0) {
        sumUnaligned += unsafeGet(--remaining)
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

    var sum = 0.0
    while (p > 0) {
        sum += stack[--p]
    }

    return sum + sumUnaligned
}