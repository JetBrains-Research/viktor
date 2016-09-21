[0.3.4](../../index.md) / [org.jetbrains.bio.viktor](../index.md) / [StridedMatrix2](index.md) / [flatten](.)

# flatten

`fun flatten(): `[`StridedVector`](../-strided-vector/index.md) [(source)](https://github.com/JetBrains-Research/viktor/blob/0.3.4/src/main/kotlin/org/jetbrains/bio/viktor/StridedMatrix2.kt#L137)

Overrides [FlatMatrixOps.flatten](../-flat-matrix-ops/flatten.md)

Flattens the matrix into a vector in O(1) time.

No data copying is performed, thus the operation is only applicable
to dense matrices.

