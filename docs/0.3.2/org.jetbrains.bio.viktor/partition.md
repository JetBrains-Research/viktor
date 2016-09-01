[0.3.2](../index.md) / [org.jetbrains.bio.viktor](index.md) / [partition](.)

# partition

`fun `[`StridedVector`](-strided-vector/index.md)`.partition(p: Int): Unit` [(source)](https://github.com/JetBrains-Research/viktor/blob/0.3.2/src/main/kotlin/org/jetbrains/bio/viktor/Sorting.kt#L75)

Partitions the vector.

Rearranges the elements in this vector in such a way that
the [p](partition.md#org.jetbrains.bio.viktor$partition(org.jetbrains.bio.viktor.StridedVector, kotlin.Int)/p)-th element moves to its position in the sorted copy
of the vector. All elements smaller than the [p](partition.md#org.jetbrains.bio.viktor$partition(org.jetbrains.bio.viktor.StridedVector, kotlin.Int)/p)-th element
are moved before this element, and all elements greater or
equals to this element are moved behind it.

The operation is done **in place**.

### Parameters

`p` - the index of the element to partition by.

**Since**
0.2.3

