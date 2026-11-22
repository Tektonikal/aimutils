package tektonikal.aimutils;

import java.util.concurrent.ThreadLocalRandom;

public final class SensRandomizer {
	public static final double MIN_MULT = 0.1;
	public static final double MAX_MULT = 10;
	public static final double MIN_FREQUENCY = 0.01;
	public static final double MAX_FREQUENCY = 20;
	private static final double MAX_STEP_SECONDS = 0.25;

	public enum Interpolation {
		STEP, LINEAR, HERMITE, QUINTIC;

		public double ease(double t) {
			return switch (this) {
				case STEP -> 0;
				case LINEAR -> t;
				case HERMITE -> t * t * (3 - 2 * t);
				case QUINTIC -> t * t * t * (t * (t * 6 - 15) + 10);
			};
		}
	}

	private boolean running;
	private long seed;
	private long lastNanos;
	private double phase;

	public double sample(long nanos, boolean enabled, double min, double max, double frequency, Interpolation mode) {
		if (!enabled) {
			running = false;
			return 1;
		}
		if (!running) {
			running = true;
			seed = ThreadLocalRandom.current().nextLong();
			phase = 0;
		} else {
			double seconds = Math.clamp((nanos - lastNanos) / 1e9, 0, MAX_STEP_SECONDS);
			phase += seconds * Math.clamp(frequency, MIN_FREQUENCY, MAX_FREQUENCY);
		}
		lastNanos = nanos;
		return valueAt(phase, seed, min, max, mode);
	}

	public static double valueAt(double phase, long seed, double min, double max, Interpolation mode) {
		long index = (long) Math.floor(phase);
		double a = keypoint(seed, index), b = keypoint(seed, index + 1);
		double u = a + (b - a) * mode.ease(phase - index);
		double lo = Math.log(Math.clamp(min, MIN_MULT, MAX_MULT));
		double hi = Math.log(Math.clamp(max, MIN_MULT, MAX_MULT));
		return Math.exp(lo + (hi - lo) * u);
	}

	// https://rosettacode.org/wiki/Pseudo-random_numbers/Splitmix64
	private static double keypoint(long seed, long index) {
		long z = seed + index * 0x9E3779B97F4A7C15L;
		z = (z ^ (z >>> 30)) * 0xBF58476D1CE4E5B9L;
		z = (z ^ (z >>> 27)) * 0x94D049BB133111EBL;
		z ^= z >>> 31;
		return (z >>> 11) * 0x1.0p-53;
	}
}
