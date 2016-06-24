package org.jetbrains.bio.jni;

import org.apache.commons.math3.util.FastMath;
import org.apache.commons.math3.util.Precision;
import org.jetbrains.bio.viktor.NativeSpeedups;
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
public class Log1pBenchmark {
    @Param({"100", "1000", "10000"})
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
            final double expected = Math.log1p(src[i]);
            if (!Precision.equals(expected, dst[i], 5)) {
                throw new IllegalStateException(String.format(
                        "log1p(%s) = %s (instead of %s)",
                        src[i], dst[i], expected));
            }
        }
    }

    @Benchmark
    public void scalarFastMath(final Blackhole bh) {
        transform(src, dst, FastMath::log1p);
        bh.consume(dst);
    }

    @Benchmark
    public void scalarMath(final Blackhole bh) {
        transform(src, dst, Math::log1p);
        bh.consume(dst);
    }

    @Benchmark
    public void vector(final Blackhole bh) {
        NativeSpeedups.INSTANCE.unsafeLog1p(src, 0, dst, 0, arraySize);
        bh.consume(dst);
    }

    private void transform(double[] src, double[] dst, final DoubleUnaryOperator op) {
        for (int i = 0; i < src.length; i++) {
            dst[i] = op.applyAsDouble(src[i]);
        }
    }
}
