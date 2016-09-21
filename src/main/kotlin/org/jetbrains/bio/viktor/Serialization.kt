package org.jetbrains.bio.viktor

import org.jetbrains.bio.npy.NpyArray
import org.jetbrains.bio.npy.NpyFile
import org.jetbrains.bio.npy.NpzFile
import java.nio.file.Path

/** Returns a view of the [NpyArray] as a strided vector. */
fun NpyArray.asStridedVector() = asDoubleArray().asStrided()

/** Returns a view of the [NpyArray] as a 2-D strided matrix. */
fun NpyArray.asStridedMatrix2(): StridedMatrix2 {
    val (numRows, numColumns) = shape
    return asStridedVector().reshape(numRows, numColumns)
}

/** Returns a view of the [NpyArray] as a 3-D strided matrix. */
fun NpyArray.asStridedMatrix3(): StridedMatrix3 {
    val (depth, numRows, numColumns) = shape
    return asStridedVector().reshape(depth, numRows, numColumns)
}

/** Writes a given vector to [path] in NPY format. */
fun NpyFile.write(path: Path, v: StridedVector) {
    write(path, v.toArray(), v.shape)
}

/** Writes a given 2-D matrix to [path] in NPY format. */
fun NpyFile.write(path: Path, m: StridedMatrix2) {
    write(path, m.flatten().toArray(), shape = m.shape)
}

/** Writes a given 3-D matrix to [path] in NPY format. */
fun NpyFile.write(path: Path, m: StridedMatrix3) {
    write(path, m.flatten().toArray(), m.shape)
}

/** Adds a given vector to an NPZ format under the specified [name]. */
fun NpzFile.Writer.write(name: String, v: StridedVector) {
    write(name, v.toArray(), v.shape)
}

/** Writes a given 2-D matrix into an NPZ file under the specified [name]. */
fun NpzFile.Writer.write(name: String, m: StridedMatrix2) {
    write(name, m.flatten().toArray(), m.shape)
}

/** Writes a given 3-D matrix into an NPZ file under the specified [name]. */
fun NpzFile.Writer.write(name: String, m: StridedMatrix3) {
    write(name, m.flatten().toArray(), m.shape)
}