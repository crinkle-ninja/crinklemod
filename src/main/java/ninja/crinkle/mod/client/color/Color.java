package ninja.crinkle.mod.client.color;

import com.mojang.logging.LogUtils;
import net.minecraft.ChatFormatting;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.util.Objects;
import java.util.Optional;

public record Color(int color, Type type) {
    private static final @NotNull Color INVALID = new Color(-1, Type.INVALID);
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final Color BLACK = new Color(0xFF000000);
    public static final Color BLUE = new Color(0xFF0000FF);
    public static final Color BROWN = new Color(0xFFA52A2A);
    public static final Color CYAN = new Color(0xFF00FFFF);
    public static final Color DEFAULT_TEXT = new Color(0xFF404040);
    public static final Color GREEN = new Color(0xFF00FF00);
    public static final Color LIGHT_GRAY = new Color(0xFF808080);
    public static final Color MAGENTA = new Color(0xFFFF00FF);
    public static final Color ORANGE = new Color(0xFFFFA500);
    public static final Color PINK = new Color(0xFFFFC0CB);
    public static final Color PURPLE = new Color(0xFF800080);
    public static final Color RAINBOW = new Color(-1, Type.RAINBOW);
    public static final Color RED = new Color(0xFFFF0000);
    public static final Color TRANSPARENT = new Color(0, Type.TRANSPARENT);
    public static final Color WHITE = new Color(0xFFFFFFFF);
    public static final Color YELLOW = new Color(0xFFFFFF00);

    public Color(int color) {
        this(color, Type.NORMAL);
    }

    public static int brightness(int color, double amount) {
        int r = (int) ((color >> 16 & 0xFF) * amount);
        int g = (int) ((color >> 8 & 0xFF) * amount);
        int b = (int) ((color & 0xFF) * amount);
        return (color & 0xFF000000) | (r << 16) | (g << 8) | b;
    }

    @Contract("_, _, _ -> new")
    public static @NotNull Color of(double red, double green, double blue) {
        return Color.of(red, green, blue, 255);
    }

    @Contract("_, _, _, _ -> new")
    public static @NotNull Color of(double red, double green, double blue, double alpha) {
        return new Color((int) (alpha * 255) << 24 | (int) (red * 255) << 16 | (int) (green * 255) << 8 | (int) (blue * 255));
    }

    @Contract("_, _, _ -> new")
    public static @NotNull Color of(int red, int green, int blue) {
        return Color.of(red, green, blue, 255);
    }

    @Contract("_, _, _, _ -> new")
    public static @NotNull Color of(int red, int green, int blue, int alpha) {
        return new Color(alpha << 24 | red << 16 | green << 8 | blue);
    }

    @Contract("_ -> new")
    public static @NotNull Color of(@NotNull String hex) {
        if (hex.equalsIgnoreCase("rainbow")) return Color.RAINBOW;
        if (hex.equalsIgnoreCase("transparent")) return Color.TRANSPARENT;
        if (hex.startsWith("0x")) hex = hex.substring(2);
        if (hex.startsWith("#")) hex = hex.substring(1);
        try {
            int color = Integer.parseInt(hex, 16);
            return new Color(color).withAlpha(1);
        } catch (NumberFormatException e) {
            LOGGER.warn("invalid number format: {}", hex);
        }
        return Color.INVALID;
    }

    public boolean isValid() {
        return type() != Type.INVALID;
    }

    @Contract("_ -> new")
    public @NotNull Color withAlpha(double alpha) {
        int a = (int) (alpha * 255);
        return new Color((get().color() & 0x00FFFFFF) | (a << 24));
    }

    @Override
    public int color() {
        return isTransparent() ? 0 : color;
    }

    public Color get() {
        return switch (type()) {
            case NORMAL, TRANSPARENT -> this;
            case RAINBOW -> Color.rainbow(1000, 0);
            case INVALID -> throw new IllegalStateException("calling get() on an invalid color");
        };
    }

    public boolean isTransparent() {
        return type() == Type.TRANSPARENT;
    }

    public static Color rainbow(long speed, long offset) {
        double hue = (System.currentTimeMillis() + offset) % speed / (double) speed;
        int rgb = java.awt.Color.HSBtoRGB((float) hue, 1, 1);
        return Color.of(rgb | 0xFF000000);
    }

    @Contract("_ -> new")
    public static @NotNull Color of(int color) {
        return new Color(color);
    }

    @Contract("_ -> new")
    public static @NotNull Color of(@NotNull ChatFormatting formatting) {
        return new Color(Optional.ofNullable(formatting.getColor()).orElseThrow());
    }

    @Contract("_ -> new")
    public static @NotNull Color ofABGR(int color) {
        return new Color(((color & 0xFF000000) | ((color & 0x00FF0000) >> 16) | (color & 0x0000FF00) | ((color & 0x000000FF) << 16)));
    }

    public int ABGR() {
        return ((color & 0xFF000000) | ((color & 0x00FF0000) >> 16) | (color & 0x0000FF00) | ((color & 0x000000FF) << 16));
    }

    public Color halftone() {
        return new Color((color & 0xFF000000) | ((color >> 1) & 0x7F7F7F));
    }

    @Override
    public int hashCode() {
        return Objects.hash(color);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (Color) obj;
        return this.color == that.color;
    }

    @Override
    public String toString() {
        return "Color[" +
                "color=" + color +
                ", red=" + getRed() +
                ", green=" + getGreen() +
                ", blue=" + getBlue() +
                ", alpha=" + getAlpha() +
                ", hex='" + String.format("%06X", color & 0xFFFFFF) + '\'' +
                ']';
    }

    public double getRed() {
        return (color >> 16 & 0xFF) / 255.0;
    }

    public double getGreen() {
        return (color >> 8 & 0xFF) / 255.0;
    }

    public double getBlue() {
        return (color & 0xFF) / 255.0;
    }

    public double getAlpha() {
        return (color >> 24 & 0xFF) / 255.0;
    }

    public Color inverted() {
        return new Color((color & 0xFF000000) | (~color & 0xFFFFFF));
    }

    public Color multiply(Color other) {
        int r = (int) (getRed() * other.getRed() * 255);
        int g = (int) (getGreen() * other.getGreen() * 255);
        int b = (int) (getBlue() * other.getBlue() * 255);
        int a = (color >> 24) & 0xFF;
        return new Color((a << 24) | (r << 16) | (g << 8) | b);
    }

    public Color lerp(Color target, double t) {
        double clamped = Math.max(0, Math.min(1, t));
        int r = (int) ((getRed() + (target.getRed() - getRed()) * clamped) * 255);
        int g = (int) ((getGreen() + (target.getGreen() - getGreen()) * clamped) * 255);
        int b = (int) ((getBlue() + (target.getBlue() - getBlue()) * clamped) * 255);
        int a = (color >> 24) & 0xFF;
        return new Color((a << 24) | (r << 16) | (g << 8) | b);
    }

    public enum Type {
        INVALID,
        NORMAL,
        RAINBOW,
        TRANSPARENT
    }
}