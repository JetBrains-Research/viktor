[0.3.2](../../index.md) / [org.jetbrains.bio.viktor](../index.md) / [NativeSpeedups](.)

# NativeSpeedups

`object NativeSpeedups` [(source)](https://github.com/JetBrains-Research/viktor/blob/0.3.2/src/main/kotlin/org/jetbrains/bio/viktor/NativeSpeedups.kt#L3)

### Functions

| Name | Summary |
|---|---|
| [cumSum](cum-sum.md) | `external fun cumSum(source: DoubleArray, sourceOffset: Int, dest: DoubleArray, destOffset: Int, length: Int): Unit` |
| [sd](sd.md) | `external fun sd(values: DoubleArray, offset: Int, length: Int): Double` |
| [sum](sum.md) | `external fun sum(values: DoubleArray, offset: Int, length: Int): Double` |
| [unsafeDiv](unsafe-div.md) | `external fun unsafeDiv(src1: DoubleArray, srcOffset1: Int, src2: DoubleArray, srcOffset2: Int, dst: DoubleArray, dstOffset: Int, length: Int): Unit` |
| [unsafeDivScalar](unsafe-div-scalar.md) | `external fun unsafeDivScalar(src1: DoubleArray, srcOffset1: Int, update: Double, dst: DoubleArray, dstOffset: Int, length: Int): Unit` |
| [unsafeDot](unsafe-dot.md) | `external fun unsafeDot(src1: DoubleArray, srcOffset1: Int, src2: DoubleArray, srcOffset2: Int, length: Int): Double` |
| [unsafeExp](unsafe-exp.md) | `external fun unsafeExp(src: DoubleArray, srcOffset: Int, dst: DoubleArray, dstOffset: Int, length: Int): Unit` |
| [unsafeExpm1](unsafe-expm1.md) | `external fun unsafeExpm1(src: DoubleArray, srcOffset: Int, dst: DoubleArray, dstOffset: Int, length: Int): Unit` |
| [unsafeLog](unsafe-log.md) | `external fun unsafeLog(src: DoubleArray, srcOffset: Int, dst: DoubleArray, dstOffset: Int, length: Int): Unit` |
| [unsafeLog1p](unsafe-log1p.md) | `external fun unsafeLog1p(src: DoubleArray, srcOffset: Int, dst: DoubleArray, dstOffset: Int, length: Int): Unit` |
| [unsafeLogAddExp](unsafe-log-add-exp.md) | `external fun unsafeLogAddExp(src1: DoubleArray, srcOffset1: Int, src2: DoubleArray, srcOffset2: Int, dst: DoubleArray, dstOffset: Int, length: Int): Unit` |
| [unsafeLogRescale](unsafe-log-rescale.md) | `external fun unsafeLogRescale(src: DoubleArray, srcOffset: Int, dst: DoubleArray, dstOffset: Int, length: Int): Unit` |
| [unsafeLogSumExp](unsafe-log-sum-exp.md) | `external fun unsafeLogSumExp(src: DoubleArray, srcOffset: Int, length: Int): Double` |
| [unsafeMax](unsafe-max.md) | `external fun unsafeMax(values: DoubleArray, offset: Int, length: Int): Double` |
| [unsafeMin](unsafe-min.md) | `external fun unsafeMin(values: DoubleArray, offset: Int, length: Int): Double` |
| [unsafeMinus](unsafe-minus.md) | `external fun unsafeMinus(src1: DoubleArray, srcOffset1: Int, src2: DoubleArray, srcOffset2: Int, dst: DoubleArray, dstOffset: Int, length: Int): Unit` |
| [unsafeMinusScalar](unsafe-minus-scalar.md) | `external fun unsafeMinusScalar(src1: DoubleArray, srcOffset1: Int, update: Double, dst: DoubleArray, dstOffset: Int, length: Int): Unit` |
| [unsafeNegate](unsafe-negate.md) | `external fun unsafeNegate(src1: DoubleArray, srcOffset1: Int, dst: DoubleArray, dstOffset: Int, length: Int): Unit` |
| [unsafePlus](unsafe-plus.md) | `external fun unsafePlus(src1: DoubleArray, srcOffset1: Int, src2: DoubleArray, srcOffset2: Int, dst: DoubleArray, dstOffset: Int, length: Int): Unit` |
| [unsafePlusScalar](unsafe-plus-scalar.md) | `external fun unsafePlusScalar(src1: DoubleArray, srcOffset1: Int, update: Double, dst: DoubleArray, dstOffset: Int, length: Int): Unit` |
| [unsafeScalarDiv](unsafe-scalar-div.md) | `external fun unsafeScalarDiv(update: Double, src1: DoubleArray, srcOffset1: Int, dst: DoubleArray, dstOffset: Int, length: Int): Unit` |
| [unsafeTimes](unsafe-times.md) | `external fun unsafeTimes(src1: DoubleArray, srcOffset1: Int, src2: DoubleArray, srcOffset2: Int, dst: DoubleArray, dstOffset: Int, length: Int): Unit` |
| [unsafeTimesScalar](unsafe-times-scalar.md) | `external fun unsafeTimesScalar(src1: DoubleArray, srcOffset1: Int, update: Double, dst: DoubleArray, dstOffset: Int, length: Int): Unit` |
| [weightedMean](weighted-mean.md) | `external fun weightedMean(values: DoubleArray, valuesOffset: Int, weights: DoubleArray, weightsOffset: Int, length: Int): Double` |
| [weightedSum](weighted-sum.md) | `external fun weightedSum(values: DoubleArray, valuesOffset: Int, weights: DoubleArray, weightsOffset: Int, length: Int): Double` |
