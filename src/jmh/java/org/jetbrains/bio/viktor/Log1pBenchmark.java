package org.jetbrains.bio.viktor;

import org.apache.commons.math3.util.FastMath;
import org.apache.commons.math3.util.Precision;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.util.concurrent.TimeUnit;
import java.util.function.DoubleUnaryOperator;

@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.SECONDS)
@State(Scope.Benchmark)
@Warmup(iterations = 5)
@Measurement(iterations = 10)
@Fork(value = 2, jvmArgsPrepend = "-Djava.library.path=./build/libs")
public class Log1pBenchmark extends AbstractMathBenchmark {

    @Param({"1_000", "100_000", "10_000_000"})
    int arraySize;

    @Override
    DoubleUnaryOperator getRegularOp() {
        return Math::log1p;
    }

    @Override
    DoubleUnaryOperator getFastOp() {
        return FastMath::log1p;
    }

    @Override
    VectorOp getVectorOp() {
        return NativeSpeedups.INSTANCE::unsafeLog1pInPlace;
    }

    @Override
    int getArraySize() {
        return arraySize;
    }
}
