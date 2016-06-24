package org.jetbrains.bio.jni;

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
public class SDBenchmark {

	@Param({"1000", "10000", "100000"})
	int arraySize;
	double[] values;

	@Setup
	public void generateData() {
		Loader.INSTANCE.ensureLoaded();

		final Random random = new Random();
		values = random.doubles(arraySize).toArray();
	}

	@Benchmark
	public double simpleSD() {
		double sum = 0., sumSquares = 0.;
		for (int i = 0; i < arraySize; i++) {
			sum += values[i];
			sumSquares += values[i] * values[i];
		}
		return Math.sqrt((sumSquares - sum * sum / arraySize) / (arraySize - 1));
	}

	@Benchmark
	public double javaSD() {
		return StridedVectorKt.asStrided(values, 0, arraySize).sd();
	}

	@Benchmark
	public double nativeSD() {
		return NativeSpeedups.INSTANCE.sd(values, 0, arraySize);
	}
}
