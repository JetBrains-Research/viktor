package org.jetbrains.bio.viktor;

import org.apache.commons.math3.util.Precision;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.util.concurrent.TimeUnit;

import static org.jetbrains.bio.viktor.MoreMathKt.logAddExp;

@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.SECONDS)
@State(Scope.Benchmark)
@Warmup(iterations = 5)
@Measurement(iterations = 10)
@Fork(value = 2, jvmArgsPrepend = "-Djava.library.path=./build/libs")
public class LogAddExpBenchmark {

    @Param({"1000", "100000", "1000000"})
    int arraySize;
    double[] src1;
    double[] src2;
    double[] dst;

    @Setup
    public void generateData() {
        src1 = new double[arraySize];
        src2 = new double[arraySize];
        dst = new double[arraySize];

        Internal.sampleUniformGamma(src1);
        Internal.sampleUniformGamma(src2);
    }

    @TearDown
    public void checkAnswer() {
        for (int i = 0; i < arraySize; i++) {
            final double expected = logAddExp(src1[i], src2[i]);
            if (!Precision.equals(expected, dst[i], 5)) {
                throw new IllegalStateException(String.format(
                        "logaddexp(%s, %s) = %s (instead of %s)",
                        src1[i], src2[i], dst[i], expected));
            }
        }
    }

    @Benchmark
    public void scalar(final Blackhole bh) {
        System.arraycopy(src1, 0, dst, 0, arraySize); // let's be fair
        for (int i = 0; i < arraySize; i++) {
            dst[i] = logAddExp(dst[i], src2[i]);
        }

        bh.consume(dst);
    }

    @Benchmark
    public void vector(final Blackhole bh) {
        System.arraycopy(src1, 0, dst, 0, arraySize);
        NativeSpeedups.INSTANCE.unsafeLogAddExp(dst, 0, src2, 0, arraySize);
        bh.consume(dst);
    }
}

