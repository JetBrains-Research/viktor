[0.3.5](../../index.md) / [org.jetbrains.bio.viktor](../index.md) / [LargeDenseVector](index.md) / [logSumExp](.)

# logSumExp

`fun logSumExp(): Double` [(source)](https://github.com/JetBrains-Research/viktor/blob/0.3.5/src/main/kotlin/org/jetbrains/bio/viktor/DenseVector.kt#L103)

Overrides [StridedVector.logSumExp](../-strided-vector/log-sum-exp.md)

Computes

log(exp(v[0](#)) + ... + exp(v[size-1](#)))

in a numerically stable way.

