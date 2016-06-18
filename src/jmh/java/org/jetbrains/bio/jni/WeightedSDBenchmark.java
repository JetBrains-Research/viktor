package org.jetbrains.bio.jni;

import org.openjdk.jmh.annotations.*;

import java.util.Random;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Benchmark)
@Warmup(iterations = 5)
@Measurement(iterations = 10)
@Fork(value = 2, jvmArgsPrepend = "-Djava.library.path=./build/libs")
public class WeightedSDBenchmark {
	@Param({"100", "1000", "10000", "100000", /*"950000",*/ "1000000", "10000000"})
	int arraySize;
	double[] values;
	double[] weights;

	@Setup
	public void generateData() {
		Loader.INSTANCE.ensureLoaded();

		final Random random = new Random();
		values = random.doubles(arraySize).toArray();
		weights = random.doubles(arraySize).toArray();
	}

	@Benchmark
	public double nativeWeightedSD() {
		return NativeSpeedups.INSTANCE.weightedSd(values, 0, weights, 0, arraySize);
	}

	@Benchmark
	public double javaWeightedSD() {
		return DoubleStatJava.INSTANCE.weightedStandardDeviation(values, 0, weights, 0, arraySize);
	}

}
