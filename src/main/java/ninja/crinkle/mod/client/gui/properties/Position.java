package ninja.crinkle.mod.client.gui.properties;

import ninja.crinkle.mod.client.gui.states.Positioning;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public record Position(Point point, Positioning positioning) implements Cloneable {
    public static final Position ABSOLUTE_ZERO = new Position(ImmutablePoint.ZERO, Positioning.Absolute);
    public static final Position RELATIVE_ZERO = new Position(ImmutablePoint.ZERO, Positioning.Relative);

    @Override
    public int hashCode() {
        return point.hashCode() * 31 + positioning.hashCode();
    }

    public Position {
        if (positioning == null) {
            throw new IllegalArgumentException("positioning cannot be null");
        }
        if (point == null) {
            throw new IllegalArgumentException("point cannot be null");
        }
    }

    @Contract("_ -> new")
    public static @NotNull Position absolute(@NotNull Point absolute) {
        return new Position(absolute, Positioning.Absolute);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        Position position = (Position) obj;
        return point().equals(position.point()) && positioning() == position.positioning();
    }

    @Override
    protected Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }

    @Contract("_, _ -> new")
    public static @NotNull Position absolute(int x, int y) {
        return absolute(new ImmutablePoint(x, y));
    }

    @Contract("_ -> new")
    public static @NotNull Position relative(@NotNull Point relative) {
        return new Position(relative, Positioning.Relative);
    }

    @Contract("_, _ -> new")
    public static @NotNull Position relative(int x, int y) {
        return relative(new ImmutablePoint(x, y));
    }

    @Contract("_, _ -> new")
    public @NotNull Position offsetBy(double x, double y) {
        return new Position(point().add(x, y), positioning());
    }

    @Contract("_ -> new")
    public @NotNull Position withBase(@NotNull Point base) {
        return new Position(base.add(point()), positioning());
    }

    @Contract("_ -> new")
    public @NotNull Position offsetBy(@NotNull Point offset) {
        return new Position(point().add(offset), positioning());
    }

    public boolean absolute() {
        return positioning().isAbsolute();
    }

    public boolean relative() {
        return positioning().isRelative();
    }

    @Override
    public @NotNull String toString() {
        return "Position[" + "point=" + point + ", positioning=" + positioning + ']';
    }
}
