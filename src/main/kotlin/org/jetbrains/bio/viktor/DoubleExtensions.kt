package org.jetbrains.bio.viktor

import org.jetbrains.bio.jni.DoubleOpsNative

/**
 * Operator overloads for [Double] and [StridedVector].
 *
 * @since 0.2.2
 */

operator fun Double.minus(other: StridedVector): StridedVector {
    val v = other.copy()
    if (v is LargeDenseVector) {
        DoubleOpsNative.criticalNegate(v.data, v.offset, v.data, v.offset, v.size)
        DoubleOpsNative.criticalPlusScalar(
                v.data, v.offset, this, v.data, v.offset, v.size)
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
