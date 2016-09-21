[0.3.5](../index.md) / [org.jetbrains.bio.viktor](index.md) / [quantile](.)

# quantile

`fun `[`StridedVector`](-strided-vector/index.md)`.quantile(q: Double = 0.5, randomGenerator: RandomGenerator = DEFAULT_RANDOM): Double` [(source)](https://github.com/JetBrains-Research/viktor/blob/0.3.5/src/main/kotlin/org/jetbrains/bio/viktor/Random.kt#L51)

Computes the [q](quantile.md#org.jetbrains.bio.viktor$quantile(org.jetbrains.bio.viktor.StridedVector, kotlin.Double, org.apache.commons.math3.random.RandomGenerator)/q)-th order statistic over this vector.

The implementation follows that of Commons Math. See JavaDoc of
[Percentile](#) for computational details.

The vector is modified in-place. Do a [copy](#) of the vector
to avoid mutation if necessary.

**Since**
0.2.0

