[0.3.5](../../index.md) / [org.jetbrains.bio.viktor](../index.md) / [StridedMatrix2](index.md) / [along](.)

# along

`fun along(axis: Int): `[`Stream`](http://docs.oracle.com/javase/6/docs/api/java/util/stream/Stream.html)`<`[`StridedVector`](../-strided-vector/index.md)`>` [(source)](https://github.com/JetBrains-Research/viktor/blob/0.3.5/src/main/kotlin/org/jetbrains/bio/viktor/StridedMatrix2.kt#L148)

Returns a stream of row or column views of the matrix.

### Parameters

`axis` - axis to go along, 0 stands for columns, 1 for rows.