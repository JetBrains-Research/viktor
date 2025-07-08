package org.jetbrains.bio.viktor

internal interface VectorOp {
    fun apply(
        dst: DoubleArray?,
        dstOffset: Int,
        src: DoubleArray?,
        srcOffset: Int,
        size: Int
    )
}