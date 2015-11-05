package org.jetbrains.bio.viktor

/**
 * @author Roman.Chernyatchik
 */
data internal class IndexedDoubleValue(val index: Int, val value: Double)
: Comparable<IndexedDoubleValue> {

    override fun compareTo(other: IndexedDoubleValue): Int {
        val res = java.lang.Double.compare(value, other.value)
        if (res != 0) {
            return res
        }
        return java.lang.Integer.compare(index, other.index)
    }
}