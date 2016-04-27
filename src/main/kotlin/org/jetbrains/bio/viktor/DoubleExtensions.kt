package org.jetbrains.bio.viktor

import info.yeppp.Core

/**
 * Operator overloads for [Double] and [StridedVector].
 *
 * @since 0.2.2
 */

operator fun Double.minus(other: StridedVector): StridedVector {
    val v = other.copy()
    if (v is LargeDenseVector) {
        Core.Subtract_S64fIV64f_IV64f(this, v.data, v.offset, v.size)
    } else {
        for (pos in 0..v.size - 1) {
            v.unsafeSet(pos, this - v.unsafeGet(pos))
        }
    }

    return v
}

operator fun Double.plus(other: StridedVector) = other + this

operator fun Double.times(other: StridedVector) = other * this

operator fun Double.div(other: StridedVector) = other / this
