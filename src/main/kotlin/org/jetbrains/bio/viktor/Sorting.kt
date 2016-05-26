package org.jetbrains.bio.viktor

import java.util.*

/**
 * Sorts the elements in this vector in descending order.
 *
 * @param reverse if `true` the elements are sorted in `ascending` order.
 *                Defaults to `false`.
 */
fun StridedVector.sort(reverse: Boolean = false) = reorder(argSort(reverse))

/**
 * Returns a permutation of indices which makes the vector sorted.
 *
 * @param reverse see [.sort] for details.
 */
fun StridedVector.argSort(reverse: Boolean = false): IntArray {
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

/** Applies a given permutation of indices to the elements in the vector. */
fun StridedVector.reorder(indices: IntArray) {
    require(size == indices.size)
    val copy = indices.clone()
    for (pos in 0..size - 1) {
        val value = unsafeGet(pos)
        var j = pos
        while (true) {
            val k = copy[j]
            copy[j] = j
            if (k == pos) {
                unsafeSet(j, value)
                break
            } else {
                unsafeSet(j, unsafeGet(k))
                j = k
            }
        }
    }
}