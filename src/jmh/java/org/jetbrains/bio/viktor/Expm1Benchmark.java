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
public class Expm1Benchmark extends AbstractMathBenchmark {

    @Param({"1_000", "100_000", "10_000_000"})
    int arraySize;

    @Override
    DoubleUnaryOperator getRegularOp() {
        return Math::expm1;
    }

    @Override
    DoubleUnaryOperator getFastOp() {
        return FastMath::expm1;
    }

    @Override
    VectorOp getVectorOp() {
        return NativeSpeedups.INSTANCE::unsafeExpm1InPlace;
    }

    @Override
    int getArraySize() {
        return arraySize;
    }
}

