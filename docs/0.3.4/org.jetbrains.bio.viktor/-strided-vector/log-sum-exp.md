[0.3.4](../../index.md) / [org.jetbrains.bio.viktor](../index.md) / [StridedVector](index.md) / [logSumExp](.)

# logSumExp

`open fun logSumExp(): Double` [(source)](https://github.com/JetBrains-Research/viktor/blob/0.3.4/src/main/kotlin/org/jetbrains/bio/viktor/StridedVector.kt#L323)

Computes

log(exp(v[0](#)) + ... + exp(v[size-1](#)))

in a numerically stable way.

