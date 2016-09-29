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

/** A common interface for whole-matrix operations. */
interface FlatMatrixOps<T : FlatMatrixOps<T>> {
    /**
     * Returns a flat view of this matrix.
     *
     * If the matrix is not dense the method must raise an error.
     */
    fun flatten(): StridedVector

    /** Returns the copy of this matrix. */
    fun copy(): T

    /** Ensures a given matrix has the same dimensions as this matrix. */
    fun checkDimensions(other: T)

    fun fill(init: Double) = flatten().fill(init)

    fun mean() = flatten().mean()

    fun sum() = flatten().sum()

    fun max() = flatten().max()

    fun argMax() = flatten().argMax()

    fun min() = flatten().min()

    fun argMin() = flatten().argMin()

    fun logSumExp() = flatten().logSumExp()

    fun logRescale() = flatten().logRescale()

    fun expInPlace() = flatten().expInPlace()

    fun exp() = copy().apply { expm1InPlace() }

    fun expm1InPlace() = flatten().expInPlace()

    fun expm1() = copy().apply { expm1InPlace() }

    fun logInPlace() = flatten().logInPlace()

    fun log() = copy().apply { logInPlace() }

    fun log1pInPlace() = flatten().logInPlace()

    fun log1p() = copy().apply { log1pInPlace() }

    infix fun logAddExp(other: T): T = copy().apply { logAddExp(other, this) }

    fun logAddExp(other: T, dst: T) {
        checkDimensions(other)
        checkDimensions(dst)
        flatten().logAddExp(other.flatten(), dst.flatten())
    }

    operator fun unaryPlus() = this

    operator fun unaryMinus() = copy().apply {
        val v = flatten()

        // XXX this might be slower for small matrices.
        NativeSpeedups.unsafeNegate(v.data, v.offset, v.data, v.offset, v.size)
    }

    operator fun plus(other: T) = copy().apply { this += other }

    operator fun plusAssign(other: T) {
        checkDimensions(other)
        flatten() += other.flatten()
    }

    operator fun plus(update: Double) = copy().apply { this += update }

    operator fun plusAssign(update: Double) {
        flatten() += update
    }

    operator fun minus(other: T) = copy().apply { this -= other }

    operator fun minusAssign(other: T) {
        checkDimensions(other)
        flatten() -= other.flatten()
    }

    operator fun minus(update: Double) = copy().apply { this -= update }

    operator fun minusAssign(update: Double) {
        flatten() -= update
    }

    operator fun times(other: T) = copy().apply { this *= other }

    operator fun timesAssign(other: T) {
        checkDimensions(other)
        flatten() *= other.flatten()
    }

    operator fun times(update: Double) = copy().apply { this *= update }

    operator fun timesAssign(update: Double) {
        flatten() *= update
    }

    operator fun div(other: T) = copy().apply { this /= other }

    operator fun divAssign(other: T) {
        checkDimensions(other)
        flatten() /= other.flatten()
    }

    operator fun div(update: Double) = copy().apply { this /= update }

    operator fun divAssign(update: Double) {
        flatten() /= update
    }
}