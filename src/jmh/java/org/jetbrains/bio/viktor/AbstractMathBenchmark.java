package org.jetbrains.bio.viktor;

import org.apache.commons.math3.util.Precision;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.infra.Blackhole;

import java.util.function.DoubleUnaryOperator;

public abstract class AbstractMathBenchmark {

    private double[] src;
    private double[] dst;

    final private DoubleUnaryOperator regularOp;
    final private DoubleUnaryOperator fastOp;
    final private VectorOp vectorOp;

    AbstractMathBenchmark() {
        regularOp = getRegularOp();
        fastOp = getFastOp();
        vectorOp = getVectorOp();
    }

    abstract DoubleUnaryOperator getRegularOp();
    abstract DoubleUnaryOperator getFastOp();
    abstract VectorOp getVectorOp();
    abstract int getArraySize();


    @Setup
    public void generateData() {
        src = new double[getArraySize()];
        dst = new double[getArraySize()];
        Internal.sampleUniformGamma(src);
    }

    @TearDown
    public void checkAnswer() {
        for (int i = 0; i < getArraySize(); i++) {
            final double expected = regularOp.applyAsDouble(src[i]);
            if (!Precision.equals(expected, dst[i], 5)) {
                throw new IllegalStateException(String.format(
                        "f(%s) = %s (instead of %s)",
                        src[i], dst[i], expected));
            }
        }
    }

    private void scalar(final Blackhole bh, final DoubleUnaryOperator op) {
        System.arraycopy(src, 0, dst, 0, getArraySize()); // let's be fair
        for (int i = 0; i < src.length; i++) {
            dst[i] = op.applyAsDouble(src[i]);
        }
        bh.consume(dst);
    }

    @Benchmark
    public void scalarFastMath(final Blackhole bh) {
        scalar(bh, fastOp);
    }

    @Benchmark
    public void scalarMath(final Blackhole bh) {
        scalar(bh, regularOp);
    }

    @Benchmark
    public void vector(final Blackhole bh) {
        System.arraycopy(src, 0, dst, 0, getArraySize());
        vectorOp.apply(dst, 0, getArraySize());
        bh.consume(dst);
    }

}

interface VectorOp {

    void apply(final double[] array, final int offset, final int size);

}