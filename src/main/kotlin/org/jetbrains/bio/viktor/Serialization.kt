package org.jetbrains.bio.viktor

import org.jetbrains.bio.npy.NpyArray
import org.jetbrains.bio.npy.NpyFile
import org.jetbrains.bio.npy.NpzFile
import java.nio.file.Path

/** Returns a view of the [NpyArray] as a strided vector. */
fun NpyArray.asStridedVector() = asDoubleArray().asVector()

/** Returns a view of the [NpyArray] as a 2-D strided matrix. */
fun NpyArray.asStridedMatrix2(): F64Matrix2 {
    val (numRows, numColumns) = shape
    return asStridedVector().reshape(numRows, numColumns)
}

/** Returns a view of the [NpyArray] as a 3-D strided matrix. */
fun NpyArray.asStridedMatrix3(): F64Matrix3 {
    val (depth, numRows, numColumns) = shape
    return asStridedVector().reshape(depth, numRows, numColumns)
}

/** Writes a given vector to [path] in NPY format. */
fun NpyFile.write(path: Path, v: F64Vector) {
    write(path, v.toArray(), v.shape)
}

/** Writes a given 2-D matrix to [path] in NPY format. */
fun NpyFile.write(path: Path, m: F64Matrix2) {
    // We could getaway without doing a double copy of tranposed
    // matrices here once `npy` supports Fortran order.
    val dense = if (m.isDense) m else m.copy()
    write(path, dense.flatten().toArray(), shape = m.shape)
}

/** Writes a given 3-D matrix to [path] in NPY format. */
fun NpyFile.write(path: Path, m: F64Matrix3) {
    val dense = if (m.isDense) m else m.copy()
    write(path, dense.flatten().toArray(), shape = m.shape)
}

/** Adds a given vector to an NPZ format under the specified [name]. */
fun NpzFile.Writer.write(name: String, v: F64Vector) {
    write(name, v.toArray(), v.shape)
}

/** Writes a given 2-D matrix into an NPZ file under the specified [name]. */
fun NpzFile.Writer.write(name: String, m: F64Matrix2) {
    val dense = if (m.isDense) m else m.copy()
    write(name, dense.flatten().toArray(), shape = m.shape)
}

/** Writes a given 3-D matrix into an NPZ file under the specified [name]. */
fun NpzFile.Writer.write(name: String, m: F64Matrix3) {
    val dense = if (m.isDense) m else m.copy()
    write(name, dense.flatten().toArray(), shape = m.shape)
}