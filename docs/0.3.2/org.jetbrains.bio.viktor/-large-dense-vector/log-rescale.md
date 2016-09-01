[0.3.2](../../index.md) / [org.jetbrains.bio.viktor](../index.md) / [LargeDenseVector](index.md) / [logRescale](.)

# logRescale

`fun logRescale(): Unit` [(source)](https://github.com/JetBrains-Research/viktor/blob/0.3.2/src/main/kotlin/org/jetbrains/bio/viktor/DenseVector.kt#L99)

Overrides [StridedVector.logRescale](../-strided-vector/log-rescale.md)

Rescales the element so that the exponent of the sum is 1.0.

Optimized for dense vectors.

The operation is done **in place**.

