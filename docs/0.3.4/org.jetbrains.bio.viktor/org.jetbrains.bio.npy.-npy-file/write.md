[0.3.4](../../index.md) / [org.jetbrains.bio.viktor](../index.md) / [org.jetbrains.bio.npy.NpyFile](index.md) / [write](.)

# write

`fun NpyFile.write(path: `[`Path`](http://docs.oracle.com/javase/6/docs/api/java/nio/file/Path.html)`, v: `[`StridedVector`](../-strided-vector/index.md)`): Unit` [(source)](https://github.com/JetBrains-Research/viktor/blob/0.3.4/src/main/kotlin/org/jetbrains/bio/viktor/Serialization.kt#L24)

Writes a given vector to [path](write.md#org.jetbrains.bio.viktor$write(org.jetbrains.bio.npy.NpyFile, java.nio.file.Path, org.jetbrains.bio.viktor.StridedVector)/path) in NPY format.

`fun NpyFile.write(path: `[`Path`](http://docs.oracle.com/javase/6/docs/api/java/nio/file/Path.html)`, m: `[`StridedMatrix2`](../-strided-matrix2/index.md)`): Unit` [(source)](https://github.com/JetBrains-Research/viktor/blob/0.3.4/src/main/kotlin/org/jetbrains/bio/viktor/Serialization.kt#L29)

Writes a given 2-D matrix to [path](write.md#org.jetbrains.bio.viktor$write(org.jetbrains.bio.npy.NpyFile, java.nio.file.Path, org.jetbrains.bio.viktor.StridedMatrix2)/path) in NPY format.

`fun NpyFile.write(path: `[`Path`](http://docs.oracle.com/javase/6/docs/api/java/nio/file/Path.html)`, m: `[`StridedMatrix3`](../-strided-matrix3/index.md)`): Unit` [(source)](https://github.com/JetBrains-Research/viktor/blob/0.3.4/src/main/kotlin/org/jetbrains/bio/viktor/Serialization.kt#L34)

Writes a given 3-D matrix to [path](write.md#org.jetbrains.bio.viktor$write(org.jetbrains.bio.npy.NpyFile, java.nio.file.Path, org.jetbrains.bio.viktor.StridedMatrix3)/path) in NPY format.

