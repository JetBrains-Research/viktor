[0.3.2](../../index.md) / [org.jetbrains.bio.viktor](../index.md) / [StridedVector](index.md) / [slice](.)

# slice

`fun slice(from: Int, to: Int = size): `[`StridedVector`](index.md) [(source)](https://github.com/JetBrains-Research/viktor/blob/0.3.2/src/main/kotlin/org/jetbrains/bio/viktor/StridedVector.kt#L84)

Creates a sliced view of this vector in O(1) time.

### Parameters

`from` - the first index of the slice (inclusive).

`to` - the last index of the slice (exclusive).