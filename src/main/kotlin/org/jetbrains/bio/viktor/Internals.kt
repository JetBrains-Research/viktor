package org.jetbrains.bio.viktor

@Suppress("nothing_to_inline")
inline fun IntArray.product() = reduce(Int::times)