[0.3.4](../index.md) / [org.jetbrains.bio.viktor](.)

## Package org.jetbrains.bio.viktor

### Types

| Name | Summary |
|---|---|
| [DenseVector](-dense-vector/index.md) | `open class DenseVector : `[`StridedVector`](-strided-vector/index.md)<br>A contiguous strided vector. |
| [FlatMatrixOps](-flat-matrix-ops/index.md) | `interface FlatMatrixOps<T : `[`FlatMatrixOps`](-flat-matrix-ops/index.md)`<T>>`<br>A common interface for whole-matrix operations. |
| [KahanSum](-kahan-sum/index.md) | `class KahanSum`<br>Kahan-Babuska summation. |
| [LargeDenseVector](-large-dense-vector/index.md) | `class LargeDenseVector : `[`DenseVector`](-dense-vector/index.md)<br>A contiguous vector of size at least `[DenseVector.DENSE_SPLIT_SIZE] + 1`. |
| [SmallDenseVector](-small-dense-vector/index.md) | `class SmallDenseVector : `[`DenseVector`](-dense-vector/index.md)<br>A contiguous strided vector of size at most [DenseVector.DENSE_SPLIT_SIZE](-dense-vector/-d-e-n-s-e_-s-p-l-i-t_-s-i-z-e.md). |
| [StridedMatrix](-strided-matrix/index.md) | `object StridedMatrix`<br>A strided matrix stored in a flat [DoubleArray](#). |
| [StridedMatrix2](-strided-matrix2/index.md) | `class StridedMatrix2 : `[`FlatMatrixOps`](-flat-matrix-ops/index.md)`<`[`StridedMatrix2`](-strided-matrix2/index.md)`>`<br>A specialization of [StridedMatrix](-strided-matrix/index.md) for 2-D data. |
| [StridedMatrix3](-strided-matrix3/index.md) | `class StridedMatrix3 : `[`FlatMatrixOps`](-flat-matrix-ops/index.md)`<`[`StridedMatrix3`](-strided-matrix3/index.md)`>`<br>A specialization of [StridedMatrix](-strided-matrix/index.md) for 3-D data. |
| [StridedVector](-strided-vector/index.md) | `open class StridedVector`<br>A strided vector stored in a [DoubleArray](#). |
| [_I](_-i.md) | `object _I`<br>A special object used to denote all indices. |

### Extensions for External Classes

| Name | Summary |
|---|---|
| [kotlin.Double](kotlin.-double/index.md) |  |
| [kotlin.DoubleArray](kotlin.-double-array/index.md) |  |
| [org.jetbrains.bio.npy.NpyArray](org.jetbrains.bio.npy.-npy-array/index.md) |  |
| [org.jetbrains.bio.npy.NpyFile](org.jetbrains.bio.npy.-npy-file/index.md) |  |
| [org.jetbrains.bio.npy.NpzFile.Writer](org.jetbrains.bio.npy.-npz-file.-writer/index.md) |  |

### Functions

| Name | Summary |
|---|---|
| [argMax](arg-max.md) | `fun `[`StridedVector`](-strided-vector/index.md)`.argMax(): Int`<br>Returns the index of the maximum element. |
| [argMin](arg-min.md) | `fun `[`StridedVector`](-strided-vector/index.md)`.argMin(): Int`<br>Returns the index of the minimum element. |
| [argSort](arg-sort.md) | `fun `[`StridedVector`](-strided-vector/index.md)`.argSort(reverse: Boolean = false): IntArray`<br>Returns a permutation of indices which makes the vector sorted. |
| [partition](partition.md) | `fun `[`StridedVector`](-strided-vector/index.md)`.partition(p: Int): Unit`<br>Partitions the vector. |
| [quantile](quantile.md) | `fun `[`StridedVector`](-strided-vector/index.md)`.quantile(q: Double = 0.5, randomGenerator: RandomGenerator = DEFAULT_RANDOM): Double`<br>Computes the [q](quantile.md#org.jetbrains.bio.viktor$quantile(org.jetbrains.bio.viktor.StridedVector, kotlin.Double, org.apache.commons.math3.random.RandomGenerator)/q)-th order statistic over this vector. |
| [reorder](reorder.md) | `fun `[`StridedVector`](-strided-vector/index.md)`.reorder(indices: IntArray): Unit`<br>Applies a given permutation of indices to the elements in the vector. |
| [reshape](reshape.md) | `fun `[`StridedVector`](-strided-vector/index.md)`.reshape(depth: Int, numRows: Int, numColumns: Int): `[`StridedMatrix3`](-strided-matrix3/index.md)<br>`fun `[`StridedVector`](-strided-vector/index.md)`.reshape(numRows: Int, numColumns: Int): `[`StridedMatrix2`](-strided-matrix2/index.md)<br>Reshapes this vector into a matrix in row-major order. |
| [searchSorted](search-sorted.md) | `fun `[`StridedVector`](-strided-vector/index.md)`.searchSorted(target: Double): Int`<br>Returns the insertion index of [target](search-sorted.md#org.jetbrains.bio.viktor$searchSorted(org.jetbrains.bio.viktor.StridedVector, kotlin.Double)/target) into a sorted vector. |
| [shuffle](shuffle.md) | `fun `[`StridedVector`](-strided-vector/index.md)`.shuffle(randomGenerator: RandomGenerator = DEFAULT_RANDOM): Unit`<br>Randomly permutes the elements of this vector. |
| [sort](sort.md) | `fun `[`StridedVector`](-strided-vector/index.md)`.sort(reverse: Boolean = false): Unit`<br>Sorts the elements in this vector in in descending order. |
