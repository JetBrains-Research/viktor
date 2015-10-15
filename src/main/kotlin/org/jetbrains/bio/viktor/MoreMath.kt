package org.jetbrains.bio.viktor

import org.apache.commons.math3.util.FastMath

/**
 * Useful mathematical routines absent in [java.util.Math]
 * and [org.apache.commons.math3.util.FastMath].
 *
 * When adding new functionality please consider reading
 * http://blog.juma.me.uk/2011/02/23/performance-of-fastmath-from-commons-math.
 *
 * @author Alexey Dievsky
 * @author Sergei Lebedev
 * @since 0.1.0
 */
object MoreMath {
    /**
     * Evaluates log(exp(a) + exp(b)) using the following trick
     *
     *     log(exp(a) + log(exp(b)) = a + log(1 + exp(b - a))
     *
     * assuming a >= b.
     */
    @JvmStatic fun logAddExp(a: Double, b: Double): Double {
        return when {
            a.isInfinite() && a < 0 -> b
            b.isInfinite() && b < 0 -> a
            else -> Math.max(a, b) + StrictMath.log1p(FastMath.exp(-Math.abs(a - b)))
        }
    }
}

/**
 * This is a simple class which sums the numbers passed to it via the
 * [.feed] method and returns the accumulated sum with the [.get] call.
 * To decrease the precision loss when summing lots of numbers, the
 * class employs a modified Kahan-Babuska summation method described
 * in the Klein et al. paper.
 *
 * See http://cage.ugent.be/~klein/papers/floating-point.pdf for details.
 *
 * @author Alexey Dievsky
 * @since 0.1.0
 */
class KahanSum private constructor(private var accumulator: Double) {
    private var compensator = 0.0

    /**
     * Supplies a number to be added to the accumulator.
     * @param value
     */
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

    /**
     * Returns the sum accumulated so far.
     */
    fun result(): Double = accumulator + compensator

    companion object {
        /**
         * Creates and returns a zero-initiated accumulator which can be
         * fed doubles and polled for the accumulated sum.
         */
        @JvmStatic fun create(): KahanSum = create(0.0)

        /**
         * Creates and returns an accumulator which can be fed
         * doubles and polled for the accumulated sum.
         */
        @JvmStatic fun create(initial: Double): KahanSum = KahanSum(initial)
    }
}
