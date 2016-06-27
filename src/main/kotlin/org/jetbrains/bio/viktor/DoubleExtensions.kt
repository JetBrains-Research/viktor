@file:Suppress("nothing_to_inline")

package org.jetbrains.bio.viktor

import org.jetbrains.bio.viktor.NativeSpeedups.unsafeNegate
import org.jetbrains.bio.viktor.NativeSpeedups.unsafePlusScalar
import org.jetbrains.bio.viktor.NativeSpeedups.unsafeScalarDiv

/**
 * Operator overloads for [Double] and [StridedVector].
 *
 * @since 0.2.2
 */

private inline fun Double.minusInPlace(other: StridedVector) {
    if (other is LargeDenseVector) {
        unsafeNegate(other.data, other.offset,
                     other.data, other.offset, other.size)
        unsafePlusScalar(other.data, other.offset, this,
                         other.data, other.offset, other.size)
    } else {
        for (pos in 0..other.size - 1) {
            other.unsafeSet(pos, this - other.unsafeGet(pos))
        }
    }
}

operator fun Double.minus(other: StridedVector): StridedVector {
    val v = other.copy()
    minusInPlace(v)
    return v
}

operator fun <T : FlatMatrixOps<T>> Double.minus(other: T): T {
    val m = other.copy()
    minusInPlace(m.flatten())
    return m
}

inline operator fun Double.plus(other: StridedVector) = other + this

inline operator fun <T : FlatMatrixOps<T>> Double.plus(other: T) = other + this

inline operator fun Double.times(other: StridedVector) = other * this

inline operator fun <T : FlatMatrixOps<T>> Double.times(other: T) = other * this

private inline fun Double.divInPlace(other: StridedVector) {
    if (other is LargeDenseVector) {
        unsafeScalarDiv(this, other.data, other.offset,
                        other.data, other.offset, other.size)
    } else {
        for (pos in 0..other.size - 1) {
            other.unsafeSet(pos, this / other.unsafeGet(pos))
        }
    }
}

operator fun Double.div(other: StridedVector): StridedVector {
    val v = other.copy()
    divInPlace(v)
    return v
}

operator fun <T : FlatMatrixOps<T>> Double.div(other: T): T {
    val m = other.copy()
    divInPlace(m.flatten())
    return m
}