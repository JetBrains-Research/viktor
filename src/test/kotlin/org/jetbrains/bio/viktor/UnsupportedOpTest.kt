package org.jetbrains.bio.viktor

import org.junit.Test
import java.lang.IllegalStateException
import java.lang.UnsupportedOperationException

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
    fun flatArrayReorder() {
        F64Array.of(1.0, 2.0, 3.0).reorder(intArrayOf(2, 1, 0), 1)
    }

}
