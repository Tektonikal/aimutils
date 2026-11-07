package tektonikal.aimutils;

public class Cm360 {
    public static final double MIN = 0.01;
    public static final double MAX = 1000;
    private static final double CM_PER_INCH = 2.54;

    public static double fromSens(double sens, int dpi, boolean linear) {
        double cubed = sens * 0.6 + 0.2;
        double degreesPerCount = linear ? sens * 0.15 : cubed * cubed * cubed * 1.2;
        return Math.clamp(360.0 / degreesPerCount / dpi * CM_PER_INCH, MIN, MAX);
    }

    public static double toSens(double cm360, int dpi, boolean linear) {
        double degreesPerCount = 360.0 / (cm360 / CM_PER_INCH * dpi);
        return linear ? degreesPerCount / 0.15 : (Math.cbrt(degreesPerCount / 1.2) - 0.2) / 0.6;
    }
}
