package org.jetbrains.bio.viktor;

import org.apache.commons.math3.random.RandomDataGenerator;
import org.apache.commons.math3.stat.StatUtils;
import org.apache.commons.math3.util.FastMath;
import org.apache.commons.math3.util.Precision;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.SECONDS)
@State(Scope.Benchmark)
@Warmup(iterations = 5)
@Measurement(iterations = 10)
@Fork(value = 2, jvmArgsPrepend = "-Djava.library.path=./build/libs")
public class LogSumExpBenchmark {

    @Param({"1_000", "100_000", "10_000_000"})
    int arraySize;
    double[] src;
    double res;

    @Setup
    public void generateData() {
        src = new double[arraySize];
        Internal.sampleUniformGamma(src);
    }

    @TearDown
    public void checkAnswer() {
        double sumExp = 0.;
        for (double value : src) {
            sumExp += Math.exp(value);
        }
        if (!Precision.equals(Math.exp(res), sumExp, 5)) {
            throw new IllegalStateException(String.format("expected %s, got %s", sumExp, Math.exp(res)));
        }
    }

    @Benchmark
    public void scalarMath(final Blackhole bh) {
        final double offset = StatUtils.max(src);
        res = 0.;
        for (double value : src) {
            res += Math.exp(value - offset);
        }
        res = Math.log(res) + offset;
        bh.consume(res);
    }

    @Benchmark
    public void scalarMathUnroll2(final Blackhole bh) {
        final double offset = StatUtils.max(src);
        double sum1 = 0.;
        double sum2 = 0.;
        for (int i = 0; i < src.length; i += 2) {
            sum1 += FastMath.exp(src[i] - offset);
            sum2 += FastMath.exp(src[i + 1] - offset);
        }
        res = Math.log(sum1 + sum2) + offset;
        bh.consume(res);
    }

    @Benchmark
    public void scalarMathUnroll4(final Blackhole bh) {
        final double offset = StatUtils.max(src);
        double sum1 = 0.;
        double sum2 = 0.;
        double sum3 = 0.;
        double sum4 = 0.;
        for (int i = 0; i < src.length; i += 4) {
            sum1 += FastMath.exp(src[i] - offset);
            sum2 += FastMath.exp(src[i + 1] - offset);
            sum3 += FastMath.exp(src[i + 2] - offset);
            sum4 += FastMath.exp(src[i + 3] - offset);
        }
        res = Math.log(sum1 + sum2 + sum3 + sum4) + offset;
        bh.consume(res);
    }

    @Benchmark
    public void scalarFastMath(final Blackhole bh) {
        final double offset = StatUtils.max(src);
        res = 0;
        for (double value : src) {
            res += FastMath.exp(value - offset);
        }
        res = FastMath.log(res) + offset;
        bh.consume(res);
    }

    @Benchmark
    public void vectorLSE(final Blackhole bh) {
        res = NativeSpeedups.INSTANCE.unsafeLogSumExp(src, 0, arraySize);
        bh.consume(res);
    }
}
