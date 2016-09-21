[0.3.4](../../index.md) / [org.jetbrains.bio.viktor](../index.md) / [org.jetbrains.bio.npy.NpzFile.Writer](index.md) / [write](.)

# write

`fun Writer.write(name: String, v: `[`StridedVector`](../-strided-vector/index.md)`): Unit` [(source)](https://github.com/JetBrains-Research/viktor/blob/0.3.4/src/main/kotlin/org/jetbrains/bio/viktor/Serialization.kt#L39)

Adds a given vector to an NPZ format under the specified [name](write.md#org.jetbrains.bio.viktor$write(org.jetbrains.bio.npy.NpzFile.Writer, kotlin.String, org.jetbrains.bio.viktor.StridedVector)/name).

`fun Writer.write(name: String, m: `[`StridedMatrix2`](../-strided-matrix2/index.md)`): Unit` [(source)](https://github.com/JetBrains-Research/viktor/blob/0.3.4/src/main/kotlin/org/jetbrains/bio/viktor/Serialization.kt#L44)

Writes a given 2-D matrix into an NPZ file under the specified [name](write.md#org.jetbrains.bio.viktor$write(org.jetbrains.bio.npy.NpzFile.Writer, kotlin.String, org.jetbrains.bio.viktor.StridedMatrix2)/name).

`fun Writer.write(name: String, m: `[`StridedMatrix3`](../-strided-matrix3/index.md)`): Unit` [(source)](https://github.com/JetBrains-Research/viktor/blob/0.3.4/src/main/kotlin/org/jetbrains/bio/viktor/Serialization.kt#L49)

Writes a given 3-D matrix into an NPZ file under the specified [name](write.md#org.jetbrains.bio.viktor$write(org.jetbrains.bio.npy.NpzFile.Writer, kotlin.String, org.jetbrains.bio.viktor.StridedMatrix3)/name).

