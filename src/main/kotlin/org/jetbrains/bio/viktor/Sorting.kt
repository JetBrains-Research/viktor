package org.jetbrains.bio.viktor

import java.util.*

/**
 * Sorts the elements in this 1-D array in in descending order.
 *
 * The operation is done **in place**.
 *
 * @param reverse if `true` the elements are sorted in `ascending` order.
 *                Defaults to `false`.
 */
fun F64Array.sort(reverse: Boolean = false) = reorder(argSort(reverse))

/**
 * Returns a permutation of indices which makes the 1-D array sorted.
 *
 * @param reverse see [sort] for details.
 */
fun F64Array.argSort(reverse: Boolean = false): IntArray {
    check1D(this)
    val comparator = Comparator(IndexedDoubleValue::compareTo)
    val indexedValues = Array(size) { IndexedDoubleValue(it, unsafeGet(it)) }
    indexedValues.sortWith(if (reverse) comparator.reversed() else comparator)
    return IntArray(size) { indexedValues[it].index }
}

/** A version of [IndexedValue] specialized to [Double]. */
private data class IndexedDoubleValue(val index: Int, val value: Double) :
        Comparable<IndexedDoubleValue> {
    override fun compareTo(other: IndexedDoubleValue): Int {
        val res = java.lang.Double.compare(value, other.value)
        return if (res != 0) {
            res
        } else {
            java.lang.Integer.compare(index, other.index)
        }
    }
}

/**
 * Partitions the array.
 *
 * Rearranges the elements in this array in such a way that
 * the [p]-th element moves to its position in the sorted copy
 * of the array. All elements smaller than the [p]-th element
 * are moved before this element, and all elements greater or
 * equals to this element are moved behind it.
 *
 * The operation is done **in place**.
 *
 * @param p the index of the element to partition by.
 * @since 0.2.3
 */
fun F64Array.partition(p: Int) {
    check1D(this)
    require(p >= 0 && p < size) { "p must be in [0, $size)" }
    partition(p, 0, size - 1)
}

/**
 * Helper [partition] extension.
 *
 * Invariants: p = partition(values, left, right, p)
 * for all i <  p: values[i] <  values[p]
 * for all i >= p: values[p] >= values[p]
 *
 * @param p the index of the element to partition by.
 * @param left start index (inclusive).
 * @param right end index (inclusive).
 */
internal fun F64Array.partition(p: Int, left: Int, right: Int): Int {
    val pivot = this[p]
    swap(p, right)  // move to end.

    var ptr = left
    for (i in left..right - 1) {
        if (this[i] < pivot) {
            swap(i, ptr)
            ptr++
        }
    }

    swap(right, ptr)
    return ptr
}

@Suppress("nothing_to_inline")
internal inline fun F64Array.swap(i: Int, j: Int) {
    val tmp = unsafeGet(i)
    unsafeSet(i, unsafeGet(j))
    unsafeSet(j, tmp)
}