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
public class SumBenchmark {

	@Param({"1_000", "100_000", "10_000_000"})
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
		if (!Precision.equals(stored, res, 5)) {
			throw new IllegalStateException(String.format("expected %s, got %s", res, stored));
		}
	}

	@Benchmark
	public void scalar(final Blackhole bh) {
		res = 0.;
		for (double value : src) {
			res += value;
		}
		if (bh != null) bh.consume(res);
	}

	@Benchmark
	public void vector(final Blackhole bh) {
		res = NativeSpeedups.INSTANCE.unsafeSum(src, 0, arraySize);
		bh.consume(res);
	}

}
