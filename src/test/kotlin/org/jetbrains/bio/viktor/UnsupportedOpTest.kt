package org.jetbrains.bio.viktor

import org.junit.Test

class UnsupportedOpTest {

    @Test(expected = UnsupportedOperationException::class)
    fun invalidArray() {
        arrayOf("foo", "bar").toF64Array()
    }

    @Test(expected = IllegalStateException::class)
    fun emptyArray() {
        emptyArray<DoubleArray>().toF64Array()
    }

    @Test(expected = UnsupportedOperationException::class)
    fun flatArrayToGeneric() {
        F64Array.of(1.0, 2.0, 3.0).toGenericArray()
    }

    @Test(expected = UnsupportedOperationException::class)
    fun flatArrayAlong() {
        F64Array.of(1.0, 2.0, 3.0).along(0)
    }

    @Test(expected = UnsupportedOperationException::class)
    fun flatArrayView() {
        F64Array.of(1.0, 2.0, 3.0).view(0, 0)
    }

    @Test(expected = UnsupportedOperationException::class)
    fun flatArrayReorder() {
        F64Array.of(1.0, 2.0, 3.0).reorder(intArrayOf(2, 1, 0), 1)
    }

    @Test(expected = IllegalArgumentException::class)
    fun indicesSizeReorder() {
        F64Array.of(1.0, 2.0, 3.0).reorder(intArrayOf(1, 0))
    }

    @Test(expected = IllegalStateException::class)
    fun mismatchedArray1() {
        arrayOf(doubleArrayOf(1.0), doubleArrayOf(2.0, 3.0)).toF64Array()
    }

    @Test(expected = IllegalStateException::class)
    fun mismatchedArray2() {
        arrayOf(
            arrayOf(doubleArrayOf(1.0)),
            arrayOf(doubleArrayOf(2.0), doubleArrayOf(3.0))
        ).toF64Array()
    }

    @Test(expected = IllegalStateException::class)
    fun mixedTypeArray1() {
        arrayOf(doubleArrayOf(1.0), arrayOf(doubleArrayOf(2.0, 3.0))).toF64Array()
    }

    @Test(expected = IllegalStateException::class)
    fun mixedTypeArray2() {
        arrayOf(arrayOf(doubleArrayOf(1.0, 2.0)), doubleArrayOf(3.0)).toF64Array()
    }

    @Test(expected = IllegalStateException::class)
    fun nullsArray1() {
        arrayOf(doubleArrayOf(1.0, 2.0, 3.0), null).toF64Array()
    }

    @Test(expected = IllegalStateException::class)
    fun nullsArray2() {
        arrayOf(arrayOf(doubleArrayOf(1.0, 2.0, 3.0)), null).toF64Array()
    }

    @Test(expected = IllegalStateException::class)
    fun nonFlattenable() {
        val m = F64Array.full(init = 42.0, shape = intArrayOf(2, 3, 2)).V[_I, 1]
        m.flatten()
    }

}
