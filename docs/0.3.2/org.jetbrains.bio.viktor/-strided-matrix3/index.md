[0.3.2](../../index.md) / [org.jetbrains.bio.viktor](../index.md) / [StridedMatrix3](.)

# StridedMatrix3

`class StridedMatrix3 : `[`FlatMatrixOps`](../-flat-matrix-ops/index.md)`<StridedMatrix3>` [(source)](https://github.com/JetBrains-Research/viktor/blob/0.3.2/src/main/kotlin/org/jetbrains/bio/viktor/StridedMatrix3.kt#L11)

A specialization of [StridedMatrix](../-strided-matrix/index.md) for 3-D data.

**Author**
Sergei Lebedev

**Since**
0.1.0

### Constructors

| Name | Summary |
|---|---|
| [&lt;init&gt;](-init-.md) | `StridedMatrix3(depth: Int, numRows: Int, numColumns: Int)` |

### Properties

| Name | Summary |
|---|---|
| [columnStride](column-stride.md) | `val columnStride: Int` |
| [columnsNumber](columns-number.md) | `val columnsNumber: Int` |
| [data](data.md) | `val data: DoubleArray` |
| [depth](depth.md) | `val depth: Int` |
| [depthStride](depth-stride.md) | `val depthStride: Int` |
| [offset](offset.md) | `val offset: Int` |
| [rowStride](row-stride.md) | `val rowStride: Int` |
| [rowsNumber](rows-number.md) | `val rowsNumber: Int` |

### Functions

| Name | Summary |
|---|---|
| [checkDimensions](check-dimensions.md) | `fun checkDimensions(other: StridedMatrix3): Unit`<br>Ensures a given matrix has the same dimensions as this matrix. |
| [copy](copy.md) | `fun copy(): StridedMatrix3`<br>Returns the copy of this matrix. |
| [copyTo](copy-to.md) | `fun copyTo(other: StridedMatrix3): Unit` |
| [equals](equals.md) | `fun equals(other: Any?): Boolean` |
| [flatten](flatten.md) | `fun flatten(): `[`StridedVector`](../-strided-vector/index.md)<br>Returns a flat view of this matrix. |
| [get](get.md) | `operator fun get(d: Int, r: Int, c: Int): Double`<br>`operator fun get(d: Int): `[`StridedMatrix2`](../-strided-matrix2/index.md)<br>`operator fun get(d: Int, r: Int): `[`StridedVector`](../-strided-vector/index.md) |
| [hashCode](hash-code.md) | `fun hashCode(): Int` |
| [set](set.md) | `operator fun set(d: Int, r: Int, c: Int, value: Double): Unit`<br>`operator fun set(d: Int, other: `[`StridedMatrix2`](../-strided-matrix2/index.md)`): Unit`<br>`operator fun set(d: Int, other: Double): Unit`<br>`operator fun set(d: Int, r: Int, other: Double): Unit`<br>`operator fun set(d: Int, r: Int, other: `[`StridedVector`](../-strided-vector/index.md)`): Unit` |
| [toArray](to-array.md) | `fun toArray(): Array<Array<DoubleArray>>` |
| [toString](to-string.md) | `fun toString(): String` |
| [view](view.md) | `fun view(d: Int): `[`StridedMatrix2`](../-strided-matrix2/index.md) |

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
