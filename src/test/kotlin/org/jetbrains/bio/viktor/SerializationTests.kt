package org.jetbrains.bio.viktor

import org.jetbrains.bio.npy.NpyFile
import org.jetbrains.bio.npy.NpzFile
import org.junit.Test
import kotlin.test.assertEquals

class TestReadWriteNpy {
    @Test fun vector() = withTempFile("v", ".npy") { path ->
        val v = StridedVector.of(1.0, 2.0, 3.0, 4.0)
        NpyFile.write(path, v)
        assertEquals(v, NpyFile.read(path).asStridedVector())
    }

    @Test fun matrix2() = withTempFile("m2", ".npy") { path ->
        val m = StridedVector.of(1.0, 2.0, 3.0, 4.0, 5.0, 6.0).reshape(2, 3)
        NpyFile.write(path, m)
        assertEquals(m, NpyFile.read(path).asStridedMatrix2())
    }

    @Test fun matrix3() = withTempFile("m3", ".npy") { path ->
        val m = StridedVector.of(1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0)
                .reshape(1, 4, 2)
        NpyFile.write(path, m)
        assertEquals(m, NpyFile.read(path).asStridedMatrix3())
    }
}

class TestReadWriteNpz {
    @Test fun combined() {
        val v = StridedVector.of(1.0, 2.0, 3.0, 4.0)
        val m2 = StridedVector.of(1.0, 2.0, 3.0, 4.0, 5.0, 6.0).reshape(2, 3)
        val m3 = StridedVector.of(1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0)
                .reshape(1, 4, 2)

        withTempFile("vm2m3", ".npz") { path ->
            NpzFile.write(path).use {
                it.write("v", v)
                it.write("m2", m2)
                it.write("m3", m3)
            }

            NpzFile.read(path).use {
                assertEquals(v, it["v"].asStridedVector())
                assertEquals(m2, it["m2"].asStridedMatrix2())
                assertEquals(m3, it["m3"].asStridedMatrix3())
            }
        }
    }
}