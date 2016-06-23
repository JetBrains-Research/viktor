package org.jetbrains.bio.jni;

import org.apache.commons.math3.random.RandomDataGenerator;
import org.apache.commons.math3.stat.StatUtils;
import org.apache.commons.math3.util.FastMath;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Benchmark)
@Warmup(iterations = 20)
@Measurement(iterations = 10)
@Fork(value = 2, jvmArgsPrepend = "-Djava.library.path=./build/libs")
public class LogSumExpBenchmark {
    @Param({"16", "32", "64"})
    int arraySize;
    double[] src;

    @Setup
    public void generateData() {
        src = new double[arraySize];
        final RandomDataGenerator gen = new RandomDataGenerator();
        for (int i = 0; i < src.length; i++) {
            if (i < src.length / 3) {
                src[i] = gen.nextUniform(0, 1);
            } else {
                src[i] = gen.nextGamma(42., 42.);
            }
        }
    }

    @Benchmark
    public void scalarLSE(final Blackhole bh) {
        final double offset = StatUtils.max(src);
        double sum = 0;
        for (double value : src) {
            sum += FastMath.exp(value - offset);
        }

        bh.consume(Math.log(sum) + offset);
    }

    @Benchmark
    public void scalarLSEUnrolled2(final Blackhole bh) {
        final double offset = StatUtils.max(src);
        double sum1 = 0;
        double sum2 = 0;
        for (int i = 0; i < src.length; i+=2) {
            sum1 += FastMath.exp(src[i] - offset);
            sum2 += FastMath.exp(src[i + 1] - offset);
        }

        bh.consume(Math.log(sum1 + sum2) + offset);
    }

    @Benchmark
    public void scalarLSEUnrolled4(final Blackhole bh) {
        final double offset = StatUtils.max(src);
        double sum1 = 0;
        double sum2 = 0;
        double sum3 = 0;
        double sum4 = 0;
        for (int i = 0; i < src.length; i+=4) {
            sum1 += FastMath.exp(src[i] - offset);
            sum2 += FastMath.exp(src[i + 1] - offset);
            sum3 += FastMath.exp(src[i + 2] - offset);
            sum4 += FastMath.exp(src[i + 3] - offset);
        }

        bh.consume(Math.log(sum1 + sum2 + sum3 + sum4) + offset);
    }

    @Benchmark
    public void vectorLSE(final Blackhole bh) {
        bh.consume(NativeSpeedups.INSTANCE.unsafeLogSumExp(src, 0, arraySize));
    }
}
