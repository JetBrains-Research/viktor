package org.jetbrains.bio.viktor

import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

public class StridedMatrix2Test {
    @Test fun testIndex() {
        val m = StridedMatrix.full(NUM_ROWS, NUM_COLUMNS, -1.0)
        for (r in 0..NUM_ROWS - 1) {
            for (c in 0..NUM_COLUMNS - 1) {
                m[r, c] = 1.0
            }
        }

        assertEquals(NUM_ROWS * NUM_COLUMNS, m.sum().toInt())
    }

    @Test fun testGetSet() {
        val m = getMatrix(NUM_ROWS, NUM_COLUMNS)
        for (r in 0..NUM_ROWS - 1) {
            for (c in 0..NUM_COLUMNS - 1) {
                assertEquals(r.toDouble() * NUM_COLUMNS + c, m[r, c])
            }
        }
    }

    @Test fun testRowView() {
        val m = getMatrix(NUM_ROWS, NUM_COLUMNS)
        for (r in 0..NUM_ROWS - 1) {
            val view = m.rowView(r)
            for (c in 0..NUM_COLUMNS - 1) {
                assertEquals(view[c], m[r, c])
            }
        }
    }

    @Test fun testColumnView() {
        val m = getMatrix(NUM_ROWS, NUM_COLUMNS)
        for (c in 0..NUM_COLUMNS - 1) {
            val view = m.columnView(c)
            for (r in 0..NUM_ROWS - 1) {
                assertEquals(view[r], m[r, c])
            }
        }
    }

    @Test fun testTranspose() {
        val m = getMatrix(NUM_ROWS, NUM_COLUMNS)
        val mt = m.transpose()
        for (r in 0..NUM_ROWS - 1) {
            for (c in 0..NUM_COLUMNS - 1) {
                assertEquals(m[r, c], mt[c, r])
            }
        }
    }

    @Test fun testCopyToFast() {
        val src = getMatrix(NUM_ROWS, NUM_COLUMNS)
        val dst = StridedMatrix(NUM_ROWS, NUM_COLUMNS)
        src.copyTo(dst)
        assertEquals(src, dst)
    }

    @Test fun testCopyToSlow() {
        val src = getMatrix(NUM_ROWS, NUM_COLUMNS)
        val dst = StridedMatrix(NUM_COLUMNS, NUM_ROWS).transpose()
        src.copyTo(dst)
        assertEquals(src, dst)
    }

    @Test fun testAlong() {
        val m = StridedMatrix(NUM_ROWS, NUM_COLUMNS)
        assertTrue(m.along(0).allMatch { it.size() == NUM_ROWS })
        assertTrue(m.along(1).allMatch { it.size() == NUM_COLUMNS })
    }

    private fun getMatrix(numRows: Int, numColumns: Int): StridedMatrix2 {
        return StridedMatrix(numRows, numColumns) { r, c ->
            (r * numColumns + c).toDouble()
        }
    }

    companion object {
        val NUM_ROWS = 3
        val NUM_COLUMNS = 5
    }
}

public class StridedMatrix3Test {
    @Test fun testIndex() {
        val m = getMatrix()
        m.fill(-1.0)

        for (d in 0..m.depth - 1) {
            for (r in 0..m.rowsNumber - 1) {
                for (c in 0..m.columnsNumber - 1) {
                    m[d, r, c] = 1.0
                }
            }
        }

        assertEquals(m.depth * m.rowsNumber * m.columnsNumber,
                     m.sum().toInt())
    }

    @Test fun testGetSet() {
        val m = getMatrix()
        var i = 0
        for (d in 0..m.depth - 1) {
            for (r in 0..m.rowsNumber - 1) {
                for (c in 0..m.columnsNumber - 1) {
                    assertEquals(i++, m[d, r, c].toInt())
                }
            }
        }
    }

    @Test fun testView() {
        val m = getMatrix()
        for (d in 0..m.depth - 1) {
            val view = m.view(d)
            for (r in 0..m.rowsNumber - 1) {
                for (c in 0..m.columnsNumber - 1) {
                    assertEquals(m[d, r, c], view[r, c])
                }
            }
        }
    }

    @Test fun testViewAssignment() {
        val magic = 100500.0
        val m = StridedMatrix(3, 4, 5)
        for (d in 0..m.depth - 1) {
            val copy = m.copy()
            copy[d] = magic
            assertEquals(StridedMatrix.full(m.rowsNumber, m.columnsNumber, magic),
                         copy[d])

            for (other in 0..m.depth - 1) {
                if (other != d) {
                    for (value in copy[other].flatten().toArray()) {
                        assertNotEquals(magic, value)
                    }
                }
            }
        }
    }

    private fun getMatrix(): StridedMatrix3 {
        var i = 0
        return StridedMatrix(7, 4, 3) { d, r, c -> i++.toDouble() }
    }
}