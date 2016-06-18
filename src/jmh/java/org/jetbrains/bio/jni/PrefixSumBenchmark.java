package org.jetbrains.bio.jni;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.util.Random;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Benchmark)
@Warmup(iterations = 5)
@Measurement(iterations = 10)
@Fork(value = 2, jvmArgsPrepend = "-Djava.library.path=./build/libs")
public class PrefixSumBenchmark {
	@Param({"100", "1000", "10000", "100000", "1000000"})
	int arraySize;

	double[] values;
	double[] sums;

	@Setup
	public void generateData() {
		Loader.INSTANCE.ensureLoaded();

		final Random random = new Random();
		values = random.doubles(arraySize).toArray();
		sums = new double[arraySize];
	}

	@Benchmark
	public void javaPrefixSum(final Blackhole blackhole) {
		double res = 0.;
		for (int i = 0; i < arraySize; i++) {
			res += values[i];
			sums[i] = res;
		}
		blackhole.consume(sums);
	}

	@Benchmark
	public void javaPrefixSumInPlace(final Blackhole blackhole) {
		double res = 0.;
		for (int i = 0; i < arraySize; i++) {
			res += values[i];
			values[i] = res;
		}
		blackhole.consume(values);
	}

	@Benchmark
	public void javaCompensatedPrefixSum(final Blackhole blackhole) {
		DoubleStatJava.INSTANCE.prefixSum(values, 0, sums, 0, arraySize);
		blackhole.consume(sums);
	}

	@Benchmark
	public void nativePrefixSum(final Blackhole blackhole) {
		NativeSpeedups.INSTANCE.prefixSum(values, 0, sums, 0, arraySize);
		blackhole.consume(sums);
	}
}
