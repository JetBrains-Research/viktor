[0.3.2](../../index.md) / [org.jetbrains.bio.viktor](../index.md) / [StridedMatrix2](.)

# StridedMatrix2

`class StridedMatrix2 : `[`FlatMatrixOps`](../-flat-matrix-ops/index.md)`<StridedMatrix2>` [(source)](https://github.com/JetBrains-Research/viktor/blob/0.3.2/src/main/kotlin/org/jetbrains/bio/viktor/StridedMatrix2.kt#L13)

A specialization of [StridedMatrix](../-strided-matrix/index.md) for 2-D data.

**Author**
Sergei Lebedev

**Since**
0.1.0

### Constructors

| Name | Summary |
|---|---|
| [&lt;init&gt;](-init-.md) | `StridedMatrix2(numRows: Int, numColumns: Int)` |

### Properties

| Name | Summary |
|---|---|
| [T](-t.md) | `val T: StridedMatrix2`<br>An alias for [transpose](transpose.md). |
| [columnStride](column-stride.md) | `val columnStride: Int` |
| [columnsNumber](columns-number.md) | `val columnsNumber: Int` |
| [data](data.md) | `val data: DoubleArray` |
| [offset](offset.md) | `val offset: Int` |
| [rowStride](row-stride.md) | `val rowStride: Int` |
| [rowsNumber](rows-number.md) | `val rowsNumber: Int` |

### Functions

| Name | Summary |
|---|---|
| [along](along.md) | `fun along(axis: Int): `[`Stream`](http://docs.oracle.com/javase/6/docs/api/java/util/stream/Stream.html)`<`[`StridedVector`](../-strided-vector/index.md)`>`<br>Returns a stream of row or column views of the matrix. |
| [checkDimensions](check-dimensions.md) | `fun checkDimensions(other: StridedMatrix2): Unit`<br>Ensures a given matrix has the same dimensions as this matrix. |
| [columnView](column-view.md) | `fun columnView(c: Int): `[`StridedVector`](../-strided-vector/index.md)<br>Returns a view of the [c](column-view.md#org.jetbrains.bio.viktor.StridedMatrix2$columnView(kotlin.Int)/c)-th column of this matrix. |
| [copy](copy.md) | `fun copy(): StridedMatrix2`<br>Returns a copy of the elements in this matrix. |
| [copyTo](copy-to.md) | `fun copyTo(other: StridedMatrix2): Unit`<br>Copies elements in this matrix to [other](copy-to.md#org.jetbrains.bio.viktor.StridedMatrix2$copyTo(org.jetbrains.bio.viktor.StridedMatrix2)/other) matrix. |
| [equals](equals.md) | `fun equals(other: Any?): Boolean` |
| [flatten](flatten.md) | `fun flatten(): `[`StridedVector`](../-strided-vector/index.md)<br>Flattens the matrix into a vector in O(1) time. |
| [get](get.md) | `operator fun get(r: Int, c: Int): Double``operator fun get(r: Int): `[`StridedVector`](../-strided-vector/index.md)<br>A less-verbose alias to [rowView](row-view.md).`operator fun get(any: `[`_I`](../_-i.md)`, c: Int): `[`StridedVector`](../-strided-vector/index.md)<br>A less-verbose alias to [columnView](column-view.md). |
| [hashCode](hash-code.md) | `fun hashCode(): Int` |
| [rowView](row-view.md) | `fun rowView(r: Int): `[`StridedVector`](../-strided-vector/index.md)<br>Returns a view of the [r](row-view.md#org.jetbrains.bio.viktor.StridedMatrix2$rowView(kotlin.Int)/r)-th row of this matrix. |
| [set](set.md) | `operator fun set(r: Int, c: Int, value: Double): Unit`<br>`operator fun set(r: Int, other: `[`StridedVector`](../-strided-vector/index.md)`): Unit`<br>`operator fun set(r: Int, init: Double): Unit`<br>`operator fun set(any: `[`_I`](../_-i.md)`, c: Int, other: `[`StridedVector`](../-strided-vector/index.md)`): Unit`<br>`operator fun set(any: `[`_I`](../_-i.md)`, c: Int, init: Double): Unit` |
| [toArray](to-array.md) | `fun toArray(): Array<DoubleArray>` |
| [toString](to-string.md) | `fun toString(): String` |
| [transpose](transpose.md) | `fun transpose(): StridedMatrix2`<br>Constructs matrix transpose in O(1) time. |

### Inherited Functions

| Name | Summary |
|---|---|
| [argMax](../-flat-matrix-ops/arg-max.md) | `open fun argMax(): Int` |
| [argMin](../-flat-matrix-ops/arg-min.md) | `open fun argMin(): Int` |
| [div](../-flat-matrix-ops/div.md) | `open operator fun div(update: Double): <ERROR CLASS>` |
| [divAssign](../-flat-matrix-ops/div-assign.md) | `open operator fun divAssign(update: Double): Unit` |
| [exp](../-flat-matrix-ops/exp.md) | `open fun exp(): <ERROR CLASS>` |
| [expInPlace](../-flat-matrix-ops/exp-in-place.md) | `open fun expInPlace(): Unit` |
| [expm1](../-flat-matrix-ops/expm1.md) | `open fun expm1(): <ERROR CLASS>` |
| [expm1InPlace](../-flat-matrix-ops/expm1-in-place.md) | `open fun expm1InPlace(): Unit` |
| [fill](../-flat-matrix-ops/fill.md) | `open fun fill(init: Double): Unit` |
| [log](../-flat-matrix-ops/log.md) | `open fun log(): <ERROR CLASS>` |
| [log1p](../-flat-matrix-ops/log1p.md) | `open fun log1p(): <ERROR CLASS>` |
| [log1pInPlace](../-flat-matrix-ops/log1p-in-place.md) | `open fun log1pInPlace(): Unit` |
| [logInPlace](../-flat-matrix-ops/log-in-place.md) | `open fun logInPlace(): Unit` |
| [logRescale](../-flat-matrix-ops/log-rescale.md) | `open fun logRescale(): Unit` |
| [logSumExp](../-flat-matrix-ops/log-sum-exp.md) | `open fun logSumExp(): Double` |
| [max](../-flat-matrix-ops/max.md) | `open fun max(): Double` |
| [mean](../-flat-matrix-ops/mean.md) | `open fun mean(): Double` |
| [min](../-flat-matrix-ops/min.md) | `open fun min(): Double` |
| [minus](../-flat-matrix-ops/minus.md) | `open operator fun minus(update: Double): <ERROR CLASS>` |
| [minusAssign](../-flat-matrix-ops/minus-assign.md) | `open operator fun minusAssign(update: Double): Unit` |
| [plus](../-flat-matrix-ops/plus.md) | `open operator fun plus(update: Double): <ERROR CLASS>` |
| [plusAssign](../-flat-matrix-ops/plus-assign.md) | `open operator fun plusAssign(update: Double): Unit` |
| [sum](../-flat-matrix-ops/sum.md) | `open fun sum(): Double` |
| [times](../-flat-matrix-ops/times.md) | `open operator fun times(update: Double): <ERROR CLASS>` |
| [timesAssign](../-flat-matrix-ops/times-assign.md) | `open operator fun timesAssign(update: Double): Unit` |
| [unaryMinus](../-flat-matrix-ops/unary-minus.md) | `open operator fun unaryMinus(): <ERROR CLASS>` |
| [unaryPlus](../-flat-matrix-ops/unary-plus.md) | `open operator fun unaryPlus(): `[`FlatMatrixOps`](../-flat-matrix-ops/index.md)`<T>` |
