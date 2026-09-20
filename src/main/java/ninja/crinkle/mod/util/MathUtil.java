package ninja.crinkle.mod.util;

public class MathUtil {

    public static int clamp(int value, int min, int max) {
        return Math.min(Math.max(min, value), max);
    }

    public static int twenties(int value) {
        return (value / 20) * 20;
    }
}
