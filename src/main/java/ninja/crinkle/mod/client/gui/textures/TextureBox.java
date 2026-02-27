package ninja.crinkle.mod.client.gui.textures;

import ninja.crinkle.mod.client.gui.properties.Point;

import java.util.Objects;

public class TextureBox {
    private final TextureSize size;

    public TextureBox(TextureSize size) {
        this.size = size;
    }

    public boolean contains(Point point) {
        return size().width() > point.x() && size().height() > point.y();
    }

    public TextureSize size() {
        return size;
    }

    public Point topLeft() {
        return Point.ZERO;
    }

    public Point topRight() {
        return topLeft().add(size.width(), 0);
    }

    public Point bottomLeft() {
        return topLeft().add(0, size.height());
    }

    public Point bottomRight() {
        return topLeft().add(size.width(), size.height());
    }
}
