package org.jetbrains.bio.viktor;

import org.apache.commons.math3.util.Precision;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.util.concurrent.TimeUnit;

@SuppressWarnings("unused")
@State(Scope.Benchmark)
public class DotBenchmark {
    @Param({"1000", "100000", "1000000"})
    int arraySize;
    private double[] src1;
    private double[] src2;
    private double res;

    @Setup
    public void generateData() {
        src1 = new double[arraySize];
        Internal.sampleUniformGamma(src1);
        src2 = new double[arraySize];
        Internal.sampleUniformGamma(src2);
    }

    @TearDown
    public void checkAnswer() {
        final double stored = res;
        scalar(null);
        if (!Precision.equals(stored, res, (stored + res) * 1E-12)) {
            throw new IllegalStateException(String.format("expected %s, got %s", res, stored));
        }
    }

    @Benchmark
    public void scalar(final Blackhole bh) {
        res = 0.;
        for (int i = 0; i < arraySize; i++) {
            res += src1[i] * src2[i];
        }
        if (bh != null) bh.consume(res);
    }

    @Benchmark
    public void vector(final Blackhole bh) {
        res = VectorApiSpeedups.INSTANCE.unsafeDot(src1, 0, src2, 0, arraySize);
        bh.consume(res);
    }
}
