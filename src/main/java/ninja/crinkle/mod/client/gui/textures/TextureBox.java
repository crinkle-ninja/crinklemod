package ninja.crinkle.mod.client.gui.textures;

import ninja.crinkle.mod.client.gui.properties.Point;

public record TextureBox(TextureSize size) {

    public Point bottomLeft() {
        return topLeft().add(0, size.height());
    }

    public Point bottomRight() {
        return topLeft().add(size.width(), size.height());
    }

    public boolean contains(Point point) {
        return size().width() > point.x() && size().height() > point.y();
    }

    public Point topRight() {
        return topLeft().add(size.width(), 0);
    }

    public Point topLeft() {
        return Point.ZERO;
    }
}
