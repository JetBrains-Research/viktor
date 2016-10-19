package org.jetbrains.bio.viktor

import java.util.*

/**
 * A strided matrix stored in a flat [DoubleArray].
 *
 * @author Sergei Lebedev
 * @since 0.1.0
 */
open class F64Matrix(
        val data: DoubleArray,
        val offset: Int,
        val strides: IntArray,
        val shape: IntArray) {

    val nDim: Int get() = shape.size

    /** Number of elements along the first dimension. */
    val size: Int get() = shape.first()

    /**
     * Dense matrices are laid out in a single contiguous block
     * of memory.
     *
     * This allows to use SIMD operations, e.g. when computing the
     * sum of elements.
     */
    internal val isDense: Boolean get() {
        // This is inaccurate, but maybe sufficient for our use-case?
        // Check with http://docs.scipy.org/doc/numpy/reference/arrays.ndarray.html
        return strides.last() == 1
    }

    /**
     * Flattens the matrix into a vector in O(1) time.
     *
     * No data copying is performed, thus the operation is only applicable
     * to dense matrices.
     */
    fun flatten(): F64Vector {
        check(isDense) { "matrix is not dense" }
        return data.asVector(offset, shape.reduce { a, b -> a * b })
    }

    /** Ensures a given matrix has the same dimensions as this matrix. */
    fun checkDimensions(other: F64Matrix) {
        check(this === other || Arrays.equals(shape, other.shape)) { "non-conformable matrices" }
    }

    companion object {
        operator fun invoke(numRows: Int, numColumns: Int): F64Matrix2 {
            return F64Matrix2(numRows, numColumns)
        }

        operator inline fun invoke(numRows: Int, numColumns: Int,
                                   block: (Int, Int) -> Double): F64Matrix2 {
            val m = F64Matrix2(numRows, numColumns)
            for (r in 0..numRows - 1) {
                for (c in 0..numColumns - 1) {
                    m[r, c] = block(r, c)
                }
            }

            return m
        }

        operator fun invoke(numRows: Int, numColumns: Int, depth: Int): F64Matrix3 {
            return F64Matrix3(numRows, numColumns, depth)
        }

        operator inline fun invoke(depth: Int, numRows: Int, numColumns: Int,
                                   block: (Int, Int, Int) -> Double): F64Matrix3 {
            val m = F64Matrix3(depth, numRows, numColumns)
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
                            init: Double): F64Matrix2 {
            return F64Matrix2(numRows, numColumns).apply { fill(init) }
        }

        @JvmStatic fun full(numRows: Int, numColumns: Int, depth: Int,
                            init: Double): F64Matrix3 {
            return F64Matrix3(numRows, numColumns, depth).apply { fill(init) }
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
}

/** A common interface for whole-matrix operations. */
interface F64MatrixOps<SELF> where SELF: F64MatrixOps<SELF>, SELF: F64Matrix {
    fun F64Vector.reshapeLike(other: SELF): SELF

    /** Purely to please the type checker. */
    fun unwrap(): SELF

    /** Returns the copy of this matrix. */
    fun copy(): SELF

    fun fill(init: Double) = unwrap().flatten().fill(init)

    fun mean() = unwrap().flatten().mean()

    fun sum() = unwrap().flatten().sum()

    fun max() = unwrap().flatten().max()

    fun argMax() = unwrap().flatten().argMax()

    fun min() = unwrap().flatten().min()

    fun argMin() = unwrap().flatten().argMin()

    fun logSumExp() = unwrap().flatten().logSumExp()

    fun logRescale() = unwrap().flatten().logRescale()

    fun expInPlace() = unwrap().flatten().expInPlace()

    fun exp() = copy().apply { expm1InPlace() }

    fun expm1InPlace() = unwrap().flatten().expInPlace()

    fun expm1() = copy().apply { expm1InPlace() }

    fun logInPlace() = unwrap().flatten().logInPlace()

    fun log() = copy().apply { logInPlace() }

    fun log1pInPlace() = unwrap().flatten().logInPlace()

    fun log1p() = copy().apply { log1pInPlace() }

    infix fun logAddExp(other: SELF): SELF = copy().apply { logAddExp(other, this) }

    fun logAddExp(other: SELF, dst: SELF) {
        unwrap().checkDimensions(other)
        unwrap().checkDimensions(dst)
        unwrap().flatten().logAddExp(other.unwrap().flatten(), dst.unwrap().flatten())
    }

    operator fun unaryPlus() = this

    operator fun unaryMinus() = copy().apply { (-unwrap().flatten()).reshapeLike(this) }

    operator fun plus(other: SELF) = copy().apply { this += other }

    operator fun plusAssign(other: SELF) {
        unwrap().checkDimensions(other)
        unwrap().flatten() += other.unwrap().flatten()
    }

    operator fun plus(update: Double) = copy().apply { this += update }

    operator fun plusAssign(update: Double) {
        unwrap().flatten() += update
    }

    operator fun minus(other: SELF) = copy().apply { this -= other }

    operator fun minusAssign(other: SELF) {
        unwrap().checkDimensions(other)
        unwrap().flatten() -= other.unwrap().flatten()
    }

    operator fun minus(update: Double) = copy().apply { this -= update }

    operator fun minusAssign(update: Double) {
        unwrap().flatten() -= update
    }

    operator fun times(other: SELF) = copy().apply { this *= other }

    operator fun timesAssign(other: SELF) {
        unwrap().checkDimensions(other)
        unwrap().flatten() *= other.unwrap().flatten()
    }

    operator fun times(update: Double) = copy().apply { this *= update }

    operator fun timesAssign(update: Double) {
        unwrap().flatten() *= update
    }

    operator fun div(other: SELF) = copy().apply { this /= other }

    operator fun divAssign(other: SELF) {
        unwrap().checkDimensions(other)
        unwrap().flatten() /= other.unwrap().flatten()
    }

    operator fun div(update: Double) = copy().apply { this /= update }

    operator fun divAssign(update: Double) {
        unwrap().flatten() /= update
    }
}
