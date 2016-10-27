@file:Suppress("nothing_to_inline")

package org.jetbrains.bio.viktor

import org.jetbrains.bio.viktor.NativeSpeedups.unsafeNegate
import org.jetbrains.bio.viktor.NativeSpeedups.unsafePlusScalar
import org.jetbrains.bio.viktor.NativeSpeedups.unsafeScalarDiv

/**
 * Operator overloads for [Double] and [F64Array].
 *
 * @since 0.2.2
 */

private inline fun Double.minusInPlace(other: F64Array) {
    if (other is F64LargeDenseArray) {
        unsafeNegate(other.data, other.offset,
                     other.data, other.offset, other.size)
        unsafePlusScalar(other.data, other.offset, this,
                         other.data, other.offset, other.size)
    } else {
        for (pos in 0..other.size - 1) {
            other.ix.unsafeSet(pos, this - other.ix.unsafeGet(pos))
        }
    }
}

operator fun Double.minus(other: F64Array): F64Array {
    val v = other.copy()
    minusInPlace(v.flatten())
    return v
}

inline operator fun Double.plus(other: F64Array) = other + this

inline operator fun Double.times(other: F64Array) = other * this

private inline fun Double.divInPlace(other: F64Array) {
    if (other is F64LargeDenseArray) {
        unsafeScalarDiv(this, other.data, other.offset,
                        other.data, other.offset, other.size)
    } else {
        for (pos in 0..other.size - 1) {
            other.ix.unsafeSet(pos, this / other.ix.unsafeGet(pos))
        }
    }
}

operator fun Double.div(other: F64Array): F64Array {
    val v = other.copy()
    divInPlace(v.flatten())
    return v
}