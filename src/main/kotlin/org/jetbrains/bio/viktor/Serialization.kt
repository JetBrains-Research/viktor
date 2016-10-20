package org.jetbrains.bio.viktor

import org.jetbrains.bio.npy.NpyArray
import org.jetbrains.bio.npy.NpyFile
import org.jetbrains.bio.npy.NpzFile
import java.nio.file.Path

/** Returns a view of the [NpyArray] as an n-dimensional array. */
fun NpyArray.asF64Array() = asDoubleArray().asVector().reshape(*shape)

/** Writes a given matrix to [path] in NPY format. */
fun NpyFile.write(path: Path, a: F64Array) {
    // We could getaway without doing a double copy of tranposed
    // matrices here once `npy` supports Fortran order.
    val dense = if (a.isDense) a else a.copy()
    write(path, dense.flatten().toArray(), shape = a.shape)
}

/** Writes a given array into an NPZ file under the specified [name]. */
fun NpzFile.Writer.write(name: String, a: F64Array) {
    val dense = if (a.isDense) a else a.copy()
    write(name, dense.flatten().toArray(), shape = a.shape)
}