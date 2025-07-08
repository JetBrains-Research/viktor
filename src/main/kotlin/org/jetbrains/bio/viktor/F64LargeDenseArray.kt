package org.jetbrains.bio.viktor

/**
 * A contiguous vector of size at least `[F64DenseFlatArray.DENSE_SPLIT_SIZE] + 1`.
 *
 * @author Sergei Lebedev
 * @since 0.1.0
 */
//internal class F64LargeDenseArray(
//    data: DoubleArray,
//    offset: Int,
//    size: Int
//) : F64DenseFlatArray(data, offset, size) {
//
//    // Import Vector API
//    private val vectorAPI = jdk.incubator.vector.DoubleVector.SPECIES_PREFERRED
//
//    private inline fun vectorTransformInPlace(crossinline op: (Double) -> Double) {
//        val dst = data
//        var dstOffset = offset
//        val dstEnd = dstOffset + length
//
//        // Process vectors
//        val vectorSize = vectorAPI.length()
//        val vectorLimit = dstOffset + (length / vectorSize) * vectorSize
//
//        while (dstOffset < vectorLimit) {
//            var vector = jdk.incubator.vector.DoubleVector.fromArray(vectorAPI, dst, dstOffset)
//            // Apply operation to each element
//            for (i in 0 until vectorSize) {
//                val value = vector.lane(i)
//                vector = vector.withLane(i, op(value))
//            }
//            vector.intoArray(dst, dstOffset)
//            dstOffset += vectorSize
//        }
//
//        // Process remaining elements
//        while (dstOffset < dstEnd) {
//            dst[dstOffset] = op(dst[dstOffset])
//            dstOffset++
//        }
//    }
//
//    private inline fun vectorTransform(crossinline op: (Double) -> Double): F64FlatArray {
//        val dst = DoubleArray(length)
//        val src = data
//        var srcOffset = offset
//        val length = length
//
//        // Process vectors
//        val vectorSize = vectorAPI.length()
//        val vectorLimit = srcOffset + (length / vectorSize) * vectorSize
//
//        var dstIndex = 0
//        while (srcOffset < vectorLimit) {
//            val vector = jdk.incubator.vector.DoubleVector.fromArray(vectorAPI, src, srcOffset)
//            var result = jdk.incubator.vector.DoubleVector.zero(vectorAPI)
//            // Apply operation to each element
//            for (i in 0 until vectorSize) {
//                val value = vector.lane(i)
//                result = result.withLane(i, op(value))
//            }
//            result.intoArray(dst, dstIndex)
//            srcOffset += vectorSize
//            dstIndex += vectorSize
//        }
//
//        // Process remaining elements
//        while (srcOffset < offset + length) {
//            dst[dstIndex] = op(src[srcOffset])
//            srcOffset++
//            dstIndex++
//        }
//
//        return create(dst, 0, this.length)
//    }
//
//    private inline fun vectorEBEInPlace(other: F64DenseFlatArray, crossinline op: (Double, Double) -> Double) {
//        val dst = data
//        val src = other.data
//        var dstOffset = offset
//        var srcOffset = other.offset
//        val length = length
//
//        // Process vectors
//        val vectorSize = vectorAPI.length()
//        val vectorLimit = dstOffset + (length / vectorSize) * vectorSize
//
//        while (dstOffset < vectorLimit) {
//            var vector1 = jdk.incubator.vector.DoubleVector.fromArray(vectorAPI, dst, dstOffset)
//            val vector2 = jdk.incubator.vector.DoubleVector.fromArray(vectorAPI, src, srcOffset)
//
//            // Apply the operation element by element
//            for (i in 0 until vectorSize) {
//                val v1 = vector1.lane(i)
//                val v2 = vector2.lane(i)
//                vector1 = vector1.withLane(i, op(v1, v2))
//            }
//
//            vector1.intoArray(dst, dstOffset)
//            dstOffset += vectorSize
//            srcOffset += vectorSize
//        }
//
//        // Process remaining elements
//        val dstEnd = offset + length
//        while (dstOffset < dstEnd) {
//            dst[dstOffset] = op(dst[dstOffset], src[srcOffset])
//            dstOffset++
//            srcOffset++
//        }
//    }
//
//    private inline fun vectorEBE(other: F64DenseFlatArray, crossinline op: (Double, Double) -> Double): F64DenseFlatArray {
//        val dst = DoubleArray(length)
//        val src1 = data
//        val src2 = other.data
//        var src1Offset = offset
//        var src2Offset = other.offset
//        val length = length
//
//        // Process vectors
//        val vectorSize = vectorAPI.length()
//        val vectorLimit = src1Offset + (length / vectorSize) * vectorSize
//
//        var dstIndex = 0
//        while (src1Offset < vectorLimit) {
//            val vector1 = jdk.incubator.vector.DoubleVector.fromArray(vectorAPI, src1, src1Offset)
//            val vector2 = jdk.incubator.vector.DoubleVector.fromArray(vectorAPI, src2, src2Offset)
//            var result = jdk.incubator.vector.DoubleVector.zero(vectorAPI)
//
//            // Apply the operation element by element
//            for (i in 0 until vectorSize) {
//                val v1 = vector1.lane(i)
//                val v2 = vector2.lane(i)
//                result = result.withLane(i, op(v1, v2))
//            }
//
//            result.intoArray(dst, dstIndex)
//            src1Offset += vectorSize
//            src2Offset += vectorSize
//            dstIndex += vectorSize
//        }
//
//        // Process remaining elements
//        val src1End = offset + length
//        while (src1Offset < src1End) {
//            dst[dstIndex] = op(src1[src1Offset], src2[src2Offset])
//            src1Offset++
//            src2Offset++
//            dstIndex++
//        }
//
//        return create(dst, 0, this.length)
//    }
//
//    override fun transformInPlace(op: (Double) -> Double) {
//        vectorTransformInPlace(op)
//    }
//
//    override fun transform(op: (Double) -> Double): F64FlatArray {
//        return vectorTransform(op)
//    }
//
//    override fun <T> fold(initial: T, op: (T, Double) -> T): T {
//        var res = initial
//        val dst = data
//        var dstOffset = offset
//        val dstEnd = dstOffset + length
//
//        while (dstOffset < dstEnd) {
//            res = op.invoke(res, dst[dstOffset])
//            dstOffset++
//        }
//
//        return res
//    }
//
//    override fun reduce(op: (Double, Double) -> Double): Double {
//        val dst = data
//        var dstOffset = offset
//        val dstEnd = dstOffset + length
//        var res = dst[dstOffset]
//        dstOffset++
//
//        while (dstOffset < dstEnd) {
//            res = op.invoke(res, dst[dstOffset])
//            dstOffset++
//        }
//
//        return res
//    }
//
//    override fun plusAssign(other: F64Array) {
//        if (other is F64DenseFlatArray) {
//            checkShape(other)
//            vectorEBEInPlace(other) { a, b -> a + b }
//        } else {
//            super.plusAssign(other)
//        }
//    }
//
//    override fun plus(other: F64Array): F64FlatArray {
//        return if (other is F64DenseFlatArray) {
//            checkShape(other)
//            vectorEBE(other) { a, b -> a + b }
//        } else {
//            super.plus(other)
//        }
//    }
//
//    override fun minusAssign(other: F64Array) {
//        if (other is F64DenseFlatArray) {
//            checkShape(other)
//            vectorEBEInPlace(other) { a, b -> a - b }
//        } else {
//            super.minusAssign(other)
//        }
//    }
//
//    override fun minus(other: F64Array): F64FlatArray {
//        return if (other is F64DenseFlatArray) {
//            checkShape(other)
//            vectorEBE(other) { a, b -> a - b }
//        } else {
//            super.minus(other)
//        }
//    }
//
//    override fun timesAssign(other: F64Array) {
//        if (other is F64DenseFlatArray) {
//            checkShape(other)
//            vectorEBEInPlace(other) { a, b -> a * b }
//        } else {
//            super.timesAssign(other)
//        }
//    }
//
//    override fun times(other: F64Array): F64FlatArray {
//        return if (other is F64DenseFlatArray) {
//            checkShape(other)
//            vectorEBE(other) { a, b -> a * b }
//        } else {
//            super.times(other)
//        }
//    }
//
//    override fun divAssign(other: F64Array) {
//        if (other is F64DenseFlatArray) {
//            checkShape(other)
//            vectorEBEInPlace(other) { a, b -> a / b }
//        } else {
//            super.divAssign(other)
//        }
//    }
//
//    override fun div(other: F64Array): F64FlatArray {
//        return if (other is F64DenseFlatArray) {
//            checkShape(other)
//            vectorEBE(other) { a, b -> a / b }
//        } else {
//            super.div(other)
//        }
//    }
//}


internal class F64LargeDenseArray(
    data: DoubleArray,
    offset: Int,
    size: Int
) : F64DenseFlatArray(data, offset, size) {

    override fun sd() = VectorApiSpeedups.unsafeSD(data, offset, length)

    override fun sum() = VectorApiSpeedups.unsafeSum(data, offset, length)

    override fun dot(other: F64Array): Double {
        return if (other is F64LargeDenseArray) {
            checkShape(other)
            VectorApiSpeedups.unsafeDot(data, offset, other.data, other.offset, length)
        } else {
            super.dot(other)
        }
    }

    private inline fun nativeTransform(
        nativeOp: (DoubleArray, Int, DoubleArray, Int, Int) -> Unit,
    ): F64FlatArray {
        val dst = DoubleArray(length)
        nativeOp(dst, 0, data, offset, length)
        return create(dst, 0, length)
    }

    override fun expInPlace() = VectorApiSpeedups.unsafeExp(data, offset, data, offset, length)

    override fun exp() = nativeTransform(VectorApiSpeedups::unsafeExp)

    override fun expm1InPlace() = VectorApiSpeedups.unsafeExpm1(data, offset, data, offset, length)

    override fun expm1() = nativeTransform(VectorApiSpeedups::unsafeExpm1)

    override fun logInPlace() = VectorApiSpeedups.unsafeLog(data, offset, data, offset, length)

    override fun log() = nativeTransform(VectorApiSpeedups::unsafeLog)

    override fun log1pInPlace() = VectorApiSpeedups.unsafeLog1p(data, offset, data, offset, length)

    override fun log1p() = nativeTransform(VectorApiSpeedups::unsafeLog1p)

    override fun logSumExp() = VectorApiSpeedups.unsafeLogSumExp(data, offset, length)

}
