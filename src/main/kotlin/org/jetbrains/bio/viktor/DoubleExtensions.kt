@file:Suppress("nothing_to_inline")

package org.jetbrains.bio.viktor

import org.jetbrains.bio.viktor.NativeSpeedups.unsafeNegateInPlace
import org.jetbrains.bio.viktor.NativeSpeedups.unsafePlusScalarAssign
import org.jetbrains.bio.viktor.NativeSpeedups.unsafeScalarDivAssign

/**
 * Operator overloads for [Double] and [F64Array].
 *
 * @since 0.2.2
 */

private inline fun Double.minusInPlace(other: F64Array) {
    if (other is F64LargeDenseArray) {
        unsafeNegateInPlace(other.data, other.offset, other.size)
        unsafePlusScalarAssign(other.data, other.offset, other.size, this)
    } else {
        for (pos in 0 until other.size) {
            other.unsafeSet(pos, this - other.unsafeGet(pos))
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
        unsafeScalarDivAssign(other.data, other.offset, other.size, this)
    } else {
        for (pos in 0 until other.size) {
            other.unsafeSet(pos, this / other.unsafeGet(pos))
        }
    }
}

operator fun Double.div(other: F64Array): F64Array {
    val v = other.copy()
    divInPlace(v.flatten())
    return v
}