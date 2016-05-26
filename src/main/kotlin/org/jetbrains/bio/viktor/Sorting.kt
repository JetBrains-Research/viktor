package org.jetbrains.bio.viktor

import java.util.*

/**
 * Sorts the elements in this vector in in descending order.
 *
 * The operation is done **in place**.
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

/**
 * Partitions the vector.
 *
 * Rearranges the elements in this vector in such a way that
 * the [p]-th element moves to its position in the sorted copy
 * of the vector. All elements smaller than the [p]-th element
 * are moved before this element, and all elements greater or
 * equals to this element are moved behind it.
 *
 * The operation is done **in place**.
 *
 * @param p the index of the element to partition by.
 * @since 0.2.3
 */
fun StridedVector.partition(p: Int) {
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
internal fun StridedVector.partition(p: Int, left: Int, right: Int): Int {
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
internal inline fun StridedVector.swap(i: Int, j: Int) {
    val tmp = unsafeGet(i)
    unsafeSet(i, unsafeGet(j))
    unsafeSet(j, tmp)
}