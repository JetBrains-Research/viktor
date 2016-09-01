[0.3.2](../../index.md) / [org.jetbrains.bio.viktor](../index.md) / [StridedMatrix3](index.md) / [flatten](.)

# flatten

`fun flatten(): `[`StridedVector`](../-strided-vector/index.md) [(source)](https://github.com/JetBrains-Research/viktor/blob/0.3.2/src/main/kotlin/org/jetbrains/bio/viktor/StridedMatrix3.kt#L80)

Overrides [FlatMatrixOps.flatten](../-flat-matrix-ops/flatten.md)

Returns a flat view of this matrix.

If the matrix is not dense the method must raise an error.

