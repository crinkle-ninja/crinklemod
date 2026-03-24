package ninja.crinkle.mod.util;

public class MathUtil {
    public static double clamp(double value, double min, double max) {
        return Math.min(Math.max(min, value), max);
    }

    public static int clamp(int value, int min, int max) {
        return Math.min(Math.max(min, value), max);
    }

    public static double lerp(double a, double b, double t) {
        return a + (b - a) * t;
    }

    public static double round(double value, int places) {
        double scale = Math.pow(10, places);
        return Math.round(value * scale) / scale;
    }

    public static int twenties(int value) {
        return (value / 20) * 20;
    }
}
