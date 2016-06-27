package org.jetbrains.bio.viktor;

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
	short[] shorts;

	@Setup
	public void generateData() {
		final Random random = new Random();
		values = random.doubles(arraySize).toArray();
		weights = random.doubles(arraySize).toArray();
		shorts = new short[arraySize];
		for (int i = 0; i < arraySize; i++) {
			shorts[i] = (short) random.nextInt(1000);
		}
	}

	@Benchmark
	public double simpleDot() {
		double res = 0.;
		for (int i = 0; i < arraySize; i++) {
			res += values[i] * weights[i];
		}
		return res;
	}

	@Benchmark
	public double dot() {
		return StridedVectorKt.asStrided(values, 0, arraySize).dot(StridedVectorKt.asStrided(weights, 0, arraySize));
	}

	@Benchmark
	public double nativeDot() {
		return NativeSpeedups.INSTANCE.weightedSum(values, 0, weights, 0, arraySize);
	}

	@Benchmark
	public double shortDot() {
		return StridedVectorKt.asStrided(values, 0, arraySize).dot(shorts);
	}

}
