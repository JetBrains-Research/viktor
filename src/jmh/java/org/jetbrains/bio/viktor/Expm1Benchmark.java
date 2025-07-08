package org.jetbrains.bio.viktor;

import org.apache.commons.math3.util.FastMath;
import org.openjdk.jmh.annotations.*;

import java.util.concurrent.TimeUnit;
import java.util.function.DoubleUnaryOperator;

@SuppressWarnings("unused")
@State(Scope.Benchmark)
public class Expm1Benchmark extends AbstractMathBenchmark {

    @Param({"1000", "100000", "1000000"})
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
        return VectorApiSpeedups.INSTANCE::unsafeExpm1;
    }

    @Override
    int getArraySize() {
        return arraySize;
    }
}

