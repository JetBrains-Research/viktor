[0.3.4](../../index.md) / [org.jetbrains.bio.viktor](../index.md) / [KahanSum](.)

# KahanSum

`class KahanSum` [(source)](https://github.com/JetBrains-Research/viktor/blob/0.3.4/src/main/kotlin/org/jetbrains/bio/viktor/MoreMath.kt#L29)

Kahan-Babuska summation.

See http://cage.ugent.be/~klein/papers/floating-point.pdf for details.

**Author**
Alexey Dievsky

**Since**
0.1.0

### Constructors

| Name | Summary |
|---|---|
| [&lt;init&gt;](-init-.md) | `KahanSum(accumulator: Double = 0.0)`<br>Kahan-Babuska summation. |

### Functions

| Name | Summary |
|---|---|
| [feed](feed.md) | `fun feed(value: Double): KahanSum`<br>Supplies a number to be added to the accumulator. |
| [plusAssign](plus-assign.md) | `operator fun plusAssign(value: Double): Unit` |
| [result](result.md) | `fun result(): Double`<br>Returns the sum accumulated so far. |
