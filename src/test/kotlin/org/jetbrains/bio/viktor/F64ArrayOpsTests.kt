package org.jetbrains.bio.viktor

import org.apache.commons.math3.special.Gamma
import org.apache.commons.math3.stat.StatUtils
import org.apache.commons.math3.util.FastMath
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import org.junit.runners.Parameterized.Parameters
import java.util.*
import java.util.stream.DoubleStream
import java.util.stream.IntStream
import kotlin.math.sqrt
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

@RunWith(Parameterized::class)
class F64BasicArrayOperationTest(private val v: F64Array) {
    @Test fun contains() {
        v.asSequence().forEach {
            assertTrue(it in v)
        }
    }

    @Test fun equals() {
        assertEquals(v, v)
        assertEquals(v, v.copy())
        assertEquals(v.copy(), v)

        if (v.nDim == 1) {
            assertEquals(v, v.toDoubleArray().asF64Array())
        } else {
            assertEquals(v, v.toGenericArray().toF64Array())
        }

        assertNotEquals(v, gappedArray(2..4))
        assertNotEquals(v, gappedArray(1..30))
    }

    @Test fun fill() {
        val copy = v.copy()
        copy.fill(42.0)
        assertEquals(F64Array.full(*v.shape, init = 42.0), copy)
    }

    companion object {
        @Parameters(name = "{0}")
        @JvmStatic fun `data`() = CASES
    }
}

@RunWith(Parameterized::class)
class F64ArrayOperationTest(private val v: F64Array) {

    /* Unary array operations */

    private fun doTestUnary(
        copyOp: F64Array.() -> F64Array,
        inplaceOp: (F64Array.() -> Unit)?,
        scalarOp: (Double) -> Double,
        delta: Double,
        opName: String
    ) {
        val vCopyOp = v.checkConstant(copyOp)
        assertArrayEquals("$opName array shape", v.shape, vCopyOp.shape)
        v.asSequence().map(scalarOp).zip(vCopyOp.asSequence()).forEach { (e, a) ->
            assertEquals("$opName copy", e, a, delta)
        }
        if (inplaceOp != null) {
            val vInplaceOp = v.clone().apply(inplaceOp)
            assertEquals("$opName in-place", vCopyOp, vInplaceOp)
        }
    }

    @Test fun transform() = doTestUnary(
        { transform(Gamma::logGamma) }, { transformInPlace(Gamma::logGamma) }, Gamma::logGamma, EXACT_DELTA, "transform"
    )
    @Test fun exp() = doTestUnary(F64Array::exp, F64Array::expInPlace, FastMath::exp, DELTA, "exp")
    @Test fun expm1() = doTestUnary(F64Array::expm1, F64Array::expm1InPlace, FastMath::expm1, DELTA, "expm1")
    @Test fun log() = doTestUnary(F64Array::log, F64Array::logInPlace, Math::log, DELTA, "log")
    @Test fun log1p() = doTestUnary(F64Array::log1p, F64Array::log1pInPlace, Math::log1p, DELTA, "log1p")
    @Test fun unaryMinus() = doTestUnary(
        F64Array::unaryMinus, { transformInPlace { -it } }, Double::unaryMinus, EXACT_DELTA, "unaryMinus"
    )

    /* Binary array operations */

    private fun doTestBinary(
        copyOp: F64Array.(F64Array) -> F64Array,
        inplaceOp: F64Array.(F64Array) -> Unit,
        scalarOp: (Double, Double) -> Double,
        commutative: Boolean,
        delta: Double,
        opName: String
    ) {
        val random = Random()
        val other = DoubleArray(v.shape.product()) { random.nextDouble() }.asF64Array().reshape(*v.shape)
        val voCopy = v.checkConstant(other, copyOp)
        val ovCopy = other.checkConstant(v, copyOp)
        val voInPlace = v.clone().apply { inplaceOp(other) }
        val ovInPlace = other.clone().apply { inplaceOp(v) }
        val voActual = v.asSequence().zip(other.asSequence()).map { (a, b) ->  scalarOp(a, b) }
        val ovActual = other.asSequence().zip(v.asSequence()).map { (a, b) ->  scalarOp(a, b) }

        voCopy.asSequence().zip(voActual).forEach { (a, e) ->
            assertEquals("v $opName other (copy)", e, a, delta)
        }
        ovCopy.asSequence().zip(ovActual).forEach { (a, e) ->
            assertEquals("other $opName v (copy)", e, a, delta)
        }

        assertEquals("v $opName other (in-place)", voCopy, voInPlace)
        assertEquals("other $opName v (in-place)", ovCopy, ovInPlace)

        if (commutative) {
            assertEquals("v $opName other != other $opName v", voCopy, ovCopy)
        }
    }
    
    @Test fun plus() = doTestBinary(
        F64Array::plus, F64Array::plusAssign, Double::plus, true, EXACT_DELTA, "plus"
    )
    @Test fun minus() = doTestBinary(
        F64Array::minus, F64Array::minusAssign, Double::minus, false, EXACT_DELTA, "minus"
    )
    @Test fun times() = doTestBinary(
        F64Array::times, F64Array::timesAssign, Double::times, true, EXACT_DELTA, "times"
    )
    @Test fun div() = doTestBinary(
        F64Array::div, F64Array::divAssign, Double::div, false, EXACT_DELTA, "div"
    )
    @Test fun logAddExp() = doTestBinary(
        F64Array::logAddExp, F64Array::logAddExpAssign, Double::logAddExp, true, DELTA, "logAddExp"
    )

    /* Binary array-scalar operations */
    
    private fun doTestBinaryArrayScalar(
        copyOp: (F64Array, Double) -> F64Array,
        inplaceOp: (F64Array, Double) -> Unit,
        scalarOp: (Double, Double) -> Double,
        delta: Double,
        opName: String
    ) {
        val scalar = 42.0
        doTestUnary({ copyOp(this, scalar) }, { inplaceOp(this, scalar) }, { scalarOp(it, scalar) }, delta, opName)
    }

    private fun doTestBinaryScalarArray(
        copyOp: (Double, F64Array) -> F64Array,
        scalarOp: (Double, Double) -> Double,
        delta: Double,
        opName: String
    ) {
        val scalar = 42.0
        doTestUnary({ copyOp(scalar, this) }, null, { scalarOp(scalar, it) }, delta, opName)
    }
    
    private fun doTestBinaryScalar(
        asCopyOp: (F64Array, Double) -> F64Array,
        saCopyOp: (Double, F64Array) -> F64Array,
        asInplaceOp: (F64Array, Double) -> Unit,
        scalarOp: (Double, Double) -> Double,
        commutative: Boolean,
        delta: Double,
        opName: String
    ) {
        doTestBinaryArrayScalar(asCopyOp, asInplaceOp, scalarOp, delta, opName)
        doTestBinaryScalarArray(saCopyOp, scalarOp, delta, opName)
        if (commutative) {
            assertEquals("$opName (scalar) should be commutative", asCopyOp(v, 42.0), saCopyOp(42.0, v))
        }
    }
    
    @Test fun plusScalar() = doTestBinaryScalar(
        F64Array::plus, Double::plus, F64Array::plusAssign, Double::plus,
        true, EXACT_DELTA, "plusScalar"
    )
    @Test fun minusScalar() = doTestBinaryScalar(
        F64Array::minus, Double::minus, F64Array::minusAssign, Double::minus,
        false, EXACT_DELTA, "plusScalar"
    )
    @Test fun timesScalar() = doTestBinaryScalar(
        F64Array::times, Double::times, F64Array::timesAssign, Double::times,
        true, EXACT_DELTA, "timesScalar"
    )
    @Test fun divScalar() = doTestBinaryScalar(
        F64Array::div, Double::div, F64Array::divAssign, Double::div,
        false, EXACT_DELTA, "plusScalar"
    )

    /* Fold (reduce) array operations */
    
    private fun doTestFold(
        arrayOp: (F64Array) -> Double,
        scalarOp: (Double, Double) -> Double,
        initial: Double,
        delta: Double,
        opName: String
    ) {
        val actual = v.checkConstant(arrayOp)
        val expected = v.asSequence().fold(initial, scalarOp)
        assertEquals(opName, expected, actual, delta)
    }
    
    @Test fun max() = doTestFold(F64Array::max, Math::max, Double.NEGATIVE_INFINITY, EXACT_DELTA, "max")
    @Test fun min() = doTestFold(F64Array::min, Math::min, Double.POSITIVE_INFINITY, EXACT_DELTA, "min")
    @Test fun sum() = doTestFold(F64Array::sum, Double::plus, 0.0, DELTA, "plus")
    @Test fun logSumExp() = doTestFold(
        F64Array::logSumExp, Double::logAddExp, Double.NEGATIVE_INFINITY, DELTA, "logSumExp"
    )

    /* Other array operations */

    @Test fun unaryPlus() = assertEquals(v, v.checkConstant { unaryPlus() })

    @Test fun rescale() = assertEquals(1.0, v.clone().apply { rescale() }.sum(), DELTA)

    @Test fun logRescale() = assertEquals(0.0, v.clone().apply { logRescale() }.logSumExp(), DELTA)

    @Test fun mean() =
        assertEquals(v.asSequence().sum() / v.shape.product(), v.checkConstant(F64Array::mean), DELTA)

    companion object {
        @Parameters(name = "{0}")
        @JvmStatic fun `data`() = CASES
    }
}

@RunWith(Parameterized::class)
class F64FlatArrayOperationTest(private val v: F64FlatArray) {
    /* Flat array operations */

    @Test fun argMax() = assertEquals(v.max(), v[v.checkConstant { argMax() }], EXACT_DELTA)

    @Test fun argMin() = assertEquals(v.min(), v[v.checkConstant { argMin() }], EXACT_DELTA)

    @Test fun sd() = assertEquals(sqrt(StatUtils.variance(v.toDoubleArray())), v.checkConstant(F64Array::sd), DELTA)

    @Test fun cumSum() {
        val actual = v.clone().apply { cumSum() }
        val acc = KahanSum()
        for (i in 0 until v.size) {
            acc.feed(v[i])
            assertEquals(acc.result(), actual[i], DELTA)
        }
    }

    @Test fun dot() {
        val random = Random()
        val other = DoubleArray(v.size) { random.nextDouble() }.asF64Array()
        val voActual = v.checkConstant(other, F64Array::dot)
        val ovActual = other.checkConstant(v, F64Array::dot)
        val voExpected = v.asSequence().zip(other.asSequence()).map { (a, b) ->  a * b }.sum()
        val ovExpected = other.asSequence().zip(v.asSequence()).map { (a, b) ->  b * a }.sum()
        assertEquals("v dot other (copy)", voExpected, voActual, DELTA)
        assertEquals("other dot v (copy)", ovExpected, ovActual, DELTA)
        assertEquals("v dot other != other dot v", voActual, ovActual, DELTA)
    }

    companion object {
        @Parameters(name = "{0}")
        @JvmStatic fun `data`() = CASES.filterIsInstance<F64FlatArray>()
    }
}

/**
 * Choosing a suitable delta for double comparison is tricky. Some of the operations being tested
 * have slightly different implementation (like exponent or logarithm), some are executed out-of-order
 * (like sum), so you can't always expect the results to be exactly the same.
 */
private const val DELTA = 2E-14
private const val EXACT_DELTA = 0.0

private const val LARGE_SIZE = F64DenseFlatArray.DENSE_SPLIT_SIZE + 1

private val CASES = listOf(
    // Gapped.
    gappedArray(1..3),
    // Gapped large.
    gappedArray(1..LARGE_SIZE),
    // Dense small.
    doubleArrayOf(1.0, 2.0, 3.0).asF64Array(),
    // Dense large.
    Random().doubles(LARGE_SIZE.toLong()).toArray().asF64Array(),
    // Dense large subarray.
    Random().doubles(3L * LARGE_SIZE).toArray()
            .asF64Array(LARGE_SIZE, LARGE_SIZE),
    // Non-flattenable array.
    Random().doubles(4L * 3 * LARGE_SIZE).toArray().asF64Array()
        .reshape(4, 3, LARGE_SIZE).view(1, 1)
)

internal fun gappedArray(r: IntRange): F64Array {
    // The NaN gaps are there for two reasons:
    //
    // 1. to ensure 'offset' and 'stride' are used correctly,
    // 2. to force the use of fallback implementation.
    val values = IntStream.range(r.first, r.last + 1)
            .mapToDouble(Int::toDouble)
            .flatMap { DoubleStream.of(Double.NaN, it) }
            .toArray()
    return F64FlatArray.create(values, offset = 1, size = r.last + 1 - r.first, stride = 2)
}

private fun <R> F64Array.checkConstant(copyOp: F64Array.() -> R): R {
    val copy = copy()
    val res = copyOp()
    assertEquals("copying operation changed the array", copy, this)
    return res
}

private fun <R> F64Array.checkConstant(other: F64Array, copyOp: F64Array.(F64Array) -> R): R {
    val copy = copy()
    val res = copyOp(other)
    assertEquals("copying operation changed the array", copy, this)
    return res
}