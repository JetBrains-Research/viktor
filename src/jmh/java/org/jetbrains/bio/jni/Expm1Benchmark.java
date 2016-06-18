package org.jetbrains.bio.jni;

import org.apache.commons.math3.util.FastMath;
import org.apache.commons.math3.util.Precision;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.util.concurrent.TimeUnit;
import java.util.function.DoubleUnaryOperator;

@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.SECONDS)
@State(Scope.Benchmark)
@Warmup(iterations = 10)
@Measurement(iterations = 5)
@Fork(value = 2, jvmArgsPrepend = "-Djava.library.path=./build/libs")
public class Expm1Benchmark {
    @Param({"1000", "10000", "100000"})
    int arraySize;
    double[] src;
    double[] dst;

    @Setup
    public void generateData() {
        src = new double[arraySize];
        dst = new double[arraySize];
        Internal.sampleUniformGamma(src);
    }

    @TearDown
    public void checkAnswer() {
        for (int i = 0; i < arraySize; i++) {
            final double expected = Math.expm1(src[i]);
            if (!Precision.equals(expected, dst[i], 5)) {
                throw new IllegalStateException(String.format(
                        "expm1(%s) = %s (instead of %s)",
                        src[i], dst[i], expected));
            }
        }
    }

    @Benchmark
    public void scalarFastMath(final Blackhole bh) {
        transform(src, dst, FastMath::expm1);
        bh.consume(dst);
    }

    @Benchmark
    public void scalarMath(final Blackhole bh) {
        transform(src, dst, Math::expm1);
        bh.consume(dst);
    }

    @Benchmark
    public void vector(final Blackhole bh) {
        DoubleMath.INSTANCE.expm1(src, dst);
        bh.consume(dst);
    }

    private void transform(double[] src, double[] dst, final DoubleUnaryOperator op) {
        for (int i = 0; i < src.length; i++) {
            dst[i] = op.applyAsDouble(src[i]);
        }
    }
}

