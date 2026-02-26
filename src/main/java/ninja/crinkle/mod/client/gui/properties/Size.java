package ninja.crinkle.mod.client.gui.properties;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public final class Size implements Cloneable {
    @Override
    protected Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }

    public static final Size ZERO = new Size(0, 0, Unit.Pixels);
    public static final Size RELATIVE_ZERO = new Size(0, 0, Unit.Percent);
    private final float width;
    private final float height;
    private final Unit unit;

    public Size resolve(Box parentBox) {
        if (unit() == Unit.Percent) {
            if (parentBox == null) {
                throw new IllegalArgumentException("Parent box cannot be null when using percent unit");
            }
            float width = width() <= 1 ? width() : width() / 100;
            float height = height() <= 1 ? height() : height() / 100;
            return new Size(Math.round(parentBox.size().width() * width),
                    Math.round(parentBox.size().height() * height), Unit.Pixels);
        } else {
            return new Size(width(), height(), unit());
        }
    }

    public enum Unit {
        Pixels, Percent
    }


    public Size(float width, float height, Unit unit) {
        if (width < 0 || height < 0) {
            throw new IllegalArgumentException("Box width and height values cannot be negative");
        }
        this.width = width;
        this.height = height;
        this.unit = unit;
    }

    public static Size ofPixels(int width, int height) {
        return new Size(width, height, Unit.Pixels);
    }

    public static Size ofPercent(float width, float height) {
        return new Size(width, height, Unit.Percent);
    }

    public static Size ofPercent(int width, int height) {
        return new Size(width, height, Unit.Percent);
    }

    public Size add(Size size) {
        if (size.unit() != unit()) {
            throw new IllegalArgumentException("Cannot add sizes with different units");
        }
        return new Size(Math.max(0, width() + size.width()), Math.max(0, height() + size.height()), unit());
    }

    public Unit unit() {
        return unit;
    }

    public Size add(int width, int height) {
        return new Size(Math.max(0, width() + width), Math.max(0, height() + height), unit());
    }

    public Size subtract(Size size) {
        return new Size(Math.max(0, width() - size.width()), Math.max(0, height() - size.height()), unit());
    }

    public Size subtract(int width, int height) {
        return new Size(Math.max(0, width() - width), Math.max(0, height() - height), unit());
    }

    public boolean greaterThan(Size size) {
        return width() > size.width() && height() > size.height();
    }

    public boolean lessThan(Size size) {
        return width() < size.width() && height() < size.height();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        Size size = (Size) obj;
        return width == size.width && height == size.height && unit == size.unit;
    }

    @Override
    public @NotNull String toString() {
        return "Size{" +
                "width=" + width +
                ", height=" + height +
                ", unit=" + unit +
                '}';
    }

    public float width() {
        return width;
    }

    public float height() {
        return height;
    }

    @Override
    public int hashCode() {
        return Objects.hash(width, height, unit);
    }

    public int widthInt() {
        return (int) width;
    }

    public int heightInt() {
        return (int) height;
    }
}
