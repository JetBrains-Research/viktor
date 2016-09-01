[0.3.2](../../index.md) / [org.jetbrains.bio.viktor](../index.md) / [StridedMatrix](.)

# StridedMatrix

`object StridedMatrix` [(source)](https://github.com/JetBrains-Research/viktor/blob/0.3.2/src/main/kotlin/org/jetbrains/bio/viktor/StridedMatrix.kt#L9)

A strided matrix stored in a flat [DoubleArray](#).

**Author**
Sergei Lebedev

**Since**
0.1.0

### Functions

| Name | Summary |
|---|---|
| [full](full.md) | `fun full(numRows: Int, numColumns: Int, init: Double): `[`StridedMatrix2`](../-strided-matrix2/index.md)<br>`fun full(numRows: Int, numColumns: Int, depth: Int, init: Double): `[`StridedMatrix3`](../-strided-matrix3/index.md) |
| [indexedStochastic](indexed-stochastic.md) | `fun indexedStochastic(depth: Int, size: Int): `[`StridedMatrix3`](../-strided-matrix3/index.md)<br>Creates a 3-D matrix with [stochastic](stochastic.md) submatrices. |
| [invoke](invoke.md) | `operator fun invoke(numRows: Int, numColumns: Int): `[`StridedMatrix2`](../-strided-matrix2/index.md)<br>`operator fun invoke(numRows: Int, numColumns: Int, block: (Int, Int) -> Double): `[`StridedMatrix2`](../-strided-matrix2/index.md)<br>`operator fun invoke(numRows: Int, numColumns: Int, depth: Int): `[`StridedMatrix3`](../-strided-matrix3/index.md)<br>`operator fun invoke(depth: Int, numRows: Int, numColumns: Int, block: (Int, Int, Int) -> Double): `[`StridedMatrix3`](../-strided-matrix3/index.md) |
| [stochastic](stochastic.md) | `fun stochastic(size: Int): `[`StridedMatrix2`](../-strided-matrix2/index.md)<br>Creates a 2-D matrix with rows summing to one. |
