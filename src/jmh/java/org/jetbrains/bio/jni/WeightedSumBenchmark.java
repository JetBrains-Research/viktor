package org.jetbrains.bio.jni;

import org.jetbrains.bio.viktor.BalancedSumKt;
import org.jetbrains.bio.viktor.NativeSpeedups;
import org.jetbrains.bio.viktor.StridedVectorKt;
import org.openjdk.jmh.annotations.*;

import java.util.Random;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Benchmark)
@Warmup(iterations = 5)
@Measurement(iterations = 10)
@Fork(value = 2, jvmArgsPrepend = "-Djava.library.path=./build/libs")
public class WeightedSumBenchmark {
	@Param({"1000", "10000", "100000"})
	int arraySize;
	double[] values;
	double[] weights;

	@Setup
	public void generateData() {
		final Random random = new Random();
		values = random.doubles(arraySize).toArray();
		weights = random.doubles(arraySize).toArray();
	}

	@Benchmark
	public double simpleSum() {
		double res = 0.;
		for (int i = 0; i < arraySize; i++) {
			res += values[i] * weights[i];
		}
		return res;
	}

	@Benchmark
	public double javaSum() {
		return BalancedSumKt.balancedDot(
				StridedVectorKt.asStrided(values, 0, arraySize),
				StridedVectorKt.asStrided(weights, 0, arraySize));
	}

	@Benchmark
	public double nativeSum() {
		return NativeSpeedups.INSTANCE.weightedSum(values, 0, weights, 0, arraySize);
	}

}
