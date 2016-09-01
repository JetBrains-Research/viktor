[0.3.2](../../index.md) / [org.jetbrains.bio.viktor](../index.md) / [SmallDenseVector](.)

# SmallDenseVector

`class SmallDenseVector : `[`DenseVector`](../-dense-vector/index.md) [(source)](https://github.com/JetBrains-Research/viktor/blob/0.3.2/src/main/kotlin/org/jetbrains/bio/viktor/DenseVector.kt#L50)

A contiguous strided vector of size at most [DenseVector.DENSE_SPLIT_SIZE](../-dense-vector/-d-e-n-s-e_-s-p-l-i-t_-s-i-z-e.md).

**Author**
Sergei Lebedev

**Since**
0.1.0

### Constructors

| Name | Summary |
|---|---|
| [&lt;init&gt;](-init-.md) | `SmallDenseVector(data: DoubleArray, offset: Int, size: Int)`<br>A contiguous strided vector of size at most [DenseVector.DENSE_SPLIT_SIZE](../-dense-vector/-d-e-n-s-e_-s-p-l-i-t_-s-i-z-e.md). |

### Inherited Functions

| Name | Summary |
|---|---|
| [copyTo](../-dense-vector/copy-to.md) | `open fun copyTo(other: `[`StridedVector`](../-strided-vector/index.md)`): Unit`<br>Copies the elements in this vector to [other](../-dense-vector/copy-to.md#org.jetbrains.bio.viktor.DenseVector$copyTo(org.jetbrains.bio.viktor.StridedVector)/other). |
| [fill](../-dense-vector/fill.md) | `open fun fill(init: Double): Unit` |
| [toArray](../-dense-vector/to-array.md) | `open fun toArray(): <ERROR CLASS>` |
| [unsafeIndex](../-dense-vector/unsafe-index.md) | `open fun unsafeIndex(pos: Int): Int` |

### Extension Functions

| Name | Summary |
|---|---|
| [argMax](../arg-max.md) | `fun `[`StridedVector`](../-strided-vector/index.md)`.argMax(): Int`<br>Returns the index of the maxmimum element. |
| [argMin](../arg-min.md) | `fun `[`StridedVector`](../-strided-vector/index.md)`.argMin(): Int`<br>Returns the index of the minimum element. |
| [argSort](../arg-sort.md) | `fun `[`StridedVector`](../-strided-vector/index.md)`.argSort(reverse: Boolean = false): IntArray`<br>Returns a permutation of indices which makes the vector sorted. |
| [partition](../partition.md) | `fun `[`StridedVector`](../-strided-vector/index.md)`.partition(p: Int): Unit`<br>Partitions the vector. |
| [quantile](../quantile.md) | `fun `[`StridedVector`](../-strided-vector/index.md)`.quantile(q: Double = 0.5, randomGenerator: RandomGenerator = DEFAULT_RANDOM): Double`<br>Computes the [q](../quantile.md#org.jetbrains.bio.viktor$quantile(org.jetbrains.bio.viktor.StridedVector, kotlin.Double, org.apache.commons.math3.random.RandomGenerator)/q)-th order statistic over this vector. |
| [reorder](../reorder.md) | `fun `[`StridedVector`](../-strided-vector/index.md)`.reorder(indices: IntArray): Unit`<br>Applies a given permutation of indices to the elements in the vector. |
| [reshape](../reshape.md) | `fun `[`StridedVector`](../-strided-vector/index.md)`.reshape(depth: Int, numRows: Int, numColumns: Int): `[`StridedMatrix3`](../-strided-matrix3/index.md)<br>`fun `[`StridedVector`](../-strided-vector/index.md)`.reshape(numRows: Int, numColumns: Int): `[`StridedMatrix2`](../-strided-matrix2/index.md)<br>Reshapes this vector into a matrix in row-major order. |
| [searchSorted](../search-sorted.md) | `fun `[`StridedVector`](../-strided-vector/index.md)`.searchSorted(target: Double): Int`<br>Returns the insertion index of [target](../search-sorted.md#org.jetbrains.bio.viktor$searchSorted(org.jetbrains.bio.viktor.StridedVector, kotlin.Double)/target) into a sorted vector. |
| [shuffle](../shuffle.md) | `fun `[`StridedVector`](../-strided-vector/index.md)`.shuffle(randomGenerator: RandomGenerator = DEFAULT_RANDOM): Unit`<br>Randomly permutes the elements of this vector. |
| [sort](../sort.md) | `fun `[`StridedVector`](../-strided-vector/index.md)`.sort(reverse: Boolean = false): Unit`<br>Sorts the elements in this vector in in descending order. |
