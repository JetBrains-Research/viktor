[0.3.2](../../index.md) / [org.jetbrains.bio.viktor](../index.md) / [kotlin.Double](index.md) / [logAddExp](.)

# logAddExp

`infix fun Double.logAddExp(b: Double): Double` [(source)](https://github.com/JetBrains-Research/viktor/blob/0.3.2/src/main/kotlin/org/jetbrains/bio/viktor/MoreMath.kt#L12)

Evaluates log(exp(a) + exp(b)) using the following trick

log(exp(a) + log(exp(b)) = a + log(1 + exp(b - a))

assuming a &gt;= b.

