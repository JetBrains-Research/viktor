[0.3.5](../../index.md) / [org.jetbrains.bio.viktor](../index.md) / [StridedMatrix2](index.md) / [get](.)

# get

`operator fun get(r: Int, c: Int): Double` [(source)](https://github.com/JetBrains-Research/viktor/blob/0.3.5/src/main/kotlin/org/jetbrains/bio/viktor/StridedMatrix2.kt#L37)

`operator fun get(r: Int): `[`StridedVector`](../-strided-vector/index.md) [(source)](https://github.com/JetBrains-Research/viktor/blob/0.3.5/src/main/kotlin/org/jetbrains/bio/viktor/StridedMatrix2.kt#L88)

A less-verbose alias to [rowView](row-view.md).

Please do NOT abuse this shortcut by double-indexing, i.e. dont
do `m[i][j]`, write `m[i, j]` instead.

`operator fun get(any: `[`_I`](../_-i.md)`, c: Int): `[`StridedVector`](../-strided-vector/index.md) [(source)](https://github.com/JetBrains-Research/viktor/blob/0.3.5/src/main/kotlin/org/jetbrains/bio/viktor/StridedMatrix2.kt#L99)

A less-verbose alias to [columnView](column-view.md).

Use in conjunction with [_I](../_-i.md), e.g. `m[_I, i]`.

