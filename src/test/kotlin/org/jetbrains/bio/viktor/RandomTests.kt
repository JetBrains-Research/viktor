package org.jetbrains.bio.viktor

import org.apache.commons.math3.stat.StatUtils
import org.apache.commons.math3.util.CombinatoricsUtils
import org.apache.commons.math3.util.Precision
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import org.junit.runners.Parameterized.Parameters
import java.util.*

class QuickSelectTest {
    @Test fun quantileRandom() {
        val values = Random().doubles(1024).toArray().asF64Array()
        for (i in 0 until values.size) {
            val q = (i.toDouble() + 1) / values.size
            assertEquals(StatUtils.percentile(values.data, q * 100),
                         values.quantile(q), Precision.EPSILON)
        }
    }

    @Test(expected = IllegalArgumentException::class) fun quantileEmpty() {
        doubleArrayOf().asF64Array().quantile(0.5)
    }

    @Test fun quantileSingleton() {
        val values = doubleArrayOf(42.0).asF64Array()
        assertEquals(42.0, values.quantile(0.0), Precision.EPSILON)
        assertEquals(42.0, values.quantile(0.6), Precision.EPSILON)
        assertEquals(42.0, values.quantile(1.0), Precision.EPSILON)
    }

    @Test fun quantileLarge() {
        val values = Random().doubles(1 shl 16).toArray().asF64Array()
        assertEquals(values.max(), values.quantile(1.0), Precision.EPSILON)
        assertEquals(values.min(), values.quantile(0.0), Precision.EPSILON)
        assertEquals(values.quantile(0.5), values.quantile(), Precision.EPSILON)
    }
}

@RunWith(Parameterized::class)
class QuickSelectAgainstR(private val q: Double,
                          private val expected: Double) {
    private val values = doubleArrayOf(
            -0.488945108002711, 0.873275572589175, 0.847986479658537,
            -1.59886444167727, 0.249354016468346, -0.689076738071403,
            0.0543457714544607, -0.0692308243756736, -1.25989203795668,
            0.382121372812074, -0.009470319810494, -1.8054972148897,
            0.067861610814957, 0.468446958114462, 0.224353680059208,
            -0.245225307498896, 0.535946008237022, 0.415292903147929,
            0.891363596131771, -0.729720196710022, 0.163686611195071,
            -0.146685146346365, -0.854292328562661, 0.56457235804922,
            -0.722260803213915, -0.174921113221112, -1.07241799924636,
            -0.522823322920814, 0.885244973408295, -0.346516108433445,
            -1.24309985605676, -0.750971331806947, -0.0623048403054132,
            1.08201477922608, -0.350437623032381, 1.60138339981255,
            -0.269227746609915, 0.567394813999463, -0.994662221866329,
            0.872948172503996, -0.413080049221564, -1.09568693094285,
            2.85855335947479, -0.366668919549065, -0.689957269702223,
            -0.732641734640687, 0.892014735418404, 0.493166028474261,
            0.25428368704835, -0.676160478225966, 0.997101813953564,
            0.168415857016069, -0.464640453789841, 0.732682558827146,
            -0.299061308130272, -0.305758044447607, 0.47688050755864,
            -0.323423915437573, -1.07443801087424, 1.01833105965806,
            0.106573790832308, -0.0368140390438757, -1.22410154910053,
            -0.623308734987264).asF64Array()

    // R uses a different approximation scheme, so we can only compare
    // the first to significant digits.
    @Test fun agreement() = assertEquals(expected, values.quantile(q), 1e-2)

    companion object {
        @JvmStatic
        @Parameters(name = "{0}")
        fun `data`() = listOf(
                arrayOf(0.0, -1.8054972),
                arrayOf(0.25, -0.6793895),
                arrayOf(0.5, -0.1079580),
                arrayOf(0.75, 0.4809519),
                arrayOf(1.0, 2.8585534))
    }
}

class ShuffleTest {
    @Test fun distribution() {
        val values = doubleArrayOf(0.0, 1.0, 2.0, 3.0).asF64Array()
        val counts = HashMap<F64Array, Int>()

        val nFactorial = CombinatoricsUtils.factorial(4).toInt()
        for (i in 0..5000 * nFactorial) {
            values.shuffle()
            val p = values.copy()
            counts[p] = (counts[p] ?: 0) + 1
        }

        assertEquals(nFactorial, counts.size)

        val total = counts.values.sum()
        for (count in counts.values) {
            val p = count.toDouble() / total
            assertEquals(1.0 / nFactorial, p, 1e-2)
        }
    }

    @Test fun trivial() {
        val values = F64Array.of(3.14)
        val stored = values.copy()
        values.shuffle()
        assertEquals(stored, values)
    }
}