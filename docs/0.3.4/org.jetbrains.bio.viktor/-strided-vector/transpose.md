[0.3.4](../../index.md) / [org.jetbrains.bio.viktor](../index.md) / [StridedVector](index.md) / [transpose](.)

# transpose

`fun transpose(): `[`StridedMatrix2`](../-strided-matrix2/index.md) [(source)](https://github.com/JetBrains-Research/viktor/blob/0.3.4/src/main/kotlin/org/jetbrains/bio/viktor/StridedVector.kt#L136)

Constructs a column-vector view of this vector in O(1) time.

A column vector is a matrix with [size](size.md) rows and a single column,
e.g. `[1, 2, 3]^T` is `[[1], [2], [3]]`.

