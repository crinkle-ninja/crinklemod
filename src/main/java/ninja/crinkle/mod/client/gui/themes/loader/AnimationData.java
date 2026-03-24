package ninja.crinkle.mod.client.gui.themes.loader;

import ninja.crinkle.mod.CrinkleMod;
import ninja.crinkle.mod.client.gui.animations.Animation;
import ninja.crinkle.mod.client.gui.animations.Frame;
import ninja.crinkle.mod.client.gui.animations.Sprite;
import ninja.crinkle.mod.client.gui.properties.Point;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public record AnimationData(String id, List<Integer> sizes, Point offset, Map<String, List<String>> sprites) {
    public Animation animation() {
        Map<String, Sprite> sprites = new HashMap<>();
        for (Map.Entry<String, List<String>> entry : sprites().entrySet()) {
            String spriteId = entry.getKey();
            List<Frame> frames = new ArrayList<>();
            for (String frameName : entry.getValue()) {
                Frame frame = new Frame(CrinkleMod.MODID, id(), frameName);
                frames.add(frame);
            }
            Sprite sprite = new Sprite(spriteId, frames);
            sprites.put(entry.getKey(), sprite);
        }
        return new Animation(id, offset, sizes, sprites);
    }
}
