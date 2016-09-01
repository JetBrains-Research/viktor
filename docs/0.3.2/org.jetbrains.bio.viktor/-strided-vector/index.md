[0.3.2](../../index.md) / [org.jetbrains.bio.viktor](../index.md) / [StridedVector](.)

# StridedVector

`open class StridedVector` [(source)](https://github.com/JetBrains-Research/viktor/blob/0.3.2/src/main/kotlin/org/jetbrains/bio/viktor/StridedVector.kt#L40)

A strided vector stored in a [DoubleArray](#).

Vector is backed by the raw [data](data.md) array, which is guaranteed to
contain at least [size](size.md) elements starting from the [offset](offset.md) index.

The term *strided* means that unlike regular [DoubleArray](#) the
elements of a vector can be at arbitrary index intervals (strides)
from each other. For example

data = [0,1,2,3,4,5](#)
offset = 1
size = 2
stride = 3

corresponds to a vector with elements

[1,4](#)

Vectors with `stride` equal to 1 are called called *dense*. The
distinction is important because some of the operations can be
significantly optimized for dense vectors.

**Author**
Sergei Lebedev

**Since**
0.1.0

### Properties

| Name | Summary |
|---|---|
| [T](-t.md) | `val T: `[`StridedMatrix2`](../-strided-matrix2/index.md)<br>An alias for [transpose](transpose.md). |
| [data](data.md) | `val data: DoubleArray`<br>Raw data array. |
| [indices](indices.md) | `val indices: IntRange` |
| [offset](offset.md) | `val offset: Int`<br>Offset of the first vector element in the raw data array. |
| [size](size.md) | `val size: Int`<br>Number of elements in the raw data array to use. |
| [stride](stride.md) | `val stride: Int`<br>Indexing step. |

### Functions

| Name | Summary |
|---|---|
| [append](append.md) | `fun append(other: StridedVector): StridedVector`<br>Appends this vector to another vector. |
| [contains](contains.md) | `operator fun contains(other: Double): Boolean` |
| [copy](copy.md) | `fun copy(): StridedVector`<br>Returns a copy of the elements in this vector. |
| [copyTo](copy-to.md) | `open fun copyTo(other: StridedVector): Unit`<br>Copies the elements in this vector to [other](copy-to.md#org.jetbrains.bio.viktor.StridedVector$copyTo(org.jetbrains.bio.viktor.StridedVector)/other). |
| [cumSum](cum-sum.md) | `open fun cumSum(): Unit`<br>Computes cumulative sum of the elements. |
| [div](div.md) | `operator fun div(other: StridedVector): <ERROR CLASS>`<br>`operator fun div(update: Double): <ERROR CLASS>` |
| [divAssign](div-assign.md) | `open operator fun divAssign(other: StridedVector): Unit`<br>`open operator fun divAssign(update: Double): Unit` |
| [dot](dot.md) | `infix fun dot(other: ShortArray): Double`<br>`infix fun dot(other: IntArray): Double`<br>`infix fun dot(other: DoubleArray): Double`<br>Computes a dot product of this vector with an array.`open infix fun dot(other: StridedVector): Double`<br>Computes a dot product between the two vectors. |
| [equals](equals.md) | `open fun equals(other: Any?): Boolean` |
| [exp](exp.md) | `fun exp(): <ERROR CLASS>` |
| [expInPlace](exp-in-place.md) | `open fun expInPlace(): Unit`<br>Computes the exponent of each element of this vector. |
| [expm1](expm1.md) | `fun expm1(): <ERROR CLASS>` |
| [expm1InPlace](expm1-in-place.md) | `open fun expm1InPlace(): Unit`<br>Computes exp(x) - 1 for each element of this vector. |
| [fill](fill.md) | `open fun fill(init: Double): Unit` |
| [get](get.md) | `operator fun get(pos: Int): Double` |
| [hashCode](hash-code.md) | `open fun hashCode(): Int` |
| [isEmpty](is-empty.md) | `fun isEmpty(): Boolean` |
| [isNotEmpty](is-not-empty.md) | `fun isNotEmpty(): Boolean` |
| [iterator](iterator.md) | `operator fun iterator(): DoubleIterator`<br>Creates an iterator over the elements of the array. |
| [log](log.md) | `fun log(): <ERROR CLASS>` |
| [log1p](log1p.md) | `fun log1p(): <ERROR CLASS>` |
| [log1pInPlace](log1p-in-place.md) | `open fun log1pInPlace(): Unit`<br>Computes log(1 + x) for each element of this vector. |
| [logAddExp](log-add-exp.md) | `infix fun logAddExp(other: StridedVector): <ERROR CLASS>`<br>`open fun logAddExp(other: StridedVector, dst: StridedVector): Unit` |
| [logInPlace](log-in-place.md) | `open fun logInPlace(): Unit`<br>Computes the natural log of each element of this vector. |
| [logRescale](log-rescale.md) | `open fun logRescale(): Unit`<br>Rescales the element so that the exponent of the sum is 1.0. |
| [logSumExp](log-sum-exp.md) | `open fun logSumExp(): Double`<br>Computes |
| [max](max.md) | `open fun max(): Double`<br>Returns the maximum element. |
| [mean](mean.md) | `open fun mean(): Double`<br>Computes the mean of the elements. |
| [min](min.md) | `open fun min(): Double`<br>Returns the minimum element. |
| [minus](minus.md) | `operator fun minus(other: StridedVector): <ERROR CLASS>`<br>`operator fun minus(update: Double): <ERROR CLASS>` |
| [minusAssign](minus-assign.md) | `open operator fun minusAssign(other: StridedVector): Unit`<br>`open operator fun minusAssign(update: Double): Unit` |
| [plus](plus.md) | `operator fun plus(other: StridedVector): <ERROR CLASS>`<br>`operator fun plus(update: Double): <ERROR CLASS>` |
| [plusAssign](plus-assign.md) | `open operator fun plusAssign(other: StridedVector): Unit`<br>`open operator fun plusAssign(update: Double): Unit` |
| [rescale](rescale.md) | `fun rescale(): Unit`<br>Rescales the elements so that the sum is 1.0. |
| [reverse](reverse.md) | `fun reverse(): Unit` |
| [sd](sd.md) | `open fun sd(): Double`<br>Computes the unbiased standard deviation of the elements. |
| [set](set.md) | `operator fun set(pos: Int, value: Double): Unit`<br>`operator fun set(any: `[`_I`](../_-i.md)`, init: Double): Unit`<br>`operator fun set(any: `[`_I`](../_-i.md)`, other: StridedVector): Unit` |
| [slice](slice.md) | `fun slice(from: Int, to: Int = size): StridedVector`<br>Creates a sliced view of this vector in O(1) time. |
| [sum](sum.md) | `open fun sum(): Double`<br>Returns the sum of the elements using balanced summation. |
| [times](times.md) | `operator fun times(other: StridedVector): <ERROR CLASS>`<br>`operator fun times(update: Double): <ERROR CLASS>` |
| [timesAssign](times-assign.md) | `open operator fun timesAssign(other: StridedVector): Unit`<br>`open operator fun timesAssign(update: Double): Unit` |
| [toArray](to-array.md) | `open fun toArray(): DoubleArray` |
| [toString](to-string.md) | `fun toString(maxDisplay: Int, format: `[`DecimalFormat`](http://docs.oracle.com/javase/6/docs/api/java/text/DecimalFormat.html)` = DecimalFormat("#.####")): String`<br>`open fun toString(): String` |
| [transpose](transpose.md) | `fun transpose(): `[`StridedMatrix2`](../-strided-matrix2/index.md)<br>Constructs a column-vector view of this vector in O(1) time. |
| [unaryMinus](unary-minus.md) | `open operator fun unaryMinus(): StridedVector` |
| [unaryPlus](unary-plus.md) | `operator fun unaryPlus(): StridedVector` |
| [unsafeIndex](unsafe-index.md) | `open fun unsafeIndex(pos: Int): Int` |

### Companion Object Functions

| Name | Summary |
|---|---|
| [concatenate](concatenate.md) | `fun concatenate(first: StridedVector, vararg rest: StridedVector): StridedVector`<br>Joins a sequence of vectors into a single vector. |
| [full](full.md) | `fun full(size: Int, init: Double): StridedVector`<br>Creates an array filled with a given [init](full.md#org.jetbrains.bio.viktor.StridedVector.Companion$full(kotlin.Int, kotlin.Double)/init) element. |
| [invoke](invoke.md) | `operator fun invoke(size: Int): StridedVector`<br>Create a zero-filled vector of a given `size`.`operator fun invoke(size: Int, block: (Int) -> Double): StridedVector` |
| [of](of.md) | `fun of(first: Double, vararg rest: Double): StridedVector`<br>Creates a vector with given elements. |
| [stochastic](stochastic.md) | `fun stochastic(size: Int): StridedVector`<br>Creates an array with elements summing to one. |

### Extension Functions

| Name | Summary |
|---|---|
| [argMax](../arg-max.md) | `fun StridedVector.argMax(): Int`<br>Returns the index of the maxmimum element. |
| [argMin](../arg-min.md) | `fun StridedVector.argMin(): Int`<br>Returns the index of the minimum element. |
| [argSort](../arg-sort.md) | `fun StridedVector.argSort(reverse: Boolean = false): IntArray`<br>Returns a permutation of indices which makes the vector sorted. |
| [partition](../partition.md) | `fun StridedVector.partition(p: Int): Unit`<br>Partitions the vector. |
| [quantile](../quantile.md) | `fun StridedVector.quantile(q: Double = 0.5, randomGenerator: RandomGenerator = DEFAULT_RANDOM): Double`<br>Computes the [q](../quantile.md#org.jetbrains.bio.viktor$quantile(org.jetbrains.bio.viktor.StridedVector, kotlin.Double, org.apache.commons.math3.random.RandomGenerator)/q)-th order statistic over this vector. |
| [reorder](../reorder.md) | `fun StridedVector.reorder(indices: IntArray): Unit`<br>Applies a given permutation of indices to the elements in the vector. |
| [reshape](../reshape.md) | `fun StridedVector.reshape(depth: Int, numRows: Int, numColumns: Int): `[`StridedMatrix3`](../-strided-matrix3/index.md)<br>`fun StridedVector.reshape(numRows: Int, numColumns: Int): `[`StridedMatrix2`](../-strided-matrix2/index.md)<br>Reshapes this vector into a matrix in row-major order. |
| [searchSorted](../search-sorted.md) | `fun StridedVector.searchSorted(target: Double): Int`<br>Returns the insertion index of [target](../search-sorted.md#org.jetbrains.bio.viktor$searchSorted(org.jetbrains.bio.viktor.StridedVector, kotlin.Double)/target) into a sorted vector. |
| [shuffle](../shuffle.md) | `fun StridedVector.shuffle(randomGenerator: RandomGenerator = DEFAULT_RANDOM): Unit`<br>Randomly permutes the elements of this vector. |
| [sort](../sort.md) | `fun StridedVector.sort(reverse: Boolean = false): Unit`<br>Sorts the elements in this vector in in descending order. |

### Inheritors

| Name | Summary |
|---|---|
| [DenseVector](../-dense-vector/index.md) | `open class DenseVector : StridedVector`<br>A contiguous strided vector. |
