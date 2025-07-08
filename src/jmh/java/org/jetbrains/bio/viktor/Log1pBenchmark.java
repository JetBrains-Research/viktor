package org.jetbrains.bio.viktor;

import org.apache.commons.math3.util.FastMath;
import org.openjdk.jmh.annotations.*;

import java.util.concurrent.TimeUnit;
import java.util.function.DoubleUnaryOperator;

@SuppressWarnings("unused")
@State(Scope.Benchmark)
public class Log1pBenchmark extends AbstractMathBenchmark {

    @Param({"1000", "100000", "1000000"})
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
        return VectorApiSpeedups.INSTANCE::unsafeLog1p;
    }

    @Override
    int getArraySize() {
        return arraySize;
    }
}
