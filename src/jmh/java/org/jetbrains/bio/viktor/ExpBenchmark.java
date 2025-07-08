package org.jetbrains.bio.viktor;

import org.apache.commons.math3.util.FastMath;
import org.openjdk.jmh.annotations.*;

import java.util.concurrent.TimeUnit;
import java.util.function.DoubleUnaryOperator;

@SuppressWarnings("unused")
@State(Scope.Benchmark)
public class ExpBenchmark extends AbstractMathBenchmark {

    @Param({"1000", "100000", "1000000"})
    int arraySize;

    @Override
    DoubleUnaryOperator getRegularOp() {
        return Math::exp;
    }

    @Override
    DoubleUnaryOperator getFastOp() {
        return FastMath::exp;
    }

    @Override
    VectorOp getVectorOp() {
        return VectorApiSpeedups.INSTANCE::unsafeExp;
    }

    @Override
    int getArraySize() {
        return arraySize;
    }
}
