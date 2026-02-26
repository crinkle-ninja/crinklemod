package ninja.crinkle.mod.client.gui.properties;

import ninja.crinkle.mod.client.gui.states.Positioning;

import java.util.Objects;

public final class Box implements Cloneable {
    private final Size size;
    private final Position position;

    public Box(Position position, Size size) {
        if (size.width() < 0 || size.height() < 0) {
            throw new IllegalArgumentException("Box width and height values cannot be negative");
        }
        this.size = size;
        this.position = position;
    }

    public Box(int x, int y, int width, int height, Positioning positioning) {
        this(positioning.isAbsolute() ? Position.absolute(x, y) : Position.relative(x, y), Size.ofPixels(width, height));
    }

    public Box add(int x, int y, int width, int height) {
        return new Box(position().offsetBy(x, y), size().add(width, height));
    }

    public Box add(Position position) {
        return new Box(position.offsetBy(this.position.point()), size);
    }

    public Position position() {
        return position;
    }

    public Box shrink(BoxProperty property) {
        return new Box(position().offsetBy(property.left(), property.top()),
                size().subtract(property.right() + property.left(), property.bottom() + property.top()));
    }

    public Point bottomLeft() {
        return ImmutablePoint.from(position().point().add(0, size.height()));
    }

    public Point bottomRight() {
        return ImmutablePoint.from(position().point().add(size.width(), size.height()));
    }

    public boolean contains(Point point) {
        return contains(point.x(), point.y());
    }

    public boolean contains(double x, double y) {
        return x >= topLeft().x() && x <= topLeft().x() + size.width() && y >= topLeft().y() && y <= topLeft().y() + size.height();
    }

    public boolean overlaps(Box box) {
        return topLeft().x() < box.topLeft().x() + box.size().width() && topLeft().x() + size().width() > box.topLeft().x() &&
                topLeft().y() < box.topLeft().y() + box.size().height() && topLeft().y() + size().height() > box.topLeft().y();
    }

    public Box subtract(int x, int y, int width, int height) {
        return new Box(position().offsetBy(-x, -y), size().subtract(width, height));
    }

    public Point topLeft() {
        return position.point();
    }

    public Point topRight() {
        return ImmutablePoint.from(position().point().add(size.width(), 0));
    }

    @Override
    public String toString() {
        return "Box{" +
                "position=" + position +
                ", bounds=" + size +
                '}';
    }

    @Override
    public Box clone() {
        try {
            return (Box) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError(e);
        }
    }

    public Size size() {
        return size;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (Box) obj;
        return Objects.equals(this.position, that.position) &&
                Objects.equals(this.size, that.size);
    }

    @Override
    public int hashCode() {
        return Objects.hash(position, size);
    }

}
