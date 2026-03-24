package ninja.crinkle.mod.client.gui.animations;

import ninja.crinkle.mod.client.gui.properties.Point;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Animation {
    private final String id;
    private final Point offset;
    private final List<Integer> sizes;
    private final Map<String, Sprite> sprites = new HashMap<>();

    public Animation(String id, Point offset, List<Integer> sizes, Map<String, Sprite> sprites) {
        this.id = id;
        this.offset = offset;
        this.sizes = sizes;
        this.sprites.putAll(sprites);
    }

    public String id() {
        return id;
    }

    public Point offset() {
        return offset;
    }

    public Sprite sprite(String id, int size) {
        if (!sizes.contains(size)) {
            throw new IllegalArgumentException(String.format("Invalid sprite size %d", size));
        }
        if (!sprites.containsKey(id)) {
            throw new IllegalArgumentException(String.format("Invalid sprite id %s", id));
        }
        return sprites.get(id);
    }
}
