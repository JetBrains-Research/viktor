package org.jetbrains.bio.viktor

@Suppress("nothing_to_inline")
inline fun IntArray.product() = fold(1, Int::times)

internal fun IntArray.remove(pos: Int) = when (pos) {
    0 -> sliceArray(1..lastIndex)
    lastIndex -> sliceArray(0 until lastIndex)
    else -> sliceArray(0 until pos) + sliceArray(pos + 1..lastIndex)
}

@Suppress("nothing_to_inline")
internal inline fun checkIndex(label: String, pos: Int, size: Int) {
    if (pos < 0 || pos >= size) {
        throw IndexOutOfBoundsException("$label must be in [0, $size), but was $pos")
    }
}

@Suppress("nothing_to_inline")
internal inline fun unsupported(): Nothing = throw UnsupportedOperationException()