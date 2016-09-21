[0.3.5](../../index.md) / [org.jetbrains.bio.viktor](../index.md) / [FlatMatrixOps](.)

# FlatMatrixOps

`interface FlatMatrixOps<T : FlatMatrixOps<T>>` [(source)](https://github.com/JetBrains-Research/viktor/blob/0.3.5/src/main/kotlin/org/jetbrains/bio/viktor/StridedMatrix.kt#L66)

A common interface for whole-matrix operations.

### Functions

| Name | Summary |
|---|---|
| [argMax](arg-max.md) | `open fun argMax(): Int` |
| [argMin](arg-min.md) | `open fun argMin(): Int` |
| [checkDimensions](check-dimensions.md) | `abstract fun checkDimensions(other: T): Unit`<br>Ensures a given matrix has the same dimensions as this matrix. |
| [copy](copy.md) | `abstract fun copy(): T`<br>Returns the copy of this matrix. |
| [div](div.md) | `open operator fun div(other: T): <ERROR CLASS>`<br>`open operator fun div(update: Double): <ERROR CLASS>` |
| [divAssign](div-assign.md) | `open operator fun divAssign(other: T): Unit`<br>`open operator fun divAssign(update: Double): Unit` |
| [exp](exp.md) | `open fun exp(): <ERROR CLASS>` |
| [expInPlace](exp-in-place.md) | `open fun expInPlace(): Unit` |
| [expm1](expm1.md) | `open fun expm1(): <ERROR CLASS>` |
| [expm1InPlace](expm1-in-place.md) | `open fun expm1InPlace(): Unit` |
| [fill](fill.md) | `open fun fill(init: Double): Unit` |
| [flatten](flatten.md) | `abstract fun flatten(): `[`StridedVector`](../-strided-vector/index.md)<br>Returns a flat view of this matrix. |
| [log](log.md) | `open fun log(): <ERROR CLASS>` |
| [log1p](log1p.md) | `open fun log1p(): <ERROR CLASS>` |
| [log1pInPlace](log1p-in-place.md) | `open fun log1pInPlace(): Unit` |
| [logAddExp](log-add-exp.md) | `open infix fun logAddExp(other: T): T`<br>`open fun logAddExp(other: T, dst: T): Unit` |
| [logInPlace](log-in-place.md) | `open fun logInPlace(): Unit` |
| [logRescale](log-rescale.md) | `open fun logRescale(): Unit` |
| [logSumExp](log-sum-exp.md) | `open fun logSumExp(): Double` |
| [max](max.md) | `open fun max(): Double` |
| [mean](mean.md) | `open fun mean(): Double` |
| [min](min.md) | `open fun min(): Double` |
| [minus](minus.md) | `open operator fun minus(other: T): <ERROR CLASS>`<br>`open operator fun minus(update: Double): <ERROR CLASS>` |
| [minusAssign](minus-assign.md) | `open operator fun minusAssign(other: T): Unit`<br>`open operator fun minusAssign(update: Double): Unit` |
| [plus](plus.md) | `open operator fun plus(other: T): <ERROR CLASS>`<br>`open operator fun plus(update: Double): <ERROR CLASS>` |
| [plusAssign](plus-assign.md) | `open operator fun plusAssign(other: T): Unit`<br>`open operator fun plusAssign(update: Double): Unit` |
| [sum](sum.md) | `open fun sum(): Double` |
| [times](times.md) | `open operator fun times(other: T): <ERROR CLASS>`<br>`open operator fun times(update: Double): <ERROR CLASS>` |
| [timesAssign](times-assign.md) | `open operator fun timesAssign(other: T): Unit`<br>`open operator fun timesAssign(update: Double): Unit` |
| [unaryMinus](unary-minus.md) | `open operator fun unaryMinus(): <ERROR CLASS>` |
| [unaryPlus](unary-plus.md) | `open operator fun unaryPlus(): FlatMatrixOps<T>` |

### Inheritors

| Name | Summary |
|---|---|
| [StridedMatrix2](../-strided-matrix2/index.md) | `class StridedMatrix2 : FlatMatrixOps<`[`StridedMatrix2`](../-strided-matrix2/index.md)`>`<br>A specialization of [StridedMatrix](../-strided-matrix/index.md) for 2-D data. |
| [StridedMatrix3](../-strided-matrix3/index.md) | `class StridedMatrix3 : FlatMatrixOps<`[`StridedMatrix3`](../-strided-matrix3/index.md)`>`<br>A specialization of [StridedMatrix](../-strided-matrix/index.md) for 3-D data. |
