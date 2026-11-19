package tektonikal.aimutils;

public class Cm360 {
	public static final double MIN = 0.01;
	public static final double MAX = 1000;
	private static final double CM_PER_INCH = 2.54;
	public static final double MENU_MIN = 0.5;
	public static final double MENU_MAX = 2;

	private static final double DEGREES_PER_COUNT = 0.15;

	private static double multiplier(double sens) {
		double d = sens * 0.6f + 0.2f;
		return d * d * d * 8;
	}

	public static double sensFor(double multiplier) {
		return (Math.cbrt(multiplier / 8) - 0.2f) / 0.6f;
	}

	public static double fromSens(double sens, int dpi, boolean linear) {
		double degreesPerCount = (linear ? sens : multiplier(sens)) * DEGREES_PER_COUNT;
		return Math.clamp(360.0 / degreesPerCount / dpi * CM_PER_INCH, MIN, MAX);
	}

	public static double toSens(double cm360, int dpi, boolean linear) {
		double mult = 360.0 / (cm360 / CM_PER_INCH * dpi) / DEGREES_PER_COUNT;
		return linear ? mult : sensFor(mult);
	}

	public static double reachableSens(double cm360, int dpi, boolean linear) {
		return Math.clamp(toSens(cm360, dpi, linear), 0d, 1d);
	}

	public static boolean isWarpNoise(double dx, double dy) {
		return Math.abs(dx) < 0.05 && Math.abs(dy) < 0.05;
	}

	public static double scaleMove(double cursor, double delta, double mult, int size) {
		return Math.clamp(cursor + delta * mult, 0, size - 1);
	}

	public static double randomSens(double min, double max) {
		min = Math.clamp(min, 0d, 1d);
		max = Math.clamp(max, 0d, 1d);
		return min + Math.random() * (max - min);
	}
}
