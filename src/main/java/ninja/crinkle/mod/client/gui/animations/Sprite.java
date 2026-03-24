package ninja.crinkle.mod.client.gui.animations;

import java.util.ArrayList;
import java.util.List;

public class Sprite {
    private final List<Frame> frames = new ArrayList<>();
    private final String id;
    private int currentFrame = 0;

    public Sprite(String id, List<Frame> frames) {
        this.id = id;
        this.frames.addAll(frames);
    }

    public Frame current() {
        return frames.get(currentFrame);
    }

    public int currentFrame() {
        return currentFrame;
    }

    public void currentFrame(int currentFrame) {
        this.currentFrame = currentFrame;
    }

    public List<Frame> frames() {
        return frames;
    }

    public String id() {
        return id;
    }
}
