[0.3.2](../../index.md) / [org.jetbrains.bio.viktor](../index.md) / [DenseVector](.)

# DenseVector

`open class DenseVector : `[`StridedVector`](../-strided-vector/index.md) [(source)](https://github.com/JetBrains-Research/viktor/blob/0.3.2/src/main/kotlin/org/jetbrains/bio/viktor/DenseVector.kt#L9)

A contiguous strided vector.

**Author**
Sergei Lebedev

**Since**
0.1.0

### Constructors

| Name | Summary |
|---|---|
| [&lt;init&gt;](-init-.md) | `DenseVector(data: DoubleArray, offset: Int, size: Int)`<br>A contiguous strided vector. |

### Inherited Properties

| Name | Summary |
|---|---|
| [T](../-strided-vector/-t.md) | `val T: `[`StridedMatrix2`](../-strided-matrix2/index.md)<br>An alias for [transpose](../-strided-vector/transpose.md). |
| [data](../-strided-vector/data.md) | `val data: DoubleArray`<br>Raw data array. |
| [indices](../-strided-vector/indices.md) | `val indices: IntRange` |
| [offset](../-strided-vector/offset.md) | `val offset: Int`<br>Offset of the first vector element in the raw data array. |
| [size](../-strided-vector/size.md) | `val size: Int`<br>Number of elements in the raw data array to use. |
| [stride](../-strided-vector/stride.md) | `val stride: Int`<br>Indexing step. |

### Functions

| Name | Summary |
|---|---|
| [copyTo](copy-to.md) | `open fun copyTo(other: `[`StridedVector`](../-strided-vector/index.md)`): Unit`<br>Copies the elements in this vector to [other](copy-to.md#org.jetbrains.bio.viktor.DenseVector$copyTo(org.jetbrains.bio.viktor.StridedVector)/other). |
| [fill](fill.md) | `open fun fill(init: Double): Unit` |
| [toArray](to-array.md) | `open fun toArray(): <ERROR CLASS>` |
| [unsafeIndex](unsafe-index.md) | `open fun unsafeIndex(pos: Int): Int` |

### Inherited Functions

| Name | Summary |
|---|---|
| [append](../-strided-vector/append.md) | `fun append(other: `[`StridedVector`](../-strided-vector/index.md)`): `[`StridedVector`](../-strided-vector/index.md)<br>Appends this vector to another vector. |
| [contains](../-strided-vector/contains.md) | `operator fun contains(other: Double): Boolean` |
| [copy](../-strided-vector/copy.md) | `fun copy(): `[`StridedVector`](../-strided-vector/index.md)<br>Returns a copy of the elements in this vector. |
| [cumSum](../-strided-vector/cum-sum.md) | `open fun cumSum(): Unit`<br>Computes cumulative sum of the elements. |
| [div](../-strided-vector/div.md) | `operator fun div(other: `[`StridedVector`](../-strided-vector/index.md)`): <ERROR CLASS>`<br>`operator fun div(update: Double): <ERROR CLASS>` |
| [divAssign](../-strided-vector/div-assign.md) | `open operator fun divAssign(other: `[`StridedVector`](../-strided-vector/index.md)`): Unit`<br>`open operator fun divAssign(update: Double): Unit` |
| [dot](../-strided-vector/dot.md) | `infix fun dot(other: ShortArray): Double`<br>`infix fun dot(other: IntArray): Double`<br>`infix fun dot(other: DoubleArray): Double`<br>Computes a dot product of this vector with an array.`open infix fun dot(other: `[`StridedVector`](../-strided-vector/index.md)`): Double`<br>Computes a dot product between the two vectors. |
| [equals](../-strided-vector/equals.md) | `open fun equals(other: Any?): Boolean` |
| [exp](../-strided-vector/exp.md) | `fun exp(): <ERROR CLASS>` |
| [expInPlace](../-strided-vector/exp-in-place.md) | `open fun expInPlace(): Unit`<br>Computes the exponent of each element of this vector. |
| [expm1](../-strided-vector/expm1.md) | `fun expm1(): <ERROR CLASS>` |
| [expm1InPlace](../-strided-vector/expm1-in-place.md) | `open fun expm1InPlace(): Unit`<br>Computes exp(x) - 1 for each element of this vector. |
| [get](../-strided-vector/get.md) | `operator fun get(pos: Int): Double` |
| [hashCode](../-strided-vector/hash-code.md) | `open fun hashCode(): Int` |
| [isEmpty](../-strided-vector/is-empty.md) | `fun isEmpty(): Boolean` |
| [isNotEmpty](../-strided-vector/is-not-empty.md) | `fun isNotEmpty(): Boolean` |
| [iterator](../-strided-vector/iterator.md) | `operator fun iterator(): DoubleIterator`<br>Creates an iterator over the elements of the array. |
| [log](../-strided-vector/log.md) | `fun log(): <ERROR CLASS>` |
| [log1p](../-strided-vector/log1p.md) | `fun log1p(): <ERROR CLASS>` |
| [log1pInPlace](../-strided-vector/log1p-in-place.md) | `open fun log1pInPlace(): Unit`<br>Computes log(1 + x) for each element of this vector. |
| [logAddExp](../-strided-vector/log-add-exp.md) | `infix fun logAddExp(other: `[`StridedVector`](../-strided-vector/index.md)`): <ERROR CLASS>`<br>`open fun logAddExp(other: `[`StridedVector`](../-strided-vector/index.md)`, dst: `[`StridedVector`](../-strided-vector/index.md)`): Unit` |
| [logInPlace](../-strided-vector/log-in-place.md) | `open fun logInPlace(): Unit`<br>Computes the natural log of each element of this vector. |
| [logRescale](../-strided-vector/log-rescale.md) | `open fun logRescale(): Unit`<br>Rescales the element so that the exponent of the sum is 1.0. |
| [logSumExp](../-strided-vector/log-sum-exp.md) | `open fun logSumExp(): Double`<br>Computes |
| [max](../-strided-vector/max.md) | `open fun max(): Double`<br>Returns the maximum element. |
| [mean](../-strided-vector/mean.md) | `open fun mean(): Double`<br>Computes the mean of the elements. |
| [min](../-strided-vector/min.md) | `open fun min(): Double`<br>Returns the minimum element. |
| [minus](../-strided-vector/minus.md) | `operator fun minus(other: `[`StridedVector`](../-strided-vector/index.md)`): <ERROR CLASS>`<br>`operator fun minus(update: Double): <ERROR CLASS>` |
| [minusAssign](../-strided-vector/minus-assign.md) | `open operator fun minusAssign(other: `[`StridedVector`](../-strided-vector/index.md)`): Unit`<br>`open operator fun minusAssign(update: Double): Unit` |
| [plus](../-strided-vector/plus.md) | `operator fun plus(other: `[`StridedVector`](../-strided-vector/index.md)`): <ERROR CLASS>`<br>`operator fun plus(update: Double): <ERROR CLASS>` |
| [plusAssign](../-strided-vector/plus-assign.md) | `open operator fun plusAssign(other: `[`StridedVector`](../-strided-vector/index.md)`): Unit`<br>`open operator fun plusAssign(update: Double): Unit` |
| [rescale](../-strided-vector/rescale.md) | `fun rescale(): Unit`<br>Rescales the elements so that the sum is 1.0. |
| [reverse](../-strided-vector/reverse.md) | `fun reverse(): Unit` |
| [sd](../-strided-vector/sd.md) | `open fun sd(): Double`<br>Computes the unbiased standard deviation of the elements. |
| [set](../-strided-vector/set.md) | `operator fun set(pos: Int, value: Double): Unit`<br>`operator fun set(any: `[`_I`](../_-i.md)`, init: Double): Unit`<br>`operator fun set(any: `[`_I`](../_-i.md)`, other: `[`StridedVector`](../-strided-vector/index.md)`): Unit` |
| [slice](../-strided-vector/slice.md) | `fun slice(from: Int, to: Int = size): `[`StridedVector`](../-strided-vector/index.md)<br>Creates a sliced view of this vector in O(1) time. |
| [sum](../-strided-vector/sum.md) | `open fun sum(): Double`<br>Returns the sum of the elements using balanced summation. |
| [times](../-strided-vector/times.md) | `operator fun times(other: `[`StridedVector`](../-strided-vector/index.md)`): <ERROR CLASS>`<br>`operator fun times(update: Double): <ERROR CLASS>` |
| [timesAssign](../-strided-vector/times-assign.md) | `open operator fun timesAssign(other: `[`StridedVector`](../-strided-vector/index.md)`): Unit`<br>`open operator fun timesAssign(update: Double): Unit` |
| [toString](../-strided-vector/to-string.md) | `fun toString(maxDisplay: Int, format: `[`DecimalFormat`](http://docs.oracle.com/javase/6/docs/api/java/text/DecimalFormat.html)` = DecimalFormat("#.####")): String`<br>`open fun toString(): String` |
| [transpose](../-strided-vector/transpose.md) | `fun transpose(): `[`StridedMatrix2`](../-strided-matrix2/index.md)<br>Constructs a column-vector view of this vector in O(1) time. |
| [unaryMinus](../-strided-vector/unary-minus.md) | `open operator fun unaryMinus(): `[`StridedVector`](../-strided-vector/index.md) |
| [unaryPlus](../-strided-vector/unary-plus.md) | `operator fun unaryPlus(): `[`StridedVector`](../-strided-vector/index.md) |

### Companion Object Properties

| Name | Summary |
|---|---|
| [DENSE_SPLIT_SIZE](-d-e-n-s-e_-s-p-l-i-t_-s-i-z-e.md) | `const val DENSE_SPLIT_SIZE: Int`<br>We only use SIMD operations on vectors larger than the split boundary. |

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

### Inheritors

| Name | Summary |
|---|---|
| [LargeDenseVector](../-large-dense-vector/index.md) | `class LargeDenseVector : DenseVector`<br>A contiguous vector of size at least `[DenseVector.DENSE_SPLIT_SIZE] + 1`. |
| [SmallDenseVector](../-small-dense-vector/index.md) | `class SmallDenseVector : DenseVector`<br>A contiguous strided vector of size at most [DenseVector.DENSE_SPLIT_SIZE](-d-e-n-s-e_-s-p-l-i-t_-s-i-z-e.md). |
