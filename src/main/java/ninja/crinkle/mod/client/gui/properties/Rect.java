package ninja.crinkle.mod.client.gui.properties;

public record Rect(int x, int y, int width, int height) {
    public Rect {
        if (width < 0 || height < 0) {
            throw new IllegalArgumentException("Rect width and height cannot be negative: " + width + "x" + height);
        }
    }

    public static final Rect ZERO = new Rect(0, 0, 0, 0);

    public boolean contains(int px, int py) {
        return px >= x && px < x + width && py >= y && py < y + height;
    }

    public boolean contains(double px, double py) {
        return px >= x && px < x + width && py >= y && py < y + height;
    }

    public Rect shrink(int top, int right, int bottom, int left) {
        return new Rect(
                x + left,
                y + top,
                Math.max(0, width - left - right),
                Math.max(0, height - top - bottom)
        );
    }

    public Rect shrink(int all) {
        return shrink(all, all, all, all);
    }

    public int right() {
        return x + width;
    }

    public int bottom() {
        return y + height;
    }
}
