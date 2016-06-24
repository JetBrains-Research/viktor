package org.jetbrains.bio.viktor

import org.apache.commons.math3.util.FastMath

/**
 * Evaluates log(exp(a) + exp(b)) using the following trick
 *
 *     log(exp(a) + log(exp(b)) = a + log(1 + exp(b - a))
 *
 * assuming a >= b.
 */
infix fun Double.logAddExp(b: Double): Double {
    val a = this
    return when {
        a.isInfinite() && a < 0 -> b
        b.isInfinite() && b < 0 -> a
        else -> Math.max(a, b) + StrictMath.log1p(FastMath.exp(-Math.abs(a - b)))
    }
}

/**
 * Kahan-Babuska summation.
 *
 * See http://cage.ugent.be/~klein/papers/floating-point.pdf for details.
 *
 * @author Alexey Dievsky
 * @since 0.1.0
 */
class KahanSum @JvmOverloads constructor(private var accumulator: Double = 0.0) {
    private var compensator = 0.0

    /** Supplies a number to be added to the accumulator. */
    fun feed(value: Double): KahanSum {
        val t = accumulator + value
        if (Math.abs(accumulator) >= Math.abs(value)) {
            compensator += (accumulator - t) + value
        } else {
            compensator += (value - t) + accumulator
        }

        accumulator = t
        return this
    }

    operator fun plusAssign(value: Double) {
        feed(value)  // Sweet, so sweet!
    }

    /** Returns the sum accumulated so far. */
    fun result() = accumulator + compensator
}
