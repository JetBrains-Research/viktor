[0.3.5](../index.md) / [org.jetbrains.bio.viktor](index.md) / [searchSorted](.)

# searchSorted

`fun `[`StridedVector`](-strided-vector/index.md)`.searchSorted(target: Double): Int` [(source)](https://github.com/JetBrains-Research/viktor/blob/0.3.5/src/main/kotlin/org/jetbrains/bio/viktor/Searching.kt#L11)

Returns the insertion index of [target](search-sorted.md#org.jetbrains.bio.viktor$searchSorted(org.jetbrains.bio.viktor.StridedVector, kotlin.Double)/target) into a sorted vector.

If [target](search-sorted.md#org.jetbrains.bio.viktor$searchSorted(org.jetbrains.bio.viktor.StridedVector, kotlin.Double)/target) already appears in this vector, the returned
index is just before the leftmost occurrence of [target](search-sorted.md#org.jetbrains.bio.viktor$searchSorted(org.jetbrains.bio.viktor.StridedVector, kotlin.Double)/target).

**Since**
0.2.3

