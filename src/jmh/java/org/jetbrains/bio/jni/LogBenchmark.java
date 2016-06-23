package org.jetbrains.bio.jni;

import org.apache.commons.math3.util.Precision;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Benchmark)
@Warmup(iterations = 10)
@Measurement(iterations = 5)
@Fork(value = 2, jvmArgsPrepend = "-Djava.library.path=./build/libs")
public class LogBenchmark {
    @Param({"1000", "10000"})
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
            final double expected = Math.log(src[i]);
            if (!Precision.equals(expected, dst[i], 5)) {
                throw new IllegalStateException(String.format(
                        "log(%s) = %s (instead of %s)",
                        src[i], dst[i], expected));
            }
        }
    }

    @Benchmark
    public void scalar(final Blackhole bh) {
        for (int i = 0; i < src.length; i++) {
            dst[i] = Math.log(src[i]);
        }

        bh.consume(dst);
    }

    @Benchmark
    public void vectorBoostSimd(final Blackhole bh) {
        NativeSpeedups.INSTANCE.unsafeLog(src, 0, dst, 0, arraySize);
        bh.consume(dst);
    }

    @Benchmark
    public void vectorYeppp(final Blackhole bh) {
        info.yeppp.Math.Log_V64f_V64f(src, 0, dst, 0, arraySize);
        bh.consume(dst);
    }
}

