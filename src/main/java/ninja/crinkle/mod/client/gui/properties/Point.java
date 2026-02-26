package ninja.crinkle.mod.client.gui.properties;

import ninja.crinkle.mod.client.gui.textures.TextureSize;

public interface Point {
    Point ZERO = new ImmutablePoint(0, 0);

    static ImmutablePoint of(int x, int y) {
        return new ImmutablePoint(x, y);
    }

    static ImmutablePoint of(double x, double y) {
        return new ImmutablePoint(x, y);
    }

    Point add(Point point);

    Point add(Size size);

    Point add(TextureSize size);

    Point add(int x, int y);

    Point add(double x, double y);

    default int distance(double pMouseX, double pMouseY) {
        return (int) Math.sqrt(Math.pow(pMouseX - x(), 2) + Math.pow(pMouseY - y(), 2));
    }

    Point subtract(double x, double y);

    Point subtract(int x, int y);

    double x();

    default float xFloat() {
        return (float) x();
    }

    default int xInt() {
        return Math.round(xFloat());
    }

    double y();

    default float yFloat() {
        return (float) y();
    }

    default int yInt() {
        return Math.round(yFloat());
    }
}
