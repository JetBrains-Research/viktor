package org.jetbrains.bio.viktor

/**
 * Summation algorithms balancing accuracy with throughput.
 *
 * References
 * ----------
 *
 * Dalton et al. "SIMDizing pairwise sums", 2014.
 */

internal fun F64Vector.balancedSum(): Double {
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

internal inline fun F64Vector.balancedDot(getter: (Int) -> Double): Double {
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
        var v = (unsafeGet(i) * getter(i) +
                unsafeGet(i + 1) * getter(i + 1))
        val w = (unsafeGet(i + 2) * getter(i + 2) +
                unsafeGet(i + 3) * getter(i + 3))
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
