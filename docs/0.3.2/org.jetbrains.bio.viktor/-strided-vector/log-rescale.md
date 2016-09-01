[0.3.2](../../index.md) / [org.jetbrains.bio.viktor](../index.md) / [StridedVector](index.md) / [logRescale](.)

# logRescale

`open fun logRescale(): Unit` [(source)](https://github.com/JetBrains-Research/viktor/blob/0.3.2/src/main/kotlin/org/jetbrains/bio/viktor/StridedVector.kt#L305)

Rescales the element so that the exponent of the sum is 1.0.

Optimized for dense vectors.

The operation is done **in place**.

