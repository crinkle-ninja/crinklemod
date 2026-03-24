package ninja.crinkle.mod.client.gui.textures;

import java.util.function.IntUnaryOperator;

public enum ColorFilters {
    NORMAL(i -> i),
    INVERT(i -> (i & 0xFF000000) | (~i & 0x00FFFFFF)),

    SEPIA(i -> {
        int alpha = (i >> 24) & 0xFF;
        int red = (i >> 16) & 0xFF;
        int green = (i >> 8) & 0xFF;
        int blue = i & 0xFF;

        // Calculate sepia tone
        int newRed = Math.min(255, (int) (red * 0.393 + green * 0.769 + blue * 0.189));
        int newGreen = Math.min(255, (int) (red * 0.349 + green * 0.686 + blue * 0.168));
        int newBlue = Math.min(255, (int) (red * 0.272 + green * 0.534 + blue * 0.131));

        return (alpha << 24) | (newRed << 16) | (newGreen << 8) | newBlue;
    }),

    GRAYSCALE(i -> {
        int alpha = (i >> 24) & 0xFF;
        int red = (i >> 16) & 0xFF;
        int green = (i >> 8) & 0xFF;
        int blue = i & 0xFF;

        // Calculate the luminance (average or weighted average)
        int gray = (int) (red * 0.3 + green * 0.59 + blue * 0.11);

        return (alpha << 24) | (gray << 16) | (gray << 8) | gray;
    });


    private final IntUnaryOperator filter;

    ColorFilters(IntUnaryOperator filter) {
        this.filter = filter;
    }

    public static ColorFilters fromString(String name) {
        for (ColorFilters filter : values()) {
            if (filter.name().equalsIgnoreCase(name)) {
                return filter;
            }
        }
        return NORMAL;
    }

    public int apply(int color) {
        return filter.applyAsInt(color);
    }

    public IntUnaryOperator filter() {
        return filter;
    }

    @Override
    public String toString() {
        return super.toString().toLowerCase();
    }
}
