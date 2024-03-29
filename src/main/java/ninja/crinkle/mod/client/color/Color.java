package ninja.crinkle.mod.client.color;

import net.minecraft.ChatFormatting;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public record Color(int color) {
    public static final Color WHITE = new Color(0xFFFFFFFF);
    public static final Color BLACK = new Color(0xFF000000);
    // public static final Color TRANSPARENT = new Color(0);
    public static final Color DEFAULT_TEXT = new Color(0xFF404040);

    @Contract("_ -> new")
    public static @NotNull Color of(@NotNull ChatFormatting formatting) {
        return new Color(Optional.ofNullable(formatting.getColor()).orElseThrow());
    }

    @Contract("_ -> new")
    public static @NotNull Color of(int color) {
        return new Color(color);
    }

    @Contract("_ -> new")
    public static @NotNull Color ofABGR(int color) {
        return new Color(((color & 0xFF000000) | ((color & 0x00FF0000) >> 16) | (color & 0x0000FF00) | ((color & 0x000000FF) << 16)));
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
        if (hex.startsWith("0x")) hex = hex.substring(2);
        if (hex.startsWith("#")) hex = hex.substring(1);
        int color = Integer.parseInt(hex, 16);
        return new Color(color);
    }

    public static int brightness(int color, double amount) {
        int r = (int) ((color >> 16 & 0xFF) * amount);
        int g = (int) ((color >> 8 & 0xFF) * amount);
        int b = (int) ((color & 0xFF) * amount);
        return (color & 0xFF000000) | (r << 16) | (g << 8) | b;
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

    @Contract("_ -> new")
    public @NotNull Color withAlpha(double alpha) {
        int a = (int) (alpha * 255);
        return new Color((color & 0x00FFFFFF) | (a << 24));
    }

    public int ABGR() {
        return ((color & 0xFF000000) | ((color & 0x00FF0000) >> 16) | (color & 0x0000FF00) | ((color & 0x000000FF) << 16));
    }
}
