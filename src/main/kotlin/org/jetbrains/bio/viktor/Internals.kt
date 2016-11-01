package org.jetbrains.bio.viktor

@Suppress("nothing_to_inline")
inline fun IntArray.product() = reduce(Int::times)

internal fun IntArray.remove(pos: Int) = when (pos) {
    0 -> sliceArray(1..lastIndex)
    lastIndex -> sliceArray(0..lastIndex - 1)
    else -> sliceArray(0..pos - 1) + sliceArray(pos + 1..lastIndex)
}

@Suppress("nothing_to_inline")
internal inline fun checkIndex(label: String, pos: Int, size: Int) {
    if (pos < 0 || pos >= size) {
        throw IndexOutOfBoundsException("$label must be in [0, $size)")
    }
}

@Suppress("nothing_to_inline")
internal inline fun check1D(a: F64Array) {
    check(a.nDim == 1) { "expected a 1-D array" }
}

@Suppress("nothing_to_inline")
internal inline fun unsupported(): Nothing = throw UnsupportedOperationException()