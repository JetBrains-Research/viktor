package org.jetbrains.bio.viktor;

import org.apache.commons.math3.util.Precision;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.SECONDS)
@State(Scope.Benchmark)
@Warmup(iterations = 5)
@Measurement(iterations = 10)
@Fork(value = 2, jvmArgsPrepend = "-Djava.library.path=./build/libs")
public class SDBenchmark {

	@Param({"1000", "100000", "1000000"})
	int arraySize;
	private double[] src;
	private double res;

	@Setup
	public void generateData() {
		src = new double[arraySize];
		Internal.sampleUniformGamma(src);
	}

	@TearDown
	public void checkAnswer() {
		final double stored = res;
		scalar(null);
		if (!Precision.equals(stored, res, (stored + res) * 1E-12)) {
			throw new IllegalStateException(String.format("expected %s, got %s", res, stored));
		}
	}

	@Benchmark
	public void scalar(final Blackhole bh) {
		double sum = 0., sumSquares = 0.;
		for (double value : src) {
			sum += value;
			sumSquares += value * value;
		}
		res = Math.sqrt((sumSquares - sum * sum / arraySize) / (arraySize - 1));
		if (bh != null)	bh.consume(res);
	}

	@Benchmark
	public void vector(final Blackhole bh) {
		res = NativeSpeedups.INSTANCE.unsafeSD(src, 0, arraySize);
		bh.consume(res);
	}

}
