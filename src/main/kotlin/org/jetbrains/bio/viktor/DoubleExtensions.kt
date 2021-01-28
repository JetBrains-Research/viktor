@file:Suppress("nothing_to_inline")

package org.jetbrains.bio.viktor

/**
 * Operator overloads for [Double] and [F64Array].
 *
 * @since 0.2.2
 */

inline operator fun Double.minus(other: F64Array): F64Array = other.transform { this - it }

inline operator fun Double.plus(other: F64Array) = other + this

inline operator fun Double.times(other: F64Array) = other * this

inline operator fun Double.div(other: F64Array): F64Array = other.transform { this / it }