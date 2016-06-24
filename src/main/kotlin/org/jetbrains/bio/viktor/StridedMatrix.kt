package org.jetbrains.bio.viktor

/**
 * A strided matrix stored in a flat [DoubleArray].
 *
 * @author Sergei Lebedev
 * @since 0.1.0
 */
object StridedMatrix {
    operator fun invoke(numRows: Int, numColumns: Int): StridedMatrix2 {
        return StridedMatrix2(numRows, numColumns)
    }

    operator inline fun invoke(numRows: Int, numColumns: Int,
                               block: (Int, Int) -> Double): StridedMatrix2 {
        val m = StridedMatrix2(numRows, numColumns)
        for (r in 0..numRows - 1) {
            for (c in 0..numColumns - 1) {
                m[r, c] = block(r, c)
            }
        }

        return m
    }

    operator fun invoke(numRows: Int, numColumns: Int, depth: Int): StridedMatrix3 {
        return StridedMatrix3(numRows, numColumns, depth)
    }

    operator inline fun invoke(depth: Int, numRows: Int, numColumns: Int,
                               block: (Int, Int, Int) -> Double): StridedMatrix3 {
        val m = StridedMatrix3(depth, numRows, numColumns)
        for (d in 0..depth - 1) {
            for (r in 0..numRows - 1) {
                for (c in 0..numColumns - 1) {
                    m[d, r, c] = block(d, r, c)
                }
            }
        }

        return m
    }

    @JvmStatic fun full(numRows: Int, numColumns: Int,
                        init: Double): StridedMatrix2 {
        return StridedMatrix2(numRows, numColumns).apply { fill(init) }
    }

    @JvmStatic fun full(numRows: Int, numColumns: Int, depth: Int,
                        init: Double): StridedMatrix3 {
        return StridedMatrix3(numRows, numColumns, depth).apply { fill(init) }
    }

    /**
     * Creates a 2-D matrix with rows summing to one.
     */
    @JvmStatic fun stochastic(size: Int) = full(size, size, 1.0 / size)

    /**
     * Creates a 3-D matrix with [stochastic] submatrices.
     */
    @JvmStatic fun indexedStochastic(depth: Int, size: Int) = full(depth, size, size, 1.0 / size)
}
