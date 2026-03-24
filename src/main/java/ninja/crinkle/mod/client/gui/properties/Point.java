package ninja.crinkle.mod.client.gui.properties;

import ninja.crinkle.mod.client.gui.textures.TextureSize;
import org.jetbrains.annotations.NotNull;

public record Point(double x, double y) implements Comparable<Point> {
    public static final Point ZERO = new Point(0, 0);

    public Point(int x, int y) {
        this((double) x, y);
    }

    public static Point of(int x, int y) {
        return new Point(x, y);
    }

    public static Point of(double x, double y) {
        return new Point(x, y);
    }

    public Point add(@NotNull Point point) {
        return add(point.x(), point.y());
    }

    public Point add(double x, double y) {
        return new Point(x() + x, y() + y);
    }

    public Point add(TextureSize size) {
        return add(size.width(), size.height());
    }

    public Point add(int x, int y) {
        return new Point(xInt() + x, yInt() + y);
    }

    public int xInt() {
        return Math.round(xFloat());
    }

    public int yInt() {
        return Math.round(yFloat());
    }

    public float xFloat() {
        return (float) x();
    }

    public float yFloat() {
        return (float) y();
    }

    @Override
    public int compareTo(@NotNull Point o) {
        int cx = Double.compare(x(), o.x());
        int cy = Double.compare(y(), o.y());
        return cx == 0 ? cy : cx;
    }

    public int distance(double pMouseX, double pMouseY) {
        return (int) Math.sqrt(Math.pow(pMouseX - x(), 2) + Math.pow(pMouseY - y(), 2));
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Point other)) return false;
        return x() == other.x() && y() == other.y();
    }

    @Override
    public String toString() {
        return "Point{x=" + x + ", y=" + y + "}";
    }

    public Point subtract(double x, double y) {
        return new Point(x() - x, y() - y);
    }

    public Point subtract(int x, int y) {
        return new Point(x() - x, y() - y);
    }
}
