package org.jetbrains.bio.jni

fun checkLength(x: DoubleArray, y: DoubleArray) {
    if (x.size != y.size) {
        throw IllegalArgumentException("Arrays' lengths differ.")
    }
}

fun checkOffsetAndLength(x: DoubleArray, offset: Int, length: Int) {
    if (offset < 0) {
        throw IllegalArgumentException(String.format("offset must be non-negative, but was %d", offset))
    }

    if (length < 0) {
        throw IllegalArgumentException(String.format("length must be non-negative, but was %d", length))
    }

    if (offset + length > x.size) {
        throw IllegalArgumentException(String.format(
                "array has length %d (should be at least %d)",
                x.size, length + offset))
    }
}
